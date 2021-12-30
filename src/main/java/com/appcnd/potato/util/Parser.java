package com.appcnd.potato.util;


/**
 * created by nihao 2020/07/07
 */
public class Parser {

    public static String parse(String text, String from, Object to) {
        if (text == null || from == null) {
            return text;
        }
        if (to == null) {
            to = "";
        }
        return text.replaceAll("\\$\\{" + from + "\\}", to.toString());
    }
}
