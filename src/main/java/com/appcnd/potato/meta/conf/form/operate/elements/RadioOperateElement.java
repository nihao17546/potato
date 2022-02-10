package com.appcnd.potato.meta.conf.form.operate.elements;

import com.appcnd.potato.meta.conf.form.annotition.HtmlField;
import com.appcnd.potato.meta.conf.form.enums.ElementSize;
import com.appcnd.potato.meta.conf.form.operate.enums.OperateElementType;
import com.appcnd.potato.meta.conf.form.option.Option;
import lombok.Data;

import java.util.List;

/**
 * @author nihao 2021/08/09
 */
@Data
public class RadioOperateElement extends OperateElement {
    private final OperateElementType elementType = OperateElementType.RADIO;
    @HtmlField
    private Boolean border = false;
    @HtmlField
    private ElementSize size;
    private List<Option> options;

    @Override
    public String getHtml() {
        String field = getFieldName();
        StringBuilder sb = new StringBuilder();
        sb.append("            <el-form-item label=\"").append(getLabel()).append(":\" prop=\"").append(field).append("\" label-width=\"100px\">\n")
                .append("                <el-radio-group v-model.trim=\"form.").append(field).append("\"");
        sb.append(getFieldHtml()).append(">\n");
        for (Option option : options) {
            sb.append("                    ").append(option.getRadioHtml()).append("\n");
        }
        sb.append("                </el-radio-group>\n")
                .append("            </el-form-item>\n");
        return sb.toString();
    }
}
