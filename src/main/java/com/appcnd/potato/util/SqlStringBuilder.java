package com.appcnd.potato.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author nihao 2021/07/08
 */
public class SqlStringBuilder {
    private StringBuilder builder;
    private Set<String> hasAppend;

    public SqlStringBuilder() {
        this.builder = new StringBuilder();
    }

    public SqlStringBuilder append(String string) {
        this.builder.append(string);
        return this;
    }

    public SqlStringBuilder put(String string) {
        this.builder.append("`").append(string).append("`");
        return this;
    }

    public SqlStringBuilder appendAndRecord(String string) {
        if (hasAppend == null) {
            hasAppend = new HashSet<>();
        }
        hasAppend.add(string);
        this.builder.append(string);
        return this;
    }

    public SqlStringBuilder putAndRecord(String string) {
        if (hasAppend == null) {
            hasAppend = new HashSet<>();
        }
        hasAppend.add(string);
        this.builder.append("`").append(string).append("`");
        return this;
    }

    public boolean hasAppend(String string) {
        if (this.hasAppend != null && this.hasAppend.contains(string)) {
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return builder == null || builder.length() == 0;
    }

    @Override
    public String toString() {
        return this.builder.toString();
    }

    public SqlStringBuilder deleteCharAt(int index) {
        builder.deleteCharAt(index);
        return this;
    }

    public int length() {
        return builder.length();
    }
}
