package cn.thecover.potato.model.param;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author nihao 2021/07/08
 */
@Data
public class GenerateParam implements Serializable {
    private Integer id;
    private String packageName;
    private List<Entity> entityNames;

    @Data
    public static class Entity {
        private String table;
        private String clazz;
    }
}
