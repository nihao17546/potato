package cn.thecover.potato.service;

import cn.thecover.potato.generate.boot.BootResult;
import cn.thecover.potato.meta.conf.Config;
import cn.thecover.potato.model.param.GenerateParam;

import java.util.Map;

/**
 * @author nihao 2021/07/08
 */
public interface IGenerateService {
    Map<String,String> generate(GenerateParam param, Config config);
    void boot(Integer id, Config config) throws Exception;
    void unBoot(Integer id);
}
