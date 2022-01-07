package com.appcnd.potato.model.param.response;

import lombok.Data;

/**
 * @author nihao 2022/01/07
 */
@Data
public class StringResponse extends ResponseParam {
    private final String statusType = "string";
    private String successStatus;
    private String errorStatus;
}
