package com.appcnd.potato.meta.conf;

import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2021/07/03
 */
@Data
public class Basic implements Serializable {
    private static final long serialVersionUID = -272884454160692158L;
    private String title;
    private String name;
    private Integer version;
}
