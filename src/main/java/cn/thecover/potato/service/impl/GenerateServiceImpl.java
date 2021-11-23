package cn.thecover.potato.service.impl;

import cn.thecover.potato.dao.BootDao;
import cn.thecover.potato.exception.HandlerException;
import cn.thecover.potato.generate.boot.BootResult;
import cn.thecover.potato.generate.boot.GenerateBoot;
import cn.thecover.potato.generate.constant.BootConstant;
import cn.thecover.potato.generate.context.ClassName;
import cn.thecover.potato.generate.context.FrontContext;
import cn.thecover.potato.generate.context.GenerateContext;
import cn.thecover.potato.generate.executor.BootFrontExecutor;
import cn.thecover.potato.generate.executor.ClassExecutor;
import cn.thecover.potato.generate.executor.ComponentExecutor;
import cn.thecover.potato.meta.conf.Config;
import cn.thecover.potato.meta.conf.db.Column;
import cn.thecover.potato.meta.conf.db.DbConf;
import cn.thecover.potato.meta.conf.db.FollowTable;
import cn.thecover.potato.meta.conf.db.Table;
import cn.thecover.potato.meta.conf.form.search.SearchForm;
import cn.thecover.potato.model.param.GenerateParam;
import cn.thecover.potato.model.po.Boot;
import cn.thecover.potato.model.vo.HttpStatus;
import cn.thecover.potato.properties.CoreProperties;
import cn.thecover.potato.service.IGenerateService;
import cn.thecover.potato.util.CamelUtil;
import cn.thecover.potato.util.CommonUtil;
import cn.thecover.potato.util.DesUtil;
import cn.thecover.potato.util.GenerateUtil;
import lombok.extern.slf4j.Slf4j;
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
    private BootDao bootDao;
    @Autowired
    private GenerateBoot generateBoot;
    @Autowired
    private CoreProperties coreProperties;

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
        param.setPackageName(GenerateUtil.getWord(BootConstant.bootPackage + ".", id));
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
                config.getTable(), config.getSearchForm(), path);
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
                context.getFrontContext().addFollow(BootConstant.requestPrefix + CommonUtil.getClassNameField(className.getEntityName()) + "/list",
                        config.getTable().getFollows().get(index), followSearch,
                        followTable.getForeignKey(), followTable.getName(),
                        followTable.getParentKey(), config.getDbConf().getTable().getName(), followPath);
                index ++;
            }
        }
        return context;
    }

    @Override
    public Map<String, String> generate(GenerateParam param, Config config) {
        return generate(getContext(param, config, false), config);
    }

    private Map<String, String> generate(GenerateContext context, Config config) {
        Map<String,String> map = new HashMap<>();
        Map<String,String> fileMap = new ComponentExecutor(config, context).compile();
        if (fileMap != null) {
            map.putAll(fileMap);
        }
        Map<String,String> classMap = new ClassExecutor(context).compile();
        if (classMap != null) {
            map.putAll(classMap);
        }
        return map;
    }

    Pattern pattern = Pattern.compile("namespace=\\\"[a-zA-Z\\.]+\\\"");
    @Transactional
    @Override
    public void boot(Integer id, Config config) {
        Boot boot = bootDao.selectByMetaIdAndVersion(id, config.getBasic().getVersion());
        BootResult result;
        if (boot != null) {
            result = CommonUtil.unserialize(boot.getData(), BootResult.class);
        } else {
            result = new BootResult();
            GenerateContext context = getContext(id, config, true);
            Map<String,String> map = generate(context, config);

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
                    String classFullName = key.replaceFirst(javaSrc, "").replaceAll("/", ".");
                    classFullName = classFullName.substring(0, classFullName.length() - ".java".length());
                    java.setClassName(classFullName);
                    java.setSource(map.get(key));

                    if (classFullName.endsWith("Po")) {
                        result.getPo().add(java);
                    } else if (classFullName.endsWith("Dto")) {
                        result.getDto().add(java);
                    } else if (classFullName.endsWith("Vo") || classFullName.endsWith("VO")) {
                        result.getVo().add(java);
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
            boot = new Boot();
            boot.setMetaId(id);
            boot.setVersion(config.getBasic().getVersion());
            boot.setData(CommonUtil.serialize(result));
            bootDao.insert(boot);
        }
        generateBoot.boot(result);
    }

    @Override
    public void unBoot(Integer id) {
        List<Boot> bootList = bootDao.selectListByMetaId(id);
        if (!CollectionUtils.isEmpty(bootList)) {
            bootDao.deleteByMetaId(id);
        }
        List<String> htmls = new ArrayList<>();
        for (Boot boot : bootList) {
            BootResult bootResult = CommonUtil.unserialize(boot.getData(), BootResult.class);
            htmls.addAll(bootResult.getHtml().keySet());
        }
        generateBoot.unLoad(id, htmls);
    }

}
