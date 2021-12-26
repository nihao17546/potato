package cn.thecover.potato.config;

import cn.thecover.potato.controller.*;
import cn.thecover.potato.generate.boot.HtmlServlet;
import cn.thecover.potato.model.constant.BasicConstant;
import cn.thecover.potato.properties.CoreProperties;
import cn.thecover.potato.controller.EnhancerController;
import cn.thecover.potato.service.IDbService;
import cn.thecover.potato.service.IGenerateService;
import cn.thecover.potato.service.IMetaService;
import cn.thecover.potato.service.impl.DbServiceImpl;
import cn.thecover.potato.service.impl.GenerateServiceImpl;
import cn.thecover.potato.service.impl.MetaServiceImpl;
import cn.thecover.potato.servlet.ResourceServlet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @Bean(name = BasicConstant.beanNamePrefix + "HtmlController", initMethod = "requestMapping")
    public HtmlController htmlController() {
        modify(HtmlController.class);
        return new EnhancerController<>(new HtmlController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "SettingController", initMethod = "requestMapping")
    public SettingController settingController() {
        modify(SettingController.class);
        return new EnhancerController<>(new SettingController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaController", initMethod = "requestMapping")
    public MetaController metaController() {
        modify(MetaController.class);
        return new EnhancerController<>(new MetaController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "DbController", initMethod = "requestMapping")
    public DbController dbController() {
        modify(DbController.class);
        return new EnhancerController<>(new DbController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaTableController", initMethod = "requestMapping")
    public MetaTableController metaTableController() {
        modify(MetaTableController.class);
        return new EnhancerController<>(new MetaTableController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaDbController", initMethod = "requestMapping")
    public MetaDbController metaDbController() {
        modify(MetaDbController.class);
        return new EnhancerController<>(new MetaDbController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaSearchController", initMethod = "requestMapping")
    public MetaSearchController metaSearchController() {
        modify(MetaSearchController.class);
        return new EnhancerController<>(new MetaSearchController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaOperateController", initMethod = "requestMapping")
    public MetaOperateController metaOperateController() {
        modify(MetaOperateController.class);
        return new EnhancerController<>(new MetaOperateController()).creatController();
    }

    @Bean(name = BasicConstant.beanNamePrefix + "MetaStorageController", initMethod = "requestMapping")
    public MetaStorageController metaStorageController() {
        modify(MetaStorageController.class);
        return new EnhancerController<>(new MetaStorageController()).creatController();
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
