package cn.thecover.potato.generate.context;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author nihao 2021/11/13
 */
@Data
public class RemoteContext implements Serializable {
    private String sql;
    private String labelColumn;
    private String valueColumn;

    public RemoteContext(String sql, String labelColumn, String valueColumn) {
        this.sql = sql;
        this.labelColumn = labelColumn;
        this.valueColumn = valueColumn;
    }

    public RemoteContext() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o.getClass().equals(this.getClass())) {
            RemoteContext that = (RemoteContext) o;
            return Objects.equals(sql, that.sql) &&
                    Objects.equals(labelColumn, that.labelColumn) &&
                    Objects.equals(valueColumn, that.valueColumn);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sql, labelColumn, valueColumn);
    }
}
