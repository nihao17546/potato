package cn.thecover.potato.meta.conf.form.search;

import cn.thecover.potato.meta.conf.form.search.enums.RelationType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author nihao 2021/07/05
 */
@Data
public class DefaultSearchGroup implements Serializable {
    private static final long serialVersionUID = 7292955985554074101L;
    private List<DefaultSearchColumn> columns;
    private RelationType relation;
}
