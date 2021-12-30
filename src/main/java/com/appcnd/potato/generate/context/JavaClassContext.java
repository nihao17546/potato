package com.appcnd.potato.generate.context;

import com.appcnd.potato.generate.annotation.AnnotationInfo;
import com.appcnd.potato.generate.method.MethodInfo;
import com.appcnd.potato.util.CommonUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author nihao 2021/07/12
 */
public class JavaClassContext {
    @Getter
    @Setter
    private String packageName;

    @Getter
    @Setter
    private Integer version;

    // 修饰
    @Getter
    @Setter
    private String decorate = "";

    @Getter
    @Setter
    private String name;

    @Getter
    private Set<String> imports;

    @Getter
    private List<ClassField> fields;

    @Getter
    private String extendsClassName;

    @Getter
    private List<String> implementsClassNames;

    @Getter
    private List<ClassConstructor> constructors;

    @Getter
    private List<MethodInfo> methods;

    @Getter
    private List<AnnotationInfo> annotations;

    public JavaClassContext(String packageName, Integer version, String decorate, String className) {
        this.packageName = packageName;
        this.version = version;
        this.decorate = decorate;
        this.name = CommonUtil.getSimpleClassName(className);
    }

    public void addAnnotation(AnnotationInfo... annotation) {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        for (AnnotationInfo annotationInfo : annotation) {
            annotations.add(annotationInfo);
        }
    }

    public void addMethod(MethodInfo method) {
        if (methods == null) {
            methods = new ArrayList<>();
        }
        methods.add(method);
    }

    public void addMethods(List<MethodInfo> methods) {
        if (this.methods == null) {
            this.methods = new ArrayList<>();
        }
        this.methods.addAll(methods);
    }

    public void addConstructor(ClassConstructor constructor) {
        if (constructors == null) {
            constructors = new ArrayList<>();
        }
        constructors.add(constructor);
    }

    public String addImport(String className) {
        if (imports == null) {
            imports = new HashSet<>();
        }
        boolean b = hasImportOther(className);
        if (b) {
            return className;
        } else {
            imports.add(className);
            return CommonUtil.getSimpleClassName(className);
        }
    }

    private boolean hasImportOther(String className) {
        if (imports == null || imports.isEmpty()) {
            return false;
        }
        String simpleClassName = CommonUtil.getSimpleClassName(className);
        for (String string : imports) {
            String str = CommonUtil.getSimpleClassName(string);
            if (str.equals(simpleClassName) && !className.equals(string)) {
                return true;
            }
        }
        return false;
    }

    public void addField(ClassField classField) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        if (fields.contains(classField)) {
            throw new RuntimeException("属性[" + classField.getName() + "]重复添加");
        }
        fields.add(classField);
    }

    public boolean hasField(ClassField classField) {
        if (fields != null) {
            return fields.contains(classField);
        }
        return false;
    }

    public void addFields(List<ClassField> classFields) {
        for (ClassField classField : classFields) {
            addField(classField);
        }
    }

    public void addImplementsClassName(String className) {
        if (implementsClassNames == null) {
            implementsClassNames = new ArrayList<>();
        }
        implementsClassNames.add(className);
    }
    public void setExtendsClassName(String className) {
        this.extendsClassName = className;
    }

    public String getClassName() {
        return this.packageName + "." + this.name;
    }
}
