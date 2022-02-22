package com.appcnd.potato.exception;

/**
 * @Auther: iddy
 * @Date: 2022/2/22
 * @Description
 */
@FunctionalInterface
public interface HandlerExceptionFunction {
    void throwException(int code, String msg);
}
