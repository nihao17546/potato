package com.appcnd.potato.meta.trans;

import com.appcnd.potato.meta.conf.db.Column;
import com.appcnd.potato.meta.conf.db.DbConf;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.db.Table;
import com.appcnd.potato.meta.conf.db.enums.ColumnType;
import com.appcnd.potato.meta.conf.db.enums.PrimaryType;
import com.appcnd.potato.meta.conf.table.UIColumn;
import com.appcnd.potato.meta.db.TableFieldInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nihao 2021/07/01
 */
public class DbTransfer {
    private static Pattern pattern = Pattern.compile("\\(\\S+\\)");
    private static Pattern priPattern = Pattern.compile("PRIMARY KEY \\(\\S+\\)[A-Za-z ]*,?\\n");
    private static Pattern uniPattern = Pattern.compile("UNIQUE KEY \\S*\\s* \\(\\S+\\)[A-Za-z ]*,?\\n");

    private static List<String> getPrimaryFields(String sql) {
        Matcher priMatcher = priPattern.matcher(sql);
        if (priMatcher.find()) {
            return getKeyFields(sql, priMatcher);
        }
        return Collections.emptyList();
    }

    private static List<String> getKeyFields(String sql, Matcher keyMatcher) {
        List<String> fields = new ArrayList<>();
        String str = sql.substring(keyMatcher.start(),keyMatcher.end());
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            str = str.substring(matcher.start() + 1, matcher.end() - 1);
            String[] strings = str.split(",");
            for (String string : strings) {
                fields.add(string.replaceAll("`","").trim());
            }
        }
        return fields;
    }

    private static List<List<String>> getUniqueFields(String sql) {
        List<List<String>> lists = new ArrayList<>();
        Matcher uniMatcher = uniPattern.matcher(sql);
        while (uniMatcher.find()) {
            List<String> list = getKeyFields(sql, uniMatcher);
            if (list != null && !list.isEmpty()) {
                lists.add(list);
            }
        }
        return lists;
    }

    public static Table transfer(List<TableFieldInfo> list, String sql) {
        List<String> primaryFields = getPrimaryFields(sql);
        List<List<String>> uniqueFields = getUniqueFields(sql);
        Table table = new Table();
        table.setSql(sql);
        List<Column> columns = new ArrayList<>();
        table.setColumns(columns);
        String autoIncrementField = null;
        Map<String,Column> columnMap = new HashMap<>(list.size());
        for (TableFieldInfo fieldInfo : list) {
            String type = fieldInfo.getType();
            Double length = null;
            if (type.endsWith(" unsigned")) {
                type = type.substring(0, type.length() - " unsigned".length());
            }
            Matcher matcher = pattern.matcher(type);
            if (matcher.find()) {
                String string = type.substring(matcher.start(), matcher.end());
                string = string.substring(1, string.length() - 1);
                String[] ss = string.split(",");
                if (ss.length == 1) {
                    length = Double.parseDouble(ss[0]);
                } else if (ss.length == 2) {
                    length = Double.parseDouble(ss[0] + "." + ss[1]);
                } else {
                    throw new RuntimeException("字段[" + fieldInfo.getField() + "]类型解析异常:" + fieldInfo.getType());
                }
                type = matcher.replaceAll("");
            }
            ColumnType columnType = ColumnType.get(type);
            if (columnType == null) {
                throw new RuntimeException("字段[" + fieldInfo.getField() + "]类型不支持:" + fieldInfo.getType());
            }
            Column column = new Column();
            column.setField(fieldInfo.getField());
            column.setType(columnType);
            column.setLength(length);
            column.setCanBeNull("YES".equalsIgnoreCase(fieldInfo.getNull()));
            if (!"CURRENT_TIMESTAMP".equalsIgnoreCase(fieldInfo.getDefault())) {
                column.setDefaultValue(fieldInfo.getDefault());
            }
            if ("auto_increment".equalsIgnoreCase(fieldInfo.getExtra())) {
                autoIncrementField = fieldInfo.getField();
            }
            columns.add(column);
            columnMap.put(column.getField(), column);
        }
        if (primaryFields != null && !primaryFields.isEmpty()) {
            table.setPrimaryFields(primaryFields);
            if (autoIncrementField != null) {
                if (primaryFields.contains(autoIncrementField)) {
                    if (primaryFields.size() == 1) {
                        table.setPrimaryType(PrimaryType.AUTO);
                    }
                }
            }
            if (table.getPrimaryType() == null) {
                if (primaryFields.size() == 1) {
                    Column column = columnMap.get(primaryFields.get(0));
                    if (column.getType().isString()) {
                        table.setPrimaryType(PrimaryType.UUID);
                    }
                }
                if (table.getPrimaryType() == null) {
                    table.setPrimaryType(PrimaryType.NORMAL);
                }
            }
        }
        if (uniqueFields != null && !uniqueFields.isEmpty()) {
            table.setUniques(uniqueFields);
        }
        return table;
    }

    public static List<UIColumn> getMainUIColumns(DbConf dbConf) {
        List<UIColumn> uiColumns = new ArrayList<>();
        Table mainTable = dbConf.getTable();
        if (mainTable != null && mainTable.getColumns() != null) {
            for (Column column : mainTable.getColumns()) {
                uiColumns.add(new UIColumn(mainTable.getName(), column));
            }
        }
        if (dbConf.getAssociationTables() != null) {
            for (FollowTable followTable : dbConf.getAssociationTables()) {
                if (followTable.getColumns() != null) {
                    for (Column column : followTable.getColumns()) {
                        uiColumns.add(new UIColumn(followTable.getName(), column));
                    }
                }
            }
        }
        return uiColumns;
    }

    public static List<UIColumn> getFollowUIColumns(FollowTable table) {
        List<UIColumn> uiColumns = new ArrayList<>();
        if (table.getColumns() != null) {
            for (Column column : table.getColumns()) {
                uiColumns.add(new UIColumn(table.getName(), column));
            }
        }
        return uiColumns;
    }

}
