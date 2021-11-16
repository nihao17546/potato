package cn.thecover.potato.generate.executor;

import cn.thecover.potato.generate.annotation.AnnotationInfo;
import cn.thecover.potato.generate.constant.BootConstant;
import cn.thecover.potato.generate.context.*;
import cn.thecover.potato.generate.executor.handler.*;
import cn.thecover.potato.generate.method.MethodInfo;
import cn.thecover.potato.generate.method.ParamInfo;
import cn.thecover.potato.meta.conf.Config;
import cn.thecover.potato.meta.conf.db.FollowTable;
import cn.thecover.potato.meta.conf.db.Table;
import cn.thecover.potato.meta.conf.form.operate.OperateForm;
import cn.thecover.potato.meta.conf.form.operate.elements.OperateElement;
import cn.thecover.potato.meta.conf.form.operate.elements.RemoteSelectOperateElement;
import cn.thecover.potato.meta.conf.form.search.SearchForm;
import cn.thecover.potato.meta.conf.form.search.element.RemoteSelectSearchElement;
import cn.thecover.potato.meta.conf.form.search.element.SearchElement;
import cn.thecover.potato.meta.conf.table.UITable;
import cn.thecover.potato.util.CamelUtil;
import cn.thecover.potato.util.CommonUtil;
import cn.thecover.potato.util.GenerateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * @author nihao 2021/07/12
 */
@Slf4j
public class ComponentExecutor extends Executor {
    private final String FILE_PATH = "codeless/backend/Mapper.xml";
    private Config config;
    private GenerateContext context;

    public ComponentExecutor(Config config, GenerateContext context) {
        Assert.notNull(config, "config can not be null");
        Assert.notNull(context, "context can not be null");
        Assert.notNull(config.getDbConf(), "dbConf can not be null");
        Assert.notNull(config.getTable(), "table can not be null");

        this.config = config;
        this.context = context;
    }

    private void pojo(ClassName className, Table table, List<FollowTable> associationTables, UITable uiTable, HandlerRequest handlerRequest) {
        // po
        JavaClassContext po = new JavaClassContext(CommonUtil.getPackageName(className.getPoClassName()),
                config.getBasic().getVersion(), "public class", CommonUtil.getSimpleClassName(className.getPoClassName()));
        po.addImplementsClassName(Serializable.class.getName());
        po.addFields(GenerateUtil.getFields(table.getColumns()));
        po.addMethods(GenerateUtil.getSetterAndGetterMethods(po.getFields()));

        JavaClassContext vo = new JavaClassContext(CommonUtil.getPackageName(className.getVoClassName()),
                config.getBasic().getVersion(), "public class", CommonUtil.getSimpleClassName(className.getVoClassName()));
        vo.addConstructor(new ClassConstructor("public"));

        if (!CollectionUtils.isEmpty(associationTables)) {
            // dto
            JavaClassContext dto = new JavaClassContext(CommonUtil.getPackageName(className.getDtoClassName()),
                    config.getBasic().getVersion(), "public class", CommonUtil.getSimpleClassName(className.getDtoClassName()));
            dto.addImplementsClassName(Serializable.class.getName());
            dto.addFields(GenerateUtil.getFieldsFromUiColumn(uiTable.getColumns(), handlerRequest.getColumnMap()));
            dto.addMethods(GenerateUtil.getSetterAndGetterMethods(dto.getFields()));
            context.addJavaClassContext(dto);
            handlerRequest.setDto(dto);

            // vo
            vo.addFields(dto.getFields());
            vo.addMethods(dto.getMethods());

            ClassConstructor constructor = new ClassConstructor("public");
            ParamInfo paramInfo = new ParamInfo(dto.getClassName(), CommonUtil.getClassNameField(dto.getName()));
            constructor.addParam(paramInfo);
            StringBuilder sb = new StringBuilder();
            for (ClassField classField : dto.getFields()) {
                sb.append("        this.").append(classField.getName()).append(" = ").append(paramInfo.getName()).append(".")
                        .append(CamelUtil.get(classField.getName())).append("();\n");
            }
            constructor.setContent(sb.toString());
            vo.addConstructor(constructor);
        } else {
            // vo
            vo.addFields(po.getFields());
            vo.addMethods(po.getMethods());
        }

        ClassConstructor constructor = new ClassConstructor("public");
        ParamInfo paramInfo = new ParamInfo(po.getClassName(), CommonUtil.getClassNameField(po.getName()));
        constructor.addParam(paramInfo);
        StringBuilder sb = new StringBuilder();
        for (ClassField classField : po.getFields()) {
            sb.append("        this.").append(classField.getName()).append(" = ").append(paramInfo.getName()).append(".")
                    .append(CamelUtil.get(classField.getName())).append("();\n");
        }
        constructor.setContent(sb.toString());
        vo.addConstructor(constructor);

        context.addJavaClassContext(po);
        handlerRequest.setDto(po);
        context.addJavaClassContext(vo);
        handlerRequest.setDto(vo);
    }

