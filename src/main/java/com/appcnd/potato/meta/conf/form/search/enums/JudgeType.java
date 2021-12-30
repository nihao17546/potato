package com.appcnd.potato.meta.conf.form.search.enums;

/**
 * 判断方式
 * Created by nihao on 16/12/20.
 */
public enum JudgeType {
    /*
    相等
     */
    eq("相等", "="),

    /*
    大于
     */
    gt("大于", "&gt;"),

    /*
    小于
     */
    lt("小于", "&lt;"),

    /*
    大于等于
     */
    gteq("大于等于", "&gt;="),

    /*
    小于等于
     */
    lteq("小于等于", "&lt;="),

    /*
    in
     */
    in("集合", "in"),

    /*
    like模糊查询
     */
    like("模糊查询", "like"),

    /*
    在...区间
     */
    bt("区间", "between"),

    /*
    为空
     */
    isnull("为空", "IS NULL"),

    /*
    不为空
     */
    isnotnull("不为空", "IS NOT NULL");

    private String desc;
    private String sql;
    JudgeType(String desc, String sql) {
        this.desc = desc;
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public String getDesc() {
        return desc;
    }

}
