package com.appcnd.potato.model.param.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2022/01/07
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "statusType")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = StringResponse.class, name = "string"),
        @JsonSubTypes.Type(value = IntegerResponse.class, name = "integer"),
        @JsonSubTypes.Type(value = BooleanResponse.class, name = "boolean")
})
public abstract class ResponseParam implements Serializable {
    // 类名
    private String className;
    // 状态field
    private String statusField;
    // 提示信息field
    private String messageField;
    // 内容field
    private String contentField;
}
