package com.appcnd.potato.meta.conf.form.search;

import com.appcnd.potato.meta.conf.form.search.element.SearchElement;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author nihao 2021/07/03
 */
@Data
public class SearchForm implements Serializable {
    private static final long serialVersionUID = 6004527711981633144L;
    private List<DefaultSearchGroup> defaultSearchGroups;
    private List<SearchElement> elements;

    private List<SearchForm> follows;
}
