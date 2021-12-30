package com.appcnd.potato.service.impl;

import com.appcnd.potato.dao.DbDao;
import com.appcnd.potato.meta.conf.db.Table;
import com.appcnd.potato.meta.db.TableFieldInfo;
import com.appcnd.potato.meta.trans.DbTransfer;
import com.appcnd.potato.service.IDbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * @author nihao 2021/11/23
 */
@Slf4j
public class DbServiceImpl implements IDbService {
    @Autowired
    private DbDao dbDao;

    @Override
    public List<String> getAllTables() {
        List<String> list = dbDao.selectTables();
        list.remove("meta");
        list.remove("boot");
        return list;
    }

    @Override
    public Table descTable(String table) {
        Map<String,Object> map = dbDao.descTable(table);
        List<TableFieldInfo> list = dbDao.getTableFieldInfo(table);
        return DbTransfer.transfer(list, (String) map.get("Create Table"));
    }
}
