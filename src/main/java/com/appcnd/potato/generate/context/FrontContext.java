package com.appcnd.potato.generate.context;

import com.appcnd.potato.generate.constant.ApiConstant;
import com.appcnd.potato.meta.conf.form.search.SearchForm;
import com.appcnd.potato.meta.conf.form.search.element.SearchElement;
import com.appcnd.potato.meta.conf.form.storage.Storage;
import com.appcnd.potato.meta.conf.table.UIColumn;
import com.appcnd.potato.meta.conf.table.UIFollowTable;
import com.appcnd.potato.meta.conf.table.UITable;
import com.appcnd.potato.model.param.response.BooleanResponse;
import com.appcnd.potato.model.param.response.IntegerResponse;
import com.appcnd.potato.model.param.response.ResponseParam;
import com.appcnd.potato.model.param.response.StringResponse;
import com.appcnd.potato.util.FieldUtil;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author nihao 2021/07/23
 */
@Data
public class FrontContext {
    private String title;
    private String httpRequest;
    private String listRequest;
    private String infoRequest;
    private String saveRequest;
    private String updateRequest;
    private String deleteRequest;
    private String tokenRequest;
    private UITable uiTable;
    private List<FrontSearchElementContext> searchElements;
    private Map<String,String> propMap;
    private String parentPath;
    private String path;

    // 当前表外键
    private String foreignKey;
    private String table;
    // 父表键
    private String parentKey;
    private String parentTable;

    private List<FrontContext> follows;

    private FrontOperateContext operateContext;
    private Set<RemoteContext> remoteContexts;
    private Storage storage;

    private ResponseVoSetting responseVoSetting;

    public void addRemoteContext(RemoteContext remoteContext) {
        if (remoteContexts == null) {
            remoteContexts = new HashSet<>();
        }
        remoteContexts.add(remoteContext);
    }

    public FrontContext(String title, String httpRequest, UITable uiTable, SearchForm searchForm, String path, Storage storage, ResponseParam responseParam,
                        boolean insert, boolean update, boolean delete) {
        this.title = title;
        this.httpRequest = httpRequest;
        this.listRequest = ApiConstant.LIST;
        if (insert) {
            this.saveRequest = ApiConstant.SAVE;
        }
        if (update) {
            this.updateRequest = ApiConstant.UPDATE;
        }
        if (delete) {
            this.deleteRequest = ApiConstant.DELETE;
        }
        if (insert || update) {
            this.infoRequest = ApiConstant.INFO;
        }
        if (storage != null) {
            this.tokenRequest = ApiConstant.TOKEN;
        }
        this.uiTable = uiTable;
        this.propMap = new HashMap<>();
        this.path = path;
        this.storage = storage;
        if (searchForm != null && !CollectionUtils.isEmpty(searchForm.getElements())) {
            this.searchElements = new ArrayList<>();
            for (SearchElement element : searchForm.getElements()) {
                FrontSearchElementContext elementContext = new FrontSearchElementContext();
                elementContext.setElement(element);
                this.searchElements.add(elementContext);
            }
        }
        this.responseVoSetting = new ResponseVoSetting();
        if (responseParam != null) {
            responseVoSetting.setStatusKey(responseParam.getStatusField());
            responseVoSetting.setMessageKey(responseParam.getMessageField());
            responseVoSetting.setContentKey(responseParam.getContentField());
            if (responseParam instanceof IntegerResponse) {
                IntegerResponse response = (IntegerResponse) responseParam;
                responseVoSetting.setSuccessValue(response.getSuccessStatus().toString());
                responseVoSetting.setErrorValue(response.getSuccessStatus().toString());
            } else if (responseParam instanceof BooleanResponse) {
                BooleanResponse response = (BooleanResponse) responseParam;
                responseVoSetting.setSuccessValue(response.getSuccessStatus().toString());
                responseVoSetting.setErrorValue(response.getSuccessStatus().toString());
            } else if (responseParam instanceof StringResponse) {
                StringResponse response = (StringResponse) responseParam;
                responseVoSetting.setSuccessValue("'" + response.getSuccessStatus() + "'");
                responseVoSetting.setErrorValue("'" + response.getSuccessStatus() + "'");
            }
        } else {
            responseVoSetting.setStatusKey("code");
            responseVoSetting.setMessageKey("message");
            responseVoSetting.setContentKey("data");
            responseVoSetting.setSuccessValue("0");
            responseVoSetting.setErrorValue("1");
        }
    }

    public void addFollow(String httpRequest, UIFollowTable uiTable, SearchForm searchForm, String foreignKey, String table,
                          String parentKey, String parentTable, String path , Storage storage, ResponseParam responseParam,
                          boolean insert, boolean update, boolean delete) {
        if (follows == null) {
            follows = new ArrayList<>();
        }
        FrontContext frontContext = new FrontContext(uiTable.getBottom(), httpRequest, uiTable, searchForm, path, storage, responseParam, insert, update, delete);
        frontContext.setForeignKey(foreignKey);
        frontContext.setParentKey(parentKey);
        frontContext.setParentTable(parentTable);
        frontContext.setTable(table);
        frontContext.setParentPath(this.path);
        follows.add(frontContext);
    }

    public String getParentKeyProp(Map<String,String> propMap) {
        return propMap.get(FieldUtil.getField(parentTable, parentKey));
    }

    public String getForeignKeyProp() {
        return propMap.get(FieldUtil.getField(table, foreignKey));
    }

    public void addProp(String table, String field, String prop) {
        propMap.put(FieldUtil.getField(table, field), prop);
    }

    public void addProp(UIColumn column, String prop) {
        propMap.put(FieldUtil.getField(column.getTable(), column.getColumn().getField()), prop);
    }

    public String getProp(UIColumn column) {
        return propMap.get(FieldUtil.getField(column.getTable(), column.getColumn().getField()));
    }

    @Data
    public static class ResponseVoSetting {
        private String statusKey;
        private String messageKey;
        private String contentKey;
        private String successValue;
        private String errorValue;
    }
}
