package com.appcnd.potato.generate.executor.handler;

import com.appcnd.potato.generate.annotation.AnnotationInfo;
import com.appcnd.potato.generate.context.*;
import com.appcnd.potato.generate.executor.ComponentExecutor;
import com.appcnd.potato.generate.method.MethodInfo;
import com.appcnd.potato.generate.method.ParamInfo;
import com.appcnd.potato.meta.conf.db.Column;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.db.Table;
import com.appcnd.potato.meta.conf.form.operate.OperateForm;
import com.appcnd.potato.util.CamelUtil;
import com.appcnd.potato.util.SqlStringBuilder;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nihao 2021/11/04
 */
public class DelComponentHandler extends ComponentHandler {
    @Override
    protected List<ComponentExecutor.El> handler(HandlerRequest request) {
        FrontOperateContext infoContext = request.getFrontContext().getOperateContext();
        if (infoContext == null) {
            infoContext = new FrontOperateContext();
            infoContext.setPrimaryKeys(request.getTable().getPrimaryFields());
            infoContext.setDelete(true);
            request.getFrontContext().setOperateContext(infoContext);
        } else {
            infoContext.setDelete(true);
        }

        List<ComponentExecutor.El> elList = new ArrayList<>();
        Table dbTable = request.getTable();
        ClassName className = request.getClassName();
        JavaClassContext daoClass = request.getDaoClass();
        JavaClassContext serviceClass = request.getServiceClass();
        JavaClassContext serviceImplClass = request.getServiceImplClass();
        JavaClassContext controllerClass = request.getControllerClass();
        FrontContext frontContext = request.getFrontContext();

        SqlStringBuilder sqlStringBuilder = new SqlStringBuilder();
        sqlStringBuilder.append("DELETE FROM ").put(request.getTable().getName())
                .append(" WHERE ");
        for (String key : dbTable.getPrimaryFields()) {
            sqlStringBuilder.put(key).append("=#{")
                    .append(CamelUtil.underlineToCamel(key)).append("},");
        }
        sqlStringBuilder.deleteCharAt(sqlStringBuilder.length() - 1);

        MethodInfo daoMethod = new MethodInfo();
        daoMethod.setHasContent(false);
        daoMethod.addContentClass(className.getPoClassName());
        daoMethod.addContentClass(Integer.class.getName());
        daoMethod.setReturnString(Integer.class.getName());
        daoMethod.setMethodName("delete");
        daoMethod.addParam(new ParamInfo(className.getPoClassName(), "param"));
        daoClass.addMethod(daoMethod);

        MethodInfo serviceMethod = new MethodInfo();
        serviceMethod.setHasContent(false);
        serviceMethod.addContentClass(className.getPoClassName());
        serviceMethod.setMethodName("delete");
        serviceMethod.addParam(new ParamInfo(className.getPoClassName(), "param"));
        serviceClass.addMethod(serviceMethod);

        MethodInfo serviceImplMethod = new MethodInfo();
        serviceImplMethod.setDecorate("public");
        serviceImplMethod.setHasContent(true);
        serviceImplMethod.addAnnotation(new AnnotationInfo(Override.class.getName()));
        serviceImplMethod.addContentClass(className.getPoClassName());
        serviceImplMethod.setMethodName("delete");
        serviceImplMethod.addParam(new ParamInfo(className.getPoClassName(), "param"));
        StringBuilder serviceContentBuilder = new StringBuilder();
        ClassField daoClassField = null;
        for (ClassField classField : serviceImplClass.getFields()) {
            if (classField.getClassName().equals(daoClass.getClassName())) {
                daoClassField = classField;
                break;
            }
        }
        serviceContentBuilder.append("        this.").append(daoClassField.getName()).append(".delete(param);\n");
        serviceImplMethod.setContent(serviceContentBuilder.toString());
        serviceImplClass.addMethod(serviceImplMethod);

        MethodInfo controllerMethod = new MethodInfo();
        controllerMethod.setDecorate("public");
        controllerMethod.setHasContent(true);
        controllerMethod.setMethodName("delete");
        controllerMethod.addAnnotation(new AnnotationInfo(ResponseBody.class.getName()));
        AnnotationInfo requestMapping = new AnnotationInfo(RequestMapping.class.getName());
        requestMapping.addField("value", frontContext.getDeleteRequest());
        requestMapping.addField("produces", "application/json;charset=UTF-8");
        controllerMethod.addAnnotation(requestMapping);
        controllerMethod.addContentClass(className.getPoClassName());
        controllerMethod.addContentClass(Map.class.getName());
        controllerMethod.addContentClass(HashMap.class.getName());
        controllerMethod.setReturnString(Map.class.getName());
        controllerMethod.addParam(new ParamInfo(className.getPoClassName(), "param"));
        ClassField serviceClassField = null;
        for (ClassField classField : controllerClass.getFields()) {
            if (classField.getClassName().equals(serviceClass.getClassName())) {
                serviceClassField = classField;
                break;
            }
        }
        StringBuilder controllerContentBuilder = new StringBuilder();
        controllerContentBuilder.append("        ").append(Map.class.getName()).append(" result = new ").append(HashMap.class.getName()).append("();\n");
        controllerContentBuilder.append("        try {\n")
                .append("            this.").append(serviceClassField.getName()).append(".delete(param);\n")
                .append("            result.put(\"code\", 0);\n")
                .append("            result.put(\"message\", \"OK\");\n")
                .append("        } catch (Exception e) {\n")
                .append("            result.put(\"code\", 1);\n")
                .append("            result.put(\"message\", e.getMessage());\n")
                .append("        }\n")
                .append("        return result;\n");
        controllerMethod.setContent(controllerContentBuilder.toString());
        controllerClass.addMethod(controllerMethod);


        ComponentExecutor.El el = new ComponentExecutor.El();
        el.setId("delete");
        el.setParameterType(className.getPoClassName());
        el.setType("delete");
        el.setSql(sqlStringBuilder.toString());
        elList.add(el);

        cascadingDelete(request, elList, serviceImplMethod);

        return elList;
    }