    private void componentClass(ClassName className, HandlerRequest handlerRequest) {
        JavaClassContext daoClass = new JavaClassContext(CommonUtil.getPackageName(className.getDaoClassName()),
                config.getBasic().getVersion(), "public interface",
                CommonUtil.getSimpleClassName(className.getDaoClassName()));

        JavaClassContext serviceClass = new JavaClassContext(CommonUtil.getPackageName(className.getServiceClassName()),
                config.getBasic().getVersion(), "public interface",
                CommonUtil.getSimpleClassName(className.getServiceClassName()));

        JavaClassContext serviceImplClass = new JavaClassContext(CommonUtil.getPackageName(className.getServiceImplClassName()),
                config.getBasic().getVersion(), "public class",
                CommonUtil.getSimpleClassName(className.getServiceImplClassName()));
        serviceImplClass.addAnnotation(new AnnotationInfo(Service.class.getName()));
        serviceImplClass.addImplementsClassName(className.getServiceClassName());

        JavaClassContext controllerClass = new JavaClassContext(CommonUtil.getPackageName(className.getControllerClassName()),
                config.getBasic().getVersion(), "public class",
                CommonUtil.getSimpleClassName(className.getControllerClassName()));
        context.addJavaClassContext(daoClass, serviceClass, serviceImplClass, controllerClass);

        handlerRequest.setDaoClass(daoClass);
        handlerRequest.setServiceClass(serviceClass);
        handlerRequest.setServiceImplClass(serviceImplClass);
        handlerRequest.setControllerClass(controllerClass);
    }

