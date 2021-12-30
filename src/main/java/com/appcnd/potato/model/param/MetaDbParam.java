package com.appcnd.potato.model.param;

import com.appcnd.potato.meta.conf.db.DbConf;
import lombok.Data;

/**
 * @author nihao 2021/07/02
 */
@Data
public class MetaDbParam extends Param {
    private DbConf config;
}
