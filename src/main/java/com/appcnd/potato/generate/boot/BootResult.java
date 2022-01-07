package com.appcnd.potato.generate.boot;

import lombok.Data;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

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
    private List<Java> param;
    private List<Java> dao;
    private List<Java> services;
    private List<Java> serviceImpls;
    private List<Java> controllers;
    private List<Mapper> mappers;
    private Map<String,Html> html;
    private String url;

    private PotatoClassLoader classLoader;

    private SqlSessionFactory sqlSessionFactory;

    public void clear() {
        if (this.po != null) {
            this.po.clear();
        }
        if (this.dto != null) {
            this.dto.clear();
        }
        if (this.vo != null) {
            this.vo.clear();
        }
        if (this.param != null) {
            this.param.clear();
        }
        if (this.dao != null) {
            this.dao.clear();
        }
        if (this.services != null) {
            this.services.clear();
        }
        if (this.serviceImpls != null) {
            this.serviceImpls.clear();
        }
        if (this.controllers != null) {
            this.controllers.clear();
        }
        if (this.mappers != null) {
            this.mappers.clear();
        }
        if (this.html != null) {
            this.html.clear();
        }
        if (this.classLoader != null) {
            this.classLoader.clear();
        }
        this.classLoader = null;
        if (this.sqlSessionFactory != null && this.sqlSessionFactory.getConfiguration() != null) {
            try {
                TypeAliasRegistry typeAliasRegistry = this.sqlSessionFactory.getConfiguration().getTypeAliasRegistry();
                Field[] fields = typeAliasRegistry.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType().equals(Map.class) || field.getType().equals(HashMap.class)) {
                        field.setAccessible(true);
                        Map map = (Map) field.get(typeAliasRegistry);
                        map.clear();
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                Field[] fields = this.sqlSessionFactory.getConfiguration().getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType().equals(MapperRegistry.class)) {
                        field.setAccessible(true);
                        MapperRegistry mapperRegistry = (MapperRegistry) field.get(this.sqlSessionFactory.getConfiguration());
                        Field[] ff = mapperRegistry.getClass().getDeclaredFields();
                        for (Field f : ff) {
                            if (f.getType().equals(Map.class) || f.getType().equals(HashMap.class)) {
                                f.setAccessible(true);
                                Map map = (Map) f.get(mapperRegistry);
                                map.clear();
                                break;
                            }
                        }
                        break;
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.sqlSessionFactory = null;
        }
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
        this.param = new ArrayList<>();
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
        list.addAll(param);
        list.addAll(dao);
        list.addAll(services);
        list.addAll(serviceImpls);
        list.addAll(controllers);
        return list;
    }
}
