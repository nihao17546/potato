package cn.thecover.potato.meta.conf.form.annotition;

import java.lang.annotation.*;

/**
 * @author nihao 2021/11/13
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HtmlField {
}
