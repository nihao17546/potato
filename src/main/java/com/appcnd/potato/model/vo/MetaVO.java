package com.appcnd.potato.model.vo;

import com.appcnd.potato.meta.conf.db.DbConf;
import com.appcnd.potato.meta.conf.form.operate.OperateForm;
import com.appcnd.potato.meta.conf.form.search.SearchForm;
import com.appcnd.potato.meta.conf.form.storage.Storage;
import com.appcnd.potato.meta.conf.table.UIMainTable;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author nihao 2021/06/28
 */
@Data
public class MetaVO implements Serializable {
    private Integer id;
    private String name;
    private String title;
    private Integer version;
    @JSONField(name = "create_time")
    private Date createTime;
    @JSONField(name = "update_time")
    private Date updateTime;
    private DbConf dbConf;
    private UIMainTable table;
    private SearchForm search;
    private OperateForm operate;
    private Storage storage;

    private String url;
}
