package com.appcnd.potato.meta.conf.db;

import lombok.Data;

/**
 * @author nihao 2021/06/29
 */
@Data
public class FollowTable extends Table {
    private String title;
    // 当前表外键
    private String foreignKey;
    // 父表键
    private String parentKey;
}
