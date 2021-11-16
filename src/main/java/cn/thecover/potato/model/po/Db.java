package cn.thecover.potato.model.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author nihao 2021/06/28
 */
@Data
public class Db implements Serializable {
    private Integer id;
    private String name;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Date createTime;
    private Date updateTime;
    private String sshHost;
    private String sshUsername;
    private Integer sshPort;
    private String sshPassword;
    private String sshKeyName;
    private String sshKeyValue;
}
