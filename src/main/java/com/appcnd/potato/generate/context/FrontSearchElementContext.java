package com.appcnd.potato.generate.context;

import com.appcnd.potato.meta.conf.form.search.element.SearchElement;
import lombok.Data;

/**
 * @author nihao 2021/07/28
 */
@Data
public class FrontSearchElementContext {
    private SearchElement element;
    private String field;
    private String startField;
    private String endField;
}