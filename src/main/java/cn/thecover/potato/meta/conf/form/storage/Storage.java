package cn.thecover.potato.meta.conf.form.storage;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2021/12/08
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = LocalStorage.class, name = "LOCAL"),
        @JsonSubTypes.Type(value = HuaweiStorage.class, name = "HUA_WEI"),
        @JsonSubTypes.Type(value = QiniuStorage.class, name = "QI_NIU")
})
public abstract class Storage implements Serializable {
}
