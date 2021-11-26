package cn.thecover.potato.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author nihao 2021/11/14
 */
@Data
@ConfigurationProperties(prefix = "spring.potato")
public class CoreProperties {
    private String path;
    private String classPath;

    @PostConstruct
    public void init() {
        File file = new File(classPath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new RuntimeException(classPath + " 目录创建失败");
            }
        }
    }
}
