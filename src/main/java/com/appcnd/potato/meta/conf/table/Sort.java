package com.appcnd.potato.meta.conf.table;

import com.appcnd.potato.meta.conf.table.enums.SortType;
import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2021/07/02
 */
@Data
public class Sort implements Serializable {
    private static final long serialVersionUID = 49908523253606429L;
    private String table;
    private String column;
    private SortType sort;
    private String key;
}
