package com.appcnd.potato.model.param.response;

import lombok.Data;

/**
 * @author nihao 2022/01/07
 */
@Data
public class BooleanResponse extends ResponseParam {
    private final String statusType = "boolean";
    private Boolean successStatus;
    private Boolean errorStatus;
}
