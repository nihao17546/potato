package com.appcnd.potato.model.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author nihao 2021/06/27
 */
@Data
public class Meta implements Serializable {
    private Integer id;
    private String name;
    private String title;
    private Integer version;
    private Date createTime;
    private Date updateTime;
    private String db;
    private String table;
    private String search;
    private String operate;
    // 对象存储配置
    private String storage;
    private String api;
    private Boolean loaded;
}
