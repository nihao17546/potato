package cn.thecover.potato.config;

import cn.thecover.potato.dao.BootDao;
import cn.thecover.potato.dao.DbDao;
import cn.thecover.potato.dao.MetaDao;
import cn.thecover.potato.generate.boot.MapperBoot;
import cn.thecover.potato.model.constant.BasicConstant;
import cn.thecover.potato.properties.DbProperties;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.io.*;

/**
 * @author nihao 2021/11/16
 */
@Slf4j
public class PotatoDataSourceAutoConfigure implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Autowired
    private DbProperties dbProperties;

    @Bean(BasicConstant.beanNamePrefix + "DataSource")
    @ConditionalOnProperty("spring.potato.db.url")
    public DataSource dataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(dbProperties.getUrl());
        druidDataSource.setUsername(dbProperties.getUsername());
        druidDataSource.setPassword(dbProperties.getPassword());
        if (dbProperties.getInitialSize() != null) {
            druidDataSource.setInitialSize(dbProperties.getInitialSize());
        }
        if (dbProperties.getMinIdle() != null) {
            druidDataSource.setMinIdle(dbProperties.getMinIdle());
        }
        if (dbProperties.getMaxActive() != null) {
            druidDataSource.setMaxActive(dbProperties.getMaxActive());
        }
        if (dbProperties.getMaxWait() != null) {
            druidDataSource.setMaxWait(dbProperties.getMaxWait());
        }
        if (dbProperties.getTimeBetweenEvictionRunsMillis() != null) {
            druidDataSource.setTimeBetweenEvictionRunsMillis(dbProperties.getTimeBetweenEvictionRunsMillis());
        }
        if (dbProperties.getMinEvictableIdleTimeMillis() != null) {
            druidDataSource.setMinEvictableIdleTimeMillis(dbProperties.getMinEvictableIdleTimeMillis());
        }
        if (dbProperties.getValidationQuery() != null) {
            druidDataSource.setValidationQuery(dbProperties.getValidationQuery());
        }
        if (dbProperties.getTestWhileIdle() != null) {
            druidDataSource.setTestWhileIdle(dbProperties.getTestWhileIdle());
        }
        if (dbProperties.getTestOnBorrow() != null) {
            druidDataSource.setTestOnBorrow(dbProperties.getTestOnBorrow());
        }
        if (dbProperties.getTestOnReturn() != null) {
            druidDataSource.setTestOnReturn(dbProperties.getTestOnReturn());
        }
        if (dbProperties.getPoolPreparedStatements() != null) {
            druidDataSource.setPoolPreparedStatements(dbProperties.getPoolPreparedStatements());
        }
        if (dbProperties.getMaxPoolPreparedStatementPerConnectionSize() != null) {
            druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(dbProperties.getMaxPoolPreparedStatementPerConnectionSize());
        }
        return druidDataSource;
    }


    @Bean(name = BasicConstant.beanNamePrefix + "MapperBoot")
    public MapperBoot mapperBoot() {
        SqlSessionFactory sqlSessionFactory = null;
        try {
            sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);
        } catch (Exception e) {
            throw new RuntimeException("SqlSessionFactory 未找到");
        }
        return new MapperBoot((DefaultListableBeanFactory)((ConfigurableApplicationContext) applicationContext).getBeanFactory(), sqlSessionFactory);
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaDao")
    public MetaDao metaDao(@Autowired MapperBoot mapperBoot) {
        log.info("创建 MetaDao");
        String data = getResource("MetaMapper.xml");
        return mapperBoot.registerMapper(data, MetaDao.class);
    }

    @Bean(name = BasicConstant.beanNamePrefix + "BootDao")
    public BootDao bootDao(@Autowired MapperBoot mapperBoot) {
        log.info("创建 BootDao");
        String data = getResource("BootMapper.xml");
        return mapperBoot.registerMapper(data, BootDao.class);
    }

    @Bean(name = BasicConstant.beanNamePrefix + "DbDao")
    public DbDao dbDao(@Autowired MapperBoot mapperBoot) {
        log.info("创建 DbDao");
        String data = getResource("DbMapper.xml");
        return mapperBoot.registerMapper(data, DbDao.class);
    }

    private String getResource(String fileName) {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("potato_mappers/" + fileName);
        if (in == null) {
            throw new RuntimeException(fileName + " 不存在");
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                sb.append(tempString);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(fileName + " 读取异常");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
