package com.appcnd.potato.meta.conf.form.operate.elements;

import com.appcnd.potato.meta.conf.form.annotition.HtmlField;
import com.appcnd.potato.meta.conf.form.enums.ElementSize;
import com.appcnd.potato.meta.conf.form.operate.enums.OperateElementType;
import com.appcnd.potato.util.CommonUtil;
import lombok.Data;

import java.util.Objects;

/**
 * @author nihao 2021/08/06
 */
@Data
public class RemoteSelectOperateElement extends OperateElement {
    private final OperateElementType elementType = OperateElementType.SELECT_REMOTE;
    @HtmlField
    private ElementSize size;
    @HtmlField
    private Boolean clearable = true;
    @HtmlField
    private String placeholder;
    @HtmlField
    private Boolean filterable = true;
    private String sql;
    private String labelColumn;
    private String valueColumn;

    public void setSql(String sql) {
        // 校验sql合法性
        CommonUtil.checkSelectSql(sql);
        this.sql = sql;
    }

    @Override
    public String getHtml() {
        String field = getFieldName();
        StringBuilder sb = new StringBuilder();
        String optionName = "data" + hashCode();
        optionName = optionName.replaceAll("-", "A");
        sb.append("            <el-form-item label=\"").append(getLabel()).append(":\" prop=\"").append(field).append("\" label-width=\"100px\">\n")
                .append("                <el-select style=\"width: 100%\" v-model.trim=\"form.").append(field).append("\"");
        sb.append(getFieldHtml()).append(">\n");
        sb.append("                    <el-option\n");
        sb.append("                      v-for=\"item in ").append(optionName).append("\"\n");
        sb.append("                      :key=\"item.").append(valueColumn).append("\"\n");
        sb.append("                      :label=\"item.").append(labelColumn).append("\"\n");
        sb.append("                      :value=\"item.").append(valueColumn).append("\">\n");
        sb.append("                    </el-option>\n");
        sb.append("                </el-select>\n")
                .append("            </el-form-item>\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o.getClass().equals(this.getClass())) {
            RemoteSelectOperateElement that = (RemoteSelectOperateElement) o;
            return Objects.equals(sql, that.sql) &&
                    Objects.equals(labelColumn, that.labelColumn) &&
                    Objects.equals(valueColumn, that.valueColumn);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sql, labelColumn, valueColumn);
    }
}
