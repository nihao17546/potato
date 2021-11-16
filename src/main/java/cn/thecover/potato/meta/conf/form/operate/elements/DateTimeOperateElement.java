package cn.thecover.potato.meta.conf.form.operate.elements;

import cn.thecover.potato.meta.conf.form.annotition.HtmlField;
import cn.thecover.potato.meta.conf.form.enums.ElementSize;
import cn.thecover.potato.meta.conf.form.operate.enums.OperateElementType;
import lombok.Data;

/**
 * @author nihao 2021/08/09
 */
@Data
public class DateTimeOperateElement extends OperateElement {
    private final OperateElementType elementType = OperateElementType.DATE_TIME;
    @HtmlField
    private final String type = "datetime";
    // 显示在输入框中的格式
    @HtmlField
    private String format = "yyyy-MM-dd HH:mm:ss";
    @HtmlField
    private String placeholder;
    // 绑定值的格式
    @HtmlField
    private String valueFormat;
    @HtmlField
    private ElementSize size;
    @HtmlField
    private Boolean clearable = true;

    @Override
    public String getHtml() {
        String field = getFieldName();
        StringBuilder sb = new StringBuilder();
        sb.append("            <el-form-item label=\"").append(getLabel()).append(":\" prop=\"").append(field).append("\" label-width=\"100px\">\n");
        sb.append("                <el-date-picker style=\"width: 100%;\" v-model.trim=\"form.").append(field).append("\"");
        sb.append(getFieldHtml());
        sb.append("></el-date-picker>\n")
                .append("            </el-form-item>\n");
        return sb.toString();
    }
}
