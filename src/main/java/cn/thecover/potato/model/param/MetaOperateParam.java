package cn.thecover.potato.model.param;

import cn.thecover.potato.meta.conf.form.operate.OperateForm;
import lombok.Data;

/**
 * @author nihao 2021/07/02
 */
@Data
public class MetaOperateParam extends Param {
    private OperateForm config;
}
