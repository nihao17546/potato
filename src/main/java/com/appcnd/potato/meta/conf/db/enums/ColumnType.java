package com.appcnd.potato.meta.conf.db.enums;

/**
 * @author nihao 2021/06/29
 */
public enum ColumnType {
    INT("int"),
    TINYINT("tinyint"),
    SMALLINT("smallint"),
    MEDIUMINT("mediumint"),
    BIGINT("bigint"),
    FLOAT("float"),
    DOUBLE("double"),
    DECIMAL("decimal"),
    BIT("bit"),
    CHAR("char"),
    VARCHAR("varchar"),
    TINYTEXT("tinytext"),
    TEXT("text"),
    MEDIUMTEXT("mediumtext"),
    LONGTEXT("longtext"),
    TINYBLOB("tinyblob"),
    MEDIUMBLOB("mediumblob"),
    BLOB("blob"),
    LONGBLOB("longblob"),
    BINARY("binary"),
    VARBINARY("varbinary"),
    DATE("date"),
    DATETIME("datetime"),
    TIMESTAMP("timestamp"),
    TIME("time"),
    YEAR("year");
    private String value;

    ColumnType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ColumnType get(String type) {
        for (ColumnType columnType : ColumnType.values()) {
            if (columnType.value.equalsIgnoreCase(type)) {
                return columnType;
            }
        }
        return null;
    }

    public boolean isString() {
        return this == CHAR || this == VARCHAR
                || this == TINYTEXT
                || this == TEXT
                || this == MEDIUMTEXT
                || this == LONGTEXT;
    }
}
