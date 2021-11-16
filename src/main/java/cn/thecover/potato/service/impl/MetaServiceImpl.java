package cn.thecover.potato.service.impl;

import cn.thecover.potato.dao.MetaDao;
import cn.thecover.potato.exception.HandlerException;
import cn.thecover.potato.generate.boot.BootResult;
import cn.thecover.potato.generate.boot.GenerateBoot;
import cn.thecover.potato.meta.conf.Basic;
import cn.thecover.potato.meta.conf.Config;
import cn.thecover.potato.meta.conf.db.DbConf;
import cn.thecover.potato.meta.conf.form.operate.OperateForm;
import cn.thecover.potato.meta.conf.form.search.SearchForm;
import cn.thecover.potato.meta.conf.table.UIMainTable;
import cn.thecover.potato.model.param.*;
import cn.thecover.potato.model.po.Meta;
import cn.thecover.potato.model.vo.HttpStatus;
import cn.thecover.potato.model.vo.ListVO;
import cn.thecover.potato.model.vo.MetaVO;
import cn.thecover.potato.service.IGenerateService;
import cn.thecover.potato.service.IMetaService;
import cn.thecover.potato.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author nihao 2021/11/14
 */
@Slf4j
public class MetaServiceImpl implements IMetaService {
    @Autowired
    private MetaDao metaDao;
    @Autowired
    private GenerateBoot generateBoot;
    @Autowired
    private IGenerateService generateService;

    private void checkName(Integer id, String name) {
        List<Meta> list = metaDao.selectByNameAndNeId(id, name);
        if (list != null && !list.isEmpty()) {
            throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "配置名称已存在");
        }
    }

    @Transactional
    @Override
    public void add(MetaParam param) {
        checkName(null, param.getName());
        Meta meta = new Meta();
        meta.setName(param.getName());
        meta.setTitle(param.getTitle());
        meta.setCreateTime(new Date());
        meta.setVersion(1);
        metaDao.insert(meta);
        if (param.getName().equals("0")) {
            System.out.println(1/0);
        }
    }

    @Override
    public ListVO<MetaVO> getPagination(Integer page, Integer pageSize, String name, HttpServletRequest request) {
        ListVO<MetaVO> listVO = new ListVO<>(page, pageSize);
        Long count = metaDao.selectCount(name);
        listVO.setTotalCount(count);
        if (count <= 0) {
            return listVO;
        }
        List<Meta> list = metaDao.selectList(name, (page - 1) * pageSize, pageSize);
        List<MetaVO> voList = list.stream().map(po -> {
            MetaVO vo = transfer(po);
            BootResult bootResult = generateBoot.getLoaded(po.getId());
            if (bootResult != null) {
                vo.setUrl(request.getContextPath() + bootResult.getUrl());
                vo.setVersion(bootResult.getVersion());
            }
            return vo;
        }).collect(Collectors.toList());
        listVO.setList(voList);
        return listVO;
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        metaDao.deleteById(id);
        generateService.unBoot(id);
    }

    @Transactional
    @Override
    public void updateInfo(MetaParam param) {
        update(param);
    }

    private void update(Param param) {
        Meta meta = new Meta();
        if (param instanceof MetaParam) {
            MetaParam metaParam = (MetaParam) param;
            checkName(metaParam.getId(), metaParam.getName());
            meta.setName(metaParam.getName());
            meta.setTitle(metaParam.getTitle());
        } else if (param instanceof MetaDbParam) {
            MetaDbParam metaDbParam = (MetaDbParam) param;
            meta.setDb(CommonUtil.serialize(metaDbParam.getConfig()));
        } else if (param instanceof MetaTableParam) {
            MetaTableParam metaTableParam = (MetaTableParam) param;
            meta.setTable(CommonUtil.serialize(metaTableParam.getConfig()));
        } else if (param instanceof MetaSearchParam) {
            MetaSearchParam metaSearchParam = (MetaSearchParam) param;
            meta.setSearch(CommonUtil.serialize(metaSearchParam.getConfig()));
        } else if (param instanceof MetaOperateParam) {
            MetaOperateParam metaOperateParam = (MetaOperateParam) param;
            meta.setOperate(CommonUtil.serialize(metaOperateParam.getConfig()));
        }
        Integer version = metaDao.selectVersionById(param.getId());
        if (version == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        meta.setId(param.getId());
        meta.setVersion(version);
        meta.setUpdateTime(new Date());
        int a = metaDao.update(meta);
        if (a == 0) {
            throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "数据已被修改，请刷新后重新操作");
        }
        generateService.unBoot(param.getId());
    }

    @Transactional
    @Override
    public void updateDb(MetaDbParam param) {
        update(param);
    }

    @Transactional
    @Override
    public void updateTable(MetaTableParam param) {
        update(param);
    }

    @Transactional
    @Override
    public void updateSearch(MetaSearchParam param) {
        update(param);
    }

    @Transactional
    @Override
    public void updateOperate(MetaOperateParam param) {
        update(param);
    }

    @Override
    public MetaVO getDb(Integer id) {
        Meta po = metaDao.selectById(id);
        if (po == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        return transfer(po);
    }

    @Override
    public MetaVO getDbAndTable(Integer id) {
        Meta po = metaDao.selectById(id);
        if (po == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        return transfer(po);
    }

    @Override
    public MetaVO getDbAndSearch(Integer id) {
        Meta po = metaDao.selectById(id);
        if (po == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        return transfer(po);
    }

    @Override
    public MetaVO getDbAndOperate(Integer id) {
        Meta po = metaDao.selectById(id);
        if (po == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        return transfer(po);
    }

    @Override
    public Config getConfig(Integer id) {
        Meta po = metaDao.selectById(id);
        if (po == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        Config config = new Config();
        Basic basic = new Basic();
        basic.setTitle(po.getTitle());
        basic.setName(po.getName());
        basic.setVersion(po.getVersion());
        config.setBasic(basic);
        if (po.getTable() != null) {
            config.setTable(CommonUtil.unserialize(po.getTable(), UIMainTable.class));
        }
        if (po.getDb() != null) {
            config.setDbConf(CommonUtil.unserialize(po.getDb(), DbConf.class));
        }
        if (po.getSearch() != null) {
            config.setSearchForm(CommonUtil.unserialize(po.getSearch(), SearchForm.class));
        }
        if (po.getOperate() != null) {
            config.setOperateForm(CommonUtil.unserialize(po.getOperate(), OperateForm.class));
        }
        return config;
    }

    private MetaVO transfer(Meta meta) {
        MetaVO metaVO = new MetaVO();
        metaVO.setId(meta.getId());
        metaVO.setName(meta.getName());
        metaVO.setTitle(meta.getTitle());
        metaVO.setVersion(meta.getVersion());
        metaVO.setCreateTime(meta.getCreateTime());
        metaVO.setUpdateTime(meta.getUpdateTime());
        if (meta.getDb() != null) {
            metaVO.setDbConf(CommonUtil.unserialize(meta.getDb(), DbConf.class));
        }
        if (meta.getTable() != null) {
            metaVO.setTable(CommonUtil.unserialize(meta.getTable(), UIMainTable.class));
        }
        if (meta.getSearch() != null) {
            metaVO.setSearch(CommonUtil.unserialize(meta.getSearch(), SearchForm.class));
        }
        if (meta.getOperate() != null) {
            metaVO.setOperate(CommonUtil.unserialize(meta.getOperate(), OperateForm.class));
        }
        return metaVO;
    }
}
