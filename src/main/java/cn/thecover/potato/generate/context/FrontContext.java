package cn.thecover.potato.generate.context;

import cn.thecover.potato.meta.conf.form.search.SearchForm;
import cn.thecover.potato.meta.conf.form.search.element.SearchElement;
import cn.thecover.potato.meta.conf.form.storage.Storage;
import cn.thecover.potato.meta.conf.table.UIColumn;
import cn.thecover.potato.meta.conf.table.UIFollowTable;
import cn.thecover.potato.meta.conf.table.UITable;
import cn.thecover.potato.util.FieldUtil;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author nihao 2021/07/23
 */
@Data
public class FrontContext {
    private String title;
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

    public void addRemoteContext(RemoteContext remoteContext) {
        if (remoteContexts == null) {
            remoteContexts = new HashSet<>();
        }
        remoteContexts.add(remoteContext);
    }

    public FrontContext(String title, String listRequest, UITable uiTable, SearchForm searchForm, String path, Storage storage) {
        this.title = title;
        this.listRequest = listRequest;
        this.infoRequest = listRequest.substring(0, listRequest.lastIndexOf("/")) + "/getInfo";
        this.saveRequest = listRequest.substring(0, listRequest.lastIndexOf("/")) + "/save";
        this.updateRequest = listRequest.substring(0, listRequest.lastIndexOf("/")) + "/update";
        this.deleteRequest = listRequest.substring(0, listRequest.lastIndexOf("/")) + "/delete";
        this.tokenRequest = listRequest.substring(0, listRequest.lastIndexOf("/")) + "/token";
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
    }

    public void addFollow(String listRequest, UIFollowTable uiTable, SearchForm searchForm, String foreignKey, String table,
                          String parentKey, String parentTable, String path , Storage storage) {
        if (follows == null) {
            follows = new ArrayList<>();
        }
        FrontContext frontContext = new FrontContext(uiTable.getBottom(), listRequest, uiTable, searchForm, path, storage);
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
}
