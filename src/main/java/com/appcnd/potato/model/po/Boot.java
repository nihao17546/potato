package com.appcnd.potato.model.po;

import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2021/07/24
 */
@Data
public class Boot implements Serializable {
    private Integer id;
    private Integer metaId;
    private Integer version;
    private String data;
}
