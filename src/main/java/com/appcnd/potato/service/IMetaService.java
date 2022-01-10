package com.appcnd.potato.service;

import com.appcnd.potato.meta.conf.Config;
import com.appcnd.potato.model.vo.ListVO;
import com.appcnd.potato.model.vo.MetaVO;
import com.appcnd.potato.model.param.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author nihao 2021/06/27
 */
public interface IMetaService {
    void add(MetaParam param);
    ListVO<MetaVO> getPagination(Integer page, Integer pageSize, String name, HttpServletRequest request);
    void delete(Integer id);
    void updateInfo(MetaParam param);
    void updateDb(MetaDbParam param);
    void updateTable(MetaTableParam param);
    void updateSearch(MetaSearchParam param);
    void updateOperate(MetaOperateParam param);
    void updateStorage(MetaStorageParam param);
    MetaVO getDb(Integer id);
    MetaVO getDbAndTable(Integer id);
    MetaVO getDbAndSearch(Integer id);
    MetaVO getDbAndOperate(Integer id);
    MetaVO getDbAnStorage(Integer id);
}
