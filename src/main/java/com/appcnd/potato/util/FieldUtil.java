package com.appcnd.potato.util;

/**
 * @author nihao 2021/07/10
 */
public class FieldUtil {

    public static String getField(String table, String column) {
        return table + "_" + column;
    }

}
