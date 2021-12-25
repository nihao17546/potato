package cn.thecover.potato.controller;

import cn.thecover.potato.exception.HandlerException;
import cn.thecover.potato.model.param.MetaStorageParam;
import cn.thecover.potato.model.vo.HttpResult;
import cn.thecover.potato.model.vo.HttpStatus;
import cn.thecover.potato.model.vo.MetaVO;
import cn.thecover.potato.service.IMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author nihao 2021/07/02
 */
@RequestMapping("/metaStorage")
public class MetaStorageController extends BaseController {
    @Autowired
    private IMetaService metaService;

    @GetMapping("/info")
    @ResponseBody
    public String info(@RequestParam Integer id) {
        MetaVO metaVO = metaService.getDbAnStorage(id);
        if (metaVO.getDbConf() == null) {
            throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "数据库未配置，请先配置数据库");
        }
        return HttpResult.success().pull(metaVO).json();
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(@RequestBody MetaStorageParam param) {
        metaService.updateStorage(param);
        return HttpResult.success().json();
    }
}
