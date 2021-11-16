package cn.thecover.potato.generate.context;

import cn.thecover.potato.meta.conf.form.operate.elements.OperateElement;
import lombok.Data;

import java.util.List;

/**
 * @author nihao 2021/11/12
 */
@Data
public class FrontOperateContext {
    private List<String> primaryKeys;
    private List<OperateElement> elements;
}
