package com.appcnd.potato.generate.executor.handler;

import com.appcnd.potato.exception.HandlerException;
import com.appcnd.potato.generate.annotation.AnnotationInfo;
import com.appcnd.potato.generate.context.*;
import com.appcnd.potato.generate.executor.ComponentExecutor;
import com.appcnd.potato.generate.method.MethodInfo;
import com.appcnd.potato.generate.method.ParamInfo;
import com.appcnd.potato.meta.conf.db.Table;
import com.appcnd.potato.meta.conf.form.operate.OperateForm;
import com.appcnd.potato.meta.conf.form.operate.Unique;
import com.appcnd.potato.meta.conf.form.operate.elements.CreateTimeElement;
import com.appcnd.potato.meta.conf.form.operate.elements.OperateElement;
import com.appcnd.potato.meta.conf.form.operate.elements.UpdateTimeElement;
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
public class UpdateComponentHandler extends ComponentHandler {
    @Override
    protected List<ComponentExecutor.El> handler(HandlerRequest request) {
        FrontOperateContext infoContext = request.getFrontContext().getOperateContext();
        if (infoContext == null) {
            infoContext = new FrontOperateContext();
            infoContext.setPrimaryKeys(request.getTable().getPrimaryFields());
            infoContext.setElements(request.getOperateForm().getElements());
            infoContext.setUpdate(true);
            request.getFrontContext().setOperateContext(infoContext);
        } else {
            infoContext.setUpdate(true);
        }

        Table dbTable = request.getTable();
        OperateForm operateForm = request.getOperateForm();
        ClassName className = request.getClassName();
        JavaClassContext daoClass = request.getDaoClass();
        JavaClassContext serviceClass = request.getServiceClass();
        JavaClassContext serviceImplClass = request.getServiceImplClass();
        JavaClassContext controllerClass = request.getControllerClass();
        FrontContext frontContext = request.getFrontContext();

        List<ComponentExecutor.El> elList = new ArrayList<>();

        SqlStringBuilder sqlStringBuilder = new SqlStringBuilder();
        sqlStringBuilder.append("UPDATE ");
        sqlStringBuilder.put(dbTable.getName()).append(" SET ");
        for (OperateElement element : operateForm.getElements()) {
            if ((Boolean.TRUE.equals(element.getCanEdit()) && !(element instanceof CreateTimeElement))
                    || element instanceof UpdateTimeElement) {
                sqlStringBuilder.put(element.getColumn().getField())
                        .append("=#{").append(CamelUtil.underlineToCamel(element.getColumn().getField())).append("},");
            }
        }
        sqlStringBuilder.deleteCharAt(sqlStringBuilder.length() - 1);
        sqlStringBuilder.append(" WHERE ");
        for (String key : dbTable.getPrimaryFields()) {
            sqlStringBuilder.put(key).append("=#{")
                    .append(CamelUtil.underlineToCamel(key)).append("},");
        }
        sqlStringBuilder.deleteCharAt(sqlStringBuilder.length() - 1);

        if (operateForm.getUniques() != null && !operateForm.getUniques().isEmpty()) {
            int i = 0;
            for (Unique unique : operateForm.getUniques()) {
                SqlStringBuilder sb = new SqlStringBuilder();
                sb.append("SELECT ");
                int index = 0;
                for (String key : dbTable.getPrimaryFields()) {
                    if (index++ > 0){
                        sb.append(",");
                    }
                    sb.put(key).append(" AS ").append(CamelUtil.underlineToCamel(key));
                }
                sb.append(" FROM ").put(dbTable.getName())
                        .append(" WHERE ");
                index = 0;
                for (String column : unique.getColumns()) {
                    if (index++ > 0){
                        sb.append(" AND ");
                    }
                    sb.put(column).append("=#{").append(CamelUtil.underlineToCamel(column)).append("}");
                }
                for (String key : dbTable.getPrimaryFields()) {
                    sb.append(" AND ").put(key).append("!=#{").append(CamelUtil.underlineToCamel(key)).append("}");
                }
                String id = "selectUniqueByUpdate" + i++;
                ComponentExecutor.El el = new ComponentExecutor.El();
                el.setId(id);
                el.setParameterType(className.getPoClassName());
                el.setResultType(className.getPoClassName());
                el.setType("select");
                el.setSql(sb.toString());
                elList.add(el);

                MethodInfo daoMethod = new MethodInfo();
                daoMethod.setHasContent(false);
                daoMethod.addContentClass(className.getPoClassName());
                daoMethod.addContentClass(List.class.getName());
                daoMethod.setReturnString(List.class.getName() + "<" + className.getPoClassName() + ">");
                daoMethod.setMethodName(id);
                daoMethod.addParam(new ParamInfo(className.getPoClassName(), "param"));
                daoClass.addMethod(daoMethod);
            }
        }

        MethodInfo daoMethod = new MethodInfo();
        daoMethod.setHasContent(false);
        daoMethod.addContentClass(className.getPoClassName());
        daoMethod.addContentClass(Integer.class.getName());
        daoMethod.setReturnString(Integer.class.getName());
        daoMethod.setMethodName("update");
        daoMethod.addParam(new ParamInfo(className.getPoClassName(), "param"));
        daoClass.addMethod(daoMethod);

        MethodInfo serviceMethod = new MethodInfo();
        serviceMethod.setHasContent(false);
        serviceMethod.addContentClass(className.getUpdateReqClassName());
        serviceMethod.setMethodName("update");
        serviceMethod.addParam(new ParamInfo(className.getUpdateReqClassName(), "param"));
        serviceClass.addMethod(serviceMethod);

        MethodInfo serviceImplMethod = new MethodInfo();
        serviceImplMethod.setDecorate("public");
        serviceImplMethod.setHasContent(true);
        serviceImplMethod.addAnnotation(new AnnotationInfo(Override.class.getName()));
        serviceImplMethod.addContentClass(className.getPoClassName());
        serviceImplMethod.addContentClass(className.getUpdateReqClassName());
        serviceImplMethod.setMethodName("update");
        serviceImplMethod.addParam(new ParamInfo(className.getUpdateReqClassName(), "param"));
        ClassField daoClassField = null;
        for (ClassField classField : serviceImplClass.getFields()) {
            if (classField.getClassName().equals(daoClass.getClassName())) {
                daoClassField = classField;
                break;
            }
        }
        StringBuilder selectSingleParam = new StringBuilder();
        for (int i = 0, size = request.getTable().getPrimaryFields().size(); i < size; i ++) {
            if (i > 0) {
                selectSingleParam.append(",");
            }
            String pk = request.getTable().getPrimaryFields().get(i);
            String param = CamelUtil.underlineToCamel(pk);
            String method = CamelUtil.get(param);
            selectSingleParam.append("param.").append(method).append("()");
        }
        StringBuilder serviceContentBuilder = new StringBuilder();
        serviceContentBuilder.append("        ").append(className.getPoClassName()).append(" dbPo = ")
                .append("this.").append(daoClassField.getName()).append(".selectSingle(").append(selectSingleParam.toString()).append(");\n");
        serviceContentBuilder
                .append("        if (dbPo == null) {\n")
                .append("             throw new NullPointerException(\"不存在\");\n")
                .append("        }\n");
        serviceContentBuilder.append("        ").append(request.getClassName().getPoClassName())
                .append(" po = param.transferToPo();\n");
        for (OperateElement element : operateForm.getElements()) {
            if (element instanceof UpdateTimeElement) {
                UpdateTimeElement updateTimeElement = (UpdateTimeElement) element;
                if (updateTimeElement.getTimeFormatType() == TimeFormatType.DATE) {
                    if (updateTimeElement.getColumn().getJavaType().equals(Date.class)) {
                        serviceImplMethod.addContentClass(Date.class.getName());
                        serviceContentBuilder.append("        po.").append(CamelUtil.set(CamelUtil.underlineToCamel(updateTimeElement.getColumn().getField())))
                                .append("(new ").append(Date.class.getName()).append("());\n");
                    } else if (updateTimeElement.getColumn().getJavaType().equals(java.sql.Timestamp.class)) {
                        serviceImplMethod.addContentClass(java.sql.Timestamp.class.getName());
                        serviceImplMethod.addContentClass(System.class.getName());
                        serviceContentBuilder.append("        po.").append(CamelUtil.set(CamelUtil.underlineToCamel(updateTimeElement.getColumn().getField())))
                                .append("(new ").append(java.sql.Timestamp.class.getName()).append("(")
                                .append(System.class.getName()).append(".currentTimeMillis()));\n");
                    } else if (updateTimeElement.getColumn().getJavaType().equals(java.sql.Time.class)) {
                        serviceImplMethod.addContentClass(java.sql.Time.class.getName());
                        serviceImplMethod.addContentClass(System.class.getName());
                        serviceContentBuilder.append("        po.").append(CamelUtil.set(CamelUtil.underlineToCamel(updateTimeElement.getColumn().getField())))
                                .append("(new ").append(java.sql.Time.class.getName()).append("(")
                                .append(System.class.getName()).append(".currentTimeMillis()));\n");
                    } else {
                        throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "数据格式不支持");
                    }
                } else if (updateTimeElement.getTimeFormatType() == TimeFormatType.LONG) {
                    serviceImplMethod.addContentClass(System.class.getName());
                    serviceContentBuilder.append("        po.").append(CamelUtil.set(CamelUtil.underlineToCamel(updateTimeElement.getColumn().getField())))
                            .append("(").append(System.class.getName()).append(".currentTimeMillis());\n");
                }
            }
        }
        if (operateForm.getUniques() != null && !operateForm.getUniques().isEmpty()) {
            Set<String> uniqueColumns = new HashSet<>();
            for (Unique unique : operateForm.getUniques()) {
                uniqueColumns.addAll(unique.getColumns());
            }
            serviceContentBuilder.append("        ").append(request.getClassName().getPoClassName())
                    .append(" checkPo = new ").append(request.getClassName().getPoClassName()).append("();\n");
            for (String pk : request.getTable().getPrimaryFields()) {
                uniqueColumns.remove(pk);
                String getMethod = CamelUtil.get(pk);
                String setMethod = CamelUtil.set(pk);
                serviceContentBuilder.append("        checkPo.").append(setMethod)
                        .append("(param.").append(getMethod).append("());\n");
            }
            for (String uniqueColumn : uniqueColumns) {
                String param = CamelUtil.underlineToCamel(uniqueColumn);
                String getMethod = CamelUtil.get(param);
                String setMethod = CamelUtil.set(param);
                serviceContentBuilder
                        .append("        if (po.").append(getMethod).append("() != null) {\n")
                        .append("            checkPo.").append(setMethod).append("(po.").append(getMethod).append("());\n")
                        .append("        } else {\n")
                        .append("            checkPo.").append(setMethod).append("(dbPo.").append(getMethod).append("());\n")
                        .append("        }\n");
            }
            serviceImplMethod.addContentClass(List.class.getName());
            int i = 0;
            for (Unique unique : operateForm.getUniques()) {
                String methodName = "selectUniqueByUpdate" + i++;
                String name = methodName.replaceFirst("selectUniqueByUpdate", "unique");
                serviceContentBuilder.append("        ")
                        .append(List.class.getName()).append("<").append(className.getPoClassName()).append("> ");
                serviceContentBuilder.append(name)
                        .append(" = this.").append(daoClassField.getName()).append(".").append(methodName).append("(checkPo);\n");
                serviceContentBuilder.append("        if (").append(name).append(" != null && !").append(name).append(".isEmpty()) {\n");
                serviceContentBuilder.append("            throw new IllegalArgumentException(\"")
                        .append(unique.getToast()).append("\");\n");
                serviceContentBuilder.append("        }\n");
            }
        }
        serviceContentBuilder.append("        this.").append(daoClassField.getName()).append(".update(po);\n");
        serviceImplMethod.setContent(serviceContentBuilder.toString());
        serviceImplClass.addMethod(serviceImplMethod);

