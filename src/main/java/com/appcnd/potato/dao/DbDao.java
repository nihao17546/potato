package com.appcnd.potato.dao;

import com.appcnd.potato.meta.db.TableFieldInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author nihao 2021/11/23
 */
public interface DbDao {
    List<String> selectTables();
    Map<String,Object> descTable(@Param("table") String table);
    List<TableFieldInfo> getTableFieldInfo(@Param("table") String table);
}