    @Override
    protected Map<String,Map<String, String>> analysis() {
        Map<String,Map<String, String>> result = new HashMap<>();

        Set<RemoteContext> remoteContextSet = new HashSet<>();

        // 主表
        HandlerRequest handlerRequest = new HandlerRequest();
        handlerRequest.setVersion(config.getBasic().getVersion());
        handlerRequest.setContext(context);
        Map<String,String> columnMap = getFieldMap(config.getDbConf().getTable(), config.getDbConf().getAssociationTables());
        handlerRequest.setColumnMap(columnMap);
        handlerRequest.setClassName(context.getMainClassName());
        handlerRequest.setFrontContext(context.getFrontContext());
        handlerRequest.setTable(config.getDbConf().getTable());
        handlerRequest.setAssociationTables(config.getDbConf().getAssociationTables());
        handlerRequest.setFollowTables(config.getDbConf().getFollowTables());
        handlerRequest.setUiTable(config.getTable());
        handlerRequest.setSearchForm(config.getSearchForm());
        handlerRequest.setOperateForm(config.getOperateForm());

        // pojo
        pojo(context.getMainClassName(), config.getDbConf().getTable(), config.getDbConf().getAssociationTables(),
                config.getTable(), handlerRequest);
        // component
        componentClass(context.getMainClassName(), handlerRequest);

        ComponentHandler handler = new ListComponentHandler();

        if (config.getOperateForm() != null) {
            if (config.getOperateForm().getElements() != null && !config.getOperateForm().getElements().isEmpty()) {
                if (Boolean.TRUE.equals(config.getOperateForm().getInsert())) {
                    // 新增
                    ComponentHandler insertHandler = new InsertComponentHandler();
                    handler.setNext(insertHandler);
                }
                if (Boolean.TRUE.equals(config.getOperateForm().getUpdate())) {
                    // 编辑
                    ComponentHandler infoHandler = new InfoComponentHandler();
                    ComponentHandler updateHandler = new UpdateComponentHandler();
                    handler.setNext(infoHandler);
                    handler.setNext(updateHandler);
                }
                for (OperateElement element : config.getOperateForm().getElements()) {
                    if (element instanceof RemoteSelectOperateElement) {
                        RemoteSelectOperateElement el = (RemoteSelectOperateElement) element;
                        RemoteContext remoteContext = new RemoteContext(el.getSql(), el.getLabelColumn(), el.getValueColumn());
                        remoteContextSet.add(remoteContext);
                        context.getFrontContext().addRemoteContext(remoteContext);
                    }
                }
            }
            if (Boolean.TRUE.equals(config.getOperateForm().getDelete())) {
                // 删除
                ComponentHandler delHandler = new DelComponentHandler();
                handler.setNext(delHandler);
            }
        }

        if (config.getSearchForm() != null && config.getSearchForm().getElements() != null) {
            for (SearchElement element : config.getSearchForm().getElements()) {
                if (element instanceof RemoteSelectSearchElement) {
                    RemoteSelectSearchElement el = (RemoteSelectSearchElement) element;
                    RemoteContext remoteContext = new RemoteContext(el.getSql(), el.getLabelColumn(), el.getValueColumn());
                    remoteContextSet.add(remoteContext);
                    context.getFrontContext().addRemoteContext(remoteContext);
                }
            }
        }

        handler.execute(handlerRequest);

        if (!CollectionUtils.isEmpty(handlerRequest.getEls())) {
            Map<String, String> map = new HashMap<>();
            map.put("namespace", handlerRequest.getClassName().getDaoClassName());
            map.put("elements", compileEls(handlerRequest.getEls()));
            result.put(getMapperPath(context.getMainClassName().getEntityName()), map);
        }

        if (!CollectionUtils.isEmpty(config.getDbConf().getFollowTables())) {
            int index = 0;
            for (FollowTable table : config.getDbConf().getFollowTables()) {
                UITable uiTable = config.getTable().getFollows().get(index);
                ClassName className = context.getFollowClassNames().get(index);
                SearchForm searchForm = null;
                FrontContext frontContext = context.getFrontContext().getFollows().get(index);
                if (config.getSearchForm() != null && config.getSearchForm().getFollows() != null
                        && config.getSearchForm().getFollows().size() == config.getDbConf().getFollowTables().size()) {
                    searchForm = config.getSearchForm().getFollows().get(index);
                }
                OperateForm operateForm = null;
                if (config.getOperateForm() != null && config.getOperateForm().getFollows() != null
                        && config.getOperateForm().getFollows().size() > index) {
                    operateForm = config.getOperateForm().getFollows().get(index);
                }

                // 从表
                HandlerRequest request = new HandlerRequest();
                request.setVersion(config.getBasic().getVersion());
                request.setContext(context);
                request.setColumnMap(getFieldMap(table, null));
                request.setClassName(className);
                request.setFrontContext(frontContext);
                request.setTable(table);
                request.setUiTable(uiTable);
                request.setSearchForm(searchForm);
                request.setOperateForm(operateForm);

                // pojo
                pojo(className, table, null, uiTable, request);
                // component
                componentClass(className, request);

                ComponentHandler followHandler = new ListComponentHandler();
                if (operateForm != null) {
                    if (operateForm.getElements() != null && !operateForm.getElements().isEmpty()) {
                        if (Boolean.TRUE.equals(operateForm.getInsert())) {
                            // 新增
                            ComponentHandler insertHandler = new InsertComponentHandler();
                            followHandler.setNext(insertHandler);
                        }
                        if (Boolean.TRUE.equals(operateForm.getUpdate())) {
                            // 编辑
                            ComponentHandler infoHandler = new InfoComponentHandler();
                            ComponentHandler updateHandler = new UpdateComponentHandler();
                            followHandler.setNext(infoHandler);
                            followHandler.setNext(updateHandler);
                        }
                        for (OperateElement element : operateForm.getElements()) {
                            if (element instanceof RemoteSelectOperateElement) {
                                RemoteSelectOperateElement el = (RemoteSelectOperateElement) element;
                                RemoteContext remoteContext = new RemoteContext(el.getSql(), el.getLabelColumn(), el.getValueColumn());
                                remoteContextSet.add(remoteContext);
                                frontContext.addRemoteContext(remoteContext);
                            }
                        }
                    }
                    if (Boolean.TRUE.equals(operateForm.getDelete())) {
                        // 删除
                        ComponentHandler delHandler = new DelComponentHandler();
                        followHandler.setNext(delHandler);
                    }
                }

                if (searchForm != null && searchForm.getElements() != null) {
                    for (SearchElement element : searchForm.getElements()) {
                        if (element instanceof RemoteSelectSearchElement) {
                            RemoteSelectSearchElement el = (RemoteSelectSearchElement) element;
                            RemoteContext remoteContext = new RemoteContext(el.getSql(), el.getLabelColumn(), el.getValueColumn());
                            remoteContextSet.add(remoteContext);
                            frontContext.addRemoteContext(remoteContext);
                        }
                    }
                }

                followHandler.execute(request);

                if (!CollectionUtils.isEmpty(request.getEls())) {
                    Map<String, String> map = new HashMap<>();
                    map.put("namespace", request.getClassName().getDaoClassName());
                    map.put("elements", compileEls(request.getEls()));
                    result.put(getMapperPath(className.getEntityName()), map);
                }

                index ++;
            }
        }

        // 远程搜索
        if (!remoteContextSet.isEmpty()) {
            String controllerClassName = context.getMainClassName().getPackageName() + ".controller." + "RemoteController";
            String daoClassName = context.getMainClassName().getPackageName() + ".dao." + "RemoteDao";
            List<El> elList = getRemote(remoteContextSet, controllerClassName, daoClassName);
            if (elList != null && !elList.isEmpty()) {
                Map<String, String> map = new HashMap<>();
                map.put("namespace", daoClassName);
                map.put("elements", compileEls(elList));
                result.put(getMapperPath("Remote"), map);
            }
        }

        return result;
    }

