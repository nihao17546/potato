package cn.thecover.potato.generate.executor;

import cn.thecover.potato.generate.annotation.AnnotationInfo;
import cn.thecover.potato.generate.context.ClassConstructor;
import cn.thecover.potato.generate.context.ClassField;
import cn.thecover.potato.generate.context.GenerateContext;
import cn.thecover.potato.generate.context.JavaClassContext;
import cn.thecover.potato.generate.method.MethodInfo;
import cn.thecover.potato.generate.method.ParamInfo;
import cn.thecover.potato.util.SimpleDateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @author nihao 2021/07/12
 */
@Slf4j
public class ClassExecutor extends Executor {
    private final String FILE_PATH = "codeless/backend/Class.java";
    protected GenerateContext generateContext;

    public ClassExecutor(GenerateContext generateContext) {
        Assert.notNull(generateContext, "generateContext can not be null");
        this.generateContext = generateContext;
    }

    protected void handleBefore() {}

    @Override
    protected Map<String, Map<String, String>> analysis() {
        handleBefore();
        if (CollectionUtils.isEmpty(generateContext.getJavaClassContexts())) {
            return new HashMap<>(0);
        }
        Map<String,Map<String,String>> result = new HashMap<>(generateContext.getJavaClassContexts().size());
        for (JavaClassContext context : generateContext.getJavaClassContexts()) {
            Map<String,String> map = new HashMap<>();
            map.put("packageName", context.getPackageName());
            map.put("version", context.getVersion().toString());
            map.put("now", SimpleDateUtil.format(new Date()));
            map.put("className", context.getName());

            if (!CollectionUtils.isEmpty(context.getAnnotations())) {
                StringBuilder sb = new StringBuilder();
                List<String> strings = fillAnnotation(context.getAnnotations(), context);
                for (String str : strings) {
                    sb.append(str).append("\n");
                }
                sb.append(context.getDecorate());
                map.put("decorate", sb.toString());
            } else {
                map.put("decorate", context.getDecorate());
            }

            StringBuilder classNameSuffixBuilder = new StringBuilder();
            if (context.getExtendsClassName() != null && !context.getExtendsClassName().isEmpty()) {
                String className = context.addImport(context.getExtendsClassName());
                classNameSuffixBuilder.append("extends ").append(className).append(" ");
            }
            if (!CollectionUtils.isEmpty(context.getImplementsClassNames())) {
                int index = 0;
                for (String className : context.getImplementsClassNames()) {
                    className = context.addImport(className);
                    if (index ++ == 0) {
                        classNameSuffixBuilder.append("implements ");
                    }  else {
                        classNameSuffixBuilder.append(", ");
                    }
                    classNameSuffixBuilder.append(className);
                }
                classNameSuffixBuilder.append(" ");
            }
            map.put("classNameSuffix", classNameSuffixBuilder.toString());

            StringBuilder fieldsBuilder = new StringBuilder();
            if (context.getFields() != null && !context.getFields().isEmpty()) {
                for (ClassField classField : context.getFields()) {
                    if (classField.getAnnotations() != null && !classField.getAnnotations().isEmpty()) {
                        List<String> list = fillAnnotation(classField.getAnnotations(), context);
                        for (String str : list) {
                            fieldsBuilder.append("    ").append(str).append("\n");
                        }
                    }
                    String className = context.addImport(classField.getClassName());
                    fieldsBuilder.append("    ").append(classField.getDecorate()).append(" ").append(className).append(" ").append(classField.getName()).append(";\n");
                }
            }
            map.put("fields", fieldsBuilder.toString());

            StringBuilder constructorBuilder = new StringBuilder();
            if (!CollectionUtils.isEmpty(context.getConstructors())) {
                for (ClassConstructor constructor : context.getConstructors()) {
                    constructorBuilder.append("    ");
                    constructorBuilder.append(constructor.getDecorate()).append(" ").append(context.getName()).append("(");
                    if (!CollectionUtils.isEmpty(constructor.getParams())) {
                        fillParam(constructor.getParams(), constructorBuilder, context);
                    }
                    constructorBuilder.append(") {\n");
                    if (constructor.getContent() != null && !constructor.getContent().isEmpty()) {
                        constructorBuilder.append(constructor.getContent());
                    }
                    constructorBuilder.append("    }\n");
                }
            }
            map.put("constructors", constructorBuilder.toString());

            StringBuilder methodsBuilder = new StringBuilder();
            if (!CollectionUtils.isEmpty(context.getMethods())) {
                for (MethodInfo methodInfo : context.getMethods()) {
                    methodsBuilder.append("    \n");
                    if (!CollectionUtils.isEmpty(methodInfo.getAnnotations())) {
                        List<String> strings = fillAnnotation(methodInfo.getAnnotations(), context);
                        for (String str : strings) {
                            methodsBuilder.append("    ").append(str).append("\n");
                        }
                    }

                    methodsBuilder.append("    ");
                    methodsBuilder.append(methodInfo.getDecorate()).append(" ");
                    if (methodInfo.getReturnString() != null && !methodInfo.getReturnString().isEmpty()) {
                        String str = methodInfo.getReturnString();
                        if (!CollectionUtils.isEmpty(methodInfo.getContentClass())) {
                            for (String className : methodInfo.getContentClass()) {
                                String simpleClassName = context.addImport(className);
                                str = str.replaceAll(className, simpleClassName);
                            }
                        }
                        methodsBuilder.append(str);
                    } else {
                        methodsBuilder.append("void");
                    }
                    methodsBuilder.append(" ").append(methodInfo.getMethodName()).append("(");
                    if (!CollectionUtils.isEmpty(methodInfo.getParams())) {
                        fillParam(methodInfo.getParams(), methodsBuilder, context);
                    }
                    if (Boolean.FALSE.equals(methodInfo.getHasContent())) {
                        methodsBuilder.append(");");
                    } else {
                        methodsBuilder.append(") {\n");
                        if (methodInfo.getContent() != null && !methodInfo.getContent().isEmpty()) {
                            String str = methodInfo.getContent();
                            if (!CollectionUtils.isEmpty(methodInfo.getContentClass())) {
                                for (String className : methodInfo.getContentClass()) {
                                    String simpleClassName = context.addImport(className);
                                    str = str.replaceAll(className, simpleClassName);
                                }
                            }
                            methodsBuilder.append(str);
                        }
                        methodsBuilder.append("    }");
                    }
                    methodsBuilder.append("\n");
                }
            }
            map.put("methods", methodsBuilder.toString());

            StringBuilder importBuilder = new StringBuilder();
            if (context.getImports() != null) {
                List<String> list = new ArrayList<>(context.getImports());
                Collections.sort(list);
                for (String className : list) {
                    if (!className.startsWith("java.lang.")) {
                        importBuilder.append("import ").append(className).append(";\n");
                        generateContext.addNeedLoadClasse(className);
                    }
                }
            }
            map.put("imports", importBuilder.toString());
            result.put(getJavaPath(context), map);
        }
        return result;
    }

