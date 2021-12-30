package com.appcnd.potato.meta.conf.form.operate.elements;

import com.appcnd.potato.meta.conf.form.annotition.HtmlField;
import com.appcnd.potato.meta.conf.form.enums.ElementSize;
import com.appcnd.potato.meta.conf.form.operate.enums.OperateElementType;
import lombok.Data;

/**
 * @author nihao 2021/08/06
 */
@Data
public class InputOperateElement extends OperateElement {
    private final OperateElementType elementType = OperateElementType.INPUT;
    private final String type = "text";
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
    @HtmlField
    private Boolean showPassword;
    @HtmlField
    private ElementSize size;
    @HtmlField
    private String prefixIcon;
    @HtmlField
    private String suffixIcon;

    @Override
    public String getHtml() {
        String field = getFieldName();
        StringBuilder sb = new StringBuilder();
        sb.append("            <el-form-item label=\"").append(getLabel()).append(":\" prop=\"").append(field).append("\" label-width=\"100px\">\n")
                .append("                <el-input v-model.trim=\"form.").append(field).append("\"");
        sb.append(getFieldHtml());
        sb.append("></el-input>\n")
                .append("            </el-form-item>\n");
        return sb.toString();
    }
}
