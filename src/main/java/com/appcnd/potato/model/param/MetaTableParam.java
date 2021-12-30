package com.appcnd.potato.model.param;

import com.appcnd.potato.meta.conf.table.UIMainTable;
import lombok.Data;

/**
 * @author nihao 2021/07/02
 */
@Data
public class MetaTableParam extends Param {
    private UIMainTable config;
}
