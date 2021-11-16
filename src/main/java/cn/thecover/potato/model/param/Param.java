package cn.thecover.potato.model.param;

import lombok.Data;

import java.io.Serializable;

/**
 * @author nihao 2021/07/03
 */
@Data
public abstract class Param implements Serializable {
    private Integer id;
    private Integer version;
}
