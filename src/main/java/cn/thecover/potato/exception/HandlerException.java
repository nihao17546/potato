package cn.thecover.potato.exception;

import cn.thecover.potato.model.vo.HttpStatus;

/**
 * @author nihao 2019/07/03
 */
public class HandlerException extends RuntimeException {
    private HttpStatus.Status status;

    public HttpStatus.Status getStatus() {
        return status;
    }

    public void setStatus(HttpStatus.Status status) {
        this.status = status;
    }

    public HandlerException(HttpStatus.Status status) {
        super(status.getName());
        this.status = status;
    }

    public HandlerException(int code, String msg) {
        super(msg);
        this.status = new HttpStatus.Status(code, msg);
    }
}
