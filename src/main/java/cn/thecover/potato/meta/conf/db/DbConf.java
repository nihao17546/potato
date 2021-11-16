package cn.thecover.potato.meta.conf.db;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author nihao 2021/06/29
 */
@Data
public class DbConf implements Serializable {
    private static final long serialVersionUID = -7921879247209176706L;
    private Table table;
    // 一对一
    private List<FollowTable> associationTables;
    // 一对多
    private List<FollowTable> followTables;
}
