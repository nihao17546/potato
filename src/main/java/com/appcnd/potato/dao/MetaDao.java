package com.appcnd.potato.dao;


import com.appcnd.potato.model.po.Meta;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author nihao 2021/11/14
 */
public interface MetaDao {

    List<Meta> selectByNameAndNeId(@Param("id") Integer id,
                                   @Param("name") String name);
    int insert(Meta meta);
    Long selectCount(@Param("name") String name);
    List<Meta> selectList(@Param("name") String name,
                          @Param("loaded") Boolean loaded,
                          @Param("offset") Integer offset,
                          @Param("pageSize") Integer pageSize);
    int deleteById(@Param("id") Integer id);
    Integer selectVersionById(@Param("id") Integer id);
    int update(Meta meta);
    Meta selectById(@Param("id") Integer id);
    Meta selectColumnsById(@Param("id") Integer id,
                           @Param("columns") List<String> columns);
    int updateLoaded(@Param("id") Integer id,
                     @Param("version") Integer version,
                     @Param("loaded") Boolean loaded);

}
