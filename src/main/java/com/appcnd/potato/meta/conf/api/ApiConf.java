package com.appcnd.potato.meta.conf.api;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * created by nihao 2022/01/27
 */
@Data
public class ApiConf implements Serializable {
    /**
     * 页面路由
     */
    private String uri;
    /**
     * http接口前缀
     */
    private String apiPrefix;
    /**
     * 从表
     */
    private List<ApiConf> follows;
}
