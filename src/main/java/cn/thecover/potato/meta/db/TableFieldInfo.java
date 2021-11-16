package cn.thecover.potato.meta.db;


import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2021/06/29
 */
@Data
public class TableFieldInfo implements Serializable {
    private String Field;
    private String Type;
    private String Null;
    private String Key;
    private String Default;
    private String Extra;
}
