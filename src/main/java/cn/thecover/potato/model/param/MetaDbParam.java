package cn.thecover.potato.model.param;

import cn.thecover.potato.meta.conf.db.DbConf;
import lombok.Data;

/**
 * @author nihao 2021/07/02
 */
@Data
public class MetaDbParam extends Param {
    private DbConf config;
}
