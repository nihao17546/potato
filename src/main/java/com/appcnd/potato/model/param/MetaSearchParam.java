package com.appcnd.potato.model.param;

import com.appcnd.potato.meta.conf.form.search.SearchForm;
import lombok.Data;

/**
 * @author nihao 2021/07/02
 */
@Data
public class MetaSearchParam extends Param {
    private SearchForm config;
}
