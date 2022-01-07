package com.appcnd.potato.generate.context;

import com.appcnd.potato.generate.annotation.AnnotationInfo;
import com.appcnd.potato.util.CommonUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author nihao 2021/07/11
 */
public class ClassField implements Serializable {
    @Getter
    private String decorate = "";

    @Getter
    @Setter
    private String className;

    @Getter
    private String name;

    @Getter
    private List<AnnotationInfo> annotations;

    public ClassField(String decorate, String className, AnnotationInfo... annotations) {
        if (decorate != null) {
            this.decorate = decorate;
        }
        this.className = className;
        this.name = CommonUtil.getClassNameField(className);
        if (annotations != null) {
            for (AnnotationInfo annotationInfo : annotations) {
                addAnnotation(annotationInfo);
            }
        }
    }

    public ClassField(String decorate, String className, String name, AnnotationInfo... annotations) {
        if (decorate != null) {
            this.decorate = decorate;
        }
        this.className = className;
        this.name = name;
        if (annotations != null) {
            for (AnnotationInfo annotationInfo : annotations) {
                addAnnotation(annotationInfo);
            }
        }
    }

    public void addAnnotation(AnnotationInfo annotation) {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        annotations.add(annotation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassField that = (ClassField) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
