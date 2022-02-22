package com.appcnd.potato.exception;

import com.appcnd.potato.model.vo.HttpStatus;

import java.util.Collection;

/**
 * @Auther: iddy
 * @Date: 2022/2/22
 * @Description
 */
public class ExceptionAssert {
    public final static void throwException(int errorCode, String errorMsg) {
        throw new HandlerException(errorCode, errorMsg);
    }
    public final static void throwException(HttpStatus.Status status) {
        throw new HandlerException(status);
    }

    public final static HandlerExceptionFunction isTrue(boolean b) {
        return (errorCode, errorMsg) -> {
            if (b) {
                throwException(errorCode, errorMsg);
            }
        };
    }

    public final static HandlerExceptionFunction isEmpty(Collection collection) {
        return isTrue(collection == null || collection.isEmpty());
    }

    public final static HandlerExceptionFunction isEmpty(String string) {
        return isTrue(string == null || string.isEmpty());
    }

    public final static HandlerExceptionFunction isNull(Object obj) {
        return isTrue(obj == null);
    }



    public final static HandlerExceptionStatusFunction ifTrue(boolean b) {
        return (status) -> {
            if (b) {
                throwException(status);
            }
        };
    }

    public final static HandlerExceptionStatusFunction ifEmpty(Collection collection) {
        return ifTrue(collection == null || collection.isEmpty());
    }

    public final static HandlerExceptionStatusFunction ifEmpty(String string) {
        return ifTrue(string == null || string.isEmpty());
    }

    public final static HandlerExceptionStatusFunction ifNull(Object obj) {
        return ifTrue(obj == null);
    }
}
