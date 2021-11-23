package cn.thecover.potato.service.impl;

import cn.thecover.potato.dao.DbDao;
import cn.thecover.potato.meta.conf.db.Table;
import cn.thecover.potato.meta.db.TableFieldInfo;
import cn.thecover.potato.meta.trans.DbTransfer;
import cn.thecover.potato.service.IDbService;
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
        return dbDao.selectTables();
    }

    @Override
    public Table descTable(String table) {
        Map<String,Object> map = dbDao.descTable(table);
        List<TableFieldInfo> list = dbDao.getTableFieldInfo(table);
        return DbTransfer.transfer(list, (String) map.get("Create Table"));
    }
}
