package cn.thecover.potato.generate.boot;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.io.ByteArrayInputStream;

/**
 * @author nihao 2021/07/14
 */
@Slf4j
public class MapperBoot {
    private MapperBoot() {}
    private static SqlSessionFactory sqlSessionFactory;
    private static DefaultListableBeanFactory beanFactory;
    public static void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        if (MapperBoot.sqlSessionFactory != null) {
            throw new IllegalArgumentException();
        }
        MapperBoot.sqlSessionFactory = sqlSessionFactory;
    }
    public static void setBeanFactory(DefaultListableBeanFactory beanFactory) {
        if (MapperBoot.beanFactory != null) {
            throw new IllegalArgumentException();
        }
        MapperBoot.beanFactory = beanFactory;
    }


    public static void addMapper(String mapperId, String mapper) {
        try {
            Object object = registerMapper(mapper, Class.forName(mapperId));
            log.info("向spring中注册Mapper:{}", mapperId);
            beanFactory.registerSingleton(mapperId, object);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static  <T> T registerMapper(String data, Class<T> clazz) {
        log.info("向注册SqlSessionFactory中注册Mapper:{}", clazz);
        Configuration configuration = sqlSessionFactory.getConfiguration();
        try {
            String mapperId = clazz.getName();
            ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes());
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(is, configuration, mapperId, configuration.getSqlFragments());
            xmlMapperBuilder.parse();
            T object = sqlSessionFactory.openSession().getMapper(clazz);
            return object;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
