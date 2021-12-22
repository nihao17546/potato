package cn.thecover.potato.generate.method;

import cn.thecover.potato.generate.annotation.AnnotationInfo;
import lombok.Data;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author nihao 2021/07/10
 */
@Data
public class MethodInfo implements Serializable {
    private Set<String> contentClass;
    private String returnString;
    private String methodName;
    private List<ParamInfo> params;
    private String content;
    private String decorate = "";
    private Boolean hasContent;
    private List<AnnotationInfo> annotations;

    public void addAnnotation(AnnotationInfo... annotation) {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        for (AnnotationInfo annotationInfo : annotation) {
            annotations.add(annotationInfo);
        }
    }

    public void addAnnotationToTop(AnnotationInfo annotation) {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        annotations.add(0, annotation);
    }

    public void addContentClass(String className) {
        if (contentClass == null) {
            contentClass = new HashSet<>();
        }
        if (!contentClass.contains(className)) {
            contentClass.add(className);
        }
    }

    public void addParam(ParamInfo param) {
        if (params == null) {
            params = new ArrayList<>();
        }
        params.add(param);
    }

    public void addParam(List<ParamInfo> params) {
        if (this.params == null) {
            this.params = new ArrayList<>();
        }
        this.params.addAll(params);
    }

    public MethodInfo deepClone() {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            // 序列化
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            // 反序列化
            bis = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bis);
            return (MethodInfo) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (Exception e) {
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
