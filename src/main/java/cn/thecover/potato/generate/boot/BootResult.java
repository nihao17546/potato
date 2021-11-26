package cn.thecover.potato.generate.boot;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nihao 2021/07/17
 */
@Data
public class BootResult implements Serializable {
    private static final long serialVersionUID = -272881234560692158L;

    private Integer id;
    private Integer version;

    private String basePackage;

    private List<Java> po;
    private List<Java> dto;
    private List<Java> vo;
    private List<Java> dao;
    private List<Java> services;
    private List<Java> serviceImpls;
    private List<Java> controllers;
    private List<Mapper> mappers;
    private Map<String,Html> html;
    private String url;

    private ClassLoader classLoader;

    public void clear() {
//        this.po = null;
//        this.dto = null;
//        this.vo = null;
//        this.dao = null;
//        this.services = null;
//        this.html = null;
    }

    public void addHtml(String path, String source) {
        if (html == null) {
            html = new HashMap<>();
        }
        Html html = new Html();
        html.setSource(source);
        this.html.put(path, html);
    }

    public BootResult() {
        this.po = new ArrayList<>();
        this.dto = new ArrayList<>();
        this.vo = new ArrayList<>();
        this.dao = new ArrayList<>();
        this.services = new ArrayList<>();
        this.serviceImpls = new ArrayList<>();
        this.controllers = new ArrayList<>();
        this.mappers = new ArrayList<>();
    }

    @Data
    public static class Html implements Serializable {
        private static final long serialVersionUID = -272123234560692158L;
        private String source;
    }

    @Data
    public static class Java implements Serializable {
        private static final long serialVersionUID = -272881234560113158L;
        private String className;
        private String source;
    }

    @Data
    public static class Mapper implements Serializable {
        private static final long serialVersionUID = -272881231330692158L;
        private String mapperId;
        private String source;
    }

    public List<Java> getAllJava() {
        List<Java> list = new ArrayList<>();
        list.addAll(po);
        list.addAll(dto);
        list.addAll(vo);
        list.addAll(dao);
        list.addAll(services);
        list.addAll(serviceImpls);
        list.addAll(controllers);
        return list;
    }
}
