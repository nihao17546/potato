package cn.thecover.potato.meta.conf.form.operate.elements;

import cn.thecover.potato.meta.conf.form.operate.enums.OperateElementType;
import lombok.Data;

import java.util.List;

/**
 * @author nihao 2021/12/09
 */
@Data
public class ImageElement extends OperateElement {
    private final OperateElementType elementType = OperateElementType.IMAGE;
    private String placeholder;
    /**
     * 支出上传的类型（文件名后缀）
     */
    private List<String> acceptType;
    /**
     * 限制上传文件大小（单位：byte）
     */
    private Integer limitSize;
    /**
     * 是否支持裁剪
     */
    private Boolean cut;

    @Override
    public String getHtml() {
        String field = getFieldName();
        StringBuilder sb = new StringBuilder();
        sb
                .append("            <el-form-item label=\"").append(getLabel()).append(":\" prop=\"").append(field).append("\" label-width=\"100px\">\n")
                .append("                <el-upload v-if=\"!form.").append(field).append(" && typeof uploadImageProgress['").append(field).append("'] == 'undefined'\"\n")
                .append("                        action=\"\"\n");
        if (acceptType != null && !acceptType.isEmpty()) {
            StringBuilder atBuilder = new StringBuilder();
            for (String at : acceptType) {
                if (atBuilder.length() > 0) {
                    atBuilder.append(",");
                }
                atBuilder.append(at);
            }
            sb.append("                        accept=\"").append(atBuilder.toString()).append("\"\n");
        }
        sb
                .append("                        ref=\"").append(field).append("\"\n")
                .append("                        :show-file-list=\"false\"\n")
                .append("                        :multiple=\"false\"\n")
                .append("                        :on-change=\"(file, fileList) => {uploadChange(file, fileList, '").append(field).append("')}\"\n")
                .append("                        :auto-upload=\"false\">\n")
                .append("                    <el-button type=\"primary\" icon=\"el-icon-plus\" circle></el-button>\n")
                .append("                </el-upload>\n")
                .append("                <el-progress type=\"circle\" v-if=\"uploadImageProgress['").append(field).append("'] >= -1\" :percentage=\"uploadImageProgress['").append(field).append("']\"\n")
                .append("                             :status=\"uploadImageProgress['").append(field).append("'] === -1 ? 'exception' : uploadImageProgress['").append(field).append("'] === 100 ? 'success' : ''\"></el-progress>\n")
                .append("                <el-image v-if=\"form.").append(field).append(" && typeof uploadImageProgress['").append(field).append("'] == 'undefined'\"\n")
                .append("                          style=\"width: 150px;\"\n")
                .append("                          :src=\"form.").append(field).append("\"\n")
                .append("                          :preview-src-list=\"[form.").append(field).append("]\">\n")
                .append("                </el-image>\n")
                .append("                <el-button v-if=\"form.").append(field).append(" && typeof uploadImageProgress['").append(field).append("'] == 'undefined'\" type=\"danger\" icon=\"el-icon-delete\" @click=\"removeImage('").append(field).append("')\" circle></el-button>\n")
                .append("            </el-form-item>\n");
        return sb.toString();
    }
}
