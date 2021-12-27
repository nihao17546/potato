package cn.thecover.potato.controller;

import cn.thecover.potato.exception.HandlerException;
import cn.thecover.potato.meta.conf.db.FollowTable;
import cn.thecover.potato.model.param.MetaDbParam;
import cn.thecover.potato.model.vo.HttpResult;
import cn.thecover.potato.model.vo.HttpStatus;
import cn.thecover.potato.model.vo.MetaVO;
import cn.thecover.potato.service.IMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author nihao 2021/07/03
 */
@RequestMapping("/metaDb")
public class MetaDbController extends BaseController {
    @Autowired
    private IMetaService metaService;

    @GetMapping("/info")
    @ResponseBody
    public String info(@RequestParam Integer id) {
        MetaVO metaVO = metaService.getDb(id);
        return HttpResult.success().pull(metaVO).json();
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody MetaDbParam param) {
        Set<String> tables = new HashSet<>();
        if (param.getConfig() != null) {
            if (param.getConfig().getTable() != null) {
                checkTable(tables, param.getConfig().getTable().getName());
            }
            if (param.getConfig().getAssociationTables() != null) {
                for (FollowTable followTable : param.getConfig().getAssociationTables()) {
                    checkTable(tables, followTable.getName());
                }
            }
            if (param.getConfig().getFollowTables() != null) {
                for (FollowTable followTable : param.getConfig().getFollowTables()) {
                    checkTable(tables, followTable.getName());
                }
            }
        }
        metaService.updateDb(param);
        return HttpResult.success().json();
    }

    private void checkTable(Set<String> tables, String table) {
        if (tables.contains(table)) {
            throw new HandlerException(HttpStatus.PARAM_ERROR);
        }
        tables.add(table);
    }

}
