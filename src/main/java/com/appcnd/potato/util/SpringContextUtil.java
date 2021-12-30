package com.appcnd.potato.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * created by nihao 2020/07/07
 */
@Slf4j
public class SpringContextUtil implements ApplicationContextAware {
    private DefaultListableBeanFactory beanFactory;
    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.beanFactory = (DefaultListableBeanFactory)((ConfigurableApplicationContext) applicationContext).getBeanFactory();
    }

    public void removeBean(String beanName) {
        log.info("卸载Bean:{}", beanName);
        beanFactory.removeBeanDefinition(beanName);
    }

    public void destroySingleton(String beanName) {
        log.info("卸载Singleton:{}", beanName);
        beanFactory.destroySingleton(beanName);
    }

    public void registerController(String beanId, String className) {
        addBean(className, beanId);
        registerController(beanId);
    }

    public void registerController(String beanId, Class clazz) {
        registerBean(beanId, clazz);
        registerController(beanId);
    }

    public void registerController(String controllerBeanName) {
        try {
            log.info("注册controller:{}", controllerBeanName);
            final RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping)
                    applicationContext.getBean("requestMappingHandlerMapping");

            if (requestMappingHandlerMapping != null) {
                String handler = controllerBeanName;
                Object controller = applicationContext.getBean(handler);
                if (controller == null) {
                    return;
                }
                unregisterController(controllerBeanName);
                //注册Controller
                Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().
                        getDeclaredMethod("detectHandlerMethods", Object.class);
                method.setAccessible(true);
                method.invoke(requestMappingHandlerMapping, handler);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void unregisterController(String controllerBeanName) {
        final RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping)
                applicationContext.getBean("requestMappingHandlerMapping");
        if (requestMappingHandlerMapping != null) {
            String handler = controllerBeanName;
            Object controller = applicationContext.getBean(handler);
            if (controller == null) {
                return;
            }
            final Class<?> targetClass = controller.getClass();
            ReflectionUtils.doWithMethods(targetClass, new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) {
                    Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                    try {
                        Method createMappingMethod = RequestMappingHandlerMapping.class.
                                getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
                        createMappingMethod.setAccessible(true);
                        RequestMappingInfo requestMappingInfo = (RequestMappingInfo)
                                createMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, targetClass);
                        if (requestMappingInfo != null) {
                            requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
        }
    }

    public void registerSingleton(String id, Object bean) {
        log.info("注册Singleton:{}", id);
        beanFactory.registerSingleton(id, bean);
    }

    public void registerBean(String beanId, Class clazz) {
        log.info("注册bean:{}", beanId);
        // get the BeanDefinitionBuilder
        BeanDefinitionBuilder beanDefinitionBuilder =
                BeanDefinitionBuilder.genericBeanDefinition(clazz);
        // get the BeanDefinition
        BeanDefinition beanDefinition=beanDefinitionBuilder.getBeanDefinition();
        // register the bean
        beanFactory.registerBeanDefinition(beanId,beanDefinition);
    }

    public void addBean(String className, String serviceName) {
        try {
            log.info("注册bean:{}", serviceName);
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
            registerBean(serviceName, beanDefinitionBuilder.getRawBeanDefinition());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void addBean(Class clazz, String serviceName) {
        log.info("注册bean:{}", serviceName);
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        registerBean(serviceName, beanDefinitionBuilder.getRawBeanDefinition());
    }

    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public Object getBean(String beanId) {
        return applicationContext.getBean(beanId);
    }

    private void registerBean(String beanName, BeanDefinition beanDefinition) {
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
        BeanDefinitionRegistry beanDefinitonRegistry = (BeanDefinitionRegistry) configurableApplicationContext
                .getBeanFactory();
        beanDefinitonRegistry.registerBeanDefinition(beanName, beanDefinition);
    }
}
