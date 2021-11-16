package cn.thecover.potato.meta.conf.form.operate;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author nihao 2021/10/21
 */
@Data
public class Unique implements Serializable {
    private String toast;
    private List<String> columns;
}
