package cn.thecover.potato.generate.executor.handler;

import cn.thecover.potato.generate.annotation.AnnotationInfo;
import cn.thecover.potato.generate.context.ClassField;
import cn.thecover.potato.generate.context.FrontOperateContext;
import cn.thecover.potato.generate.executor.ComponentExecutor;
import cn.thecover.potato.generate.method.MethodInfo;
import cn.thecover.potato.generate.method.ParamInfo;
import cn.thecover.potato.meta.conf.db.Column;
import cn.thecover.potato.meta.conf.db.FollowTable;
import cn.thecover.potato.meta.conf.form.operate.elements.OperateElement;
import cn.thecover.potato.util.CamelUtil;
import cn.thecover.potato.util.SqlStringBuilder;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author nihao 2021/11/03
 */
public class InfoComponentHandler extends ComponentHandler {
    @Override
    public List<ComponentExecutor.El> handler(HandlerRequest request) {
        FrontOperateContext infoContext = request.getFrontContext().getOperateContext();
        if (infoContext == null) {
            infoContext = new FrontOperateContext();
            infoContext.setPrimaryKeys(request.getTable().getPrimaryFields());
            infoContext.setElements(request.getOperateForm().getElements());
            request.getFrontContext().setOperateContext(infoContext);
        }

        SqlStringBuilder sqlStringBuilder = new SqlStringBuilder();
        sqlStringBuilder.append("SELECT ");

        Map<String,Class> classMap = new HashMap<>();
        for (Column column : request.getTable().getColumns()) {
            classMap.put(column.getField(), column.getJavaType());
        }

        Set<String> fields = new HashSet<>();
        for (OperateElement element : request.getOperateForm().getElements()) {
            Column column = element.getColumn();
            String field = CamelUtil.underlineToCamel(column.getField());
            sqlStringBuilder.put(column.getField())
                    .append(" AS ")
                    .put(field).append(",");
            fields.add(field);
        }
        sqlStringBuilder.deleteCharAt(sqlStringBuilder.length() - 1);

        for (String pk : request.getTable().getPrimaryFields()) {
            if (!fields.contains(pk)) {
                String field = CamelUtil.underlineToCamel(pk);
                sqlStringBuilder.append(",").put(pk)
                        .append(" AS ")
                        .put(field);
                fields.add(field);
            }
        }

        if (request.getFollowTables() != null) {
            for (FollowTable followTable : request.getFollowTables()) {
                if (!fields.contains(followTable.getParentKey())) {
                    String field = CamelUtil.underlineToCamel(followTable.getParentKey());
                    sqlStringBuilder.append(",").put(followTable.getParentKey())
                            .append(" AS ")
                            .put(field);
                    fields.add(field);
                }
            }
        }

        sqlStringBuilder.append(" FROM ").put(request.getTable().getName())
                .append(" WHERE ");

        List<ParamInfo> daoParamInfoList = new ArrayList<>();
        List<ParamInfo> serviceParamInfoList = new ArrayList<>();
        List<ParamInfo> controllerParamInfoList = new ArrayList<>();
        StringBuilder serviceParamBuilder = new StringBuilder();
        for (int i = 0, size = request.getTable().getPrimaryFields().size(); i < size; i ++) {
            String pk = request.getTable().getPrimaryFields().get(i);
            String param = CamelUtil.underlineToCamel(pk);
            Class clazz = classMap.get(pk);

            if (i != 0) {
                sqlStringBuilder.append(" AND ");
            }
            sqlStringBuilder.put(pk).append("=#{").append(param).append("}");

            ParamInfo daoParamInfo = new ParamInfo(clazz.getName(), param);
            AnnotationInfo annotationInfo = new AnnotationInfo(Param.class.getName());
            annotationInfo.addField("value", param);
            daoParamInfo.addAnnotation(annotationInfo);
            daoParamInfoList.add(daoParamInfo);

            ParamInfo serviceParamInfo = new ParamInfo(clazz.getName(), param);
            serviceParamInfoList.add(serviceParamInfo);

            ParamInfo controllerParamInfo = new ParamInfo(clazz.getName(), param);
            AnnotationInfo controllerAnnotationInfo = new AnnotationInfo(RequestParam.class.getName());
            controllerAnnotationInfo.addField("required", true);
            controllerAnnotationInfo.addField("name", param);
            controllerParamInfo.addAnnotation(controllerAnnotationInfo);
            controllerParamInfoList.add(controllerParamInfo);

            serviceParamBuilder.append(param).append(",");
        }
        if (serviceParamBuilder.length() > 0) {
            serviceParamBuilder.deleteCharAt(serviceParamBuilder.length() - 1);
        }

        MethodInfo daoGetInfoMethod = new MethodInfo();
        daoGetInfoMethod.setHasContent(false);
        daoGetInfoMethod.addContentClass(request.getClassName().getPoClassName());
        daoGetInfoMethod.setReturnString(request.getClassName().getPoClassName());
        daoGetInfoMethod.setMethodName("selectSingle");
        daoGetInfoMethod.addParam(daoParamInfoList);
        request.getDaoClass().addMethod(daoGetInfoMethod);

        MethodInfo serviceGetInfoMethod = new MethodInfo();
        serviceGetInfoMethod.setHasContent(false);
        serviceGetInfoMethod.addContentClass(request.getClassName().getPoClassName());
        serviceGetInfoMethod.addContentClass(request.getClassName().getVoClassName());
        serviceGetInfoMethod.setReturnString(request.getClassName().getVoClassName());
        serviceGetInfoMethod.setMethodName("getSingle");
        serviceGetInfoMethod.addParam(serviceParamInfoList);
        request.getServiceClass().addMethod(serviceGetInfoMethod);

        MethodInfo serviceImplGetInfoMethod = new MethodInfo();
        serviceImplGetInfoMethod.setDecorate("public");
        serviceImplGetInfoMethod.setHasContent(true);
        serviceImplGetInfoMethod.addAnnotation(new AnnotationInfo(Override.class.getName()));
        serviceImplGetInfoMethod.addContentClass(request.getClassName().getPoClassName());
        serviceImplGetInfoMethod.addContentClass(request.getClassName().getVoClassName());
        serviceImplGetInfoMethod.setReturnString(request.getClassName().getVoClassName());
        serviceImplGetInfoMethod.setMethodName("getSingle");
        serviceImplGetInfoMethod.addParam(serviceParamInfoList);
        StringBuilder serviceContentBuilder = new StringBuilder();
        ClassField daoClassField = null;
        for (ClassField classField : request.getServiceImplClass().getFields()) {
            if (classField.getClassName().equals(request.getDaoClass().getClassName())) {
                daoClassField = classField;
                break;
            }
        }
        serviceContentBuilder.append("        ")
                .append(request.getClassName().getPoClassName())
                .append(" po = this.").append(daoClassField.getName()).append(".selectSingle(");
        if (serviceParamBuilder.length() > 0) {
            serviceContentBuilder.append(serviceParamBuilder.toString());
        }
        serviceContentBuilder.append(");\n");
        serviceContentBuilder.append("        if (po == null) {\n");
        serviceContentBuilder.append("            return null;\n");
        serviceContentBuilder.append("        }\n");
        serviceContentBuilder.append("        return new ").append(request.getClassName().getVoClassName()).append("(po);\n");
        serviceImplGetInfoMethod.setContent(serviceContentBuilder.toString());
        request.getServiceImplClass().addMethod(serviceImplGetInfoMethod);

        MethodInfo controllerGetInfoMethod = new MethodInfo();
        controllerGetInfoMethod.setDecorate("public");
        controllerGetInfoMethod.setHasContent(true);
        controllerGetInfoMethod.setMethodName("getSingle");
        controllerGetInfoMethod.addAnnotation(new AnnotationInfo(ResponseBody.class.getName()));
        AnnotationInfo requestMapping = new AnnotationInfo(RequestMapping.class.getName());
        requestMapping.addField("value", request.getFrontContext().getInfoRequest());
        requestMapping.addField("produces", "application/json;charset=UTF-8");
        controllerGetInfoMethod.addAnnotation(requestMapping);
        controllerGetInfoMethod.addContentClass(request.getClassName().getPoClassName());
        controllerGetInfoMethod.addContentClass(request.getClassName().getVoClassName());
        controllerGetInfoMethod.setReturnString(request.getClassName().getVoClassName());
        controllerGetInfoMethod.addParam(controllerParamInfoList);
        ClassField serviceClassField = null;
        for (ClassField classField : request.getControllerClass().getFields()) {
            if (classField.getClassName().equals(request.getServiceClass().getClassName())) {
                serviceClassField = classField;
                break;
            }
        }
        StringBuilder controllerContentBuilder = new StringBuilder();
        controllerContentBuilder.append("        return this.").append(serviceClassField.getName()).append(".getSingle(");
        if (serviceParamBuilder.length() > 0) {
            controllerContentBuilder.append(serviceParamBuilder.toString());
        }
        controllerContentBuilder.append(");\n");
        controllerGetInfoMethod.setContent(controllerContentBuilder.toString());
        request.getControllerClass().addMethod(controllerGetInfoMethod);



        List<ComponentExecutor.El> elList = new ArrayList<>();
        ComponentExecutor.El el = new ComponentExecutor.El();
        el.setId("selectSingle");
        el.setResultType(request.getClassName().getPoClassName());
        el.setType("select");
        el.setSql(sqlStringBuilder.toString());
        elList.add(el);

        return elList;
    }
}
