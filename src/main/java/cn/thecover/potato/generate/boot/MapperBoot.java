package cn.thecover.potato.generate.boot;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;

/**
 * @author nihao 2021/07/14
 */
@Slf4j
public class MapperBoot {
    private SqlSessionFactory sqlSessionFactory;
    private DefaultListableBeanFactory beanFactory;

    public MapperBoot(DefaultListableBeanFactory beanFactory, DataSource dataSource) {
        this.beanFactory = beanFactory;
        Environment environment = new Environment ("development", new SpringManagedTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);
        this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    public void addMapper(String mapperId, String mapper) {
        try {
            Object object = registerMapper(mapper, Class.forName(mapperId));
            log.info("向spring中注册Mapper:{}", mapperId);
            beanFactory.registerSingleton(mapperId, object);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <bean id="userMapper" class="org.mybatis.Spring.mapper.MapperFactoryBean">
     *     <property name="mapperInterface" value="test.mybatis.dao.UserMapper"/>
     *     <property name="sqlSessionFactory" ref = "sqlSessionFactory"/>
     * </bean>
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public  <T> T registerMapper(String data, Class<T> clazz) {
        log.info("向注册SqlSessionFactory中注册Mapper:{}", clazz);
        Configuration configuration = sqlSessionFactory.getConfiguration();
        try {
//            sqlSessionFactory.getConfiguration().addMapper(clazz);
            String mapperId = clazz.getName();
            ByteArrayInputStream is = new ByteArrayInputStream(data.getBytes());
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(is, configuration, mapperId, configuration.getSqlFragments());
            xmlMapperBuilder.parse();

            MapperFactoryBean<T> mapperFactoryBean = new MapperFactoryBean();
            mapperFactoryBean.setSqlSessionFactory(sqlSessionFactory);
            mapperFactoryBean.setMapperInterface(clazz);
            return mapperFactoryBean.getObject();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
