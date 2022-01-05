package com.appcnd.potato.generate.executor.handler;

import com.appcnd.potato.generate.annotation.AnnotationInfo;
import com.appcnd.potato.generate.context.ClassField;
import com.appcnd.potato.generate.context.JavaClassContext;
import com.appcnd.potato.generate.executor.ComponentExecutor;
import com.appcnd.potato.generate.method.MethodInfo;
import com.appcnd.potato.generate.method.ParamInfo;
import com.appcnd.potato.meta.conf.db.Column;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.form.search.DefaultSearchColumn;
import com.appcnd.potato.meta.conf.form.search.DefaultSearchGroup;
import com.appcnd.potato.meta.conf.form.search.element.*;
import com.appcnd.potato.meta.conf.form.search.enums.JudgeType;
import com.appcnd.potato.meta.conf.table.Sort;
import com.appcnd.potato.meta.conf.table.UIColumn;
import com.appcnd.potato.util.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nihao 2021/11/03
 */
public class ListComponentHandler extends ComponentHandler {

    @Override
    public List<ComponentExecutor.El> handler(HandlerRequest request) {
        SqlStringBuilder selectTablesBuilder = new SqlStringBuilder();
        SqlStringBuilder selectColumnBuilder = new SqlStringBuilder();
        SqlStringBuilder whereBuilder = new SqlStringBuilder();
        SqlStringBuilder sortBuilder = new SqlStringBuilder();

        boolean hasAssociationTable = !CollectionUtils.isEmpty(request.getAssociationTables());
        selectTablesBuilder.put(request.getTable().getName());
        if (hasAssociationTable) {
            for (FollowTable leftJoinTable : request.getAssociationTables()) {
                selectTablesBuilder.append(" LEFT JOIN ").put(leftJoinTable.getName())
                        .append(" ON ").put(leftJoinTable.getName()).append(".").put(leftJoinTable.getForeignKey())
                        .append("=").put(request.getTable().getName()).append(".").put(leftJoinTable.getParentKey());
            }
        }


        // 查询字段
        Set<String> hasAddColumns = new HashSet<>();
        // 主表主键
        for (int i = 0, size = request.getTable().getPrimaryFields().size(); i < size; i ++) {
            String pk = request.getTable().getPrimaryFields().get(i);
            String prop = fillSelectColumn(selectColumnBuilder, request.getTable().getName(), pk, hasAddColumns, request.getColumnMap(), hasAssociationTable);
            request.getFrontContext().addProp(request.getTable().getName(), pk, prop);
        }
        // 一对多从表外键
        if (!CollectionUtils.isEmpty(request.getFollowTables())) {
            for (FollowTable followTable : request.getFollowTables()) {
                String prop = fillSelectColumn(selectColumnBuilder, request.getTable().getName(), followTable.getParentKey(), hasAddColumns, request.getColumnMap(), hasAssociationTable);
                request.getFrontContext().addProp(request.getTable().getName(), followTable.getParentKey(), prop);
            }
        }
        // 主表外键
        if (request.getTable() instanceof FollowTable) {
            FollowTable main = (FollowTable) request.getTable();
            String prop = fillSelectColumn(selectColumnBuilder, main.getName(), main.getForeignKey(), hasAddColumns, request.getColumnMap(), hasAssociationTable);
            request.getFrontContext().addProp(main.getName(), main.getForeignKey(), prop);
        }
        // 一对一从表外键
        if (!CollectionUtils.isEmpty(request.getAssociationTables())) {
            for (FollowTable leftJoinTable : request.getAssociationTables()) {
                String prop = fillSelectColumn(selectColumnBuilder, leftJoinTable.getName(), leftJoinTable.getForeignKey(), hasAddColumns, request.getColumnMap(), hasAssociationTable);
                request.getFrontContext().addProp(leftJoinTable.getName(), leftJoinTable.getForeignKey(), prop);
            }
        }

        boolean sortable = false;
        // select字段
        if (request.getUiTable() != null) {
            if (!CollectionUtils.isEmpty(request.getUiTable().getColumns())) {
                for (UIColumn uiColumn : request.getUiTable().getColumns()) {
                    String prop = fillSelectColumn(selectColumnBuilder, uiColumn.getTable(), uiColumn.getColumn().getField(), hasAddColumns, request.getColumnMap(), hasAssociationTable);
                    request.getFrontContext().addProp(uiColumn, prop);
                    if (Boolean.TRUE.equals(uiColumn.getSortable())) {
                        sortable = true;
                    }
                }
            }
        }

        String foreignKeyParam = null;
        List<ParamInfo> paramInfoList = new ArrayList<>();
        Set<String> sqlParams = new HashSet<>();
        // 查询条件
        if (request.getSearchForm() != null) {
            // 默认查询条件
            if (!CollectionUtils.isEmpty(request.getSearchForm().getDefaultSearchGroups())) {
                for (int i = 0, size = request.getSearchForm().getDefaultSearchGroups().size(); i < size; i ++) {
                    DefaultSearchGroup defaultSearchGroup = request.getSearchForm().getDefaultSearchGroups().get(i);
                    if (i > 0) {
                        whereBuilder.append("\n        ").append(defaultSearchGroup.getRelation().name()).append(" ");
                    }
                    whereBuilder.append("(");
                    for (int j = 0, jSize = defaultSearchGroup.getColumns().size(); j < jSize; j ++) {
                        DefaultSearchColumn defaultSearchColumn = defaultSearchGroup.getColumns().get(j);
                        Column column = request.getContext().getColumn(defaultSearchColumn.getColumn().getTable(), defaultSearchColumn.getColumn().getColumn());
                        boolean isString =  !(Number.class.isAssignableFrom(column.getJavaType())
                                || Boolean.class.isAssignableFrom(column.getJavaType()));
                        if (j > 0) {
                            whereBuilder.append(" ").append(defaultSearchColumn.getRelation().name()).append(" ");
                        }
                        if (hasAssociationTable) {
                            whereBuilder.put(defaultSearchColumn.getColumn().getTable()).append(".");
                        }
                        whereBuilder.put(defaultSearchColumn.getColumn().getColumn())
                                .append(" ").append(defaultSearchColumn.getJudge().getSql());
                        if (defaultSearchColumn.getJudge() == JudgeType.in) {
                            whereBuilder.append(" (");
                            for (int k = 0, kSize = defaultSearchColumn.getInValue().size(); k < kSize; k ++) {
                                if (k > 0) {
                                    whereBuilder.append(",");
                                }
                                if (isString) {
                                    whereBuilder.append("'").append(defaultSearchColumn.getInValue().get(k)).append("'");
                                } else {
                                    whereBuilder.append(defaultSearchColumn.getInValue().get(k));
                                }
                            }
                            whereBuilder.append(")");
                        } else if (defaultSearchColumn.getJudge() == JudgeType.bt) {
                            if (isString) {
                                whereBuilder.append(" '").append(defaultSearchColumn.getStartValue()).append("'")
                                        .append(" AND ").append("'").append(defaultSearchColumn.getEndValue()).append("'");
                            } else {
                                whereBuilder.append(" ").append(defaultSearchColumn.getStartValue())
                                        .append(" AND ").append(defaultSearchColumn.getEndValue());
                            }
                        } else if (defaultSearchColumn.getJudge() == JudgeType.like) {
                            whereBuilder.append(" '%").append(defaultSearchColumn.getValue()).append("%'");
                        } else if (defaultSearchColumn.getJudge() == JudgeType.isnotnull
                                || defaultSearchColumn.getJudge() == JudgeType.isnull) {
                        } else {
                            if (isString) {
                                whereBuilder.append(" '").append(defaultSearchColumn.getValue()).append("'");
                            } else {
                                whereBuilder.append(" ").append(defaultSearchColumn.getValue());
                            }
                        }
                    }
                    whereBuilder.append(")");
                }
            }

            if (!CollectionUtils.isEmpty(request.getSearchForm().getElements())) {
                if (whereBuilder.isEmpty()) {
                    whereBuilder.append("\n        1=1 ");
                }
                for (int i = 0, size = request.getSearchForm().getElements().size(); i < size; i ++) {
                    whereBuilder.append("\n        <if test=\"");
                    SearchElement searchElement = request.getSearchForm().getElements().get(i);
                    String field = request.getColumnMap().get(FieldUtil.getField(searchElement.getColumn().getTable(), searchElement.getColumn().getColumn()));
                    String param = CamelUtil.underlineToCamel(field);
                    param = getSqlParam(sqlParams, param);
                    Column column = request.getContext().getColumn(searchElement.getColumn().getTable(), searchElement.getColumn().getColumn());
                    Class javaType = column.getJavaType();
                    String javaTypeName = javaType.getName();
                    if (javaType.equals(Date.class) || javaType.equals(Timestamp.class)) {
                        javaTypeName = String.class.getName();
                    }
                    if (searchElement instanceof SelectSearchElement) {
                        SelectSearchElement selectSearchElement = (SelectSearchElement) searchElement;
                        if (Boolean.TRUE.equals(selectSearchElement.getMultiple())) {
                            whereBuilder.append(param).append(" != null and ")
                                    .append(param).append(".length > 0\">");
                            whereBuilder.append("\n            AND ");
                            if (hasAssociationTable) {
                                whereBuilder.put(searchElement.getColumn().getTable()).append(".");
                            }
                            whereBuilder.put(searchElement.getColumn().getColumn());
                            whereBuilder.append(" in \n");
                            whereBuilder.append("            <foreach collection=\"")
                                    .append(param).append("\" separator=\",\" item=\"item\" open=\"(\" close=\")\">\n");
                            whereBuilder.append("                #{item}\n");
                            whereBuilder.append("            </foreach>");

                            ParamInfo paramInfo = new ParamInfo(javaTypeName + "[]", param);
                            AnnotationInfo annotationInfo = new AnnotationInfo(Param.class.getName());
                            annotationInfo.addField("value", param);
                            paramInfo.addAnnotation(annotationInfo);
                            paramInfoList.add(paramInfo);
                            request.getFrontContext().getSearchElements().get(i).setField(param);
                        } else {
                            whereBuilder.append(param).append(" != null\">");
                            whereBuilder.append("\n            AND ");
                            if (hasAssociationTable) {
                                whereBuilder.put(searchElement.getColumn().getTable()).append(".");
                            }
                            whereBuilder.put(searchElement.getColumn().getColumn());
                            whereBuilder.append(" = #{").append(param).append("}");

                            ParamInfo paramInfo = new ParamInfo(javaTypeName, param);
                            AnnotationInfo annotationInfo = new AnnotationInfo(Param.class.getName());
                            annotationInfo.addField("value", param);
                            paramInfo.addAnnotation(annotationInfo);
                            paramInfoList.add(paramInfo);
                            request.getFrontContext().getSearchElements().get(i).setField(param);
                        }
                    } else if (searchElement instanceof EqInputSearchElement
                            || searchElement instanceof DateTimeSearchElement
                            || searchElement instanceof RemoteSelectSearchElement) {
                        whereBuilder.append(param).append(" != null\">");
                        whereBuilder.append("\n            AND ");
                        if (hasAssociationTable) {
                            whereBuilder.put(searchElement.getColumn().getTable()).append(".");
                        }
                        whereBuilder.put(searchElement.getColumn().getColumn());
                        whereBuilder.append(" = #{").append(param).append("}");

                        ParamInfo paramInfo = new ParamInfo(javaTypeName, param);
                        AnnotationInfo annotationInfo = new AnnotationInfo(Param.class.getName());
                        annotationInfo.addField("value", param);
                        paramInfo.addAnnotation(annotationInfo);
                        paramInfoList.add(paramInfo);
                        request.getFrontContext().getSearchElements().get(i).setField(param);
                    } else if (searchElement instanceof LikeInputSearchElement) {
                        whereBuilder.append(param).append(" != null\">");
                        whereBuilder.append("\n            AND ");
                        if (hasAssociationTable) {
                            whereBuilder.put(searchElement.getColumn().getTable()).append(".");
                        }
                        whereBuilder.put(searchElement.getColumn().getColumn());
                        whereBuilder.append(" LIKE concat('%',#{").append(param).append("},'%')");

                        ParamInfo paramInfo = new ParamInfo(javaTypeName, param);
                        AnnotationInfo annotationInfo = new AnnotationInfo(Param.class.getName());
                        annotationInfo.addField("value", param);
                        paramInfo.addAnnotation(annotationInfo);
                        paramInfoList.add(paramInfo);
                        request.getFrontContext().getSearchElements().get(i).setField(param);
                    } else if (searchElement instanceof DateTimeRangeSearchElement) {
                        String paramStart = getSqlParam(sqlParams, param + "Start");
                        String paramEnd = getSqlParam(sqlParams, param + "End");
                        whereBuilder.append(paramStart).append(" != null and ").append(paramEnd).append(" != null\">");
                        whereBuilder.append("\n            AND ");
                        if (hasAssociationTable) {
                            whereBuilder.put(searchElement.getColumn().getTable()).append(".");
                        }
                        whereBuilder.put(searchElement.getColumn().getColumn());
                        whereBuilder.append(" BETWEEN #{").append(param).append("Start} AND #{").append(param).append("End}");
                        whereBuilder.append("\n        </if>");
                        whereBuilder.append("\n        <if test=\"");
                        whereBuilder.append(paramStart).append(" != null and ").append(paramEnd).append(" == null\">");
                        whereBuilder.append("\n            AND ");
                        if (hasAssociationTable) {
                            whereBuilder.put(searchElement.getColumn().getTable()).append(".");
                        }
                        whereBuilder.put(searchElement.getColumn().getColumn());
                        whereBuilder.append(" &gt;= #{").append(paramStart).append("}");
                        whereBuilder.append("\n        </if>");
                        whereBuilder.append("\n        <if test=\"");
                        whereBuilder.append(paramStart).append(" == null and ").append(paramEnd).append(" != null\">");
                        whereBuilder.append("\n            AND ");
                        if (hasAssociationTable) {
                            whereBuilder.put(searchElement.getColumn().getTable()).append(".");
                        }
                        whereBuilder.append(searchElement.getColumn().getColumn());
                        whereBuilder.append(" &lt;= #{").append(paramEnd).append("}");

                        ParamInfo paramInfoStart = new ParamInfo(javaTypeName, paramStart);
                        AnnotationInfo annotationInfoStart = new AnnotationInfo(Param.class.getName());
                        annotationInfoStart.addField("value", paramStart);
                        paramInfoStart.addAnnotation(annotationInfoStart);
                        paramInfoList.add(paramInfoStart);

                        ParamInfo paramInfoEnd = new ParamInfo(javaTypeName, paramEnd);
                        AnnotationInfo annotationInfoEnd = new AnnotationInfo(Param.class.getName());
                        annotationInfoEnd.addField("value", paramEnd);
                        paramInfoEnd.addAnnotation(annotationInfoEnd);
                        paramInfoList.add(paramInfoEnd);


                        request.getFrontContext().getSearchElements().get(i).setField(param);
                        request.getFrontContext().getSearchElements().get(i).setStartField(paramStart);
                        request.getFrontContext().getSearchElements().get(i).setEndField(paramEnd);
                    } else {
                        throw new IllegalArgumentException();
                    }
                    whereBuilder.append("\n        </if>");
                }
            }
        }

        // 从表外键查询条件
        if (request.getTable() instanceof FollowTable) {
            FollowTable main = (FollowTable) request.getTable();
            if (!whereBuilder.isEmpty()) {
                whereBuilder.append("\n        AND ");
            }
            if (hasAssociationTable) {
                whereBuilder.put(main.getName()).append(".");
            }
            foreignKeyParam = CamelUtil.underlineToCamel(main.getForeignKey());
            foreignKeyParam = getSqlParam(sqlParams, foreignKeyParam);
            whereBuilder.put(main.getForeignKey()).append(" = #{").append(foreignKeyParam).append("}");

            Column column = request.getContext().getColumn(main.getName(), main.getForeignKey());
            Class javaType = column.getJavaType();
            String javaTypeName = javaType.getName();
            ParamInfo paramInfo = new ParamInfo(javaTypeName, foreignKeyParam);
            AnnotationInfo annotationInfo = new AnnotationInfo(Param.class.getName());
            annotationInfo.addField("value", foreignKeyParam);
            paramInfo.addAnnotation(annotationInfo);
            paramInfoList.add(paramInfo);
        }

        String sortName = null;
        String orderName = null;
        List<ParamInfo> orderParamInfoList = new ArrayList<>();
        if (sortable) {
            sortName = getSqlParam(sqlParams, "sort");
            ParamInfo sort = new ParamInfo(String.class.getName(), sortName);
            AnnotationInfo annotationInfoSort = new AnnotationInfo(Param.class.getName());
            annotationInfoSort.addField("value", sortName);
            sort.addAnnotation(annotationInfoSort);
            orderParamInfoList.add(sort);

            orderName = getSqlParam(sqlParams, "order");
            ParamInfo order = new ParamInfo(String.class.getName(), orderName);
            AnnotationInfo annotationInfoOrder = new AnnotationInfo(Param.class.getName());
            annotationInfoOrder.addField("value", orderName);
            order.addAnnotation(annotationInfoOrder);
            orderParamInfoList.add(order);

            sortBuilder.append("\n        <choose>");
            sortBuilder.append("\n            <when test=\"");
            sortBuilder.append(sortName).append(" != null and ")
                    .append(orderName).append(" != null\">");
            sortBuilder.append("\n                ORDER BY ${").append(sortName).append("} ")
                    .append("${").append(orderName).append("}");
            sortBuilder.append("\n            </when>");

            // 默认排序
            if (request.getUiTable().getSorts() != null && !request.getUiTable().getSorts().isEmpty()) {
                sortBuilder.append("\n            <otherwise>");
                SqlStringBuilder sb = new SqlStringBuilder();
                for (Sort uiSort : request.getUiTable().getSorts()) {
                    if (!sb.isEmpty()) {
                        sb.append(",");
                    }
                    if (hasAssociationTable) {
                        sb.put(uiSort.getTable()).append(".");
                    }
                    sb.put(uiSort.getColumn()).append(" ").append(uiSort.getSort().name());
                }
                sortBuilder.append("\n                ORDER BY ").append(sb.toString());
                sortBuilder.append("\n            </otherwise>");
            }

            sortBuilder.append("\n        </choose>");
        } else {
            // 排序
            if (request.getUiTable().getSorts() != null && !request.getUiTable().getSorts().isEmpty()) {
                sortBuilder.append("\n        ORDER BY ");
                int index = 0;
                for (Sort sort : request.getUiTable().getSorts()) {
//                    if (!sortBuilder.isEmpty()) {
//                        sortBuilder.append(",");
//                    }
                    if (index ++ > 0) {
                        sortBuilder.append(",");
                    }
                    if (hasAssociationTable) {
                        sortBuilder.put(sort.getTable()).append(".");
                    }
                    sortBuilder.put(sort.getColumn()).append(" ").append(sort.getSort().name());
                }
            }
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(" FROM ").append(selectTablesBuilder.toString());
        if (!whereBuilder.isEmpty()) {
            sqlBuilder.append("\n        WHERE ").append(whereBuilder.toString());
        }

        StringBuilder listSqlBuilder = new StringBuilder();
        listSqlBuilder.append("SELECT ").append(selectColumnBuilder.toString())
                .append(sqlBuilder.toString()).toString();
        if (!sortBuilder.isEmpty()) {
            listSqlBuilder.append(sortBuilder.toString());
        }

        String pageSize = null;
        String curPage = null;
        List<ParamInfo> serviceSortParamInfoList = new ArrayList<>();
        List<ComponentExecutor.El> elList = new ArrayList<>();
        List<ParamInfo> sortParamInfoList = new ArrayList<>();

        if (Boolean.TRUE.equals(request.getUiTable().getPagination())) {
            // 分页
            String from  = getSqlParam(sqlParams, "from");
            String to  = getSqlParam(sqlParams, "to");

            pageSize = getSqlParam(sqlParams, "pageSize");
            curPage = getSqlParam(sqlParams, "curPage");

            listSqlBuilder.append("\n        LIMIT #{");
            listSqlBuilder.append(from);
            listSqlBuilder.append("},#{");
            listSqlBuilder.append(to);
            listSqlBuilder.append("}");
            // 查询条目数
            StringBuilder countSqlBuilder = new StringBuilder();
            countSqlBuilder.append("SELECT ").append("count(1)").append(sqlBuilder.toString());
            ComponentExecutor.El el = new ComponentExecutor.El();
            el.setId("selectCount");
            el.setResultType("java.lang.Long");
            el.setType("select");
            el.setSql(countSqlBuilder.toString());
            elList.add(el);

            ParamInfo fromParamInfo = new ParamInfo(Integer.class.getName(), from);
            AnnotationInfo fromAnnotation = new AnnotationInfo(Param.class.getName());
            fromAnnotation.addField("value", from);
            fromParamInfo.addAnnotation(fromAnnotation);
            sortParamInfoList.add(fromParamInfo);

            ParamInfo toParamInfo = new ParamInfo(Integer.class.getName(), to);
            AnnotationInfo toAnnotation = new AnnotationInfo(Param.class.getName());
            toAnnotation.addField("value", to);
            toParamInfo.addAnnotation(toAnnotation);
            sortParamInfoList.add(toParamInfo);

            serviceSortParamInfoList.add(new ParamInfo(Integer.class.getName(), curPage));
            serviceSortParamInfoList.add(new ParamInfo(Integer.class.getName(), pageSize));


            // dao类添加getCount方法
            MethodInfo getCount = new MethodInfo();
            getCount.setHasContent(false);
            getCount.addContentClass(Long.class.getName());
            getCount.setReturnString(Long.class.getName());
            getCount.setMethodName("selectCount");
            getCount.addParam(paramInfoList);
            request.getDaoClass().addMethod(getCount);
        }

        if (sortName != null && orderName != null) {
            serviceSortParamInfoList.add(new ParamInfo(String.class.getName(), sortName));
            serviceSortParamInfoList.add(new ParamInfo(String.class.getName(), orderName));
        }

        String resultType = request.getClassName().getPoClassName();
        if (hasAssociationTable) {
            resultType = request.getClassName().getDtoClassName();
        }


        ComponentExecutor.El el = new ComponentExecutor.El();
        el.setId("selectList");
        el.setResultType(resultType);
        el.setType("select");
        el.setSql(listSqlBuilder.toString());
        elList.add(el);

        // dao类添加getList方法
        MethodInfo getList = new MethodInfo();
        getList.setHasContent(false);
        getList.addContentClass(List.class.getName());
        getList.addContentClass(resultType);
        getList.setReturnString(List.class.getName() + "<" + resultType + ">");
        getList.setMethodName("selectList");
        getList.addParam(paramInfoList);
        getList.addParam(sortParamInfoList);
        getList.addParam(orderParamInfoList);
        request.getDaoClass().addMethod(getList);

        MethodInfo serviceGetList = new MethodInfo();
        serviceGetList.setHasContent(false);

        ClassField controllerField = new ClassField("private", request.getServiceClass().getClassName(), new AnnotationInfo(Autowired.class.getName()));
        request.getControllerClass().addField(controllerField);
        MethodInfo controllerGetListMethod = new MethodInfo();
        controllerGetListMethod.setDecorate("public");
        controllerGetListMethod.setHasContent(true);
        controllerGetListMethod.setMethodName("list");
        controllerGetListMethod.addAnnotation(new AnnotationInfo(ResponseBody.class.getName()));
        AnnotationInfo requestMapping = new AnnotationInfo(RequestMapping.class.getName());

        requestMapping.addField("value", request.getFrontContext().getListRequest());
        requestMapping.addField("produces", "application/json;charset=UTF-8");
        controllerGetListMethod.addAnnotation(requestMapping);

        JavaClassContext listVOClass = null;
        if (Boolean.TRUE.equals(request.getUiTable().getPagination())) {
            listVOClass = GenerateUtil.getListVOJavaClass(CommonUtil.getPackageName(request.getClassName().getVoClassName()), request.getVersion());
            request.getContext().addJavaClassContext(listVOClass);
            serviceGetList.addContentClass(listVOClass.getClassName());
            serviceGetList.setReturnString(listVOClass.getClassName());
        } else {
            serviceGetList.addContentClass(List.class.getName());
            serviceGetList.addContentClass(request.getClassName().getVoClassName());
            serviceGetList.setReturnString(List.class.getName() + "<" + request.getClassName().getVoClassName() + ">");
        }

        serviceGetList.setMethodName("getList");
        List<ParamInfo> serviceGetListParams = new ArrayList<>();
        if (!CollectionUtils.isEmpty(paramInfoList)) {
            for (ParamInfo paramInfo : paramInfoList) {
                ParamInfo param = paramInfo.deepClone();
                param.setAnnotations(null);
                serviceGetListParams.add(param);

                ParamInfo controllerParam = paramInfo.deepClone();
                if (controllerParam.getAnnotations() != null) {
                    for (AnnotationInfo annotationInfo : controllerParam.getAnnotations()) {
                        annotationInfo.setName(RequestParam.class.getName());
                        annotationInfo.setFields(null);
                        if (!controllerParam.getName().equals(foreignKeyParam)) {
                            annotationInfo.addField("required", false);
                        }
                        annotationInfo.addField("name", controllerParam.getName());
                    }
                }
                controllerGetListMethod.addParam(controllerParam);
            }
        }
        serviceGetListParams.addAll(serviceSortParamInfoList);
        serviceGetList.addParam(serviceGetListParams);
        request.getServiceClass().addMethod(serviceGetList);

        MethodInfo serviceGetListImpl = serviceGetList.deepClone();
        serviceGetListImpl.setDecorate("public");
        serviceGetListImpl.setHasContent(true);
        serviceGetListImpl.addAnnotation(new AnnotationInfo(Override.class.getName()));
        request.getServiceImplClass().addMethod(serviceGetListImpl);
        AnnotationInfo annotationInfo = new AnnotationInfo(Autowired.class.getName());
        ClassField classField = new ClassField("private", request.getClassName().getDaoClassName(), annotationInfo);
        request.getServiceImplClass().addField(classField);
        StringBuilder serviceGetListImplContentBuilder = new StringBuilder();
        StringBuilder controllerListContentBuilder = new StringBuilder();
        controllerListContentBuilder.append("        return this.")
                .append(controllerField.getName()).append(".").append("getList(");
        if (Boolean.TRUE.equals(request.getUiTable().getPagination())) {
            String countName = getSqlParam(sqlParams, "count");
            serviceGetListImplContentBuilder.append("        Long ");
            serviceGetListImplContentBuilder.append(countName);
            serviceGetListImplContentBuilder.append(" = this.").append(classField.getName())
                    .append(".selectCount(");
            SqlStringBuilder paramStrBuilder = new SqlStringBuilder();
            if (!CollectionUtils.isEmpty(paramInfoList)) {
                int i = 0;
                for (ParamInfo paramInfo : paramInfoList) {
                    if (i ++ > 0) {
                        paramStrBuilder.append(", ");
                    }
                    paramStrBuilder.append(paramInfo.getName());
                }
            }

            controllerGetListMethod.addContentClass(listVOClass.getClassName());
            controllerGetListMethod.setReturnString(listVOClass.getClassName());

            serviceGetListImplContentBuilder.append(paramStrBuilder.toString());
            serviceGetListImplContentBuilder.append(");\n");
            serviceGetListImplContentBuilder.append("        if (").append(countName)
                    .append(" == 0) {\n");
            serviceGetListImpl.addContentClass(listVOClass.getClassName());
            serviceGetListImplContentBuilder.append("            return new ")
                    .append(listVOClass.getClassName()).append("();\n");
            serviceGetListImplContentBuilder.append("        } else {\n");
            serviceGetListImpl.addContentClass(List.class.getName());
            serviceGetListImpl.addContentClass(request.getClassName().getVoClassName());
            serviceGetListImpl.addContentClass(resultType);
            serviceGetListImplContentBuilder.append("            ")
                    .append(List.class.getName() + "<" + resultType + "> ");
            String listName = getSqlParam(sqlParams, "list");
            serviceGetListImplContentBuilder.append(listName).append(" = ")
                    .append("this.").append(classField.getName()).append(".selectList(");
            if (!paramStrBuilder.isEmpty()) {
                paramStrBuilder.append(", ");
            }

            controllerListContentBuilder.append(paramStrBuilder.toString()).append(curPage)
                    .append(", ").append(pageSize);
            if (sortName != null && orderName != null) {
                controllerListContentBuilder.append(", ").append(sortName).append(", ").append(orderName);
            }

            paramStrBuilder.append("(").append(curPage).append(" - 1) * ").append(pageSize)
                    .append(", ").append(pageSize);
            if (sortName != null && orderName != null) {
                paramStrBuilder.append(", ").append(sortName).append(", ").append(orderName);
            }

            serviceGetListImplContentBuilder.append(paramStrBuilder.toString());
            serviceGetListImplContentBuilder.append(");\n");

            String voListName = getSqlParam(sqlParams, "voList");
            serviceGetListImplContentBuilder.append("            ")
                    .append(List.class.getName() + "<" + request.getClassName().getVoClassName() + "> ")
                    .append(voListName).append(" = new ").append(ArrayList.class.getName()).append("();\n");
            serviceGetListImpl.addContentClass(ArrayList.class.getName());
            serviceGetListImplContentBuilder.append("            for (").append(resultType).append(" po : ").append(listName).append(") {\n");
            serviceGetListImplContentBuilder.append("                ").append(voListName).append(".add(new ").append(request.getClassName().getVoClassName()).append("(po)").append(");\n");
            serviceGetListImplContentBuilder.append("            }\n");
            String lisVOName = getSqlParam(sqlParams, "listVO");
            serviceGetListImplContentBuilder.append("            ")
                    .append(listVOClass.getClassName()).append(" ").append(lisVOName)
                    .append(" = new ").append(listVOClass.getClassName()).append("(");
            serviceGetListImplContentBuilder.append(voListName).append(", ").append(countName);
            serviceGetListImplContentBuilder.append(");\n");
            serviceGetListImplContentBuilder.append("            return ").append(lisVOName).append(";\n");
            serviceGetListImplContentBuilder.append("        }\n");

            AnnotationInfo curPageAnno = new AnnotationInfo(RequestParam.class.getName());
            curPageAnno.addField("required", false);
            curPageAnno.addField("defaultValue", "1");
            curPageAnno.addField("name", curPage);
            ParamInfo controllerCurParam = new ParamInfo(Integer.class.getName(), curPage);
            controllerCurParam.addAnnotation(curPageAnno);

            AnnotationInfo pageSizePageAnno = new AnnotationInfo(RequestParam.class.getName());
            pageSizePageAnno.addField("required", false);
            pageSizePageAnno.addField("defaultValue", "10");
            pageSizePageAnno.addField("name", pageSize);
            ParamInfo controllerPageSizeParam = new ParamInfo(Integer.class.getName(), pageSize);
            controllerPageSizeParam.addAnnotation(pageSizePageAnno);

            controllerGetListMethod.addParam(controllerCurParam);
            controllerGetListMethod.addParam(controllerPageSizeParam);
        } else {
            controllerGetListMethod.addContentClass(List.class.getName());
            controllerGetListMethod.addContentClass(request.getClassName().getVoClassName());
            controllerGetListMethod.setReturnString(List.class.getName() + "<" + request.getClassName().getVoClassName() + ">");

            serviceGetListImpl.addContentClass(List.class.getName());
            serviceGetListImpl.addContentClass(request.getClassName().getVoClassName());
            serviceGetListImpl.addContentClass(resultType);
            serviceGetListImplContentBuilder.append("        ")
                    .append(List.class.getName() + "<" + resultType + "> ");
            String listName = getSqlParam(sqlParams, "list");
            SqlStringBuilder paramStrBuilder = new SqlStringBuilder();
            if (!CollectionUtils.isEmpty(paramInfoList)) {
                int i = 0;
                for (ParamInfo paramInfo : paramInfoList) {
                    if (i ++ > 0) {
                        paramStrBuilder.append(", ");
                    }
                    paramStrBuilder.append(paramInfo.getName());
                }
            }

            if (sortName != null && orderName != null) {
                if (!paramStrBuilder.isEmpty()) {
                    paramStrBuilder.append(", ");
                }
                paramStrBuilder.append(sortName).append(", ").append(orderName);
            }

            controllerListContentBuilder.append(paramStrBuilder.toString());

            serviceGetListImplContentBuilder.append(listName).append(" = ")
                    .append("this.").append(classField.getName()).append(".selectList(");
            serviceGetListImplContentBuilder.append(paramStrBuilder.toString());
            serviceGetListImplContentBuilder.append(");\n");
            String voListName = getSqlParam(sqlParams, "voList");
            serviceGetListImplContentBuilder.append("        ")
                    .append(List.class.getName() + "<" + request.getClassName().getVoClassName() + "> ")
                    .append(voListName).append(" = new ").append(ArrayList.class.getName()).append("();\n");
            serviceGetListImpl.addContentClass(ArrayList.class.getName());
            serviceGetListImplContentBuilder.append("            for (").append(resultType).append(" po : ").append(listName).append(") {\n");
            serviceGetListImplContentBuilder.append("                ").append(voListName).append(".add(new ").append(request.getClassName().getVoClassName()).append("(po)").append(");\n");
            serviceGetListImplContentBuilder.append("            }\n");
            serviceGetListImplContentBuilder.append("        return ").append(voListName).append(";\n");
        }

        if (sortName != null && orderName != null) {
            AnnotationInfo sortAnno = new AnnotationInfo(RequestParam.class.getName());
            sortAnno.addField("required", false);
            sortAnno.addField("name", sortName);
            ParamInfo controllerSortParam = new ParamInfo(String.class.getName(), sortName);
            controllerSortParam.addAnnotation(sortAnno);

            AnnotationInfo orderAnno = new AnnotationInfo(RequestParam.class.getName());
            orderAnno.addField("required", false);
            orderAnno.addField("name", orderName);
            ParamInfo controllerOrderParam = new ParamInfo(String.class.getName(), orderName);
            controllerOrderParam.addAnnotation(orderAnno);

            controllerGetListMethod.addParam(controllerSortParam);
            controllerGetListMethod.addParam(controllerOrderParam);
        }

        serviceGetListImpl.setContent(serviceGetListImplContentBuilder.toString());



        controllerListContentBuilder.append(");\n");
        request.getControllerClass().addAnnotation(new AnnotationInfo(Controller.class.getName()));
        controllerGetListMethod.setContent(controllerListContentBuilder.toString());
        request.getControllerClass().addMethod(controllerGetListMethod);

//        elList.addAll(getOthers(request.getSearchForm(), request.getDaoClass(), request.getControllerClass(), request.getClassName()));

        return elList;
    }

//    private List<ComponentExecutor.El> getOthers(SearchForm searchForm, JavaClassContext daoClass, JavaClassContext controllerClass, ClassName className) {
//        List<ComponentExecutor.El> elList = new ArrayList<>();
//        // 校验是否有远程下拉框
//        List<RemoteSelectSearchElement> remoteSelectSearchElementList = new ArrayList<>();
//        if (searchForm != null && searchForm.getElements() != null) {
//            for (SearchElement searchElement : searchForm.getElements()) {
//                if (searchElement instanceof RemoteSelectSearchElement) {
//                    RemoteSelectSearchElement remote = (RemoteSelectSearchElement) searchElement;
//                    if (!remoteSelectSearchElementList.contains(remote)) {
//                        remoteSelectSearchElementList.add(remote);
//                    }
//                }
//            }
//        }
//        if (!remoteSelectSearchElementList.isEmpty()) {
//            AnnotationInfo annotationInfo = new AnnotationInfo(Autowired.class.getName());
//            ClassField classField = new ClassField("private", className.getDaoClassName(), annotationInfo);
//            if (!controllerClass.hasField(classField)) {
//                controllerClass.addField(classField);
//            }
//            for (RemoteSelectSearchElement element : remoteSelectSearchElementList) {
//                String methodName = "getRemote" + element.hashCode();
//                methodName = methodName.replaceAll("-", "_");
//
//                ComponentExecutor.El el = new ComponentExecutor.El();
//                el.setId(methodName);
//                el.setType("select");
//                el.setResultType(Map.class.getName());
//                el.setSql(element.getSql());
//                elList.add(el);
//
//                MethodInfo daoMethod = new MethodInfo();
//                daoMethod.setHasContent(false);
//                daoMethod.setMethodName(methodName);
//                daoMethod.addContentClass(Map.class.getName());
//                daoMethod.addContentClass(List.class.getName());
//                daoMethod.setReturnString(List.class.getName() + "<" + Map.class.getName() + ">");
//                daoClass.addMethod(daoMethod);
//
//                MethodInfo controllerMethod = new MethodInfo();
//                controllerMethod.setDecorate("public");
//                controllerMethod.setHasContent(true);
//                controllerMethod.setMethodName(methodName);
//                controllerMethod.addAnnotation(new AnnotationInfo(ResponseBody.class.getName()));
//                AnnotationInfo requestMapping = new AnnotationInfo(RequestMapping.class.getName());
//                requestMapping.addField("value", BootConstant.requestPrefix + methodName);
//                requestMapping.addField("produces", "application/json;charset=UTF-8");
//                controllerMethod.addAnnotation(requestMapping);
//                controllerMethod.addContentClass(Map.class.getName());
//                controllerMethod.addContentClass(List.class.getName());
//                controllerMethod.setReturnString(List.class.getName() + "<" + Map.class.getName() + ">");
//
//                StringBuilder sb = new StringBuilder();
//                sb.append("        return this.").append(classField.getName()).append(".").append(methodName).append("();\n");
//                controllerMethod.setContent(sb.toString());
//
//                controllerClass.addMethod(controllerMethod);
//            }
//        }
//
//        return elList;
//    }

    private String getSqlParam(Set<String> params, String param) {
        if (params.contains(param)) {
            param = param + getRandom();
            return getSqlParam(params, param);
        }
        params.add(param);
        return param;
    }

    private String getRandom() {
        // 65-90
        int a = new Random().nextInt(25) + 65;
        return String.valueOf((char) a);
    }

    private String fillSelectColumn(SqlStringBuilder selectColumnsBuilder, String tableName, String column,
                                    Set<String> hasAddColumns, Map<String,String> columnMap, boolean hasAssociationTable) {
        String field = columnMap.get(FieldUtil.getField(tableName, column));
        String as = CamelUtil.underlineToCamel(field);
        if (!hasAddColumns.contains(as)) {
            hasAddColumns.add(as);
            if (!selectColumnsBuilder.isEmpty()) {
                selectColumnsBuilder.append(",");
            }
            if (hasAssociationTable) {
                selectColumnsBuilder.put(tableName).append(".");
            }
            selectColumnsBuilder.put(column);
            if (!column.equals(as)) {
                selectColumnsBuilder.append(" AS ").append(as);
            }
        }
        return as;
    }

}
