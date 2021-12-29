package cn.thecover.potato.meta.conf.form.operate.enums;

import java.util.Arrays;
import java.util.List;

/**
 * @author nihao 2021/08/06
 */
public enum OperateElementType {
    INPUT("普通文本输入框", Arrays.asList("label","maxlength", "minlength", "showWordLimit",
            "placeholder", "clearable", "size", "prefixIcon", "suffixIcon", "canEdit", "showPassword", "rule")),
    TEXT_AREA("文本域输入框", Arrays.asList("label","maxlength", "minlength", "showWordLimit",
            "placeholder", "clearable", "rows", "autosize", "canEdit", "rule")),
    SELECT("普通下拉选择框", Arrays.asList("label","size", "clearable", "placeholder", "filterable", "options", "canEdit", "rule")),
    SELECT_REMOTE("远程下拉选择框", Arrays.asList("label","size", "clearable", "placeholder", "filterable",
            "sql", "labelColumn", "valueColumn", "canEdit", "rule")),
    RADIO("单选框", Arrays.asList("label", "border", "size", "options", "canEdit", "rule")),
    DATE_TIME("日期时间选择器", Arrays.asList("label","format", "placeholder", "valueFormat", "size", "canEdit", "clearable", "rule")),
    CREATE_TIME("创建时间", Arrays.asList("timeFormatType")),
    UPDATE_TIME("更新时间", Arrays.asList("timeFormatType")),
    IMAGE("图片上传", Arrays.asList("label", "placeholder", "canEdit", "rule", "acceptType", "limitSize", "cut")),
    MARKDOWN("Markdown", Arrays.asList("label", "placeholder", "uploadImage", "canEdit", "rule")),
    RICH_TINYMCE("tinymce富文本编辑器", Arrays.asList("label", "placeholder", "canEdit", "rule"));

    private final String desc;
    private final List<String> contains;

    public String getDesc() {
        return desc;
    }

    public List<String> getContains() {
        return contains;
    }

    OperateElementType(String desc, List<String> contains) {
        this.desc = desc;
        this.contains = contains;
    }
}
