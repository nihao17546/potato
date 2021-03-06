package com.appcnd.potato.service.impl;

import com.appcnd.potato.dao.MetaDao;
import com.appcnd.potato.exception.ExceptionAssert;
import com.appcnd.potato.generate.boot.BootResult;
import com.appcnd.potato.generate.boot.GenerateBoot;
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
import com.appcnd.potato.meta.conf.api.ApiConf;
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
        ExceptionAssert.isNull(config.getDbConf()).throwException(HttpStatus.SYSTEM_ERROR.getCode(), "??????????????????");
        ExceptionAssert.isNull(config.getTable()).throwException(HttpStatus.SYSTEM_ERROR.getCode(), "???????????????");
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
        ApiConf apiConf = config.getApi();
        String path = null;
        if (apiConf == null || apiConf.getUri() == null || apiConf.getUri().isEmpty()) {
            path = "/" + DesUtil.encrypt(param.getId().toString()) + ".html";
        } else {
            path = apiConf.getUri();
        }
        String httpRequest;
        if (config.getApi() != null && config.getApi().getApiPrefix() != null && !config.getApi().getApiPrefix().isEmpty()) {
            httpRequest = config.getApi().getApiPrefix();
        } else {
            httpRequest = GenerateUtil.getWord("/api/", param.getId());
        }
        if (isBoot) {
            httpRequest = coreProperties.getPath() + httpRequest;
        }
        FrontContext frontContext = new FrontContext(config.getBasic().getTitle(), httpRequest,
                config.getTable(), config.getSearchForm(), path, config.getStorage(), param.getResponseParam(),
                config.getOperateForm() != null && Boolean.TRUE.equals(config.getOperateForm().getInsert()),
                config.getOperateForm() != null && Boolean.TRUE.equals(config.getOperateForm().getUpdate()),
                config.getOperateForm() != null && Boolean.TRUE.equals(config.getOperateForm().getDelete()));
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
                String followPath = null;
                if (apiConf == null || apiConf.getFollows() == null || apiConf.getFollows().isEmpty()
                        || apiConf.getFollows().get(index).getUri() == null
                        || apiConf.getFollows().get(index).getUri().isEmpty()) {
                    followPath = "/" + DesUtil.encrypt(param.getId() + "," + index) + ".html";
                } else {
                    followPath = apiConf.getFollows().get(index).getUri();
                }
                String followHttpRequest;
                if (apiConf != null && apiConf.getFollows() != null && !apiConf.getFollows().isEmpty()
                        && apiConf.getFollows().get(index).getApiPrefix() != null
                        && !apiConf.getFollows().get(index).getApiPrefix().isEmpty()) {
                    followHttpRequest = apiConf.getFollows().get(index).getApiPrefix();
                } else {
                    followHttpRequest = GenerateUtil.getWord("/api/", param.getId()) + GenerateUtil.getWord("/", index + 1);
                }
                if (isBoot) {
                    followHttpRequest = coreProperties.getPath() + followHttpRequest;
                }
                OperateForm operateForm = null;
                if (config.getOperateForm() != null && config.getOperateForm().getFollows() != null && !config.getOperateForm().getFollows().isEmpty()) {
                    operateForm = config.getOperateForm().getFollows().get(index);
                }
                context.getFrontContext().addFollow(followHttpRequest,
                        config.getTable().getFollows().get(index), followSearch,
                        followTable.getForeignKey(), followTable.getName(),
                        followTable.getParentKey(), config.getDbConf().getTable().getName(), followPath, config.getStorage(), param.getResponseParam(),
                        operateForm != null && Boolean.TRUE.equals(operateForm.getInsert()),
                        operateForm != null && Boolean.TRUE.equals(operateForm.getUpdate()),
                        operateForm != null && Boolean.TRUE.equals(operateForm.getDelete())
                        );
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
        // ????????????
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
                if (isBoot) {
                    datas.put("ak", qiniuStorage.getAk());
                    datas.put("sk", qiniuStorage.getSk());
                    datas.put("bucket", qiniuStorage.getBucket());
                    datas.put("host", qiniuStorage.getHost());
                }
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
                if (isBoot) {
                    datas.put("region", huaweiStorage.getRegion());
                    datas.put("ak", huaweiStorage.getAk());
                    datas.put("sk", huaweiStorage.getSk());
                    datas.put("bucket", huaweiStorage.getBucket());
                    datas.put("host", huaweiStorage.getHost());
                }
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
                    .append("\"").append(context.getFrontContext().getHttpRequest()).append("\"");
            if (context.getFrontContext().getFollows() != null && !context.getFrontContext().getFollows().isEmpty()) {
                for (FrontContext follow : context.getFrontContext().getFollows()) {
                    requestMappingBuilder.append(",").append("\"").append(follow.getHttpRequest()).append("\"");
                }
            }
            requestMappingBuilder.append("}");
            datas.put("requestMapping", requestMappingBuilder.toString());
            bytes = new StringSubstitutor(datas).replace(content);
            map.put(path, bytes);
        }
        return map;
    }

    private void checkApi(ApiConf apiConf, Integer id) {
        if (apiConf == null) {
            return;
        }
        if (apiConf.getUri() != null && !apiConf.getUri().isEmpty()) {
            BootResult bootResult = generateBoot.getLoaded(apiConf.getUri());
            ExceptionAssert.isTrue(bootResult != null && !bootResult.getId().equals(id))
                    .throwException(HttpStatus.PARAM_ERROR.getCode(), "????????????: " + apiConf.getUri() +
                            " ????????????????????????: " + (bootResult != null ? bootResult.getName() : "") + "??????????????????");
        }
        if (apiConf.getApiPrefix() != null && !apiConf.getApiPrefix().isEmpty()) {
            BootResult bootResult = generateBoot.getLoadedByApi(coreProperties.getPath() + apiConf.getApiPrefix());
            ExceptionAssert.isTrue(bootResult != null && !bootResult.getId().equals(id))
                    .throwException(HttpStatus.PARAM_ERROR.getCode(), "http????????????: " + apiConf.getApiPrefix() +
                            " ????????????????????????: " + (bootResult != null ? bootResult.getName() : "") + "??????????????????");
        }
    }

    public static void main(String[] args) {
        BootResult bootResult = null;
        Integer id = 1;
        ExceptionAssert.isTrue(bootResult != null && !bootResult.getId().equals(id))
                .throwException(HttpStatus.PARAM_ERROR.getCode(), "http????????????: " + "" +
                        " ????????????????????????: " + (bootResult != null ? bootResult.getName() : "") + "??????????????????");
    }

    Pattern pattern = Pattern.compile("namespace=\\\"[a-zA-Z\\.]+\\\"");
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void boot(Integer id, Integer version) throws Exception {
        synchronized (id.toString().intern()) {
            Config config = getConfig(id);
            ExceptionAssert.isTrue(!config.getBasic().getVersion().equals(version))
                    .throwException(HttpStatus.PARAM_ERROR.getCode(), "?????????????????????????????????????????????");
            int a = metaDao.updateLoaded(id, config.getBasic().getVersion(), true);
            ExceptionAssert.isTrue(a == 0)
                    .throwException(HttpStatus.PARAM_ERROR.getCode(), "?????????????????????????????????????????????");
            if (generateBoot.getLoaded(id) != null) {
                return;
            }
            // ????????????
            checkApi(config.getApi(), id);
            if (config.getApi() != null && config.getApi().getFollows() != null && !config.getApi().getFollows().isEmpty()) {
                for (ApiConf apiConf : config.getApi().getFollows()) {
                    checkApi(apiConf, id);
                }
            }

            BootResult result = new BootResult();
            result.setName(config.getBasic().getName());
            GenerateContext context = getContext(id, config, true);
            result.setBasePackage(context.getPackageName());
            Map<String,String> map = generate(context, config, true);
            FrontContext frontContext = context.getFrontContext();
            result.addApi(config.getDbConf().getTable().getName(), coreProperties.getPath(), frontContext.getPath(), "????????????");
            result.addApi(config.getDbConf().getTable().getName(), frontContext.getHttpRequest(), frontContext.getListRequest(), "??????????????????");
            result.addApi(config.getDbConf().getTable().getName(), frontContext.getHttpRequest(), frontContext.getSaveRequest(), "????????????");
            result.addApi(config.getDbConf().getTable().getName(), frontContext.getHttpRequest(), frontContext.getUpdateRequest(), "????????????");
            result.addApi(config.getDbConf().getTable().getName(), frontContext.getHttpRequest(), frontContext.getDeleteRequest(), "????????????");
            result.addApi(config.getDbConf().getTable().getName(), frontContext.getHttpRequest(), frontContext.getInfoRequest(), "??????????????????");
            result.addApi(config.getDbConf().getTable().getName(), frontContext.getHttpRequest(), frontContext.getTokenRequest(), "??????????????????token??????");
            result.addHttpRequest(frontContext.getHttpRequest());
            if (frontContext.getFollows() != null) {
                int index = 0;
                for (FrontContext fc : frontContext.getFollows()) {
                    result.addApi(config.getDbConf().getFollowTables().get(index).getName(), coreProperties.getPath(), fc.getPath(), "????????????");
                    result.addApi(config.getDbConf().getFollowTables().get(index).getName(), fc.getHttpRequest(), fc.getListRequest(), "??????????????????");
                    result.addApi(config.getDbConf().getFollowTables().get(index).getName(), fc.getHttpRequest(), fc.getSaveRequest(), "????????????");
                    result.addApi(config.getDbConf().getFollowTables().get(index).getName(), fc.getHttpRequest(), fc.getUpdateRequest(), "????????????");
                    result.addApi(config.getDbConf().getFollowTables().get(index).getName(), fc.getHttpRequest(), fc.getDeleteRequest(), "????????????");
                    result.addApi(config.getDbConf().getFollowTables().get(index).getName(), fc.getHttpRequest(), fc.getInfoRequest(), "??????????????????");
                    result.addApi(config.getDbConf().getFollowTables().get(index).getName(), fc.getHttpRequest(), fc.getTokenRequest(), "??????????????????token??????");
                    result.addHttpRequest(fc.getHttpRequest());
                    index ++;
                }
            }
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
        generateBoot.unLoad(id);
    }

    @Transactional
    @Override
    public void unBoot(Integer id, Integer version) {
        synchronized (id.toString().intern()) {
            int a = metaDao.updateLoaded(id, version, false);
            ExceptionAssert.isTrue(a == 0)
                    .throwException(HttpStatus.PARAM_ERROR.getCode(), "?????????????????????????????????????????????");
            unBoot(id);
        }
    }

    @Override
    public Config getConfig(Integer id) {
        Meta po = metaDao.selectById(id);
        ExceptionAssert.ifNull(po).throwException(HttpStatus.NOT_FOUND);
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
        if (po.getApi() != null) {
            config.setApi(CommonUtil.unserialize(po.getApi(), ApiConf.class));
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
