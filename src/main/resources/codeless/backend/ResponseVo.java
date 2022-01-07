package ${basePackageName}.pojo.vo;

import java.io.Serializable;

/**
 * @author codelee ${now}
 * @version ${version}
 */
public class ResponseVo<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public ResponseVo setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ResponseVo setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public ResponseVo setData(T data) {
        this.data = data;
        return this;
    }
}
