package com.appcnd.potato.controller;

import com.appcnd.potato.exception.HandlerException;
import com.appcnd.potato.meta.conf.db.Column;
import com.appcnd.potato.meta.conf.db.DbConf;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.table.*;
import com.appcnd.potato.meta.trans.DbTransfer;
import com.appcnd.potato.model.param.MetaTableParam;
import com.appcnd.potato.model.vo.HttpResult;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.model.vo.MetaVO;
import com.appcnd.potato.service.IMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nihao 2021/07/02
 */
@RequestMapping("/metaTable")
public class MetaTableController {
    @Autowired
    private IMetaService metaService;

    @GetMapping("/info")
    @ResponseBody
    public String info(@RequestParam Integer id) {
        MetaVO metaVO = metaService.getDbAndTable(id);
        if (metaVO.getDbConf() == null) {
            throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "数据库未配置，请先配置数据库");
        }
        check(metaVO.getDbConf(), metaVO.getTable());
        HttpResult result = HttpResult.success().pull("version", metaVO.getVersion());
        List<UIColumn> uiColumns = DbTransfer.getMainUIColumns(metaVO.getDbConf());
        result.pull("mainTableColumns", uiColumns);
        result.pull("mainDbColumns", new ArrayList<>(uiColumns));
        UIMainTable uiMainTable =  metaVO.getTable();
        result.pull("table", uiMainTable);
        // 排除已选择的
        if (uiMainTable != null) {
            if (uiMainTable.getColumns() != null) {
                uiColumns.removeAll(uiMainTable.getColumns());
            }
        }
        if (metaVO.getDbConf().getFollowTables() != null && !metaVO.getDbConf().getFollowTables().isEmpty()) {
            List<Map<String,List<UIColumn>>> ff = new ArrayList<>();
            for (FollowTable followTable : metaVO.getDbConf().getFollowTables()) {
                Map<String,List<UIColumn>> map = new HashMap<>(2);
                List<UIColumn> followUiColumns = DbTransfer.getFollowUIColumns(followTable);
                List<UIColumn> dbColumns = new ArrayList<>(followUiColumns);
                map.put("followUiColumns", followUiColumns);
                map.put("dbColumns", dbColumns);
                // 排除已选择的
                if (uiMainTable != null) {
                    if (uiMainTable.getFollows() != null) {
                        for (UITable uiTable : uiMainTable.getFollows()) {
                            if (uiTable.getColumns() != null && !uiTable.getColumns().isEmpty()) {
                                followUiColumns.removeAll(uiTable.getColumns());
                            }
                        }
                    }
                }
                ff.add(map);
            }
            result.pull("followTableColumns", ff);
        }
        return result.json();
    }

    private void check(DbConf dbConf, UIMainTable uiMainTable) {
        if (uiMainTable == null) {
            return;
        }
        Map<String,Set<String>> mainTableMap = new HashMap<>();
        mainTableMap.put(dbConf.getTable().getName(), dbConf.getTable().getColumns().stream().map(Column::getField).collect(Collectors.toSet()));
        if (dbConf.getAssociationTables() != null) {
            for (FollowTable followTable : dbConf.getAssociationTables()) {
                mainTableMap.put(followTable.getName(), followTable.getColumns().stream().map(Column::getField).collect(Collectors.toSet()));
            }
        }
        if (uiMainTable.getColumns() != null) {
            Iterator<UIColumn> iterator = uiMainTable.getColumns().iterator();
            while (iterator.hasNext()) {
                UIColumn uiColumn = iterator.next();
                String table = uiColumn.getTable();
                Set<String> columns = mainTableMap.get(table);
                if (columns == null) {
                    iterator.remove();
                    continue;
                }
                String field = uiColumn.getColumn().getField();
                if (!columns.contains(field)) {
                    iterator.remove();
                    continue;
                }
            }
        }
        if (uiMainTable.getSorts() != null) {
            Iterator<Sort> iterator = uiMainTable.getSorts().iterator();
            while (iterator.hasNext()) {
                Sort sort = iterator.next();
                Set<String> columns = mainTableMap.get(sort.getTable());
                if (columns == null) {
                    iterator.remove();
                    continue;
                }
                if (!columns.contains(sort.getColumn())) {
                    iterator.remove();
                    continue;
                }
            }
        }

        if (dbConf.getFollowTables() == null || dbConf.getFollowTables().isEmpty()) {
            if (uiMainTable.getFollows() != null) {
                uiMainTable.getFollows().clear();
            }
        } else {
            if (uiMainTable.getFollows() == null || uiMainTable.getFollows().isEmpty()) {
                List<UIFollowTable> list = new ArrayList<>();
                for (FollowTable followTable : dbConf.getFollowTables()) {
                    list.add(new UIFollowTable());
                }
                uiMainTable.setFollows(list);
            } else {
                if (uiMainTable.getFollows().size() > dbConf.getFollowTables().size()) {
                    uiMainTable.setFollows(uiMainTable.getFollows().subList(0, dbConf.getFollowTables().size()));
                } else if (uiMainTable.getFollows().size() < dbConf.getFollowTables().size()) {
                    int size = dbConf.getFollowTables().size() - uiMainTable.getFollows().size();
                    for (int i = 0; i < size; i ++) {
                        uiMainTable.getFollows().add(new UIFollowTable());
                    }
                }
                for (int i = 0; i < dbConf.getFollowTables().size(); i ++) {
                    FollowTable followTable = dbConf.getFollowTables().get(i);
                    Set<String> dbColumns = followTable.getColumns().stream().map(Column::getField).collect(Collectors.toSet());

                    UIFollowTable uiFollowTable = uiMainTable.getFollows().get(i);
                    if (uiFollowTable.getColumns() != null && !uiFollowTable.getColumns().isEmpty()) {
                        Iterator<UIColumn> iterator = uiFollowTable.getColumns().iterator();
                        while (iterator.hasNext()) {
                            UIColumn uiColumn = iterator.next();
                            String table = uiColumn.getTable();
                            if (!table.equalsIgnoreCase(followTable.getName())) {
                                iterator.remove();
                                continue;
                            }
                            String field = uiColumn.getColumn().getField();
                            if (!dbColumns.contains(field)) {
                                iterator.remove();
                                continue;
                            }
                        }
                    }
                    if (uiFollowTable.getSorts() != null) {
                        Iterator<Sort> iterator = uiFollowTable.getSorts().iterator();
                        while (iterator.hasNext()) {
                            Sort sort = iterator.next();
                            if (!sort.getTable().equalsIgnoreCase(followTable.getName())) {
                                iterator.remove();
                                continue;
                            }
                            if (!dbColumns.contains(sort.getColumn())) {
                                iterator.remove();
                                continue;
                            }
                        }
                    }
                }
            }
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody MetaTableParam param) {
        // 校验页面路由
        Set<String> set = new HashSet<>();
        if (param.getConfig().getUri() != null && !param.getConfig().getUri().isEmpty()) {
            set.add(param.getConfig().getUri());
        }
        if (param.getConfig().getFollows() != null) {
            for (UIFollowTable followTable : param.getConfig().getFollows()) {
                if (followTable.getUri() != null && !followTable.getUri().isEmpty()) {
                    if (set.contains(followTable.getUri())) {
                        return HttpResult.fail("路由 " + followTable.getUri() + " 重复，请重新设置").json();
                    }
                    set.add(followTable.getUri());
                }
            }
        }
        metaService.updateTable(param);
        return HttpResult.success().json();
    }
}
