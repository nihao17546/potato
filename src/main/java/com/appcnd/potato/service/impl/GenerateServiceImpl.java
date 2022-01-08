package com.appcnd.potato.service.impl;

import com.appcnd.potato.dao.MetaDao;
import com.appcnd.potato.exception.HandlerException;
import com.appcnd.potato.generate.boot.BootResult;
import com.appcnd.potato.generate.boot.GenerateBoot;
import com.appcnd.potato.generate.boot.HtmlServlet;
import com.appcnd.potato.generate.constant.BootConstant;
import com.appcnd.potato.generate.context.ClassName;
import com.appcnd.potato.generate.context.FrontContext;
import com.appcnd.potato.generate.context.GenerateContext;
import com.appcnd.potato.generate.executor.BootClassExecutor;
import com.appcnd.potato.generate.executor.BootFrontExecutor;
import com.appcnd.potato.generate.executor.ClassExecutor;
import com.appcnd.potato.generate.executor.ComponentExecutor;
import com.appcnd.potato.meta.conf.Basic;
import com.appcnd.potato.meta.conf.Config;
import com.appcnd.potato.meta.conf.db.Column;
import com.appcnd.potato.meta.conf.db.DbConf;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.db.Table;
import com.appcnd.potato.meta.conf.form.operate.OperateForm;
import com.appcnd.potato.meta.conf.form.search.SearchForm;
import com.appcnd.potato.meta.conf.form.storage.HuaweiStorage;
import com.appcnd.potato.meta.conf.form.storage.QiniuStorage;
import com.appcnd.potato.meta.conf.form.storage.Storage;
import com.appcnd.potato.meta.conf.table.UIMainTable;
import com.appcnd.potato.model.param.GenerateParam;
import com.appcnd.potato.model.po.Meta;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.properties.CoreProperties;
import com.appcnd.potato.service.IGenerateService;
import com.appcnd.potato.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author nihao 2021/07/08
 */
@Slf4j
public class GenerateServiceImpl implements IGenerateService {
    @Autowired
    private GenerateBoot generateBoot;
    @Autowired
    private HtmlServlet htmlServlet;
    @Autowired
    private CoreProperties coreProperties;
    @Autowired
    private MetaDao metaDao;

    private void fillColumnMap(Map<String,Map<String, Column>> columnMap, Table table) {
        for (Column column : table.getColumns()) {
            if (columnMap.containsKey(table.getName())) {
                columnMap.get(table.getName()).put(column.getField(), column);
            } else {
                Map<String, Column> map = new HashMap<>();
                map.put(column.getField(), column);
                columnMap.put(table.getName(), map);
            }
        }
    }

    private Map<String,Map<String,Column>> getColumnMap(DbConf dbConf) {
        Map<String,Map<String, Column>> columnMap = new HashMap<>();
        fillColumnMap(columnMap, dbConf.getTable());
        if (dbConf.getAssociationTables() != null && !dbConf.getAssociationTables().isEmpty()) {
            for (FollowTable followTable : dbConf.getAssociationTables()) {
                fillColumnMap(columnMap, followTable);
            }
        }
        if (dbConf.getFollowTables() != null && !dbConf.getFollowTables().isEmpty()) {
            for (FollowTable followTable : dbConf.getFollowTables()) {
                fillColumnMap(columnMap, followTable);
            }
        }
        return columnMap;
    }

