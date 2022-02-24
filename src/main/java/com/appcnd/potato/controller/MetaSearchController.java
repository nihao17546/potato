package com.appcnd.potato.controller;

import com.appcnd.potato.exception.ExceptionAssert;
import com.appcnd.potato.meta.conf.db.Column;
import com.appcnd.potato.meta.conf.db.DbConf;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.form.search.SearchForm;
import com.appcnd.potato.meta.conf.form.search.element.RemoteSelectSearchElement;
import com.appcnd.potato.meta.conf.form.search.element.SearchElement;
import com.appcnd.potato.meta.conf.table.UIColumn;
import com.appcnd.potato.meta.trans.DbTransfer;
import com.appcnd.potato.model.param.MetaSearchParam;
import com.appcnd.potato.model.vo.HttpResult;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.model.vo.MetaVO;
import com.appcnd.potato.service.IMetaService;
import com.appcnd.potato.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nihao 2021/07/03
 */
@RequestMapping("/metaSearch")
public class MetaSearchController {
    @Autowired
    private IMetaService metaService;

    @GetMapping("/info")
    @ResponseBody
    public String info(@RequestParam Integer id) {
        MetaVO metaVO = metaService.getDbAndSearch(id);
        ExceptionAssert.isNull(metaVO.getDbConf()).throwException(HttpStatus.SYSTEM_ERROR.getCode(), "数据库未配置，请先配置数据库");
        check(metaVO.getDbConf(), metaVO.getSearch());
        SearchForm searchForm = metaVO.getSearch();
        if (searchForm == null) {
            searchForm = new SearchForm();
        }
        List<UIColumn> mainDbColumns = DbTransfer.getMainUIColumns(metaVO.getDbConf());
        List<Map<String,List<UIColumn>>> followTableColumns = new ArrayList<>();
        if (metaVO.getDbConf().getFollowTables() != null && !metaVO.getDbConf().getFollowTables().isEmpty()) {
            boolean b = false;
            if (searchForm.getFollows() == null) {
                b = true;
                searchForm.setFollows(new ArrayList<>(metaVO.getDbConf().getFollowTables().size()));
            }
            for (FollowTable followTable : metaVO.getDbConf().getFollowTables()) {
                Map<String,List<UIColumn>> map = new HashMap<>(2);
                List<UIColumn> followDbColumns = DbTransfer.getFollowUIColumns(followTable);
                map.put("dbColumns", followDbColumns);
                followTableColumns.add(map);
                if (b) {
                    searchForm.getFollows().add(new SearchForm());
                }
            }
        }
        return HttpResult.success().pull("search", searchForm)
                .pull("mainDbColumns", mainDbColumns)
                .pull("version", metaVO.getVersion())
                .pull("followTableColumns",followTableColumns).json();
    }

    private void check(DbConf dbConf, SearchForm searchForm) {
        if (searchForm == null) {
            return;
        }
        Map<String,Set<String>> mainTableMap = new HashMap<>();
        mainTableMap.put(dbConf.getTable().getName(), dbConf.getTable().getColumns().stream().map(Column::getField).collect(Collectors.toSet()));
        if (dbConf.getAssociationTables() != null) {
            for (FollowTable followTable : dbConf.getAssociationTables()) {
                mainTableMap.put(followTable.getName(), followTable.getColumns().stream().map(Column::getField).collect(Collectors.toSet()));
            }
        }
        if (searchForm.getElements() != null && !searchForm.getElements().isEmpty()) {
            Iterator<SearchElement> iterator = searchForm.getElements().iterator();
            while (iterator.hasNext()) {
                SearchElement element = iterator.next();
                String table = element.getColumn().getTable();
                Set<String> columns = mainTableMap.get(table);
                if (columns == null) {
                    iterator.remove();
                    continue;
                }
                String field = element.getColumn().getColumn();
                if (!columns.contains(field)) {
                    iterator.remove();
                    continue;
                }
            }
        }
        if (dbConf.getFollowTables() == null || dbConf.getFollowTables().isEmpty()) {
            if (searchForm.getFollows() != null) {
                searchForm.getFollows().clear();
            }
        } else {
            if (searchForm.getFollows() == null || searchForm.getFollows().isEmpty()) {
                List<SearchForm> list = new ArrayList<>();
                for (FollowTable followTable : dbConf.getFollowTables()) {
                    SearchForm sf = new SearchForm();
                    sf.setElements(new ArrayList<>(0));
                    list.add(sf);
                }
                searchForm.setFollows(list);
            } else {
                if (searchForm.getFollows().size() > dbConf.getFollowTables().size()) {
                    searchForm.setFollows(searchForm.getFollows().subList(0, dbConf.getFollowTables().size()));
                } else if (searchForm.getFollows().size() < dbConf.getFollowTables().size()) {
                    int size = dbConf.getFollowTables().size() - searchForm.getFollows().size();
                    for (int i = 0; i < size; i ++) {
                        SearchForm sf = new SearchForm();
                        sf.setElements(new ArrayList<>(0));
                        searchForm.getFollows().add(sf);
                    }
                }
                for (int i = 0; i < dbConf.getFollowTables().size(); i ++) {
                    SearchForm sf = searchForm.getFollows().get(i);
                    if (sf.getElements() == null || sf.getElements().isEmpty()) {
                        continue;
                    }
                    FollowTable followTable = dbConf.getFollowTables().get(i);
                    Set<String> dbColumns = followTable.getColumns().stream().map(Column::getField).collect(Collectors.toSet());
                    Iterator<SearchElement> iterator = sf.getElements().iterator();
                    while (iterator.hasNext()) {
                        SearchElement element = iterator.next();
                        String table = element.getColumn().getTable();
                        if (!table.equalsIgnoreCase(followTable.getName())) {
                            iterator.remove();
                            continue;
                        }
                        String field = element.getColumn().getColumn();
                        if (!dbColumns.contains(field)) {
                            iterator.remove();
                            continue;
                        }
                    }
                }
            }
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody MetaSearchParam param) {
        if (param.getConfig().getElements() != null) {
            for (SearchElement element : param.getConfig().getElements()) {
                if (element instanceof RemoteSelectSearchElement) {
                    RemoteSelectSearchElement remoteSelectSearchElement = (RemoteSelectSearchElement) element;
                    CommonUtil.checkSelectSql(remoteSelectSearchElement.getSql());
                }
            }
        }
        metaService.updateSearch(param);
        return HttpResult.success().json();
    }

}
