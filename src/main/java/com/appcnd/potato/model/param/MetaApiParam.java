package com.appcnd.potato.model.param;

import com.appcnd.potato.meta.conf.api.ApiConf;
import lombok.Data;

/**
 * @author nihao 2021/07/02
 */
@Data
public class MetaApiParam extends Param {
    private ApiConf config;
}
