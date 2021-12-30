package com.appcnd.potato.meta.conf.form.search.element;

import com.appcnd.potato.meta.conf.form.annotition.HtmlField;
import com.appcnd.potato.meta.conf.form.enums.ElementSize;
import com.appcnd.potato.meta.conf.form.search.enums.SearchElementType;
import lombok.Data;

import java.util.Objects;

/**
 * @author nihao 2021/07/06
 */
@Data
public class RemoteSelectSearchElement extends SearchElement {
    private final SearchElementType elementType = SearchElementType.SELECT_REMOTE;
    @HtmlField
    private Boolean multiple = false;
    @HtmlField
    private ElementSize size;
    @HtmlField
    private Boolean clearable = false;
    @HtmlField
    private Integer multipleLimit = 0;
    @HtmlField
    private String placeholder = "请选择";
    @HtmlField
    private Boolean filterable = false;
    private String sql;
    private String labelColumn;
    private String valueColumn;

    @Override
    public String getOptions(String field) {
        StringBuilder sb = new StringBuilder();
        sb.append("        <el-form-item label=\"").append(getLabel()).append("\">\n")
                .append("            <el-select style=\"width: 200px;\" v-model=\"").append(field).append("\"");
        sb.append(getFieldHtml());
        String optionName = "data" + hashCode();
        optionName = optionName.replaceAll("-", "A");
        sb.append(" @clear=\"search\" @change=\"search\">\n");
        sb.append("                <el-option\n");
        sb.append("                  v-for=\"item in ").append(optionName).append("\"\n");
        sb.append("                  :key=\"item.").append(valueColumn).append("\"\n");
        sb.append("                  :label=\"item.").append(labelColumn).append("\"\n");
        sb.append("                  :value=\"item.").append(valueColumn).append("\">\n");
        sb.append("                </el-option>\n");
        sb.append("            </el-select>\n");
        sb.append("        </el-form-item>\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o.getClass().equals(this.getClass())) {
            RemoteSelectSearchElement that = (RemoteSelectSearchElement) o;
            return Objects.equals(sql, that.sql) &&
                    Objects.equals(labelColumn, that.labelColumn) &&
                    Objects.equals(valueColumn, that.valueColumn);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sql, labelColumn, valueColumn);
    }
}
