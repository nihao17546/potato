package com.appcnd.potato.controller;

import com.appcnd.potato.exception.HandlerException;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.form.operate.OperateForm;
import com.appcnd.potato.model.param.MetaOperateParam;
import com.appcnd.potato.model.vo.HttpResult;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.model.vo.MetaVO;
import com.appcnd.potato.service.IMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * @author nihao 2021/08/26
 */
@RequestMapping("/metaOperate")
public class MetaOperateController {
    @Autowired
    private IMetaService metaService;

    @GetMapping("/info")
    @ResponseBody
    public String info(@RequestParam Integer id) {
        MetaVO metaVO = metaService.getDbAndOperate(id);
        if (metaVO.getDbConf() == null) {
            throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "数据库未配置，请先配置数据库");
        }
        OperateForm operateForm = metaVO.getOperate();
        if (operateForm == null) {
            operateForm = new OperateForm();
            operateForm.setElements(new ArrayList<>());
            if (metaVO.getDbConf().getFollowTables() != null) {
                operateForm.setFollows(new ArrayList<>(metaVO.getDbConf().getFollowTables().size()));
                for (FollowTable followTable : metaVO.getDbConf().getFollowTables()) {
                    OperateForm of = new OperateForm();
                    of.setElements(new ArrayList<>());
                    operateForm.getFollows().add(of);
                }
            }
        }

        return HttpResult.success().pull("operate", operateForm)
                .pull("version", metaVO.getVersion())
                .pull("db", metaVO.getDbConf()).json();
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody MetaOperateParam param) {
        metaService.updateOperate(param);
        return HttpResult.success().json();
    }
}
