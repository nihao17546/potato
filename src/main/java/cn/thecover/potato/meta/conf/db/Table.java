package cn.thecover.potato.meta.conf.db;

import cn.thecover.potato.meta.conf.db.enums.PrimaryType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author nihao 2021/06/29
 */
@Data
public class Table implements Serializable {
    private static final long serialVersionUID = -990068851017782853L;
    private String name;
    private String sql;
    private List<Column> columns;
    private PrimaryType primaryType;
    private List<String> primaryFields;
    private List<List<String>> uniques;
}
