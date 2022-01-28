package com.appcnd.potato.controller;

import com.appcnd.potato.exception.HandlerException;
import com.appcnd.potato.meta.conf.api.ApiConf;
import com.appcnd.potato.meta.conf.db.DbConf;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.model.param.MetaApiParam;
import com.appcnd.potato.model.vo.HttpResult;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.model.vo.MetaVO;
import com.appcnd.potato.service.IMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * created by nihao 2022/01/27
 */
@RequestMapping("/metaApi")
public class MetaApiController {
    @Autowired
    private IMetaService metaService;

    @GetMapping("/info")
    @ResponseBody
    public String info(@RequestParam Integer id) {
        MetaVO metaVO = metaService.getDbAndApi(id);
        if (metaVO.getDbConf() == null) {
            throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "数据库未配置，请先配置数据库");
        }
        check(metaVO.getDbConf(), metaVO.getApi());
        ApiConf apiConf = metaVO.getApi();
        if (apiConf == null) {
            apiConf = new ApiConf();
            if (metaVO.getDbConf().getFollowTables() != null) {
                apiConf.setFollows(new ArrayList<>(metaVO.getDbConf().getFollowTables().size()));
                for (FollowTable followTable : metaVO.getDbConf().getFollowTables()) {
                    ApiConf of = new ApiConf();
                    apiConf.getFollows().add(of);
                }
            }
        }
        return HttpResult.success().pull("api", apiConf)
                .pull("version", metaVO.getVersion())
                .pull("db", metaVO.getDbConf()).json();
    }

    private void check(DbConf dbConf, ApiConf apiConf) {
        if (apiConf == null) {
            return;
        }
        if (dbConf.getFollowTables() == null || dbConf.getFollowTables().isEmpty()) {
            if (apiConf.getFollows() != null) {
                apiConf.getFollows().clear();
            }
        } else {
            if (apiConf.getFollows().size() > dbConf.getFollowTables().size()) {
                apiConf.setFollows(apiConf.getFollows().subList(0, dbConf.getFollowTables().size()));
            } else if (apiConf.getFollows().size() < dbConf.getFollowTables().size()) {
                int size = dbConf.getFollowTables().size() - apiConf.getFollows().size();
                for (int i = 0; i < size; i ++) {
                    ApiConf of = new ApiConf();
                    apiConf.getFollows().add(of);
                }
            }
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody MetaApiParam param) {
        Set<String> set = new HashSet<>();
        if (!StringUtils.isEmpty(param.getConfig().getUri())) {
            set.add(param.getConfig().getUri());
        }
        if (!StringUtils.isEmpty(param.getConfig().getApiPrefix())) {
            if (set.contains(param.getConfig().getApiPrefix())) {
                return HttpResult.fail(param.getConfig().getApiPrefix() + "重复，请重新填写").json();
            }
            set.add(param.getConfig().getApiPrefix());
        }
        if (!CollectionUtils.isEmpty(param.getConfig().getFollows())) {
            for (ApiConf apiConf : param.getConfig().getFollows()) {
                if (!StringUtils.isEmpty(apiConf.getUri())) {
                    if (set.contains(apiConf.getUri())) {
                        return HttpResult.fail(apiConf.getUri() + "重复，请重新填写").json();
                    }
                    set.add(apiConf.getUri());
                }
                if (!StringUtils.isEmpty(apiConf.getApiPrefix())) {
                    if (set.contains(apiConf.getApiPrefix())) {
                        return HttpResult.fail(apiConf.getApiPrefix() + "重复，请重新填写").json();
                    }
                    set.add(apiConf.getApiPrefix());
                }
            }
        }
        metaService.updateApi(param);
        return HttpResult.success().json();
    }
}
