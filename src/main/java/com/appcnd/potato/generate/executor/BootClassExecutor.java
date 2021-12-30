package com.appcnd.potato.generate.executor;

import com.appcnd.potato.generate.annotation.AnnotationInfo;
import com.appcnd.potato.generate.context.ClassField;
import com.appcnd.potato.generate.context.GenerateContext;
import com.appcnd.potato.generate.context.JavaClassContext;
import com.appcnd.potato.generate.method.MethodInfo;
import com.appcnd.potato.model.constant.BasicConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nihao 2021/12/23
 */
public class BootClassExecutor extends ClassExecutor {
    public BootClassExecutor(GenerateContext generateContext) {
        super(generateContext);
    }

    @Override
    protected void handleBefore() {
        if (generateContext.getJavaClassContexts() != null) {
            for (JavaClassContext context : generateContext.getJavaClassContexts()) {
                handleTransactional(context);
            }
        }
    }

    /**
     * 处理Transactional注解事务
     * Spring @Transactional 注解事务，cglib代理创建子类的时候，使用的classloader不是自定义PotatoClassLoader，
     * 加载不到Class，所以使用手动事务方式
     * @param context
     */
    private void handleTransactional(JavaClassContext context) {
        if (!CollectionUtils.isEmpty(context.getMethods())) {
            List<MethodInfo> methodInfos = new ArrayList<>();
            for (MethodInfo methodInfo : context.getMethods()) {
                if (!CollectionUtils.isEmpty(methodInfo.getAnnotations())) {
                    AnnotationInfo annotationInfo = new AnnotationInfo(Transactional.class.getName());
                    if (methodInfo.getAnnotations().contains(annotationInfo)) {
                        methodInfo.getAnnotations().remove(annotationInfo);
                        methodInfos.add(methodInfo);
                    }
                }
            }
            if (!methodInfos.isEmpty()) {
                AnnotationInfo autowired = new AnnotationInfo(Autowired.class.getName());
                AnnotationInfo qualifier = new AnnotationInfo(Qualifier.class.getName());
                qualifier.addField("value", BasicConstant.beanNamePrefix + "DataSourceTransactionManager");
                ClassField classField = new ClassField("private", DataSourceTransactionManager.class.getName(),
                        "transactionManager",
                        autowired, qualifier);
                context.addField(classField);
                for (MethodInfo methodInfo : methodInfos) {
                    if (Boolean.TRUE.equals(methodInfo.getHasContent()) &&
                            methodInfo.getContent() != null && !methodInfo.getContent().isEmpty()) {
                        String[] strings = methodInfo.getContent().split("\\n");
                        StringBuilder sb = new StringBuilder();

                        methodInfo.addContentClass(TransactionStatus.class.getName());
                        methodInfo.addContentClass(DefaultTransactionDefinition.class.getName());
                        sb.append("        ").append(TransactionStatus.class.getName())
                                .append(" transactionStatus = transactionManager.getTransaction(new ")
                                .append(DefaultTransactionDefinition.class.getName()).append("());\n");
                        sb.append("            try {\n");
                        for (String str : strings) {
                            sb.append("        ").append(str).append("\n");
                        }
                        sb.append("                ").append("transactionManager.commit(transactionStatus);\n");
                        sb.append("            } catch (Exception e) {\n");
                        sb.append("                transactionManager.rollback(transactionStatus);\n");
                        sb.append("                throw new RuntimeException(e);\n");
                        sb.append("            }\n");
                        methodInfo.setContent(sb.toString());
                    }
                }
            }
        }
    }
}
