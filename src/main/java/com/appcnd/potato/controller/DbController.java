package com.appcnd.potato.controller;

import com.appcnd.potato.meta.conf.db.Table;
import com.appcnd.potato.model.vo.HttpResult;
import com.appcnd.potato.service.IDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author nihao 2021/06/28
 */
@RequestMapping("/db")
public class DbController extends BaseController {
    @Autowired
    private IDbService dbService;

    @GetMapping("/tables")
    @ResponseBody
    public String tables() {
        return HttpResult.success().pull("list", dbService.getAllTables()).json();
    }

    @GetMapping("/descTable")
    @ResponseBody
    public String descTable(String table) {
        Table result = dbService.descTable(table);
        return HttpResult.success().pull(result).json();
    }

}
