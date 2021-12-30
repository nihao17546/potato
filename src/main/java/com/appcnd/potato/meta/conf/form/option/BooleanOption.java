package com.appcnd.potato.meta.conf.form.option;

import lombok.Data;

/**
 * @author nihao 2021/07/07
 */
@Data
public class BooleanOption extends Option {
    private final String type = "boolean";
    private Boolean value;

    @Override
    public String getOptionHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<el-option :key=\"").append(getValue()).append("\"")
                .append(" label=\"").append(getLabel()).append("\"")
                .append(" :value=\"").append(getValue()).append("\"></el-option>");
        return sb.toString();
    }

    @Override
    public String getRadioHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<el-radio :label=\"").append(getValue()).append("\">").append(getLabel()).append("</el-radio>");
        return sb.toString();
    }
}