        MethodInfo controllerMethod = new MethodInfo();
        controllerMethod.setDecorate("public");
        controllerMethod.setHasContent(true);
        controllerMethod.setMethodName("update");
        controllerMethod.addAnnotation(new AnnotationInfo(ResponseBody.class.getName()));
        AnnotationInfo requestMapping = new AnnotationInfo(RequestMapping.class.getName());
        requestMapping.addField("value", frontContext.getUpdateRequest());
        requestMapping.addField("produces", "application/json;charset=UTF-8");
        controllerMethod.addAnnotation(requestMapping);
        controllerMethod.addContentClass(className.getUpdateReqClassName());
        controllerMethod.addContentClass(className.getVoClassName());
        controllerMethod.addContentClass(Map.class.getName());
        controllerMethod.addContentClass(request.getResponseVoSetting().getClassName());
        controllerMethod.setReturnString(request.getResponseVoSetting().getClassName());
        ParamInfo controllerParam = new ParamInfo(className.getUpdateReqClassName(), "param");
        controllerParam.addAnnotation(new AnnotationInfo(RequestBody.class.getName()));
        controllerParam.addAnnotation(new AnnotationInfo(Validated.class.getName()));
        controllerMethod.addParam(controllerParam);
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
                .append("            this.").append(serviceClassField.getName()).append(".update(param);\n")
                .append("            result.").append(request.getResponseVoSetting().getSetSuccessMethod()).append(";\n")
                .append("            result.").append(request.getResponseVoSetting().getMessageSetMethod()).append("(\"更新成功!\");\n")
                .append("        } catch (Exception e) {\n")
                .append("            result.").append(request.getResponseVoSetting().getSetErrorMethod()).append(";\n")
                .append("            result.").append(request.getResponseVoSetting().getMessageSetMethod()).append("(e.getMessage());\n")
                .append("        }\n")
                .append("        return result;\n");
        controllerMethod.setContent(controllerContentBuilder.toString());
        controllerClass.addMethod(controllerMethod);

        ComponentExecutor.El el = new ComponentExecutor.El();
        el.setId("update");
        el.setParameterType(className.getPoClassName());
        el.setType("update");
        el.setSql(sqlStringBuilder.toString());
        elList.add(el);
        return elList;
    }
}
