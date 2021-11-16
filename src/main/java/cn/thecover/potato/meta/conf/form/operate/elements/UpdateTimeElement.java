package cn.thecover.potato.meta.conf.form.operate.elements;

import cn.thecover.potato.meta.conf.form.operate.enums.OperateElementType;
import cn.thecover.potato.meta.conf.form.operate.enums.TimeFormatType;
import lombok.Data;

/**
 * @author nihao 2021/08/27
 */
@Data
public class UpdateTimeElement extends OperateElement {
    private final OperateElementType elementType = OperateElementType.UPDATE_TIME;
    private TimeFormatType timeFormatType = TimeFormatType.DATE;

    @Override
    public String getHtml() {
        return "";
    }
}
