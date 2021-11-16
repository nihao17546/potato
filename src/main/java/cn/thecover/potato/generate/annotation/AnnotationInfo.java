package cn.thecover.potato.generate.annotation;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nihao 2021/07/11
 */
public class AnnotationInfo implements Serializable {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Map<String,Object> fields;

    public void addField(String key, Object value) {
        if (fields == null) {
            fields = new HashMap<>();
        }
        fields.put(key, value);
    }

    public AnnotationInfo(String name) {
        this.name = name;
    }
}
