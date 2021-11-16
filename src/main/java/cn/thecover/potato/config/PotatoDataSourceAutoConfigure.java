package cn.thecover.potato.config;

import cn.thecover.potato.dao.BootDao;
import cn.thecover.potato.dao.MetaDao;
import cn.thecover.potato.generate.boot.MapperBoot;
import cn.thecover.potato.model.constant.BasicConstant;
import cn.thecover.potato.properties.DbProperties;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.*;

/**
 * @author nihao 2021/11/16
 */
@Slf4j
@EnableConfigurationProperties(DbProperties.class)
public class PotatoDataSourceAutoConfigure implements ApplicationContextAware {
    @Autowired
    private DbProperties dbProperties;
    private ApplicationContext applicationContext;

    private DataSource dataSource() {
        log.info("创建指定数据源");
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
        log.info("数据源创建成功");
        return druidDataSource;
    }

    @PostConstruct
    public void init() {
        DataSource dataSource = null;
        if (dbProperties.getUrl() != null) {
            log.info("创建指定数据源");
            dataSource = dataSource();
        } else {
            log.info("使用项目自带数据源");
            try {
                dataSource = applicationContext.getBean(DataSource.class);
            } catch (Exception e) {
                log.error("未找到项目自带数据源");
                throw new RuntimeException(e);
            }
        }
        log.info("创建SqlSessionFactory");
        TransactionFactory transactionFactory = new SpringManagedTransactionFactory();
        Environment environment = new Environment ("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        MapperBoot.setSqlSessionFactory(sqlSessionFactory);
        MapperBoot.setBeanFactory((DefaultListableBeanFactory)((ConfigurableApplicationContext) applicationContext).getBeanFactory());
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaDao")
    public MetaDao metaDao() {
        log.info("创建 MetaDao");
        String data = getResource("MetaMapper.xml");
        return MapperBoot.registerMapper(data, MetaDao.class);
    }

    @Bean(name = BasicConstant.beanNamePrefix + "booBootDaotDao")
    public BootDao bootDao() {
        log.info("创建 BootDao");
        String data = getResource("BootMapper.xml");
        return MapperBoot.registerMapper(data, BootDao.class);
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
