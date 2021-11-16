package cn.thecover.potato.model.vo;

import cn.thecover.potato.meta.conf.db.DbConf;
import cn.thecover.potato.meta.conf.form.operate.OperateForm;
import cn.thecover.potato.meta.conf.form.search.SearchForm;
import cn.thecover.potato.meta.conf.table.UIMainTable;
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

    private String url;
    @JSONField(name = "load_version")
    private Integer loadVersion;
}
