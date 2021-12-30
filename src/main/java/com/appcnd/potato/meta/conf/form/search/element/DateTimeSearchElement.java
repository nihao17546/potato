package com.appcnd.potato.meta.conf.form.search.element;

import com.appcnd.potato.meta.conf.form.annotition.HtmlField;
import com.appcnd.potato.meta.conf.form.enums.ElementSize;
import com.appcnd.potato.meta.conf.form.search.enums.SearchElementType;
import lombok.Data;

/**
 * @author nihao 2021/07/08
 */
@Data
public class DateTimeSearchElement extends SearchElement {
    private final SearchElementType elementType = SearchElementType.DATE_TIME;
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

    @Override
    public String getOptions(String field) {
        StringBuilder sb = new StringBuilder();
        sb.append("        <el-form-item label=\"").append(getLabel()).append("\">\n")
                .append("            <el-date-picker style=\"width: 200px;\" v-model=\"").append(field).append("\"");
        sb.append(getFieldHtml());
        sb.append("@clear=\"search\" @change=\"search\">\n");
        sb.append("            </el-date-picker>\n");
        sb.append("        </el-form-item>\n");
        return sb.toString();
    }
}
