package com.appcnd.potato.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author nihao 2021/11/14
 */
@Data
@ConfigurationProperties(prefix = "spring.potato")
@Slf4j
public class CoreProperties {
    private String path;
    private String classPath;
    private Boolean cluster = Boolean.FALSE;
    private String loginname;
    private String password;
    private String[] excludeTables;

    @PostConstruct
    public void init() {
        if (classPath == null) {
            classPath = System.getProperty("java.io.tmpdir");
            if (classPath == null) {
                throw new RuntimeException("临时目录获取异常，请手动指定spring.potato.class-path");
            }
        }
        if (classPath.endsWith(File.separator)) {
            classPath = classPath.substring(0, classPath.length() - File.separator.length());
        }
        log.info("POTATO CLASSPATH:{}", classPath);
        File file = new File(classPath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new RuntimeException(classPath + " 目录创建失败");
            }
        }
    }
}
