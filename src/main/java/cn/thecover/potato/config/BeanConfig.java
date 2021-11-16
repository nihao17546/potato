package cn.thecover.potato.config;

import cn.thecover.potato.controller.HtmlController;
import cn.thecover.potato.controller.MetaController;
import cn.thecover.potato.controller.SettingController;
import cn.thecover.potato.generate.boot.HtmlServlet;
import cn.thecover.potato.model.constant.BasicConstant;
import cn.thecover.potato.properties.CoreProperties;
import cn.thecover.potato.service.IGenerateService;
import cn.thecover.potato.service.IMetaService;
import cn.thecover.potato.service.impl.GenerateServiceImpl;
import cn.thecover.potato.service.impl.MetaServiceImpl;
import cn.thecover.potato.servlet.ResourceServlet;
import cn.thecover.potato.util.SpringContextUtil;
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
    @Autowired
    private SpringContextUtil springContextUtil;

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

    @Bean(name = BasicConstant.beanNamePrefix + "generateService")
    public IGenerateService generateService() {
        return new GenerateServiceImpl();
    }

    @PostConstruct
    public void init() throws Exception {
        modify(HtmlController.class);
        springContextUtil.addBean(HtmlController.class, BasicConstant.beanNamePrefix + "configController");
        springContextUtil.registerController(BasicConstant.beanNamePrefix + "configController");

        modify(SettingController.class);
        springContextUtil.addBean(SettingController.class, BasicConstant.beanNamePrefix + "settingController");
        springContextUtil.registerController(BasicConstant.beanNamePrefix + "settingController");

        modify(MetaController.class);
        springContextUtil.addBean(MetaController.class, BasicConstant.beanNamePrefix + "metaController");
        springContextUtil.registerController(BasicConstant.beanNamePrefix + "metaController");
    }

    /**
     * 修改RequestMapping path值，统一加前缀
     * @param clazz controller类
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void modify(Class clazz) throws NoSuchFieldException, IllegalAccessException {
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
    }

}
