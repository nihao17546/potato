package cn.thecover.potato.service;

import cn.thecover.potato.meta.conf.db.Table;

import java.util.List;

/**
 * @author nihao 2021/11/23
 */
public interface IDbService {
    List<String> getAllTables();
    Table descTable(String table);
}
