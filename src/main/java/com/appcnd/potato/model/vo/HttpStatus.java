package com.appcnd.potato.model.vo;

import lombok.Data;

/**
 * @author nihao 2019/07/02
 */
public class HttpStatus {

    public static final Status OK = new Status(0, "ok");
    public static final Status SYSTEM_ERROR = new Status(1, "系统异常");
    public static final Status PARAM_ERROR = new Status(400, "参数错误");
    public static final Status NEED_LOGIN = new Status(403, "未登录");
    public static final Status AUTH_PENDING = new Status(403, "账号待验证");
    public static final Status AUTH_FORBIDDEN = new Status(403, "账号未验证通过");
    public static final Status NOT_FOUND = new Status(404, "未找到");
    public static final Status NOT_VIP = new Status(405, "不是会员");
    public static final Status SQUEEZE  = new Status(406, "其他设备已登录,状态失效");

    @Data
    public static class Status {
        private int code;
        private String name;

        public Status(int code, String name) {
            this.code = code;
            this.name = name;
        }
    }
}