    private List<El> getRemote(Set<RemoteContext> remoteContextSet, String controllerClassName, String daoClassName) {
        List<El> elList = new ArrayList<>();
        if (!remoteContextSet.isEmpty()) {
            JavaClassContext daoClass = new JavaClassContext(CommonUtil.getPackageName(daoClassName),
                    config.getBasic().getVersion(), "public interface",
                    CommonUtil.getSimpleClassName(daoClassName));
            
            JavaClassContext controllerClass = new JavaClassContext(CommonUtil.getPackageName(controllerClassName),
                    config.getBasic().getVersion(), "public class",
                    CommonUtil.getSimpleClassName(controllerClassName));
            context.addJavaClassContext(daoClass, controllerClass);

            AnnotationInfo annotationInfo = new AnnotationInfo(Autowired.class.getName());
            ClassField classField = new ClassField("private", daoClassName, annotationInfo);
            if (!controllerClass.hasField(classField)) {
                controllerClass.addField(classField);
            }
            for (RemoteContext remoteContext : remoteContextSet) {
                String methodName = "getRemote" + remoteContext.hashCode();
                methodName = methodName.replaceAll("-", "A");

                ComponentExecutor.El el = new ComponentExecutor.El();
                el.setId(methodName);
                el.setType("select");
                el.setResultType(Map.class.getName());
                el.setSql(remoteContext.getSql());
                elList.add(el);

                MethodInfo daoMethod = new MethodInfo();
                daoMethod.setHasContent(false);
                daoMethod.setMethodName(methodName);
                daoMethod.addContentClass(Map.class.getName());
                daoMethod.addContentClass(List.class.getName());
                daoMethod.setReturnString(List.class.getName() + "<" + Map.class.getName() + ">");
                daoClass.addMethod(daoMethod);

                MethodInfo controllerMethod = new MethodInfo();
                controllerMethod.setDecorate("public");
                controllerMethod.setHasContent(true);
                controllerMethod.setMethodName(methodName);
                controllerMethod.addAnnotation(new AnnotationInfo(ResponseBody.class.getName()));
                AnnotationInfo requestMapping = new AnnotationInfo(RequestMapping.class.getName());
                requestMapping.addField("value", BootConstant.requestPrefix + methodName);
                requestMapping.addField("produces", "application/json;charset=UTF-8");
                controllerMethod.addAnnotation(requestMapping);
                controllerMethod.addContentClass(Map.class.getName());
                controllerMethod.addContentClass(List.class.getName());
                controllerMethod.setReturnString(List.class.getName() + "<" + Map.class.getName() + ">");

                StringBuilder sb = new StringBuilder();
                sb.append("        return this.").append(classField.getName()).append(".").append(methodName).append("();\n");
                controllerMethod.setContent(sb.toString());

                controllerClass.addMethod(controllerMethod);
            }
        }

        return elList;
    }