    /**
     * 处理级联删除
     */
    private void cascadingDelete(HandlerRequest request, List<ComponentExecutor.El> elList, MethodInfo serviceImplMethod) {
        if (Boolean.TRUE.equals(request.getOperateForm().getCascadingDelete())) {
            FollowTable table = (FollowTable) request.getTable();
            SqlStringBuilder sqlStringBuilder = new SqlStringBuilder();
            String paramName = CamelUtil.underlineToCamel(table.getForeignKey());
            sqlStringBuilder.append("DELETE FROM ").put(request.getTable().getName())
                    .append(" WHERE ").put(table.getForeignKey()).append("=#{")
                    .append(paramName).append("}");

            Class paramType = null;
            Class foreignJavaType = null;
            for (Column column : request.getTable().getColumns()) {
                if (column.getField().equals(table.getForeignKey())) {
                    foreignJavaType = column.getJavaType();
                    break;
                }
            }
            Class parentJavaType = request.getParentKeyClass();
            if (foreignJavaType.equals(parentJavaType)) {
                paramType = foreignJavaType;
            } else {
                paramType = Object.class;
            }


            JavaClassContext daoClass = request.getDaoClass();
            MethodInfo daoMethod = new MethodInfo();
            daoMethod.setHasContent(false);
            daoMethod.addContentClass(Integer.class.getName());
            daoMethod.setReturnString(Integer.class.getName());
            daoMethod.setMethodName("deleteCascading");
            ParamInfo paramInfo = new ParamInfo(paramType.getName(), CamelUtil.underlineToCamel(table.getForeignKey()));
            AnnotationInfo annotationInfo = new AnnotationInfo(Param.class.getName());
            annotationInfo.addField("value", paramName);
            paramInfo.addAnnotation(annotationInfo);
            daoMethod.addParam(paramInfo);
            daoClass.addMethod(daoMethod);

            ComponentExecutor.El el = new ComponentExecutor.El();
            el.setId("deleteCascading");
            el.setType("delete");
            el.setSql(sqlStringBuilder.toString());
            elList.add(el);
        }
        if (request.getOperateForm().getFollows() != null) {
            for (int i = 0; i < request.getOperateForm().getFollows().size(); i ++) {
                OperateForm followOperateForm = request.getOperateForm().getFollows().get(i);
                if (Boolean.TRUE.equals(followOperateForm.getCascadingDelete())) {
                    HandlerRequest followHandlerRequest = request.getFollowHandlerRequests().get(i);
                    ClassName followClassName = followHandlerRequest.getClassName();
                    AnnotationInfo annotationInfo = new AnnotationInfo(Autowired.class.getName());
                    ClassField classField = new ClassField("private", followClassName.getDaoClassName(), annotationInfo);
                    if (!request.getServiceImplClass().getFields().contains(classField)) {
                        request.getServiceImplClass().addField(classField);
                    }

                    StringBuilder contentBuilder = new StringBuilder();
                    contentBuilder.append(serviceImplMethod.getContent());
                    contentBuilder.append("        this.").append(classField.getName())
                            .append(".deleteCascading(param.")
                            .append(CamelUtil.get(((FollowTable) followHandlerRequest.getTable()).getParentKey())).append("()")
                            .append(");\n");
                    serviceImplMethod.setContent(contentBuilder.toString());
                    serviceImplMethod.addAnnotationToTop(new AnnotationInfo(Transactional.class.getName()));
                }
            }
        }
    }
}
