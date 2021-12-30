package com.appcnd.potato.meta.conf.form.operate;

import lombok.Data;

import java.io.Serializable;

/**
 * 表单项校验规则
 * @author nihao 2021/08/06
 */
@Data
public class Rule implements Serializable {
    private static final long serialVersionUID = 907787093758506705L;
    private Boolean required;
    private String message;
    private String regular;
}
