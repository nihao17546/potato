package cn.thecover.potato.meta.conf.form.search;

import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2021/07/05
 */
@Data
public class SearchColumn implements Serializable {
    private static final long serialVersionUID = -3097436628638716222L;
    private String table;
    private String column;
    private String key;
}
