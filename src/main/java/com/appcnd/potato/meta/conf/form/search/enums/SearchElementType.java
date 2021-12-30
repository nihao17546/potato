package com.appcnd.potato.meta.conf.form.search.enums;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author nihao 2021/07/06
 */
public enum SearchElementType {
    EQ_INPUT("完全匹配文本输入框", Arrays.asList("maxlength","minlength","showWordLimit",
            "placeholder","clearable","size","prefixIcon","suffixIcon")),
    LIKE_INPUT("模糊匹配文本输入框", Arrays.asList("maxlength","minlength","showWordLimit",
            "placeholder","clearable","size","prefixIcon","suffixIcon")),
    SELECT("普通下拉选择框", Arrays.asList("multiple","size","clearable","multipleLimit",
            "placeholder","filterable","options")),
    SELECT_REMOTE("远程下拉选择框", Arrays.asList("multiple","size","clearable","multipleLimit",
            "placeholder","filterable","sql","labelColumn","valueColumn")),
    DATE_TIME("日期时间选择器", Arrays.asList("placeholder","valueFormat","size","format")),
    DATE_TIME_RANGE("日期时间区间选择器", Arrays.asList("size","startPlaceholder","endPlaceholder","format","valueFormat"));
    private final String desc;
    private final List<String> contains;

    public String getDesc() {
        return desc;
    }

    public List<String> getContains() {
        return contains;
    }

    SearchElementType(String desc, List<String> contains) {
        this.desc = desc;
        this.contains = contains;
    }
}
