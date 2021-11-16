package cn.thecover.potato.generate.context;

import cn.thecover.potato.meta.conf.db.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author nihao 2021/07/12
 */
public class GenerateContext {
    // 主表pojo类名
    @Getter
    @Setter
    private ClassName mainClassName;

    // 一对一pojo类名
    @Getter
    private List<ClassName> associationClassNames;

    // 一对多pojo类名
    @Getter
    private List<ClassName> followClassNames;

    // 数据库字段
    @Setter
    private Map<String,Map<String, Column>> columnMap;

    @Getter
    private List<JavaClassContext> javaClassContexts;

    @Getter
    @Setter
    private FrontContext frontContext;

    public void addJavaClassContext(JavaClassContext... javaClassContextList) {
        if (javaClassContexts == null) {
            javaClassContexts = new ArrayList<>();
        }
        for (JavaClassContext classContext : javaClassContextList) {
            javaClassContexts.add(classContext);
        }
    }

    /**
     * 根据表名、字段名获取数据库字段定义
     * @param table 表名
     * @param field 字段名
     * @return
     */
    public Column getColumn(String table, String field) {
        return columnMap.get(table).get(field);
    }

    public void addAssociationClassName(ClassName className) {
        if (associationClassNames == null) {
            associationClassNames = new ArrayList<>();
        }
        associationClassNames.add(className);
    }

    public void addFollowClassName(ClassName className) {
        if (followClassNames == null) {
            followClassNames = new ArrayList<>();
        }
        followClassNames.add(className);
    }
}
