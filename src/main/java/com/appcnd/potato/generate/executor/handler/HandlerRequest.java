package com.appcnd.potato.generate.executor.handler;

import com.appcnd.potato.generate.context.ClassName;
import com.appcnd.potato.generate.context.FrontContext;
import com.appcnd.potato.generate.context.GenerateContext;
import com.appcnd.potato.generate.context.JavaClassContext;
import com.appcnd.potato.generate.executor.ComponentExecutor;
import com.appcnd.potato.meta.conf.db.Column;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.db.Table;
import com.appcnd.potato.meta.conf.form.operate.OperateForm;
import com.appcnd.potato.meta.conf.form.search.SearchForm;
import com.appcnd.potato.meta.conf.table.UITable;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author nihao 2021/11/03
 */
@Data
public class HandlerRequest {
    private JavaClassContext daoClass;
    private JavaClassContext serviceClass;
    private JavaClassContext serviceImplClass;
    private JavaClassContext controllerClass;
    private GenerateContext context;
    private Map<String,String> columnMap;
    private JavaClassContext po;
    private JavaClassContext vo;
    private JavaClassContext dto;

    private ClassName className;

    private FrontContext frontContext;

    private Table table;
    private List<FollowTable> associationTables;
    private List<FollowTable> followTables;

    private UITable uiTable;
    private SearchForm searchForm;

    private Integer version;

    private List<ComponentExecutor.El> els;

    private OperateForm operateForm;

    private List<HandlerRequest> followHandlerRequests;

    /**
     * 从表关联的父表的字段对应的java类
     */
    private Class parentKeyClass;

    public void addFollow(HandlerRequest handlerRequest) {
        if (this.followHandlerRequests == null) {
            this.followHandlerRequests = new ArrayList<>();
        }
        this.followHandlerRequests.add(handlerRequest);
        String key = ((FollowTable) handlerRequest.getTable()).getParentKey();
        for (Column column : table.getColumns()) {
            if (column.getField().equals(key)) {
                handlerRequest.setParentKeyClass(column.getJavaType());
                break;
            }
        }
    }
}
