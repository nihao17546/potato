package com.appcnd.potato.model.param;

import com.appcnd.potato.meta.conf.form.operate.OperateForm;
import lombok.Data;

/**
 * @author nihao 2021/07/02
 */
@Data
public class MetaOperateParam extends Param {
    private OperateForm config;
}