    private List<String> fillAnnotation(List<AnnotationInfo> annotationInfoList, JavaClassContext context) {
        List<String> list = new ArrayList<>(annotationInfoList.size());
        for (AnnotationInfo annotationInfo : annotationInfoList) {
            StringBuilder stringBuilder = new StringBuilder();
            String annotationName = context.addImport(annotationInfo.getName());
            stringBuilder.append("@").append(annotationName);
            if (!CollectionUtils.isEmpty(annotationInfo.getFields())) {
                stringBuilder.append("(");
                int fieldIndex = 0;
                for (String key : annotationInfo.getFields().keySet()) {
                    if (fieldIndex ++ != 0) {
                        stringBuilder.append(", ");
                    }
                    Object value = annotationInfo.getFields().get(key);
                    stringBuilder.append(key).append(" = ");
                    if (value instanceof String) {
                        stringBuilder.append("\"").append(value).append("\"");
                    } else {
                        context.addImport(value.getClass().getName());
                        stringBuilder.append(value);
                    }
                }
                stringBuilder.append(")");
            }
            list.add(stringBuilder.toString());
        }
        return list;
    }

    private void fillParam(List<ParamInfo> paramInfoList, StringBuilder stringBuilder, JavaClassContext context) {
        int pIndex = 0;
        for (ParamInfo paramInfo : paramInfoList) {
            if (pIndex ++ > 0) {
                stringBuilder.append(", ");
            }
            if (!CollectionUtils.isEmpty(paramInfo.getAnnotations())) {
                List<String> list = fillAnnotation(paramInfo.getAnnotations(), context);
                int index = 0;
                for (String str : list) {
                    if (index ++ > 0) {
                        stringBuilder.append(" ");
                    }
                    stringBuilder.append(str);
                }
                stringBuilder.append(" ");
            }
            String className = context.addImport(paramInfo.getType());
            stringBuilder.append(className).append(" ").append(paramInfo.getName());
        }
    }

    @Override
    protected String getFile() {
        return FILE_PATH;
    }

    private String getJavaPath(JavaClassContext context) {
        return "backend" + File.separator + "src" +  File.separator+ "main" + File.separator + "java" + File.separator +
                context.getPackageName().replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + File.separator + context.getName() + ".java";
    }
}
