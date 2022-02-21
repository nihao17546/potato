package com.appcnd.potato.meta.conf.form.search.element;

import com.alibaba.fastjson.annotation.JSONField;
import com.appcnd.potato.meta.conf.form.Element;
import com.appcnd.potato.meta.conf.form.search.SearchColumn;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * @author nihao 2021/07/05
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "elementType")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = EqInputSearchElement.class, name = "EQ_INPUT"),
        @JsonSubTypes.Type(value = LikeInputSearchElement.class, name = "LIKE_INPUT"),
        @JsonSubTypes.Type(value = SelectSearchElement.class, name = "SELECT"),
        @JsonSubTypes.Type(value = DateTimeSearchElement.class, name = "DATE_TIME"),
        @JsonSubTypes.Type(value = DateTimeRangeSearchElement.class, name = "DATE_TIME_RANGE"),
        @JsonSubTypes.Type(value = RemoteSelectSearchElement.class, name = "SELECT_REMOTE")
})
public abstract class SearchElement extends Element {
    private SearchColumn column;

    @JSONField(serialize = false)
    public abstract String getOptions(String field);
}
