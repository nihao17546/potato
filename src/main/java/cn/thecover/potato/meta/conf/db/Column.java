package cn.thecover.potato.meta.conf.db;

import cn.thecover.potato.meta.conf.db.enums.ColumnType;
import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2021/06/29
 */
@Data
public class Column implements Serializable {
    private static final long serialVersionUID = -1934997201482217269L;
    private String field;
    private ColumnType type;
    private Double length;
    private Boolean canBeNull;
    private String defaultValue;
    
    public Class getJavaType() {
        Class clazz = null;
        if (type == ColumnType.INT
                || type == ColumnType.MEDIUMINT) {
            clazz = Integer.class;
        } else if (type == ColumnType.TINYINT) {
            if (length == 1) {
                clazz = Boolean.class;
            } else {
                clazz = Integer.class;
            }
        } else if (type == ColumnType.SMALLINT) {
            clazz = Short.class;
        } else if (type == ColumnType.BIGINT) {
            clazz = Long.class;
        } else if (type == ColumnType.FLOAT) {
            clazz = Float.class;
        } else if (type == ColumnType.DOUBLE) {
            clazz = Double.class;
        } else if (type == ColumnType.DECIMAL) {
            clazz = java.math.BigDecimal.class;
        } else if (type == ColumnType.BIT) {
            if (length == 1) {
                clazz = Boolean.class;
            } else {
                clazz = byte[].class;
            }
        } else if (type == ColumnType.CHAR
                || type == ColumnType.VARCHAR
                || type == ColumnType.TINYTEXT
                || type == ColumnType.TEXT
                || type == ColumnType.MEDIUMTEXT
                || type == ColumnType.LONGTEXT) {
            clazz = String.class;
        } else if (type == ColumnType.TINYBLOB
                || type == ColumnType.MEDIUMBLOB
                || type == ColumnType.BLOB
                || type == ColumnType.LONGBLOB
                || type == ColumnType.BINARY
                || type == ColumnType.VARBINARY) {
            clazz = byte[].class;
        } else if (type == ColumnType.DATE
                || type == ColumnType.YEAR) {
            clazz = java.sql.Date.class;
        } else if (type == ColumnType.DATETIME
                || type == ColumnType.TIMESTAMP) {
            clazz = java.sql.Timestamp.class;
        } else if (type == ColumnType.TIME) {
            clazz = java.sql.Time.class;
        } else {
            throw new IllegalArgumentException(type.toString());
        }
        return clazz;
    }
}
