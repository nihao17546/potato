package com.appcnd.potato.controller;

import com.appcnd.potato.exception.HandlerException;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.form.search.SearchForm;
import com.appcnd.potato.meta.conf.table.UIColumn;
import com.appcnd.potato.meta.trans.DbTransfer;
import com.appcnd.potato.model.param.MetaSearchParam;
import com.appcnd.potato.model.vo.HttpResult;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.model.vo.MetaVO;
import com.appcnd.potato.service.IMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nihao 2021/07/03
 */
@RequestMapping("/metaSearch")
public class MetaSearchController extends BaseController {
    @Autowired
    private IMetaService metaService;

    @GetMapping("/info")
    @ResponseBody
    public String info(@RequestParam Integer id) {
        MetaVO metaVO = metaService.getDbAndSearch(id);
        if (metaVO.getDbConf() == null) {
            throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "数据库未配置，请先配置数据库");
        }
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

    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody MetaSearchParam param) {
        metaService.updateSearch(param);
        return HttpResult.success().json();
    }

}