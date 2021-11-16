package cn.thecover.potato.meta.conf.form.search.element;

import cn.thecover.potato.meta.conf.form.enums.ElementSize;
import cn.thecover.potato.meta.conf.form.option.Option;
import cn.thecover.potato.meta.conf.form.search.enums.SearchElementType;
import lombok.Data;

import java.util.List;

/**
 * @author nihao 2021/07/06
 */
@Data
public class SelectSearchElement extends SearchElement {
    private final SearchElementType elementType = SearchElementType.SELECT;
    private Boolean multiple = false;
    private ElementSize size;
    private Boolean clearable = false;
    private Integer multipleLimit = 0;
    private String placeholder = "请选择";
    private Boolean filterable = false;
    private List<Option> options;

    @Override
    public String getOptions(String field) {
        StringBuilder sb = new StringBuilder();
        sb.append("        <el-form-item label=\"").append(getLabel()).append("\">\n")
                .append("            <el-select style=\"width: 200px;\" v-model=\"").append(field).append("\"");
        if (Boolean.TRUE.equals(multiple)) {
            sb.append(" multiple");
        }
        if (size != null) {
            sb.append(" size=\"").append(size.name()).append("\"");
        }
        if (Boolean.TRUE.equals(clearable)) {
            sb.append(" clearable");
        }
        if (multipleLimit != null) {
            sb.append(" multiple-limit=\"").append(multipleLimit).append("\"");
        }
        if (placeholder != null) {
            sb.append(" placeholder=\"").append(placeholder).append("\"");
        }
        if (Boolean.TRUE.equals(filterable)) {
            sb.append(" filterable");
        }
        sb.append(" @clear=\"search\" @change=\"search\">\n");
        if (options != null) {
            for (Option option : options) {
                sb.append("                ").append(option.getOptionHtml()).append("\n");
            }
        }
        sb.append("            </el-select>\n");
        sb.append("        </el-form-item>\n");
        return sb.toString();
    }
}
