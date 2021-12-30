package com.appcnd.potato.generate.context;

import com.appcnd.potato.meta.conf.form.operate.elements.OperateElement;
import lombok.Data;

import java.util.List;

/**
 * @author nihao 2021/11/12
 */
@Data
public class FrontOperateContext {
    private List<String> primaryKeys;
    private List<OperateElement> elements;
    private Boolean insert;
    private Boolean update;
    private Boolean delete;
}
