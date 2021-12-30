package com.appcnd.potato.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author nihao 2021/06/28
 */
@Data
public class DbVO implements Serializable {
    private Integer id;
    private String name;
    private String host;
    private Integer port;
    private String username;
    private String password;
    @JSONField(name = "create_time")
    private Date createTime;
    @JSONField(name = "update_time")
    private Date updateTime;
    @JSONField(name = "ssh_host")
    private String sshHost;
    @JSONField(name = "ssh_username")
    private String sshUsername;
    @JSONField(name = "ssh_port")
    private Integer sshPort;
    @JSONField(name = "ssh_password")
    private String sshPassword;
    @JSONField(name = "ssh_file_name")
    private String sshKeyName;
    @JSONField(name = "ssh_file_value")
    private String sshKeyValue;
    private String model;
}
