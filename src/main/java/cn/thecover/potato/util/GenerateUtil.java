package cn.thecover.potato.util;

import cn.thecover.potato.generate.context.ClassConstructor;
import cn.thecover.potato.generate.context.ClassField;
import cn.thecover.potato.generate.context.JavaClassContext;
import cn.thecover.potato.generate.method.MethodInfo;
import cn.thecover.potato.generate.method.ParamInfo;
import cn.thecover.potato.meta.conf.db.Column;
import cn.thecover.potato.meta.conf.table.UIColumn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author nihao 2021/07/13
 */
public class GenerateUtil {

    public static List<ClassField> getFields(List<Column> columns) {
        return columns.stream().map(column -> {
            ClassField field = new ClassField("private", column.getJavaType().getName(), CamelUtil.underlineToCamel(column.getField()));
            return field;
        }).collect(Collectors.toList());
    }

    public static List<ClassField> getFieldsFromUiColumn(List<UIColumn> columns, Map<String,String> columnMap) {
        return columns.stream().map(column -> {
            String name = columnMap.get(FieldUtil.getField(column.getTable(), column.getColumn().getField()));
            ClassField field = new ClassField("private", column.getColumn().getJavaType().getName(), CamelUtil.underlineToCamel(name));
            return field;
        }).collect(Collectors.toList());
    }

    public static List<MethodInfo> getSetterAndGetterMethods(List<ClassField> fields) {
        List<MethodInfo> list = new ArrayList<>(fields.size() * 2);
        for (ClassField classField : fields) {
            MethodInfo get = new MethodInfo();
            get.setDecorate("public");
            get.addContentClass(classField.getClassName());
            get.setReturnString(classField.getClassName());
            get.setMethodName(CamelUtil.get(classField.getName()));
            get.setContent(new StringBuilder().append("        return ")
                    .append("this.").append(classField.getName()).append(";\n").toString());
            list.add(get);

            MethodInfo set = new MethodInfo();
            set.setDecorate("public");
            set.setMethodName(CamelUtil.set(classField.getName()));
            ParamInfo paramInfo = new ParamInfo(classField.getClassName(), classField.getName());
            set.addParam(paramInfo);
            set.setContent(new StringBuilder().append("        this.").append(classField.getName())
                    .append(" = ").append(classField.getName()).append(";\n").toString());
            list.add(set);
        }
        return list;
    }

    public static JavaClassContext getListVOJavaClass(String packageName, Integer version) {
        JavaClassContext javaClassContext = new JavaClassContext(packageName, version, "public class", "ListVO");
        javaClassContext.addImplementsClassName(Serializable.class.getName());
        javaClassContext.addField(new ClassField("private", Long.class.getName(), "count"));
        javaClassContext.addField(new ClassField("private", List.class.getName(), "list"));
        ClassConstructor constructor = new ClassConstructor("public");
        String s = javaClassContext.addImport(ArrayList.class.getName());
        constructor.setContent("        this.list = new " + s + "<>();\n        this.count = 0L;\n");
        javaClassContext.addConstructor(constructor);

        ClassConstructor constructor2 = new ClassConstructor("public");
        constructor2.addParam(new ParamInfo(List.class.getName(), "list"));
        constructor2.addParam(new ParamInfo(Long.class.getName(), "count"));
        constructor2.setContent("        this.list = list;\n        this.count = count;\n");
        javaClassContext.addConstructor(constructor2);

        javaClassContext.addMethods(GenerateUtil.getSetterAndGetterMethods(javaClassContext.getFields()));
        return javaClassContext;
    }

    public static String getRandomPackageName() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< 9; i ++) {
            int a = random.nextInt('z' - 'a') + 97;
            sb.append((char) a).append(".");
        }
        int a = random.nextInt('z' - 'a') + 97;
        sb.append((char) a);
        return sb.toString();
    }

    public static String getRandomEntityName(String tableName) {
        Random random = new Random();
        String s = CamelUtil.underlineToCamel(tableName);
        return s + ((char) (random.nextInt('z' - 'a') + 97))
                + ((char) (random.nextInt('z' - 'a') + 97))
                + ((char) (random.nextInt('z' - 'a') + 97));
    }

    public static String getRandomField(String field) {
        Random random = new Random();
        return field + ((char) (random.nextInt('z' - 'a') + 97))
                + ((char) (random.nextInt('z' - 'a') + 97))
                + ((char) (random.nextInt('z' - 'a') + 97))
                + ((char) (random.nextInt('z' - 'a') + 97))
                + ((char) (random.nextInt('z' - 'a') + 97));
    }

    public static String getWord(String prefix, int id) {
        if (id <= 0) {
            throw new RuntimeException();
        }
        if (prefix == null) {
            prefix = "";
        }
        int r = id + 'a' - 1;
        if (r <= 'z') {
            return prefix + (char) r;
        }
        return getWord(prefix + 'a', r - 'z');
    }
}
