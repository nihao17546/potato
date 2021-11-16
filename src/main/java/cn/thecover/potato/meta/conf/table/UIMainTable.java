package cn.thecover.potato.meta.conf.table;

import lombok.Data;

import java.util.List;

/**
 * @author nihao 2021/07/03
 */
@Data
public class UIMainTable extends UITable {
    private List<UIFollowTable> follows;
}
