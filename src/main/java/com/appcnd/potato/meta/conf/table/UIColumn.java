package com.appcnd.potato.meta.conf.table;

import com.appcnd.potato.meta.conf.db.Column;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author nihao 2021/07/01
 */
@Data
public class UIColumn implements Serializable {
    private static final long serialVersionUID = -8772986951550961215L;
    private String table;
    private Column column;
    private String label;
    private Integer width;
    private String formatter;
    private Boolean sortable;

    public UIColumn() {
    }

    public UIColumn(String table, Column column) {
        this.table = table;
        this.column = column;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof UIColumn) {
            UIColumn object = (UIColumn) obj;
            if (Objects.equals(object.getTable(), this.table)) {
                if (this.column != null && object.getColumn() != null) {
                    if (Objects.equals(object.getColumn().getField(), this.column.getField())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(table) + Objects.hashCode(column);
    }
}
