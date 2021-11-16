package cn.thecover.potato.dao;


import cn.thecover.potato.model.po.Meta;
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
                          @Param("offset") Integer offset,
                          @Param("pageSize") Integer pageSize);
    int deleteById(@Param("id") Integer id);
    Integer selectVersionById(@Param("id") Integer id);
    int update(Meta meta);
    Meta selectById(@Param("id") Integer id);

}
