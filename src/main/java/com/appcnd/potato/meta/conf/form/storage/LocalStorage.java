package com.appcnd.potato.meta.conf.form.storage;

import com.appcnd.potato.meta.conf.form.storage.enums.StorageType;
import lombok.Data;

/**
 * @author nihao 2021/12/08
 */
@Data
public class LocalStorage extends Storage {
    private final StorageType type = StorageType.LOCAL;

    /**
     * 文件存储绝对路径
     */
    private String path;

    /**
     * 访问域名+前缀
     */
    private String urlPrefix;
}
