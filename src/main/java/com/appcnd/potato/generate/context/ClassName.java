package com.appcnd.potato.generate.context;

import lombok.Data;

/**
 * @author nihao 2021/07/12
 */
@Data
public class ClassName {
    private String entityName;
    private String packageName;

    private String poClassName;
    private String voClassName;
    private String dtoClassName;
    private String insertReqClassName;
    private String updateReqClassName;
    private String deleteReqClassName;

    private String daoClassName;
    private String serviceClassName;
    private String serviceImplClassName;
    private String controllerClassName;

    public ClassName(String packageName, String entityName) {
        this.packageName = packageName;
        this.entityName = entityName;
        poClassName = packageName + ".pojo.po." + entityName + "Po";
        voClassName = packageName + ".pojo.vo." + entityName + "Vo";
        dtoClassName = packageName + ".pojo.dto." + entityName + "Dto";
        insertReqClassName = packageName + ".pojo.param." + entityName + "InsertReq";
        updateReqClassName = packageName + ".pojo.param." + entityName + "UpdateReq";
        deleteReqClassName = packageName + ".pojo.param." + entityName + "DeleteReq";
        daoClassName = packageName + ".dao." + entityName + "Dao";
        serviceClassName = packageName + ".service." + entityName + "Service";
        serviceImplClassName = packageName + ".service.impl." + entityName + "ServiceImpl";
        controllerClassName = packageName + ".controller." + entityName + "Controller";
    }
}
