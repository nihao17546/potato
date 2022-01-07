package com.appcnd.potato.generate.executor.handler;

import com.appcnd.potato.exception.HandlerException;
import com.appcnd.potato.generate.annotation.AnnotationInfo;
import com.appcnd.potato.generate.context.ClassField;
import com.appcnd.potato.generate.context.FrontOperateContext;
import com.appcnd.potato.generate.executor.ComponentExecutor;
import com.appcnd.potato.generate.method.MethodInfo;
import com.appcnd.potato.generate.method.ParamInfo;
import com.appcnd.potato.meta.conf.db.Column;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.db.enums.PrimaryType;
import com.appcnd.potato.meta.conf.form.operate.Unique;
import com.appcnd.potato.meta.conf.form.operate.elements.CreateTimeElement;
import com.appcnd.potato.meta.conf.form.operate.elements.OperateElement;
import com.appcnd.potato.meta.conf.form.operate.enums.TimeFormatType;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.util.CamelUtil;
import com.appcnd.potato.util.SqlStringBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author nihao 2021/11/03
 */
public class InsertComponentHandler extends ComponentHandler {
    @Override
    protected List<ComponentExecutor.El> handler(HandlerRequest request) {
        FrontOperateContext infoContext = request.getFrontContext().getOperateContext();
        if (infoContext == null) {
            infoContext = new FrontOperateContext();
            infoContext.setPrimaryKeys(request.getTable().getPrimaryFields());
            infoContext.setElements(request.getOperateForm().getElements());
            infoContext.setInsert(true);
            request.getFrontContext().setOperateContext(infoContext);
        } else {
            infoContext.setInsert(true);
        }

        List<ComponentExecutor.El> elList = new ArrayList<>();

        SqlStringBuilder sqlStringBuilder = new SqlStringBuilder();
        sqlStringBuilder.append("INSERT INTO ");
        sqlStringBuilder.put(request.getTable().getName());
        sqlStringBuilder.append("(");
        if (request.getTable().getPrimaryType() == PrimaryType.NORMAL
                || request.getTable().getPrimaryType() == PrimaryType.UUID) {
            for (String key : request.getTable().getPrimaryFields()) {
                sqlStringBuilder.put(key).append(",");
            }
        }
        String foreignKey = null;
        if (request.getTable() instanceof FollowTable) {
            FollowTable tb = (FollowTable) request.getTable();
            foreignKey = tb.getForeignKey();
            sqlStringBuilder.put(foreignKey).append(",");
        }
        for (OperateElement element : request.getOperateForm().getElements()) {
            Column column = element.getColumn();
            if (!request.getTable().getPrimaryFields().contains(column.getField())
                    && !column.getField().equals(foreignKey)) {
                sqlStringBuilder.put(column.getField()).append(",");
            }
        }
        sqlStringBuilder.deleteCharAt(sqlStringBuilder.length() - 1);
        sqlStringBuilder.append(") VALUES (");
        if (request.getTable().getPrimaryType() == PrimaryType.NORMAL
                || request.getTable().getPrimaryType() == PrimaryType.UUID) {
            for (String key : request.getTable().getPrimaryFields()) {
                sqlStringBuilder.append("#{").append(CamelUtil.underlineToCamel(key)).append("},");
            }
        }
        if (foreignKey != null) {
            sqlStringBuilder.append("#{").append(CamelUtil.underlineToCamel(foreignKey)).append("},");
        }
        for (OperateElement element : request.getOperateForm().getElements()) {
            Column column = element.getColumn();
            if (!request.getTable().getPrimaryFields().contains(column.getField())
                    && !column.getField().equals(foreignKey)) {
                sqlStringBuilder.append("#{").append(CamelUtil.underlineToCamel(column.getField())).append("},");
            }
        }
        sqlStringBuilder.deleteCharAt(sqlStringBuilder.length() - 1);
        sqlStringBuilder.append(")");

        if (request.getOperateForm().getUniques() != null && !request.getOperateForm().getUniques().isEmpty()) {
            int i = 0;
            for (Unique unique : request.getOperateForm().getUniques()) {
                SqlStringBuilder sb = new SqlStringBuilder();
                sb.append("SELECT ");
                int index = 0;
                for (String key : request.getTable().getPrimaryFields()) {
                    if (index++ > 0){
                        sb.append(",");
                    }
                    sb.put(key).append(" AS ").append(CamelUtil.underlineToCamel(key));
                }
                sb.append(" FROM ").put(request.getTable().getName())
                        .append(" WHERE ");
                index = 0;
                for (String column : unique.getColumns()) {
                    if (index++ > 0){
                        sb.append(" AND ");
                    }
                    sb.put(column).append("=#{").append(CamelUtil.underlineToCamel(column)).append("}");
                }
                String id = "selectUnique" + i++;
                ComponentExecutor.El el = new ComponentExecutor.El();
                el.setId(id);
                el.setParameterType(request.getClassName().getPoClassName());
                el.setResultType(request.getClassName().getPoClassName());
                el.setType("select");
                el.setSql(sb.toString());
                elList.add(el);

                MethodInfo daoMethod = new MethodInfo();
                daoMethod.setHasContent(false);
                daoMethod.addContentClass(request.getClassName().getPoClassName());
                daoMethod.addContentClass(List.class.getName());
                daoMethod.setReturnString(List.class.getName() + "<" + request.getClassName().getPoClassName() + ">");
                daoMethod.setMethodName(id);
                daoMethod.addParam(new ParamInfo(request.getClassName().getPoClassName(), "param"));
                request.getDaoClass().addMethod(daoMethod);
            }
        }

        MethodInfo daoMethod = new MethodInfo();
        daoMethod.setHasContent(false);
        daoMethod.addContentClass(request.getClassName().getPoClassName());
        daoMethod.addContentClass(Integer.class.getName());
        daoMethod.setReturnString(Integer.class.getName());
        daoMethod.setMethodName("insert");
        daoMethod.addParam(new ParamInfo(request.getClassName().getPoClassName(), "param"));
        request.getDaoClass().addMethod(daoMethod);

        MethodInfo serviceMethod = new MethodInfo();
        serviceMethod.setHasContent(false);
        serviceMethod.addContentClass(request.getClassName().getInsertReqClassName());
        serviceMethod.setMethodName("save");
        serviceMethod.addParam(new ParamInfo(request.getClassName().getInsertReqClassName(), "param"));
        request.getServiceClass().addMethod(serviceMethod);

        MethodInfo serviceImplMethod = new MethodInfo();
        serviceImplMethod.setDecorate("public");
        serviceImplMethod.setHasContent(true);
        serviceImplMethod.addAnnotation(new AnnotationInfo(Override.class.getName()));
        serviceImplMethod.addContentClass(request.getClassName().getInsertReqClassName());
        serviceImplMethod.addContentClass(request.getClassName().getPoClassName());
        serviceImplMethod.setMethodName("save");
        serviceImplMethod.addParam(new ParamInfo(request.getClassName().getInsertReqClassName(), "param"));
        StringBuilder serviceContentBuilder = new StringBuilder();
        serviceContentBuilder.append("        ").append(request.getClassName().getPoClassName())
                .append(" po = param.transferToPo();\n");
        ClassField daoClassField = null;
        for (ClassField classField : request.getServiceImplClass().getFields()) {
            if (classField.getClassName().equals(request.getDaoClass().getClassName())) {
                daoClassField = classField;
                break;
            }
        }
        if (request.getTable().getPrimaryType() == PrimaryType.UUID) {
            serviceImplMethod.addContentClass(UUID.class.getName());
            for (String key : request.getTable().getPrimaryFields()) {
                serviceContentBuilder.append("        po.").append(CamelUtil.set(CamelUtil.underlineToCamel(key)))
                        .append("(").append(UUID.class.getName()).append(".randomUUID().toString()").append(");\n");
            }
        }
        for (OperateElement element : request.getOperateForm().getElements()) {
            if (element instanceof CreateTimeElement) {
                CreateTimeElement createTimeElement = (CreateTimeElement) element;
                if (createTimeElement.getTimeFormatType() == TimeFormatType.DATE) {
                    if (createTimeElement.getColumn().getJavaType().equals(Date.class)) {
                        serviceImplMethod.addContentClass(Date.class.getName());
                        serviceContentBuilder.append("        po.").append(CamelUtil.set(CamelUtil.underlineToCamel(createTimeElement.getColumn().getField())))
                                .append("(new ").append(Date.class.getName()).append("());\n");
                    } else if (createTimeElement.getColumn().getJavaType().equals(java.sql.Timestamp.class)) {
                        serviceImplMethod.addContentClass(java.sql.Timestamp.class.getName());
                        serviceImplMethod.addContentClass(System.class.getName());
                        serviceContentBuilder.append("        po.").append(CamelUtil.set(CamelUtil.underlineToCamel(createTimeElement.getColumn().getField())))
                                .append("(new ").append(java.sql.Timestamp.class.getName()).append("(")
                                .append(System.class.getName()).append(".currentTimeMillis()));\n");
                    } else if (createTimeElement.getColumn().getJavaType().equals(java.sql.Time.class)) {
                        serviceImplMethod.addContentClass(java.sql.Time.class.getName());
                        serviceImplMethod.addContentClass(System.class.getName());
                        serviceContentBuilder.append("        po.").append(CamelUtil.set(CamelUtil.underlineToCamel(createTimeElement.getColumn().getField())))
                                .append("(new ").append(java.sql.Time.class.getName()).append("(")
                                .append(System.class.getName()).append(".currentTimeMillis()));\n");
                    } else {
                        throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "数据格式不支持");
                    }
                } else if (createTimeElement.getTimeFormatType() == TimeFormatType.LONG) {
                    serviceImplMethod.addContentClass(System.class.getName());
                    serviceContentBuilder.append("        po.").append(CamelUtil.set(CamelUtil.underlineToCamel(createTimeElement.getColumn().getField())))
                            .append("(").append(System.class.getName()).append(".currentTimeMillis());\n");
                }
            }
        }
        if (request.getOperateForm().getUniques() != null && !request.getOperateForm().getUniques().isEmpty()) {
            serviceImplMethod.addContentClass(List.class.getName());
            int i = 0;
            for (Unique unique : request.getOperateForm().getUniques()) {
                String methodName = "selectUnique" + i++;
                String name = methodName.replaceFirst("selectU", "u");
                serviceContentBuilder.append("        ")
                        .append(List.class.getName()).append("<").append(request.getClassName().getPoClassName()).append("> ");
                serviceContentBuilder.append(name)
                        .append(" = this.").append(daoClassField.getName()).append(".").append(methodName).append("(po);\n");
                serviceContentBuilder.append("        if (").append(name).append(" != null && !").append(name).append(".isEmpty()) {\n");
                serviceContentBuilder.append("            throw new IllegalArgumentException(\"")
                        .append(unique.getToast()).append("\");\n");
                serviceContentBuilder.append("        }\n");
            }
        }
        serviceContentBuilder.append("        this.").append(daoClassField.getName()).append(".insert(po);\n");
        serviceImplMethod.setContent(serviceContentBuilder.toString());
        request.getServiceImplClass().addMethod(serviceImplMethod);

