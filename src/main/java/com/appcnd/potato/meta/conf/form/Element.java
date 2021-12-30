package com.appcnd.potato.meta.conf.form;

import com.appcnd.potato.meta.conf.form.annotition.HtmlField;
import com.appcnd.potato.util.CamelUtil;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @author nihao 2021/07/05
 */
@Data
public abstract class Element implements Serializable {
    private static final long serialVersionUID = 807787700208506705L;
    private String label;

    protected String getFieldHtml() {
        StringBuilder sb = new StringBuilder();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            HtmlField htmlField = field.getAnnotation(HtmlField.class);
            if (htmlField != null) {
                String key = CamelUtil.camelToUnderline(field.getName());
                key = key.replaceAll("_", "-");
                Object value = null;
                try {
                    field.setAccessible(true);
                    value = field.get(this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (field.getType().equals(Boolean.class)) {
                    if (Boolean.TRUE.equals(value)) {
                        sb.append(" ").append(key).append(" ");
                    }
                } else {
                    if (value != null) {
                        sb.append(" ").append(key).append("=\"").append(value.toString()).append("\" ");
                    }
                }
            }
        }
        return sb.toString();
    }
}
