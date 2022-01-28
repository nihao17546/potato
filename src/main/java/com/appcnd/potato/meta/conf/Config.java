package com.appcnd.potato.meta.conf;

import com.appcnd.potato.meta.conf.api.ApiConf;
import com.appcnd.potato.meta.conf.db.DbConf;
import com.appcnd.potato.meta.conf.form.operate.OperateForm;
import com.appcnd.potato.meta.conf.form.search.SearchForm;
import com.appcnd.potato.meta.conf.form.storage.Storage;
import com.appcnd.potato.meta.conf.table.UIMainTable;
import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2021/07/03
 */
@Data
public class Config implements Serializable {
    private static final long serialVersionUID = 237375881936287662L;
    private Basic basic;
    private DbConf dbConf;
    private UIMainTable table;
    private SearchForm searchForm;
    private OperateForm operateForm;
    private Storage storage;
    private ApiConf api;
}
