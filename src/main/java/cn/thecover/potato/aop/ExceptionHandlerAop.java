package cn.thecover.potato.aop;

import cn.thecover.potato.exception.HandlerException;
import cn.thecover.potato.model.vo.HttpResult;
import cn.thecover.potato.model.vo.HttpStatus;
import cn.thecover.potato.properties.CoreProperties;
import cn.thecover.potato.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * controller异常捕获
 * created by nihao 2020/07/08
 */
@Aspect
@Slf4j
public class ExceptionHandlerAop {

    @Autowired
    private CoreProperties properties;

    @Pointcut("execution(public * cn.thecover.potato.controller.*.*(..))")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        try {
            Object proceed = point.proceed();
            return proceed;
        } catch (Throwable e) {
            if (CommonUtil.isAjax(request)) {
                if (e instanceof HandlerException) {
                    HandlerException handlerException = (HandlerException) e;
                    CommonUtil.responseOutWithJson(response, HttpResult.build(handlerException.getStatus()));
                } else if (e instanceof HttpRequestMethodNotSupportedException) {
                    HttpRequestMethodNotSupportedException me = (HttpRequestMethodNotSupportedException) e;
                    CommonUtil.responseOutWithJson(response, HttpResult.fail("不支持" + me.getMethod()));
                } else if (e instanceof MissingServletRequestParameterException) {
                    MissingServletRequestParameterException me = (MissingServletRequestParameterException) e;
                    CommonUtil.responseOutWithJson(response, HttpResult.fail("参数缺失[" + me.getParameterName() + "]"));
                } else if (e instanceof HttpMessageNotReadableException) {
                    CommonUtil.responseOutWithJson(response, HttpResult.fail("request body is missing"));
                } else {
                    log.error("RequestURL:{}", request.getRequestURL(), e);
                    CommonUtil.responseOutWithJson(response, HttpResult.build(HttpStatus.SYSTEM_ERROR));
                }
            } else {
                if (e instanceof MissingServletRequestParameterException) {
                    MissingServletRequestParameterException me = (MissingServletRequestParameterException) e;
                    CommonUtil.buildErrorTip(request, "参数缺失[" + me.getParameterName() + "]");
                } else {
                    log.error("RequestURL:{}", request.getRequestURL(), e);
                    CommonUtil.buildErrorTip(request, e.getMessage());
                }
                request.getRequestDispatcher(properties.getPath() + "/static/htmls/error.html").forward(request, response);
            }
            return null;
        }
    }

}
