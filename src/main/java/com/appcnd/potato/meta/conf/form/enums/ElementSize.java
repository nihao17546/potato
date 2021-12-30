package com.appcnd.potato.meta.conf.form.enums;

import com.appcnd.potato.meta.conf.form.operate.enums.OperateElementType;
import com.appcnd.potato.meta.conf.form.search.enums.SearchElementType;

import java.util.Arrays;
import java.util.List;

/**
 * @author nihao 2021/07/06
 */
public enum ElementSize {
    large("大", Arrays.asList(SearchElementType.SELECT.name(),
            SearchElementType.SELECT_REMOTE.name(),
            SearchElementType.LIKE_INPUT.name(), SearchElementType.EQ_INPUT.name(),
            OperateElementType.INPUT.name(), OperateElementType.RADIO.name(),
            OperateElementType.SELECT_REMOTE.name(),
            OperateElementType.SELECT.name(), OperateElementType.TEXT_AREA.name())),
    medium("中等", Arrays.asList(SearchElementType.DATE_TIME.name(),
            SearchElementType.DATE_TIME_RANGE.name(),
            OperateElementType.DATE_TIME.name())),
    small("小", Arrays.asList()),
    mini("迷你", Arrays.asList());
    private String desc;
    private List<String> disableTypes;
    ElementSize(String desc, List<String> disableTypes) {
        this.desc = desc;
        this.disableTypes = disableTypes;
    }

    public List<String> getDisableTypes() {
        return disableTypes;
    }

    public String getDesc() {
        return desc;
    }
}
