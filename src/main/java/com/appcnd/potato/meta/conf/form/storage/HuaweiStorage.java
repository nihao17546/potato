package com.appcnd.potato.meta.conf.form.storage;

import com.appcnd.potato.meta.conf.form.storage.enums.StorageType;
import lombok.Data;

/**
 * @author nihao 2021/12/08
 */
@Data
public class HuaweiStorage extends Storage {
    private final StorageType type = StorageType.HUA_WEI;

    private String ak;
    private String sk;
    private String bucket;
    private String host;
    private String region;
}
