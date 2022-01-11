package com.appcnd.potato.controller;

import com.appcnd.potato.meta.conf.form.enums.ElementSize;
import com.appcnd.potato.meta.conf.form.operate.elements.OperateElement;
import com.appcnd.potato.meta.conf.form.operate.enums.OperateElementType;
import com.appcnd.potato.meta.conf.form.search.enums.JudgeType;
import com.appcnd.potato.meta.conf.form.search.enums.SearchElementType;
import com.appcnd.potato.meta.conf.form.storage.enums.StorageType;
import com.appcnd.potato.model.vo.HttpResult;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author nihao 2021/11/14
 */
@RequestMapping("/setting")
public class SettingController {
    private List<Map<String,String>> judges = new ArrayList<>();
    private List<Map<String,String>> searchElementTypes = new ArrayList<>();
    private Map<String,List<String>> searchElementContains = new HashMap<>();
    private List<Map<String,Object>> elementSizes = new ArrayList<>();
    private List<String> javas = new ArrayList<>();

    private List<Map<String,String>> operateElementTypes = new ArrayList<>();
    private Map<String,List<String>> operateElementContains = new HashMap<>();
    private Set<String> allOperateContains = new HashSet<>();
    private Map<String,Object> operateDefaults = new HashMap<>();

    private List<Map<String,String>> storageElementTypes = new ArrayList<>();
    private Map<String,List<String>> storageElementContains = new HashMap<>();
    private List<Map<String,Object>> tableColumnFormatterData = new ArrayList<>();

    private void fillTableColumnFormatterData() {
        Map<String,Object> a = new HashMap<>(3);
        a.put("id", 1);
        a.put("name", "日期时间格式化");
        a.put("value", "if (row[prop]) {return new Date(row[prop]).format(\"yyyy-MM-dd hh:mm:ss\")}");
        tableColumnFormatterData.add(a);

        Map<String,Object> b = new HashMap<>(3);
        b.put("id", 2);
        b.put("name", "日期格式化");
        b.put("value", "if (row[prop]) {return new Date(row[prop]).format(\"yyyy-MM-dd\")}");
        tableColumnFormatterData.add(b);

        Map<String,Object> c = new HashMap<>(3);
        c.put("id", 3);
        c.put("name", "图片格式化");
        c.put("value", "if (row[prop]) {return \"<img width ='50' height='50' src=\"+row[prop]+\">\"}");
        tableColumnFormatterData.add(c);
    }

    @PostConstruct
    public void init() throws IllegalAccessException, InstantiationException {
        fillTableColumnFormatterData();
        for (JudgeType judgeType : JudgeType.values()) {
            Map<String,String> map = new HashMap<>(2);
            map.put("value", judgeType.name());
            map.put("label", judgeType.getDesc());
            judges.add(map);
        }
        for (SearchElementType elementType : SearchElementType.values()) {
            Map<String,String> map = new HashMap<>(2);
            map.put("label", elementType.getDesc());
            map.put("value", elementType.name());
            searchElementContains.put(elementType.name(), elementType.getContains());
            searchElementTypes.add(map);
        }
        for (StorageType storageType : StorageType.values()) {
            Map<String,String> map = new HashMap<>(2);
            map.put("label", storageType.getDesc());
            map.put("value", storageType.name());
            storageElementContains.put(storageType.name(), storageType.getContains());
            storageElementTypes.add(map);
        }
        for (ElementSize elementSize : ElementSize.values()) {
            Map<String,Object> map = new HashMap<>(2);
            map.put("label", elementSize.getDesc());
            map.put("value", elementSize.name());
            map.put("disableTypes", elementSize.getDisableTypes());
            elementSizes.add(map);
        }
        for (OperateElementType elementType : OperateElementType.values()) {
            Map<String,String> map = new HashMap<>(2);
            map.put("label", elementType.getDesc());
            map.put("value", elementType.name());
            operateElementContains.put(elementType.name(), elementType.getContains());
            operateElementTypes.add(map);
            allOperateContains.addAll(elementType.getContains());
        }
        JsonSubTypes jsonSubTypes = OperateElement.class.getAnnotation(JsonSubTypes.class);
        for (JsonSubTypes.Type type : jsonSubTypes.value()) {
            Class clazz = type.value();
            Object object = clazz.newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isFinal(field.getModifiers())) {
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if (value != null) {
                        operateDefaults.put(field.getName(), value);
                    }
                }
            }
            Class sub = clazz.getSuperclass();
            for (Field field : sub.getDeclaredFields()) {
                if (!Modifier.isFinal(field.getModifiers())) {
                    field.setAccessible(true);
                    Object value = field.get(object);
                    if (value != null) {
                        operateDefaults.put(field.getName(), value);
                    }
                }
            }
        }

        javas.add("Boolean");
        javas.add("int");
        javas.add("long");
        javas.add("short");
        javas.add("byte");
        javas.add("float");
        javas.add("double");
        javas.add("char");
        javas.add("class");
        javas.add("interface");
        javas.add("if");
        javas.add("else");
        javas.add("do");
        javas.add("while");
        javas.add("for");
        javas.add("switch");
        javas.add("case");
        javas.add("default");
        javas.add("break");
        javas.add("continue");
        javas.add("return");
        javas.add("try");
        javas.add("catch");
        javas.add("finally");
        javas.add("public");
        javas.add("protected");
        javas.add("private");
        javas.add("final");
        javas.add("void");
        javas.add("static");
        javas.add("strictfp");
        javas.add("abstract");
        javas.add("transient");
        javas.add("synchronized");
        javas.add("volatile");
        javas.add("native");
        javas.add("package");
        javas.add("import");
        javas.add("throw");
        javas.add("throws");
        javas.add("extends");
        javas.add("implements");
        javas.add("this");
        javas.add("Super");
        javas.add("instanceof");
        javas.add("new");
        javas.add("true");
        javas.add("false");
        javas.add("null");
        javas.add("goto");
        javas.add("const");
    }

    @GetMapping("/size")
    @ResponseBody
    public String size() {
        return HttpResult.success().pull("list", elementSizes).json();
    }

    @GetMapping("/judge")
    @ResponseBody
    public String info() {
        return HttpResult.success().pull("list", judges).json();
    }

    @GetMapping("/searchElementType")
    @ResponseBody
    public String searchElementType() {
        return HttpResult.success().pull("list", searchElementTypes)
                .pull("typeContains", searchElementContains).json();
    }

    @GetMapping("/operateElementType")
    @ResponseBody
    public String operateElementType() {
        return HttpResult.success().pull("list", operateElementTypes)
                .pull("typeContains", operateElementContains)
                .pull("all", allOperateContains)
                .pull("operateDefaults", operateDefaults).json();
    }

    @GetMapping("/java")
    @ResponseBody
    public String java() {
        return HttpResult.success().pull("list", javas).json();
    }

    @GetMapping("/storageElementType")
    @ResponseBody
    public String storageElementType() {
        return HttpResult.success().pull("list", storageElementTypes)
                .pull("typeContains", storageElementContains).json();
    }

    @GetMapping("/tableColumnFormatterData")
    @ResponseBody
    public String tableColumnFormatterData() {
        return HttpResult.success().pull("list", tableColumnFormatterData).json();
    }
}
