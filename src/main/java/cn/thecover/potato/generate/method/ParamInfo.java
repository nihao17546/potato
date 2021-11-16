package cn.thecover.potato.generate.method;

import cn.thecover.potato.generate.annotation.AnnotationInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nihao 2021/07/10
 */
public class ParamInfo implements Serializable {
    @Getter
    private String type;
    @Getter
    private String name;
    @Getter
    @Setter
    private List<AnnotationInfo> annotations;
    public void addAnnotation(AnnotationInfo annotation) {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        annotations.add(annotation);
    }

    public ParamInfo(String type, String name) {
        this.type = type;
        this.name = name;
    }


    public ParamInfo deepClone() {
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
            return (ParamInfo) ois.readObject();
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
