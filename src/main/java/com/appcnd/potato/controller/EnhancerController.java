package com.appcnd.potato.controller;

import com.appcnd.potato.exception.HandlerException;
import com.appcnd.potato.model.vo.HttpResult;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.util.CommonUtil;
import com.appcnd.potato.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nihao 2021/12/26
 */
@Slf4j
public class EnhancerController<T> implements MethodInterceptor {
    private T target;

    public EnhancerController(T target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        try {
            return proxy.invokeSuper(object, args);
        } catch (Exception e) {
            if (!needHandle(method)) {
                throw e;
            }
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            if (CommonUtil.isAjax(request)) {
                HttpResult httpResult = null;
                if (e instanceof HandlerException) {
                    HandlerException handlerException = (HandlerException) e;
                    httpResult = HttpResult.build(handlerException.getStatus());
                } else {
                    log.error("RequestURL:{}", request.getRequestURL(), e);
                    httpResult = HttpResult.build(HttpStatus.SYSTEM_ERROR);
                }
                if (method.getReturnType().equals(String.class)) {
                    return httpResult.json();
                } else if (method.getReturnType().equals(HttpResult.class)) {
                    return httpResult;
                } else {
                    CommonUtil.responseOutWithJson(response, httpResult);
                    return null;
                }
            } else {
                if (e instanceof HandlerException) {
                    HandlerException ex = (HandlerException) e;
                    CommonUtil.buildErrorTip(request, ex.getStatus().getCode(), ex.getStatus().getName());
                } else {
                    log.error("RequestURL:{}", request.getRequestURL(), e);
                    CommonUtil.buildErrorTip(request, e.getMessage());
                }
                response.setCharacterEncoding("utf-8");
                response.setContentType("text/html; charset=utf-8");
                Enumeration<String> enu = request.getAttributeNames();
                Map<String,String> params = new HashMap<>();
                String potatoPath = null;
                while(enu.hasMoreElements()){
                    String k = enu.nextElement();
                    Object v = request.getAttribute(k);
                    if ("potatoPath".equals(k)) {
                        potatoPath = v.toString();
                    } else {
                        params.put(k, v.toString());
                    }
                }
                String text = PageUtil.getPage(request, potatoPath, "error", params);
                response.getWriter().write(text);
                return null;
            }
        }
    }

    private boolean needHandle(Method method) {
        Annotation[] annotations = method.getDeclaredAnnotations();
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof RequestMapping
                        || annotation instanceof GetMapping
                        || annotation instanceof PostMapping
                        || annotation instanceof PutMapping
                        || annotation instanceof PatchMapping) {
                    return true;
                }
            }
        }
        return false;
    }

    public T creatController() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(this.target.getClass());
        // 设置回调方法
        enhancer.setCallback(this);
        // 创建代理对象
        return (T) enhancer.create();
    }
}