    @Override
    protected String getFile() {
        return FILE_PATH;
    }

    @Data
    public static class El {
        private String id;
        private String type;
        private String resultType;
        private String parameterType;
        private Boolean useGeneratedKeys;
        private String keyProperty;
        private String sql;
    }

    private String compileEls(List<El> els) {
        StringBuilder contentBuilder = new StringBuilder();
        for (El el : els) {
            String element = compileEl(el);
            contentBuilder.append(element).append("\n");
        }
        return contentBuilder.toString();
    }

    private String compileEl(El el) {
        Assert.notNull(el, "el can not be null");
        Assert.hasText(el.getId(), "id can not be null");
        Assert.hasText(el.getSql(), "sql can not be null");
        StringBuilder sb = new StringBuilder();
        sb.append("    <").append(el.getType()).append(" id=\"").append(el.getId()).append("\"");
        if (Strings.isNotBlank(el.getParameterType())) {
            sb.append(" parameterType=\"").append(el.getParameterType()).append("\"");
        }
        if (Strings.isNotBlank(el.getResultType())) {
            sb.append(" resultType=\"").append(el.getResultType()).append("\"");
        }
        if (el.getUseGeneratedKeys() != null) {
            sb.append(" useGeneratedKeys=\"").append(el.getUseGeneratedKeys()).append("\"");
        }
        if (el.getKeyProperty() != null) {
            sb.append(" keyProperty=\"").append(el.getKeyProperty()).append("\"");
        }
        sb.append(">\n");
        sb.append("        ").append(el.getSql()).append("\n");
        sb.append("    </").append(el.getType()).append(">");
        return sb.toString();
    }

    private String getMapperPath(String entityName) {
        return "backend" + File.separator + "src" +  File.separator+ "main" + File.separator + "resources" + File.separator +
                "potato_mappers" + File.separator + entityName + "Mapper.xml";
    }
}
