package cn.thecover.potato.meta.conf.form.operate.elements;

import cn.thecover.potato.meta.conf.form.operate.enums.OperateElementType;
import lombok.Data;

/**
 * @author nihao 2021/12/28
 */
@Data
public class RichTinymceElement extends OperateElement {
    private final OperateElementType elementType = OperateElementType.RICH_TINYMCE;
    private String placeholder;

    @Override
    public String getHtml() {
        String field = getFieldName();
        StringBuilder sb = new StringBuilder();
        sb
                .append("            <el-form-item label=\"").append(getLabel()).append(":\" prop=\"").append(field).append("\" label-width=\"100px\">\n")
                .append("                <el-input id=\"tinymce").append(field).append("\" type=\"textarea\" v-model.trim=\"form.").append(field).append("\"></el-input>\n")
                .append("            </el-form-item>\n");
        return sb.toString();
    }
}
