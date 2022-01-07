package com.appcnd.potato.generate.context;

import com.appcnd.potato.generate.method.ParamInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nihao 2021/07/12
 */
public class ClassConstructor implements Serializable {
    @Getter
    private String decorate = "";
    @Getter
    private List<ParamInfo> params;
    @Getter
    @Setter
    private String content;

    public ClassConstructor(String decorate) {
        this.decorate = decorate;
    }

    public void addParam(ParamInfo param) {
        if (params == null) {
            params = new ArrayList<>();
        }
        params.add(param);
    }
}
