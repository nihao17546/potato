package cn.thecover.potato.meta.conf.form.operate.elements;

import cn.thecover.potato.meta.conf.form.enums.ElementSize;
import cn.thecover.potato.meta.conf.form.operate.enums.OperateElementType;
import cn.thecover.potato.meta.conf.form.option.Option;
import lombok.Data;

import java.util.List;

/**
 * @author nihao 2021/08/06
 */
@Data
public class SelectOperateElement extends OperateElement {
    private final OperateElementType elementType = OperateElementType.SELECT;
    private ElementSize size;
    private Boolean clearable = true;
    private String placeholder;
    private Boolean filterable = true;
    private List<Option> options;

    @Override
    public String getHtml() {
        String field = getFieldName();
        StringBuilder sb = new StringBuilder();
        sb.append("            <el-form-item label=\"").append(getLabel()).append(":\" prop=\"").append(field).append("\" label-width=\"100px\">\n")
                .append("                <el-select style=\"width: 100%\" v-model.trim=\"form.").append(field).append("\"");
        sb.append(getFieldHtml()).append(">\n");
        for (Option option : options) {
            sb.append("                    ").append(option.getOptionHtml()).append("\n");
        }
        sb.append("                </el-select>\n")
                .append("            </el-form-item>\n");
        return sb.toString();
    }
}
