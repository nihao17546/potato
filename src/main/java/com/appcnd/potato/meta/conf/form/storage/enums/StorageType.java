package com.appcnd.potato.meta.conf.form.storage.enums;

import java.util.Arrays;
import java.util.List;

/**
 * @author nihao 2021/12/08
 */
public enum StorageType {
    LOCAL("服务器本地", Arrays.asList("path","urlPrefix")),
    QI_NIU("七牛云", Arrays.asList("ak","sk","bucket","host")),
    HUA_WEI("华为云", Arrays.asList("ak","sk","bucket","host","region"));
    private final String desc;
    private final List<String> contains;

    public String getDesc() {
        return desc;
    }

    public List<String> getContains() {
        return contains;
    }

    StorageType(String desc, List<String> contains) {
        this.desc = desc;
        this.contains = contains;
    }
}
