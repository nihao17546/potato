package com.appcnd.potato.generate.executor.handler;

import com.appcnd.potato.generate.context.ClassName;
import com.appcnd.potato.generate.context.FrontContext;
import com.appcnd.potato.generate.context.GenerateContext;
import com.appcnd.potato.generate.context.JavaClassContext;
import com.appcnd.potato.generate.executor.ComponentExecutor;
import com.appcnd.potato.meta.conf.db.Column;
import com.appcnd.potato.meta.conf.db.FollowTable;
import com.appcnd.potato.meta.conf.db.Table;
import com.appcnd.potato.meta.conf.form.operate.OperateForm;
import com.appcnd.potato.meta.conf.form.search.SearchForm;
import com.appcnd.potato.meta.conf.table.UITable;
import com.appcnd.potato.model.param.response.BooleanResponse;
import com.appcnd.potato.model.param.response.IntegerResponse;
import com.appcnd.potato.model.param.response.ResponseParam;
import com.appcnd.potato.model.param.response.StringResponse;
import com.appcnd.potato.util.CamelUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author nihao 2021/11/03
 */
@Data
public class HandlerRequest {
    private JavaClassContext daoClass;
    private JavaClassContext serviceClass;
    private JavaClassContext serviceImplClass;
    private JavaClassContext controllerClass;
    private GenerateContext context;
    private Map<String,String> columnMap;
    private JavaClassContext po;
    private JavaClassContext vo;
    private JavaClassContext dto;

    private ClassName className;

    private FrontContext frontContext;

    private Table table;
    private List<FollowTable> associationTables;
    private List<FollowTable> followTables;

    private UITable uiTable;
    private SearchForm searchForm;

    private Integer version;

    private List<ComponentExecutor.El> els;

    private OperateForm operateForm;

    private List<HandlerRequest> followHandlerRequests;

    private ResponseVoSetting responseVoSetting;

    /**
     * 从表关联的父表的字段对应的java类
     */
    private Class parentKeyClass;

    public void addFollow(HandlerRequest handlerRequest) {
        if (this.followHandlerRequests == null) {
            this.followHandlerRequests = new ArrayList<>();
        }
        this.followHandlerRequests.add(handlerRequest);
        String key = ((FollowTable) handlerRequest.getTable()).getParentKey();
        for (Column column : table.getColumns()) {
            if (column.getField().equals(key)) {
                handlerRequest.setParentKeyClass(column.getJavaType());
                break;
            }
        }
    }

    public void setResponseVo(ResponseParam responseParam) {
        if (responseParam != null) {
            ResponseVoSetting setting = new ResponseVoSetting();
            setting.setClassName(responseParam.getClassName());
            setting.setContentSetMethod(CamelUtil.get(responseParam.getContentField()));
            setting.setMessageSetMethod(CamelUtil.get(responseParam.getMessageField()));
            if (responseParam instanceof IntegerResponse) {
                IntegerResponse integerResponse = (IntegerResponse) responseParam;
                String m = CamelUtil.get(responseParam.getStatusField());
                setting.setSetSuccessMethod(m  + "(" + integerResponse.getSuccessStatus() + ")");
                setting.setSetErrorMethod(m  + "(" + integerResponse.getErrorStatus() + ")");
            } else if (responseParam instanceof BooleanResponse) {
                BooleanResponse booleanResponse = (BooleanResponse) responseParam;
                String m = CamelUtil.get(responseParam.getStatusField());
                setting.setSetSuccessMethod(m  + "(" + booleanResponse.getSuccessStatus() + ")");
                setting.setSetErrorMethod(m  + "(" + booleanResponse.getErrorStatus() + ")");
            } else if (responseParam instanceof StringResponse) {
                StringResponse stringResponse = (StringResponse) responseParam;
                String m = CamelUtil.get(responseParam.getStatusField());
                setting.setSetSuccessMethod(m  + "(\"" + stringResponse.getSuccessStatus() + "\")");
                setting.setSetErrorMethod(m  + "(\"" + stringResponse.getErrorStatus() + "\")");
            }
            this.responseVoSetting = setting;
        }
    }

    public ResponseVoSetting getResponseVoSetting() {
        if (responseVoSetting == null) {
            // ResponseVoSetting 默认处理
            responseVoSetting = new ResponseVoSetting();
            responseVoSetting.setClassName(this.getClassName().getPackageName() + ".pojo.vo.ResponseVo");
            responseVoSetting.setContentSetMethod("setData");
            responseVoSetting.setMessageSetMethod("setMessage");
            responseVoSetting.setSetSuccessMethod("setCode(0)");
            responseVoSetting.setSetErrorMethod("setCode(1)");
        }
        return responseVoSetting;
    }

    @Data
    public static class ResponseVoSetting {
        private String className;
        private String contentSetMethod;
        private String messageSetMethod;
        private String setSuccessMethod;
        private String setErrorMethod;
    }
}