        MethodInfo controllerMethod = new MethodInfo();
        controllerMethod.setDecorate("public");
        controllerMethod.setHasContent(true);
        controllerMethod.setMethodName("save");
        controllerMethod.addAnnotation(new AnnotationInfo(ResponseBody.class.getName()));
        AnnotationInfo requestMapping = new AnnotationInfo(RequestMapping.class.getName());
        requestMapping.addField("value", request.getFrontContext().getSaveRequest());
        requestMapping.addField("produces", "application/json;charset=UTF-8");
        controllerMethod.addAnnotation(requestMapping);
        controllerMethod.addContentClass(request.getClassName().getInsertReqClassName());
        controllerMethod.addContentClass(request.getClassName().getVoClassName());
        controllerMethod.addContentClass(request.getResponseVoSetting().getClassName());
        controllerMethod.setReturnString(request.getResponseVoSetting().getClassName());
        ParamInfo controllerParam = new ParamInfo(request.getClassName().getInsertReqClassName(), "param");
        controllerParam.addAnnotation(new AnnotationInfo(RequestBody.class.getName()));
        controllerParam.addAnnotation(new AnnotationInfo(Validated.class.getName()));
        controllerMethod.addParam(controllerParam);
        ClassField serviceClassField = null;
        for (ClassField classField : request.getControllerClass().getFields()) {
            if (classField.getClassName().equals(request.getServiceClass().getClassName())) {
                serviceClassField = classField;
                break;
            }
        }
        StringBuilder controllerContentBuilder = new StringBuilder();
        controllerContentBuilder.append("        ").append(request.getResponseVoSetting().getClassName())
                .append(" result = new ").append(request.getResponseVoSetting().getClassName()).append("();\n");
        controllerContentBuilder
                .append("        try {\n")
                .append("            this.").append(serviceClassField.getName()).append(".save(param);\n")
                .append("            result.").append(request.getResponseVoSetting().getSetSuccessMethod()).append(";\n")
                .append("            result.").append(request.getResponseVoSetting().getMessageSetMethod()).append("(\"创建成功!\");\n")
                .append("        } catch (Exception e) {\n")
                .append("            result.").append(request.getResponseVoSetting().getSetErrorMethod()).append(";\n")
                .append("            result.").append(request.getResponseVoSetting().getMessageSetMethod()).append("(e.getMessage());\n")
                .append("        }\n")
                .append("        return result;\n");
        controllerMethod.setContent(controllerContentBuilder.toString());
        request.getControllerClass().addMethod(controllerMethod);

        ComponentExecutor.El el = new ComponentExecutor.El();
        el.setId("insert");
        el.setParameterType(request.getClassName().getPoClassName());
        el.setType("insert");
        el.setSql(sqlStringBuilder.toString());
        if (request.getTable().getPrimaryType() == PrimaryType.AUTO) {
            el.setUseGeneratedKeys(true);
            el.setKeyProperty(CamelUtil.underlineToCamel(request.getTable().getPrimaryFields().get(0)));
        }
        elList.add(el);
        return elList;
    }
}
