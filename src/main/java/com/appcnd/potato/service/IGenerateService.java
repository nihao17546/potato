package com.appcnd.potato.service;

import com.appcnd.potato.meta.conf.Config;
import com.appcnd.potato.model.param.GenerateParam;

import java.util.Map;

/**
 * @author nihao 2021/07/08
 */
public interface IGenerateService {
    Map<String,String> generate(GenerateParam param, Config config);
    void boot(Integer id, Integer version) throws Exception;
    void unBoot(Integer id);
    void unBoot(Integer id, Integer version);
    Config getConfig(Integer id);
}
