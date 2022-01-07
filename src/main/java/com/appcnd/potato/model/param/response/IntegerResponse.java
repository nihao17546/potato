package com.appcnd.potato.model.param.response;

import lombok.Data;

/**
 * @author nihao 2022/01/07
 */
@Data
public class IntegerResponse extends ResponseParam {
    private final String statusType = "integer";
    private Integer successStatus;
    private Integer errorStatus;
}
