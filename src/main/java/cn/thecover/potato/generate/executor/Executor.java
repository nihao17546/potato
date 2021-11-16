package cn.thecover.potato.generate.executor;

import cn.thecover.potato.meta.conf.db.Column;
import cn.thecover.potato.meta.conf.db.FollowTable;
import cn.thecover.potato.meta.conf.db.Table;
import cn.thecover.potato.util.CommonUtil;
import cn.thecover.potato.util.FieldUtil;
import org.apache.commons.text.StringSubstitutor;

import java.util.*;

/**
 * @author nihao 2021/07/12
 */
public abstract class Executor {
    /**
     * key:文件路径 map:key-value
     * @return
     */
    protected abstract Map<String,Map<String,String>> analysis();
    protected abstract String getFile();

    /**
     * key:文件路径 value:内容
     * @return
     */
    public Map<String,String> compile() {
        String content = CommonUtil.readFromResource(getFile());
        Map<String,Map<String,String>> map = analysis();
        Set<Map.Entry<String, Map<String,String>>> entries = map.entrySet();
        Map<String,String> result = new HashMap<>(map.size());
        for (Map.Entry<String, Map<String,String>> entry : entries) {
            String bytes = new StringSubstitutor(entry.getValue()).replace(content);
            result.put(entry.getKey(), bytes);
        }
        return result;
    }

    protected Map<String,String> getFieldMap(Table mainTable, List<FollowTable> associationTables) {
        Map<String,String> map = new HashMap<>();
        Set<String> hasAdd = new HashSet<>();
        for (Column column : mainTable.getColumns()) {
            String key = FieldUtil.getField(mainTable.getName(), column.getField());
            map.put(key, column.getField());
            hasAdd.add(column.getField());
        }
        if (associationTables != null && !associationTables.isEmpty()) {
            for (FollowTable followTable : associationTables) {
                for (Column column : followTable.getColumns()) {
                    String key = FieldUtil.getField(followTable.getName(), column.getField());
                    if (hasAdd.contains(column.getField())) {
                        map.put(key, key);
                    } else {
                        map.put(key, column.getField());
                        hasAdd.add(column.getField());
                    }
                }
            }
        }
        return map;
    }
}
