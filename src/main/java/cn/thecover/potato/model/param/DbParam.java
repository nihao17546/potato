package cn.thecover.potato.model.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2021/06/28
 */
@Data
public class DbParam implements Serializable {
    private Integer id;
    private String name;
    private String host;
    private Integer port;
    private String username;
    private String password;
    @JsonProperty("ssh_username")
    private String sshUsername;
    @JsonProperty("ssh_host")
    private String sshHost;
    @JsonProperty("ssh_port")
    private Integer sshPort;
    @JsonProperty("ssh_password")
    private String sshPassword;
    @JsonProperty("ssh_file_name")
    private String sshFileName;
    @JsonProperty("ssh_file_value")
    private String sshFileValue;

    /**
     * data:application/octet-stream;base64,
     * @param sshFileValue
     */
    public void setSshFileValue(String sshFileValue) {
        if (sshFileValue != null && sshFileValue.startsWith("data:")) {
            int i = sshFileValue.indexOf(";base64,");
            if (i > -1) {
                sshFileValue = sshFileValue.substring(i + ";base64,".length());
            }
        }
        this.sshFileValue = sshFileValue;
    }
}
