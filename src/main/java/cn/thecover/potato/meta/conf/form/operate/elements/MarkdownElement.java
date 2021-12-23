package cn.thecover.potato.meta.conf.form.operate.elements;

import cn.thecover.potato.meta.conf.form.annotition.HtmlField;
import cn.thecover.potato.meta.conf.form.operate.enums.MarkdownImageModel;
import cn.thecover.potato.meta.conf.form.operate.enums.OperateElementType;
import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * @author nihao 2021/12/23
 */
@Data
public class MarkdownElement extends OperateElement {
    private final OperateElementType elementType = OperateElementType.MARKDOWN;
    @HtmlField
    private String placeholder;
    private MarkdownImageModel imageModel = MarkdownImageModel.NONE;

    @Override
    public String getHtml() {
        String field = getFieldName();
        StringBuilder sb = new StringBuilder();
        sb
                .append("            <el-form-item label=\"").append(getLabel()).append(":\" prop=\"").append(field).append("\" label-width=\"100px\">\n")
                .append("                <mavon-editor v-model=\"form.").append(field).append("\" ref=\"MD").append(field).append("\"");
        Toolbar toolbar = new Toolbar();
        if (imageModel == MarkdownImageModel.NONE) {
            toolbar.setImagelink(false);
        } else if (imageModel == MarkdownImageModel.STORAGE) {
            sb.append(" @imgAdd=\"mdImgAdd\"");
        }
        if (getCanEdit() != null) {
            sb.append(" :editable=\"").append(getCanEdit()).append("\"");
        }
        String toolbarStr = JSON.toJSONString(toolbar);
        toolbarStr = toolbarStr.replaceAll("\"","'");
        sb.append(" :toolbars=\"").append(toolbarStr).append("\"");
        sb
                .append(getFieldHtml()).append("></mavon-editor>\n")
                .append("            </el-form-item>\n");
        return sb.toString();
    }

    @Data
    public static class Toolbar {
        private Boolean bold = true; // 粗体
        private Boolean italic = true;// 斜体
        private Boolean header = true;// 标题
        private Boolean underline = true;// 下划线
        private Boolean strikethrough = true;// 中划线
        private Boolean mark = true;// 标记
        private Boolean superscript = true;// 上角标
        private Boolean subscript = true;// 下角标
        private Boolean quote = true;// 引用
        private Boolean ol = true;// 有序列表
        private Boolean ul = true;// 无序列表
        private Boolean link = true;// 链接
        private Boolean imagelink = true;// 图片链接
        private Boolean code = true;// code
        private Boolean table = true;// 表格
        private Boolean fullscreen = true;// 全屏编辑
        private Boolean readmodel = true;// 沉浸式阅读
        private Boolean htmlcode = true;// 展示html源码
        private Boolean help = true;// 帮助
        /* 1.3.5 */
        private Boolean undo = true;// 上一步
        private Boolean redo = true;// 下一步
        private Boolean trash = true;// 清空
        private Boolean save = false;// 保存（触发events中的save事件）
        /* 1.4.2 */
        private Boolean navigation = true;// 导航目录
        /* 2.1.8 */
        private Boolean alignleft = true;// 左对齐
        private Boolean aligncenter = true;// 居中
        private Boolean alignright = true;// 右对齐
        /* 2.2.1 */
        private Boolean subfield = true;// 单双栏模式
        private Boolean preview = true;// 预览
    }
}
