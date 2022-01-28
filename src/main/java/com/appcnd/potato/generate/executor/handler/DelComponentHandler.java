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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
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
        serviceMethod.addContentClass(className.getDeleteReqClassName());
        serviceMethod.setMethodName("delete");
        serviceMethod.addParam(new ParamInfo(className.getDeleteReqClassName(), "param"));
        serviceClass.addMethod(serviceMethod);

        MethodInfo serviceImplMethod = new MethodInfo();
        serviceImplMethod.setDecorate("public");
        serviceImplMethod.setHasContent(true);
        serviceImplMethod.addAnnotation(new AnnotationInfo(Override.class.getName()));
        serviceImplMethod.addContentClass(className.getPoClassName());
        serviceImplMethod.addContentClass(className.getDeleteReqClassName());
        serviceImplMethod.setMethodName("delete");
        serviceImplMethod.addParam(new ParamInfo(className.getDeleteReqClassName(), "param"));
        StringBuilder serviceContentBuilder = new StringBuilder();
        serviceContentBuilder.append("        ").append(request.getClassName().getPoClassName())
                .append(" po = param.transferToPo();\n");
        ClassField daoClassField = null;
        for (ClassField classField : serviceImplClass.getFields()) {
            if (classField.getClassName().equals(daoClass.getClassName())) {
                daoClassField = classField;
                break;
            }
        }
        serviceContentBuilder.append("        this.").append(daoClassField.getName()).append(".delete(po);\n");
        serviceImplMethod.setContent(serviceContentBuilder.toString());
        serviceImplClass.addMethod(serviceImplMethod);

        MethodInfo controllerMethod = new MethodInfo();
        controllerMethod.setDecorate("public");
        controllerMethod.setHasContent(true);
        controllerMethod.setMethodName("delete");
        AnnotationInfo requestMapping = new AnnotationInfo(RequestMapping.class.getName());
        requestMapping.addField("value", frontContext.getDeleteRequest());
        requestMapping.addField("produces", "application/json;charset=UTF-8");
        controllerMethod.addAnnotation(requestMapping);
        controllerMethod.addContentClass(className.getDeleteReqClassName());
        controllerMethod.addContentClass(Map.class.getName());
        controllerMethod.addContentClass(request.getResponseVoSetting().getClassName());
        controllerMethod.setReturnString(request.getResponseVoSetting().getClassName());
        ParamInfo paramInfo = new ParamInfo(className.getDeleteReqClassName(), "param");
        paramInfo.addAnnotation(new AnnotationInfo(Validated.class.getName()));
        controllerMethod.addParam(paramInfo);
        ClassField serviceClassField = null;
        for (ClassField classField : controllerClass.getFields()) {
            if (classField.getClassName().equals(serviceClass.getClassName())) {
                serviceClassField = classField;
                break;
            }
        }

        StringBuilder controllerContentBuilder = new StringBuilder();
        controllerContentBuilder.append("        ").append(request.getResponseVoSetting().getClassName())
                .append(" result = new ").append(request.getResponseVoSetting().getClassName()).append("();\n");
        controllerContentBuilder
                .append("        try {\n")
                .append("            this.").append(serviceClassField.getName()).append(".delete(param);\n")
                .append("            result.").append(request.getResponseVoSetting().getSetSuccessMethod()).append(";\n")
                .append("            result.").append(request.getResponseVoSetting().getMessageSetMethod()).append("(\"删除成功!\");\n")
                .append("        } catch (Exception e) {\n")
                .append("            result.").append(request.getResponseVoSetting().getSetErrorMethod()).append(";\n")
                .append("            result.").append(request.getResponseVoSetting().getMessageSetMethod()).append("(e.getMessage());\n")
                .append("        }\n")
                .append("        return result;\n");
        controllerMethod.setContent(controllerContentBuilder.toString());
        request.getControllerClass().addMethod(controllerMethod);


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
