package com.appcnd.potato.meta.conf.form.search.element;

import com.appcnd.potato.meta.conf.form.annotition.HtmlField;
import com.appcnd.potato.meta.conf.form.enums.ElementSize;
import com.appcnd.potato.meta.conf.form.search.enums.SearchElementType;
import lombok.Data;

/**
 * @author nihao 2021/07/06
 */
@Data
public class EqInputSearchElement extends SearchElement {
    private final SearchElementType elementType = SearchElementType.EQ_INPUT;
    @HtmlField
    private final String type = "text";
    @HtmlField
    private Integer maxlength;
    @HtmlField
    private Integer minlength;
    @HtmlField
    private Boolean showWordLimit = false;
    @HtmlField
    private String placeholder;
    @HtmlField
    private Boolean clearable = false;
    @HtmlField
    private Boolean showPassword = false;
    @HtmlField
    private ElementSize size;
    @HtmlField
    private String prefixIcon;
    @HtmlField
    private String suffixIcon;

    @Override
    public String getOptions(String field) {
        StringBuilder sb = new StringBuilder();
        sb.append("        <el-form-item label=\"").append(getLabel()).append("\">\n")
                .append("            <el-input style=\"width: 200px;\"")
                .append(" v-model.trim=\"").append(field).append("\"");
        sb.append(getFieldHtml());
        sb.append("@keyup.enter.native=\"search\" v-on:clear=\"search\"></el-input>\n");
        sb.append("        </el-form-item>\n");
        return sb.toString();
    }
}
