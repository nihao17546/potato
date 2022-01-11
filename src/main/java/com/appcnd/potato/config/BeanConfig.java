package com.appcnd.potato.config;

import com.appcnd.potato.controller.*;
import com.appcnd.potato.generate.boot.HtmlServlet;
import com.appcnd.potato.generate.boot.executor.BootExecutor;
import com.appcnd.potato.model.constant.BasicConstant;
import com.appcnd.potato.properties.CoreProperties;
import com.appcnd.potato.service.IDbService;
import com.appcnd.potato.service.IGenerateService;
import com.appcnd.potato.service.IMetaService;
import com.appcnd.potato.service.impl.DbServiceImpl;
import com.appcnd.potato.service.impl.GenerateServiceImpl;
import com.appcnd.potato.service.impl.MetaServiceImpl;
import com.appcnd.potato.servlet.ResourceServlet;
import com.appcnd.potato.controller.EnhancerController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author nihao 2021/11/14
 */
@Slf4j
@AutoConfigureAfter(PotatoDataSourceAutoConfigure.class)
public class BeanConfig {
    @Autowired
    private CoreProperties properties;

    @Bean(BasicConstant.beanNamePrefix + "resourceServletRegistrationBean")
    public ServletRegistrationBean resourceServletRegistrationBean() {
        return new ServletRegistrationBean(new ResourceServlet(BasicConstant.resourcePath, properties.getPath()), properties.getPath() + "/static/*");
    }

    @Bean(name = BasicConstant.beanNamePrefix + "htmlServlet")
    public HtmlServlet htmlServlet() {
        return new HtmlServlet();
    }

    @Bean(BasicConstant.beanNamePrefix + "htmlServletRegistrationBean")
    public ServletRegistrationBean htmlServletRegistrationBean(@Autowired HtmlServlet htmlServlet) {
        return new ServletRegistrationBean(htmlServlet, properties.getPath() + "/boot/page/*");
    }

    @Bean(name = BasicConstant.beanNamePrefix + "metaService")
    public IMetaService metaService() {
        return new MetaServiceImpl();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "dbService")
    public IDbService dbService() {
        return new DbServiceImpl();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "generateService")
    public IGenerateService generateService() {
        return new GenerateServiceImpl();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "BootExecutor")
    public BootExecutor bootExecutor() {
        return new BootExecutor();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "HtmlController")
    public HtmlController htmlController() {
        return new EnhancerController<>(new HtmlController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "SettingController")
    public SettingController settingController() {
        return new EnhancerController<>(new SettingController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaController")
    public MetaController metaController() {
        return new EnhancerController<>(new MetaController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "DbController")
    public DbController dbController() {
        return new EnhancerController<>(new DbController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaTableController")
    public MetaTableController metaTableController() {
        return new EnhancerController<>(new MetaTableController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaDbController")
    public MetaDbController metaDbController() {
        return new EnhancerController<>(new MetaDbController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaSearchController")
    public MetaSearchController metaSearchController() {
        return new EnhancerController<>(new MetaSearchController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaOperateController")
    public MetaOperateController metaOperateController() {
        return new EnhancerController<>(new MetaOperateController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaStorageController")
    public MetaStorageController metaStorageController() {
        return new EnhancerController<>(new MetaStorageController()).creatController();
    }

    @PostConstruct
    public void init() {
        modify(HtmlController.class);
        modify(SettingController.class);
        modify(MetaController.class);
        modify(DbController.class);
        modify(MetaTableController.class);
        modify(MetaDbController.class);
        modify(MetaSearchController.class);
        modify(MetaOperateController.class);
        modify(MetaStorageController.class);
    }

    /**
     * 修改RequestMapping path值，统一加前缀
     * @param clazz controller类
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void modify(Class clazz) {
        try {
            RequestMapping requestMapping = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
            String[] paths = requestMapping.value();
            String[] newPaths = new String[paths.length];
            for (int i = 0; i < paths.length; i ++) {
                newPaths[i] = properties.getPath() + paths[i];
            }
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(requestMapping);
            Field field = invocationHandler.getClass().getDeclaredField("memberValues");
            field.setAccessible(true);
            Map<String, Object> memberValues = (Map<String, Object>) field.get(invocationHandler);
            memberValues.put("value", newPaths);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
