package com.appcnd.potato.model.param;

import com.appcnd.potato.meta.conf.form.storage.Storage;
import lombok.Data;

/**
 * @author nihao 2021/12/08
 */
@Data
public class MetaStorageParam extends Param {
    private Storage config;
}
