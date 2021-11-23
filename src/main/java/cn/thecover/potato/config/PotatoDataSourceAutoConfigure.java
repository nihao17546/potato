package cn.thecover.potato.config;

import cn.thecover.potato.dao.BootDao;
import cn.thecover.potato.dao.DbDao;
import cn.thecover.potato.dao.MetaDao;
import cn.thecover.potato.generate.boot.MapperBoot;
import cn.thecover.potato.model.constant.BasicConstant;
import cn.thecover.potato.properties.DbProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.*;

/**
 * @author nihao 2021/11/16
 */
@Slf4j
@EnableConfigurationProperties(DbProperties.class)
public class PotatoDataSourceAutoConfigure implements ApplicationContextAware {
    private ApplicationContext applicationContext;


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
