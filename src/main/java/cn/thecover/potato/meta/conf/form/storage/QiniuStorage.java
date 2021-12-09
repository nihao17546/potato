package cn.thecover.potato.meta.conf.form.storage;

import cn.thecover.potato.meta.conf.form.storage.enums.StorageType;
import lombok.Data;

/**
 * @author nihao 2021/12/08
 */
@Data
public class QiniuStorage extends Storage {
    private final StorageType type = StorageType.QI_NIU;

    private String ak;
    private String sk;
    private String bucket;
    private String host;
}
