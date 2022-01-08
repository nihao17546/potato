package com.appcnd.potato.service.impl;

import com.appcnd.potato.dao.MetaDao;
import com.appcnd.potato.exception.HandlerException;
import com.appcnd.potato.generate.boot.BootResult;
import com.appcnd.potato.generate.boot.GenerateBoot;
import com.appcnd.potato.meta.conf.db.DbConf;
import com.appcnd.potato.meta.conf.form.operate.OperateForm;
import com.appcnd.potato.meta.conf.form.search.SearchForm;
import com.appcnd.potato.meta.conf.form.storage.HuaweiStorage;
import com.appcnd.potato.meta.conf.form.storage.QiniuStorage;
import com.appcnd.potato.meta.conf.form.storage.Storage;
import com.appcnd.potato.meta.conf.table.UIMainTable;
import com.appcnd.potato.model.po.Meta;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.model.vo.ListVO;
import com.appcnd.potato.model.vo.MetaVO;
import com.appcnd.potato.properties.CoreProperties;
import com.appcnd.potato.service.IGenerateService;
import com.appcnd.potato.service.IMetaService;
import com.appcnd.potato.util.CommonUtil;
import com.appcnd.potato.util.DesUtil;
import com.appcnd.potato.model.param.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
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
    @Autowired
    private CoreProperties coreProperties;

    private void checkName(Integer id, String name) {
        List<Meta> list = metaDao.selectByNameAndNeId(id, name);
        if (list != null && !list.isEmpty()) {
            throw new HandlerException(HttpStatus.SYSTEM_ERROR.getCode(), "配置名称已存在");
        }
    }

    @Override
    public void add(MetaParam param) {
        checkName(null, param.getName());
        Meta meta = new Meta();
        meta.setName(param.getName());
        meta.setTitle(param.getTitle());
        meta.setCreateTime(new Date());
        meta.setVersion(1);
        metaDao.insert(meta);
    }

    @Override
    public ListVO<MetaVO> getPagination(Integer page, Integer pageSize, String name, HttpServletRequest request) {
        ListVO<MetaVO> listVO = new ListVO<>(page, pageSize);
        Long count = metaDao.selectCount(name);
        listVO.setTotalCount(count);
        if (count <= 0) {
            return listVO;
        }
        List<Meta> list = metaDao.selectList(name, null, (page - 1) * pageSize, pageSize);
        List<MetaVO> voList = list.stream().map(po -> {
            MetaVO vo = transfer(po);
            BootResult bootResult = generateBoot.getLoaded(po.getId());
            if (bootResult != null) {
                if (bootResult.getVersion().equals(vo.getVersion())) {
                    vo.setUrl(request.getContextPath() + coreProperties.getPath() + bootResult.getUrl());
                } else {
                    generateService.unBoot(po.getId());
                }
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
        } else if (param instanceof MetaStorageParam) {
            MetaStorageParam metaStorageParam = (MetaStorageParam) param;
            if (metaStorageParam.getConfig() == null) {
                meta.setStorage("");
            } else {
                if (metaStorageParam.getConfig() instanceof HuaweiStorage) {
                    HuaweiStorage huaweiStorage = (HuaweiStorage) metaStorageParam.getConfig();
                    huaweiStorage.setAk(DesUtil.encrypt(huaweiStorage.getAk(), "storage"));
                    huaweiStorage.setSk(DesUtil.encrypt(huaweiStorage.getSk(), "storage"));
                } else if (metaStorageParam.getConfig() instanceof QiniuStorage) {
                    QiniuStorage qiniuStorage = (QiniuStorage) metaStorageParam.getConfig();
                    qiniuStorage.setAk(DesUtil.encrypt(qiniuStorage.getAk(), "storage"));
                    qiniuStorage.setSk(DesUtil.encrypt(qiniuStorage.getSk(), "storage"));
                }
                meta.setStorage(CommonUtil.serialize(metaStorageParam.getConfig()));
            }
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
    public void updateStorage(MetaStorageParam param) {
        update(param);
    }

    @Override
    public MetaVO getDb(Integer id) {
        Meta po = metaDao.selectColumnsById(id, Arrays.asList("id","db","version"));
        if (po == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        return transfer(po);
    }

    @Override
    public MetaVO getDbAndTable(Integer id) {
        Meta po = metaDao.selectColumnsById(id, Arrays.asList("id","db","`table`","version"));
        if (po == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        return transfer(po);
    }

    @Override
    public MetaVO getDbAndSearch(Integer id) {
        Meta po = metaDao.selectColumnsById(id, Arrays.asList("id","db","`search`","version"));
        if (po == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        return transfer(po);
    }

    @Override
    public MetaVO getDbAndOperate(Integer id) {
        Meta po = metaDao.selectColumnsById(id, Arrays.asList("id","db","`operate`","version"));
        if (po == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        return transfer(po);
    }

    @Override
    public MetaVO getDbAnStorage(Integer id) {
        Meta po = metaDao.selectColumnsById(id, Arrays.asList("id","db","`storage`","version"));
        if (po == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        return transfer(po);
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
        if (meta.getStorage() != null && !meta.getStorage().isEmpty()) {
            Storage storage = CommonUtil.unserialize(meta.getStorage(), Storage.class);
            if (storage instanceof HuaweiStorage) {
                HuaweiStorage huaweiStorage = (HuaweiStorage) storage;
                huaweiStorage.setAk(DesUtil.decrypt(huaweiStorage.getAk(), "storage"));
                huaweiStorage.setSk(DesUtil.decrypt(huaweiStorage.getSk(), "storage"));
            } else if (storage instanceof QiniuStorage) {
                QiniuStorage qiniuStorage = (QiniuStorage) storage;
                qiniuStorage.setAk(DesUtil.decrypt(qiniuStorage.getAk(), "storage"));
                qiniuStorage.setSk(DesUtil.decrypt(qiniuStorage.getSk(), "storage"));
            }
            metaVO.setStorage(storage);
        }
        return metaVO;
    }
}
