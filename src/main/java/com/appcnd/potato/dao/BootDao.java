package com.appcnd.potato.dao;

import com.appcnd.potato.model.po.Boot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author nihao 2021/11/14
 */
public interface BootDao {

    Boot selectByMetaIdAndVersion(@Param("metaId") Integer metaId, @Param("version") Integer version);
    List<Boot> selectListByMetaId(@Param("metaId") Integer metaId);
    int deleteByMetaId(@Param("metaId") Integer metaId);
    int insert(Boot boot);

}
