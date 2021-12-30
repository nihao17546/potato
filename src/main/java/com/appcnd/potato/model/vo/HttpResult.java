package com.appcnd.potato.model.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nihao 2019/07/04
 */
@Data
public class HttpResult {
    private Integer status;
    private String msg;
    private Object content;

    public String toString() {
        return json();
    }

    public String json(){
        return JSON.toJSONString(this,
//                SerializerFeature.WriteMapNullValue,
//                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.SkipTransientField,
                SerializerFeature.WriteBigDecimalAsPlain,
                SerializerFeature.DisableCircularReferenceDetect);
    }

    public HttpResult() {
    }

    public HttpResult(HttpStatus.Status status) {
        this.status = status.getCode();
        this.msg = status.getName();
    }

    public static HttpResult success() {
        HttpResult result = new HttpResult(HttpStatus.OK);
        return result;
    }

    public static HttpResult fail() {
        HttpResult result = new HttpResult(HttpStatus.SYSTEM_ERROR);
        return result;
    }

    public static HttpResult success(String msg) {
        HttpResult result = new HttpResult(HttpStatus.OK);
        result.setMsg(msg);
        return result;
    }

    public static HttpResult fail(String msg) {
        HttpResult result = new HttpResult(HttpStatus.SYSTEM_ERROR);
        result.setMsg(msg);
        return result;
    }

    public static HttpResult build(HttpStatus.Status status) {
        HttpResult result = new HttpResult(status);
        return result;
    }

    public HttpResult pull(String key, Object value) {
        if (this.content == null || !(this.content instanceof Map)) {
            this.content = new HashMap<>();
        }
        Map map = (Map) this.content;
        map.put(key, value);
        return this;
    }

    public HttpResult pull(Object data) {
        this.content = data;
        return this;
    }

    public HttpResult pull(ListVO listVO) {
        if (this.content == null || !(this.content instanceof Map)) {
            this.content = new HashMap<>();
        }
        Map map = (Map) this.content;
        map.put("list", listVO.getList());
        map.put("curPage", listVO.getCurPage());
        map.put("pageSize", listVO.getPageSize());
        map.put("totalCount", listVO.getTotalCount());
        map.put("totalPage", listVO.getTotalPage());
        return this;
    }
}
