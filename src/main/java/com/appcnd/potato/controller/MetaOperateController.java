package com.appcnd.potato.controller;

import com.appcnd.potato.exception.ExceptionAssert;
import com.appcnd.potato.meta.conf.db.Column;
import com.appcnd.potato.meta.conf.db.DbConf;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.form.operate.OperateForm;
import com.appcnd.potato.meta.conf.form.operate.Unique;
import com.appcnd.potato.meta.conf.form.operate.elements.OperateElement;
import com.appcnd.potato.model.param.MetaOperateParam;
import com.appcnd.potato.model.vo.HttpResult;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.model.vo.MetaVO;
import com.appcnd.potato.service.IMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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
        ExceptionAssert.isNull(metaVO.getDbConf()).throwException(HttpStatus.SYSTEM_ERROR.getCode(), "数据库未配置，请先配置数据库");
        check(metaVO.getDbConf(), metaVO.getOperate());
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

    private void check(DbConf dbConf, OperateForm operateForm) {
        if (operateForm == null) {
            return;
        }
        Set<String> mainColumns = dbConf.getTable().getColumns().stream().map(Column::getField).collect(Collectors.toSet());
        if (operateForm.getElements() != null) {
            Iterator<OperateElement> iterator = operateForm.getElements().iterator();
            while (iterator.hasNext()) {
                OperateElement element = iterator.next();
                String field = element.getColumn().getField();
                if (!mainColumns.contains(field)) {
                    iterator.remove();
                    continue;
                }
            }
        }
        if (operateForm.getUniques() != null) {
            Iterator<Unique> iterator = operateForm.getUniques().iterator();
            while (iterator.hasNext()) {
                Unique unique = iterator.next();
                for (String column : unique.getColumns()) {
                    if (!mainColumns.contains(column)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        if (dbConf.getFollowTables() == null || dbConf.getFollowTables().isEmpty()) {
            if (operateForm.getFollows() != null) {
                operateForm.getFollows().clear();
            }
        } else {
            if (operateForm.getFollows() == null || operateForm.getFollows().isEmpty()) {
                List<OperateForm> list = new ArrayList<>();
                for (FollowTable followTable : dbConf.getFollowTables()) {
                    OperateForm of = new OperateForm();
                    of.setElements(new ArrayList<>(0));
                    of.setUniques(new ArrayList<>(0));
                    list.add(of);
                }
                operateForm.setFollows(list);
            } else {
                if (operateForm.getFollows().size() > dbConf.getFollowTables().size()) {
                    operateForm.setFollows(operateForm.getFollows().subList(0, dbConf.getFollowTables().size()));
                } else if (operateForm.getFollows().size() < dbConf.getFollowTables().size()) {
                    int size = dbConf.getFollowTables().size() - operateForm.getFollows().size();
                    for (int i = 0; i < size; i ++) {
                        OperateForm of = new OperateForm();
                        of.setElements(new ArrayList<>(0));
                        of.setUniques(new ArrayList<>(0));
                        operateForm.getFollows().add(of);
                    }
                }
                for (int i = 0; i < dbConf.getFollowTables().size(); i ++) {
                    FollowTable followTable = dbConf.getFollowTables().get(i);
                    Set<String> dbColumns = followTable.getColumns().stream().map(Column::getField).collect(Collectors.toSet());

                    OperateForm followForm = operateForm.getFollows().get(i);
                    if (followForm.getElements() != null && !followForm.getElements().isEmpty()) {
                        Iterator<OperateElement> iterator = followForm.getElements().iterator();
                        while (iterator.hasNext()) {
                            OperateElement element = iterator.next();
                            String field = element.getColumn().getField();
                            if (!dbColumns.contains(field)) {
                                iterator.remove();
                                continue;
                            }
                        }
                    }
                    if (operateForm.getUniques() != null) {
                        Iterator<Unique> iterator = operateForm.getUniques().iterator();
                        while (iterator.hasNext()) {
                            Unique unique = iterator.next();
                            for (String column : unique.getColumns()) {
                                if (!dbColumns.contains(column)) {
                                    iterator.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody MetaOperateParam param) {
        metaService.updateOperate(param);
        return HttpResult.success().json();
    }
}
