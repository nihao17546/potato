package cn.thecover.potato.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author nihao 2021/11/14
 */
@Data
@ConfigurationProperties(prefix = "spring.potato")
public class CoreProperties {
    private String path;
}
