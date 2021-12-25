package cn.thecover.potato.controller;

import cn.thecover.potato.model.param.MetaDbParam;
import cn.thecover.potato.model.vo.HttpResult;
import cn.thecover.potato.model.vo.MetaVO;
import cn.thecover.potato.service.IMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        metaService.updateDb(param);
        return HttpResult.success().json();
    }

}
