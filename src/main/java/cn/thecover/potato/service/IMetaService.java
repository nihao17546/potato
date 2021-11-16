package cn.thecover.potato.service;

import cn.thecover.potato.meta.conf.Config;
import cn.thecover.potato.model.param.*;
import cn.thecover.potato.model.vo.ListVO;
import cn.thecover.potato.model.vo.MetaVO;

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
    MetaVO getDb(Integer id);
    MetaVO getDbAndTable(Integer id);
    MetaVO getDbAndSearch(Integer id);
    MetaVO getDbAndOperate(Integer id);
    Config getConfig(Integer id);
}
