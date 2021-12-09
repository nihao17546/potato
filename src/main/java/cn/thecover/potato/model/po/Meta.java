package cn.thecover.potato.model.po;

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
    private byte[] db;
    private byte[] table;
    private byte[] search;
    private byte[] operate;
    // 对象存储配置
    private String storage;
}
