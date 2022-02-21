package com.appcnd.potato.meta.conf.form.option;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2021/07/07
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = StringOption.class, name = "string"),
        @JsonSubTypes.Type(value = NumberOption.class, name = "number"),
        @JsonSubTypes.Type(value = BooleanOption.class, name = "boolean")
})
public abstract class Option implements Serializable {
    private static final long serialVersionUID = -2460217802448037453L;
    private String label;

    @JSONField(serialize = false)
    public abstract String getOptionHtml();

    @JSONField(serialize = false)
    public abstract String getRadioHtml();
}
