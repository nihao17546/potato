package cn.thecover.potato.meta.conf.form.search;

import cn.thecover.potato.meta.conf.form.search.enums.JudgeType;
import cn.thecover.potato.meta.conf.form.search.enums.RelationType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author nihao 2021/07/05
 */
@Data
public class DefaultSearchColumn implements Serializable {
    private static final long serialVersionUID = -5388203441987690089L;
    private SearchColumn column;
    private JudgeType judge;
    private RelationType relation;
    private String value;
    private List<String> inValue;
    private String startValue;
    private String endValue;
}
