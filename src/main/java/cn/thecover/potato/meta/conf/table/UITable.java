package cn.thecover.potato.meta.conf.table;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author nihao 2021/07/01
 */
@Data
public abstract class UITable implements Serializable {
    private static final long serialVersionUID = 5242388959456084823L;
    private Boolean pagination;
    private List<UIColumn> columns;
    private List<Sort> sorts;
    // 操作列列宽
    private Integer optionColumnWidth;
}
