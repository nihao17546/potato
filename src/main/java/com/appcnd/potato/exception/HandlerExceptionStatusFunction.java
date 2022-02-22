package com.appcnd.potato.exception;

import com.appcnd.potato.model.vo.HttpStatus;

/**
 * @Auther: iddy
 * @Date: 2022/2/22
 * @Description
 */
@FunctionalInterface
public interface HandlerExceptionStatusFunction {
    void throwException(HttpStatus.Status status);
}
