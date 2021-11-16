package cn.thecover.potato.generate.executor.handler;

import cn.thecover.potato.generate.context.ClassName;
import cn.thecover.potato.generate.context.FrontContext;
import cn.thecover.potato.generate.context.GenerateContext;
import cn.thecover.potato.generate.context.JavaClassContext;
import cn.thecover.potato.generate.executor.ComponentExecutor;
import cn.thecover.potato.meta.conf.db.FollowTable;
import cn.thecover.potato.meta.conf.db.Table;
import cn.thecover.potato.meta.conf.form.operate.OperateForm;
import cn.thecover.potato.meta.conf.form.search.SearchForm;
import cn.thecover.potato.meta.conf.table.UITable;
import lombok.Data;

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
}
