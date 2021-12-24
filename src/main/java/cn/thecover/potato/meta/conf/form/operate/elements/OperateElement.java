package cn.thecover.potato.meta.conf.form.operate.elements;

import cn.thecover.potato.meta.conf.db.Column;
import cn.thecover.potato.meta.conf.form.Element;
import cn.thecover.potato.meta.conf.form.operate.Rule;
import cn.thecover.potato.util.CamelUtil;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * @author nihao 2021/08/06
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "elementType")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = InputOperateElement.class, name = "INPUT"),
        @JsonSubTypes.Type(value = TextareaOperateElement.class, name = "TEXT_AREA"),
        @JsonSubTypes.Type(value = SelectOperateElement.class, name = "SELECT"),
        @JsonSubTypes.Type(value = RemoteSelectOperateElement.class, name = "SELECT_REMOTE"),
        @JsonSubTypes.Type(value = RadioOperateElement.class, name = "RADIO"),
        @JsonSubTypes.Type(value = DateTimeOperateElement.class, name = "DATE_TIME"),
        @JsonSubTypes.Type(value = CreateTimeElement.class, name = "CREATE_TIME"),
        @JsonSubTypes.Type(value = UpdateTimeElement.class, name = "UPDATE_TIME"),
        @JsonSubTypes.Type(value = ImageElement.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = MarkdownElement.class, name = "MARKDOWN")
})
public abstract class OperateElement extends Element {
    // 是否可更新
    private Boolean canEdit = true;
    // 校验规则
    private Rule rule;
    // 字段
    private Column column;

    public abstract String getHtml();

    public String getFieldName() {
        return CamelUtil.underlineToCamel(column.getField());
    }

    @Override
    protected String getFieldHtml() {
        String s = super.getFieldHtml();
        if (Boolean.FALSE.equals(canEdit)) {
            s = s + " :disabled=\"formTitle == '编辑'\" ";
        }
        return s;
    }
}
