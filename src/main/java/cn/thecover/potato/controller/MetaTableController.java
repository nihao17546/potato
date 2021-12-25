package cn.thecover.potato.controller;

import cn.thecover.potato.exception.HandlerException;
import cn.thecover.potato.meta.conf.db.FollowTable;
import cn.thecover.potato.meta.conf.table.UIColumn;
import cn.thecover.potato.meta.conf.table.UIMainTable;
import cn.thecover.potato.meta.conf.table.UITable;
import cn.thecover.potato.meta.trans.DbTransfer;
import cn.thecover.potato.model.param.MetaTableParam;
import cn.thecover.potato.model.vo.HttpResult;
import cn.thecover.potato.model.vo.HttpStatus;
import cn.thecover.potato.model.vo.MetaVO;
import cn.thecover.potato.service.IMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nihao 2021/07/02
 */
@RequestMapping("/metaTable")
public class MetaTableController extends BaseController {
    @Autowired
    private IMetaService metaService;

    @GetMapping("/info")
    @ResponseBody
    public String info(@RequestParam Integer id) {
        MetaVO metaVO = metaService.getDbAndTable(id);
        if (metaVO.getDbConf() == null) {
            throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "数据库未配置，请先配置数据库");
        }
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
                            followUiColumns.removeAll(uiTable.getColumns());
                        }
                    }
                }
                ff.add(map);
            }
            result.pull("followTableColumns", ff);
        }
        return result.json();
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody MetaTableParam param) {
        metaService.updateTable(param);
        return HttpResult.success().json();
    }
}
