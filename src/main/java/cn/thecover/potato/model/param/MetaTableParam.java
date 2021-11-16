package cn.thecover.potato.model.param;

import cn.thecover.potato.meta.conf.table.UIMainTable;
import lombok.Data;

/**
 * @author nihao 2021/07/02
 */
@Data
public class MetaTableParam extends Param {
    private UIMainTable config;
}
