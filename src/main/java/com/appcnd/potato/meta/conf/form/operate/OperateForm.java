package com.appcnd.potato.meta.conf.form.operate;

import com.appcnd.potato.meta.conf.form.operate.elements.OperateElement;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author nihao 2021/08/09
 */
@Data
public class OperateForm implements Serializable {
    private static final long serialVersionUID = 600409847163593144L;
    private Boolean insert;
    private Boolean update;
    private Boolean delete;
    private Boolean cascadingDelete;
    private List<OperateElement> elements;
    private List<OperateForm> follows;
    private List<Unique> uniques;
}
