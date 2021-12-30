package com.appcnd.potato.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author nihao 2021/12/25
 */
@Slf4j
public class BaseController {
    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    public void requestMapping() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("注册controller:{}", this.getClass());
        Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().
                getDeclaredMethod("detectHandlerMethods", Object.class);
        method.setAccessible(true);
        method.invoke(requestMappingHandlerMapping, this);
    }
}