    private void check(Config config) {
        if (config.getDbConf() == null) {
            throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "数据库未配置");
        }
        if (config.getTable() == null) {
            throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "表格未配置");
        }
    }

    private GenerateContext getContext(Integer id, Config config, boolean isBoot) {
        check(config);

        GenerateParam param = new GenerateParam();
        param.setId(id);
//        String basePackage = BootConstant.bootPackage + "." + GenerateUtil.getRandomPackageName();
        String basePackage = GenerateUtil.getWord(BootConstant.bootPackage + ".", id);
        param.setPackageName(basePackage);
        List<GenerateParam.Entity> entityNames = new ArrayList<>();
        param.setEntityNames(entityNames);

        GenerateParam.Entity entity = new GenerateParam.Entity();
        entity.setTable(config.getDbConf().getTable().getName());
        entity.setClazz(CamelUtil.getClassName(entity.getTable()));
        entityNames.add(entity);

        if (config.getDbConf().getAssociationTables() != null) {
            for (FollowTable followTable : config.getDbConf().getAssociationTables()) {
                GenerateParam.Entity entityF = new GenerateParam.Entity();
                entityF.setTable(followTable.getName());
                entityF.setClazz(CamelUtil.getClassName(entityF.getTable()));
                entityNames.add(entityF);
            }
        }

        if (config.getDbConf().getFollowTables() != null) {
            for (FollowTable followTable : config.getDbConf().getFollowTables()) {
                GenerateParam.Entity entityF = new GenerateParam.Entity();
                entityF.setTable(followTable.getName());
                entityF.setClazz(CamelUtil.getClassName(entityF.getTable()));
                entityNames.add(entityF);
            }
        }
        return getContext(param, config, isBoot);
    }

    private GenerateContext getContext(GenerateParam param, Config config, boolean isBoot) {
        check(config);

        Map<String,String> entityMap = param.getEntityNames().stream().collect(
                Collectors.toMap(GenerateParam.Entity::getTable, GenerateParam.Entity::getClazz));

        GenerateContext context = new GenerateContext();
        context.setResponseParam(param.getResponseParam());
        context.setPackageName(param.getPackageName());
        context.setColumnMap(getColumnMap(config.getDbConf()));
        context.setMainClassName(new ClassName(param.getPackageName(), entityMap.get(config.getDbConf().getTable().getName())));
        String path = BootConstant.requestPrefix + "page/" + DesUtil.encrypt(param.getId() + "," + config.getBasic().getVersion()) + ".html";
        String listRequestPath;
        if (isBoot) {
            listRequestPath = coreProperties.getPath() + BootConstant.requestPrefix + CommonUtil.getClassNameField(context.getMainClassName().getEntityName()) + "/list";
        } else {
            listRequestPath = BootConstant.requestPrefix + CommonUtil.getClassNameField(context.getMainClassName().getEntityName()) + "/list";
        }
        FrontContext frontContext = new FrontContext(config.getBasic().getTitle(), listRequestPath,
                config.getTable(), config.getSearchForm(), path, config.getStorage(), param.getResponseParam());
        context.setFrontContext(frontContext);
        if (config.getDbConf().getAssociationTables() != null) {
            for (FollowTable followTable : config.getDbConf().getAssociationTables()) {
                context.addAssociationClassName(new ClassName(param.getPackageName(), entityMap.get(followTable.getName())));
            }
        }
        if (config.getDbConf().getFollowTables() != null) {
            context.getFrontContext().setFollows(new ArrayList<>());
            int index = 0;
            for (FollowTable followTable : config.getDbConf().getFollowTables()) {
                ClassName className = new ClassName(param.getPackageName(), entityMap.get(followTable.getName()));
                context.addFollowClassName(className);
                SearchForm followSearch = null;
                if (config.getSearchForm() != null && config.getSearchForm().getFollows() != null) {
                    followSearch = config.getSearchForm().getFollows().get(index);
                }
                String followPath = BootConstant.requestPrefix + "page/"
                        + DesUtil.encrypt(param.getId() + "," + config.getBasic().getVersion() + "," + index) + ".html";
                String followListRequestPath;
                if (isBoot) {
                    followListRequestPath = coreProperties.getPath() + BootConstant.requestPrefix + CommonUtil.getClassNameField(className.getEntityName()) + "/list";
                } else {
                    followListRequestPath = BootConstant.requestPrefix + CommonUtil.getClassNameField(className.getEntityName()) + "/list";
                }
                context.getFrontContext().addFollow(followListRequestPath,
                        config.getTable().getFollows().get(index), followSearch,
                        followTable.getForeignKey(), followTable.getName(),
                        followTable.getParentKey(), config.getDbConf().getTable().getName(), followPath, config.getStorage(), param.getResponseParam());
                index ++;
            }
        }
        return context;
    }

    @Override
    public Map<String, String> generate(GenerateParam param, Config config) {
        return generate(getContext(param, config, false), config, false);
    }

    private Map<String, String> generate(GenerateContext context, Config config, boolean isBoot) {
        Map<String,String> map = new HashMap<>();
        Map<String,String> fileMap = new ComponentExecutor(config, context).compile();
        if (fileMap != null) {
            map.putAll(fileMap);
        }
        Map<String,String> classMap;
        if (isBoot) {
            classMap = new BootClassExecutor(context).compile();
        } else {
            classMap = new ClassExecutor(context).compile();
        }
        if (classMap != null) {
            map.putAll(classMap);
        }
        // ResponseVo
        if (context.getResponseParam() == null) {
            String path = "backend" + File.separator + "src" +  File.separator+ "main" + File.separator + "java" + File.separator +
                    (context.getPackageName() + ".pojo.vo").replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + File.separator + "ResponseVo.java";
            String content = CommonUtil.readFromResource("codeless/backend/ResponseVo.java");
            Map<String,String> datas = new HashMap<>();
            datas.put("basePackageName", context.getPackageName());
            datas.put("version", config.getBasic().getVersion().toString());
            datas.put("now", SimpleDateUtil.format(new Date()));
            String bytes = new StringSubstitutor(datas).replace(content);
            map.put(path, bytes);
        }
        // 对象存储
        if (config.getStorage() != null) {
            // TokenResultVo.java
            String simpleClassName = "TokenResultVo";
            String path = "backend" + File.separator + "src" +  File.separator+ "main" + File.separator + "java" + File.separator +
                    (context.getPackageName() + ".pojo.vo").replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + File.separator + simpleClassName + ".java";
            String content = CommonUtil.readFromResource("codeless/backend/storage/TokenResultVo.java");
            Map<String,String> datas = new HashMap<>();
            datas.put("basePackageName", context.getPackageName());
            datas.put("version", config.getBasic().getVersion().toString());
            datas.put("now", SimpleDateUtil.format(new Date()));
            String bytes = new StringSubstitutor(datas).replace(content);
            map.put(path, bytes);

            // IUploadService.java
            simpleClassName = "IUploadService";
            path = "backend" + File.separator + "src" +  File.separator+ "main" + File.separator + "java" + File.separator +
                    (context.getPackageName() + ".service").replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + File.separator + simpleClassName + ".java";
            content = CommonUtil.readFromResource("codeless/backend/storage/IUploadService.java");
            datas.clear();
            datas.put("basePackageName", context.getPackageName());
            datas.put("version", config.getBasic().getVersion().toString());
            datas.put("now", SimpleDateUtil.format(new Date()));
            bytes = new StringSubstitutor(datas).replace(content);
            map.put(path, bytes);

            if (config.getStorage() instanceof QiniuStorage) {
                QiniuStorage qiniuStorage = (QiniuStorage) config.getStorage();
                // QiniuUploadServiceImpl.java
                simpleClassName = "QiniuUploadServiceImpl";
                path = "backend" + File.separator + "src" +  File.separator+ "main" + File.separator + "java" + File.separator +
                        (context.getPackageName() + ".service.impl").replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + File.separator + simpleClassName + ".java";
                content = CommonUtil.readFromResource("codeless/backend/storage/QiniuUploadServiceImpl.java");
                datas.clear();
                datas.put("basePackageName", context.getPackageName());
                datas.put("version", config.getBasic().getVersion().toString());
                datas.put("now", SimpleDateUtil.format(new Date()));
                datas.put("ak", qiniuStorage.getAk());
                datas.put("sk", qiniuStorage.getSk());
                datas.put("bucket", qiniuStorage.getBucket());
                datas.put("host", qiniuStorage.getHost());
                bytes = new StringSubstitutor(datas).replace(content);
                map.put(path, bytes);
            } else if (config.getStorage() instanceof HuaweiStorage) {
                HuaweiStorage huaweiStorage = (HuaweiStorage) config.getStorage();
                // HuaweiUploadServiceImpl.java
                simpleClassName = "HuaweiUploadServiceImpl";
                path = "backend" + File.separator + "src" +  File.separator+ "main" + File.separator + "java" + File.separator +
                        (context.getPackageName() + ".service.impl").replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + File.separator + simpleClassName + ".java";
                content = CommonUtil.readFromResource("codeless/backend/storage/HuaweiUploadServiceImpl.java");
                datas.clear();
                datas.put("basePackageName", context.getPackageName());
                datas.put("version", config.getBasic().getVersion().toString());
                datas.put("now", SimpleDateUtil.format(new Date()));
                datas.put("region", huaweiStorage.getRegion());
                datas.put("ak", huaweiStorage.getAk());
                datas.put("sk", huaweiStorage.getSk());
                datas.put("bucket", huaweiStorage.getBucket());
                datas.put("host", huaweiStorage.getHost());
                bytes = new StringSubstitutor(datas).replace(content);
                map.put(path, bytes);
            }

            // StorageController.java
            simpleClassName = "StorageController";
            path = "backend" + File.separator + "src" +  File.separator+ "main" + File.separator + "java" + File.separator +
                    (context.getPackageName() + ".controller").replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + File.separator + simpleClassName + ".java";
            content = CommonUtil.readFromResource("codeless/backend/storage/StorageController.java");
            datas.clear();
            datas.put("basePackageName", context.getPackageName());
            datas.put("version", config.getBasic().getVersion().toString());
            datas.put("now", SimpleDateUtil.format(new Date()));

            StringBuilder requestMappingBuilder = new StringBuilder();
            requestMappingBuilder.append("{")
                    .append("\"").append(context.getFrontContext().getTokenRequest()).append("\"");
            if (context.getFrontContext().getFollows() != null && !context.getFrontContext().getFollows().isEmpty()) {
                for (FrontContext follow : context.getFrontContext().getFollows()) {
                    requestMappingBuilder.append(",").append("\"").append(follow.getTokenRequest()).append("\"");
                }
            }
            requestMappingBuilder.append("}");
            datas.put("requestMapping", requestMappingBuilder.toString());
            bytes = new StringSubstitutor(datas).replace(content);
            map.put(path, bytes);
        }
        return map;
    }

    Pattern pattern = Pattern.compile("namespace=\\\"[a-zA-Z\\.]+\\\"");
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void boot(Integer id, Integer version) throws Exception {
        synchronized (id.toString().intern()) {
            Config config = getConfig(id);
            if (!config.getBasic().getVersion().equals(version)) {
                throw new HandlerException(HttpStatus.PARAM_ERROR.getCode(), "数据已被修改，请刷新后重新操作");
            }
            int a = metaDao.updateLoaded(id, config.getBasic().getVersion(), true);
            if (a == 0) {
                throw new HandlerException(HttpStatus.PARAM_ERROR.getCode(), "数据已被修改，请刷新后重新操作");
            }
            if (generateBoot.getLoaded(id) != null) {
                return;
            }
            BootResult result = new BootResult();
            GenerateContext context = getContext(id, config, true);
            result.setBasePackage(context.getPackageName());
            Map<String,String> map = generate(context, config, true);
            FrontContext frontContext = context.getFrontContext();
            Map<String,String> frontMap = new BootFrontExecutor(frontContext).compile();
            if (frontMap != null) {
                map.putAll(frontMap);
            }
            if (!CollectionUtils.isEmpty(frontContext.getFollows())) {
                for (FrontContext follow : frontContext.getFollows()) {
                    Map<String,String> followMap = new BootFrontExecutor(follow).compile();
                    if (followMap != null) {
                        map.putAll(followMap);
                    }
                }
            }

            String javaSrc = "backend" + File.separator + "src" +  File.separator+ "main" + File.separator + "java" + File.separator;

            result.setId(id);
            result.setVersion(config.getBasic().getVersion());
            result.setUrl(frontContext.getPath());
            for (String key : map.keySet()) {
                if (key.startsWith(javaSrc) && key.endsWith(".java")) {
                    BootResult.Java java = new BootResult.Java();
                    String classFullName = key.replace(javaSrc, "").replaceAll(Matcher.quoteReplacement(File.separator), ".");
                    classFullName = classFullName.substring(0, classFullName.length() - ".java".length());
                    java.setClassName(classFullName);
                    java.setSource(map.get(key));

                    if (classFullName.endsWith("Po")) {
                        result.getPo().add(java);
                    } else if (classFullName.endsWith("Dto")) {
                        result.getDto().add(java);
                    } else if (classFullName.endsWith("Vo") || classFullName.endsWith("VO")) {
                        result.getVo().add(java);
                    } else if (classFullName.endsWith("Req")) {
                        result.getParam().add(java);
                    } else if (classFullName.endsWith("Dao")) {
                        result.getDao().add(java);
                    } else if (classFullName.endsWith("Service")) {
                        result.getServices().add(java);
                    } else if (classFullName.endsWith("ServiceImpl")) {
                        result.getServiceImpls().add(java);
                    } else if (classFullName.endsWith("Controller")) {
                        result.getControllers().add(java);
                    }
                } else if (key.endsWith(".xml")) {
                    String source = map.get(key);
                    Matcher matcher = pattern.matcher(source);
                    if (matcher.find()) {
                        String classFullName = source.substring(matcher.start() + "namespace=\"".length(), matcher.end() - 1);
                        BootResult.Mapper mapper = new BootResult.Mapper();
                        mapper.setMapperId(classFullName);
                        mapper.setSource(source);
                        result.getMappers().add(mapper);
                    }
                } else if (key.endsWith(".html")) {
                    result.addHtml(key, map.get(key));
                }
            }
            generateBoot.boot(result, context.getNeedLoadClasses());
        }
    }

    @Override
    public void unBoot(Integer id) {
        Set<String> htmls = generateBoot.unLoad(id);
        if (htmls != null) {
            for (String html : htmls) {
                htmlServlet.removeCache(html);
            }
        }
    }

    @Transactional
    @Override
    public void unBoot(Integer id, Integer version) {
        synchronized (id.toString().intern()) {
            int a = metaDao.updateLoaded(id, version, false);
            if (a == 0) {
                throw new HandlerException(HttpStatus.PARAM_ERROR.getCode(), "数据已被修改，请刷新后重新操作");
            }
            unBoot(id);
        }
    }

    @Override
    public Config getConfig(Integer id) {
        Meta po = metaDao.selectById(id);
        if (po == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        Config config = new Config();
        Basic basic = new Basic();
        basic.setTitle(po.getTitle());
        basic.setName(po.getName());
        basic.setVersion(po.getVersion());
        config.setBasic(basic);
        if (po.getTable() != null) {
            config.setTable(CommonUtil.unserialize(po.getTable(), UIMainTable.class));
        }
        if (po.getDb() != null) {
            config.setDbConf(CommonUtil.unserialize(po.getDb(), DbConf.class));
        }
        if (po.getSearch() != null) {
            config.setSearchForm(CommonUtil.unserialize(po.getSearch(), SearchForm.class));
        }
        if (po.getOperate() != null) {
            config.setOperateForm(CommonUtil.unserialize(po.getOperate(), OperateForm.class));
        }
        if (po.getStorage() != null && !po.getStorage().isEmpty()) {
            Storage storage = CommonUtil.unserialize(po.getStorage(), Storage.class);
            if (storage instanceof HuaweiStorage) {
                HuaweiStorage huaweiStorage = (HuaweiStorage) storage;
                huaweiStorage.setAk(DesUtil.decrypt(huaweiStorage.getAk(), "storage"));
                huaweiStorage.setSk(DesUtil.decrypt(huaweiStorage.getSk(), "storage"));
            } else if (storage instanceof QiniuStorage) {
                QiniuStorage qiniuStorage = (QiniuStorage) storage;
                qiniuStorage.setAk(DesUtil.decrypt(qiniuStorage.getAk(), "storage"));
                qiniuStorage.setSk(DesUtil.decrypt(qiniuStorage.getSk(), "storage"));
            }
            config.setStorage(storage);
        }
        return config;
    }

}
