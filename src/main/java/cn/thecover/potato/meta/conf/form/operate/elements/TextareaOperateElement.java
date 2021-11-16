package cn.thecover.potato.meta.conf.form.operate.elements;

import cn.thecover.potato.meta.conf.form.annotition.HtmlField;
import cn.thecover.potato.meta.conf.form.operate.enums.OperateElementType;
import lombok.Data;

/**
 * @author nihao 2021/08/06
 */
@Data
public class TextareaOperateElement extends OperateElement {
    private final OperateElementType elementType = OperateElementType.TEXT_AREA;
    private final String type = "textarea";
    @HtmlField
    private Integer rows = 2;
    @HtmlField
    private Boolean autosize = false;
    @HtmlField
    private Integer maxlength;
    @HtmlField
    private Integer minlength;
    @HtmlField
    private Boolean showWordLimit;
    @HtmlField
    private String placeholder;
    @HtmlField
    private Boolean clearable = true;

    @Override
    public String getHtml() {
        String field = getFieldName();
        StringBuilder sb = new StringBuilder();
        sb.append("            <el-form-item label=\"").append(getLabel()).append(":\" prop=\"").append(field).append("\" label-width=\"100px\">\n")
                .append("                <el-input type=\"textarea\" v-model.trim=\"form.").append(field).append("\"");
        sb.append(getFieldHtml());
        sb.append("></el-input>\n")
                .append("            </el-form-item>\n");
        return sb.toString();
    }
}
