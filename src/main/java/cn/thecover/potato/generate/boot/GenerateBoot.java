package cn.thecover.potato.generate.boot;

import cn.thecover.potato.exception.HandlerException;
import cn.thecover.potato.model.vo.HttpStatus;
import cn.thecover.potato.properties.CoreProperties;
import cn.thecover.potato.util.CommonUtil;
import cn.thecover.potato.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nihao 2021/07/16
 */
@Slf4j
public class GenerateBoot {
    @Autowired
    private SpringContextUtil springContextUtil;
    @Autowired
    private HtmlServlet htmlServlet;
    @Autowired
    private CoreProperties coreProperties;
    private DataSource dataSource;

    public GenerateBoot(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private final Map<Integer,BootResult> loadMap = new ConcurrentHashMap<>();

    public BootResult getLoaded(Integer metaId) {
        return loadMap.get(metaId);
    }

    public void unLoad(Integer metaId, List<String> htmls) {
        if (loadMap.containsKey(metaId)) {
            synchronized(metaId.toString().intern()) {
                if (loadMap.containsKey(metaId)) {
                    BootResult bootResult = loadMap.get(metaId);
                    String springBeanNamePrefix = bootResult.getId().toString() + "@";
                    for (BootResult.Java java : bootResult.getControllers()) {
                        String beanId = springBeanNamePrefix + CommonUtil.getClassNameField(CommonUtil.getSimpleClassName(java.getClassName()));
                        springContextUtil.unregisterController(beanId);
                        springContextUtil.removeBean(beanId);
                    }
                    for (BootResult.Java java : bootResult.getServiceImpls()) {
                        String beanId = springBeanNamePrefix + CommonUtil.getClassNameField(CommonUtil.getSimpleClassName(java.getClassName()));
                        springContextUtil.removeBean(beanId);
                    }
                    for (BootResult.Mapper mapper : bootResult.getMappers()) {
                        String beanId = springBeanNamePrefix + mapper.getMapperId();
                        springContextUtil.destroySingleton(beanId);
                    }
                    loadMap.remove(metaId);
                    for (String html : htmls) {
                        htmlServlet.removeCache(html);
                    }
                    File file = new File(coreProperties.getClassPath() + File.separator + metaId);
                    if (file.exists()) {
                        file.delete();
                    }
                    System.gc();
                }
            }
        }
    }

    private class StringObject extends SimpleJavaFileObject {
        private String contents = null;
        public StringObject(String className, String contents) {
            super(URI.create("String:///" + className + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return contents;
        }
    }

    private void compile(List<BootResult.Java> javas, String basicPath) {
        List<JavaFileObject> files = new ArrayList<>(javas.size());
        for (BootResult.Java java : javas) {
            String className = java.getClassName();
            String javaSource = java.getSource();
            String simpleClassName = CommonUtil.getSimpleClassName(className);
            StringObject so = new StringObject(simpleClassName, javaSource);
            JavaFileObject file = so;
            files.add(file);
        }
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager standardFileManager = javaCompiler.getStandardFileManager(null, null, null);
        Iterable options = Arrays.asList("-d", basicPath + "java");
        JavaCompiler.CompilationTask task = javaCompiler.getTask(null, standardFileManager, null, options, null, files);
        Boolean result = task.call();
        if (result) {
            log.info("class编译成功");
        } else {
            log.info("class编译异常");
            throw new RuntimeException("编译异常");
        }
    }

    public void boot(BootResult bootResult) throws Exception {
        if (!loadMap.containsKey(bootResult.getId())) {
            synchronized (bootResult.getId().toString().intern()) {
                if (!loadMap.containsKey(bootResult.getId())) {
                    log.info("加载{}", bootResult.getId());
                    String basicPath = coreProperties.getClassPath() + File.separator + bootResult.getId() + File.separator;
                    File f = new File(basicPath + "java");
                    if (!f.exists()) {
                        if (!f.mkdirs()) {
                            throw new RuntimeException(f.getAbsolutePath() + " 目录创建失败");
                        }
                    }

                    PotatoClassLoader classLoader = new PotatoClassLoader(getClass().getClassLoader(),
                            basicPath + "java");
                    bootResult.setClassLoader(classLoader);
                    List<BootResult.Java> javas = new ArrayList<>();
                    for (BootResult.Java java : bootResult.getPo()) {
                        javas.add(java);
                    }
                    for (BootResult.Java java : bootResult.getDto()) {
                        javas.add(java);
                    }
                    for (BootResult.Java java : bootResult.getVo()) {
                        javas.add(java);
                    }
                    for (BootResult.Java java : bootResult.getDao()) {
                        javas.add(java);
                    }
                    for (BootResult.Java java : bootResult.getServices()) {
                        javas.add(java);
                    }
                    for (BootResult.Java java : bootResult.getServiceImpls()) {
                        javas.add(java);
                    }
                    for (BootResult.Java java : bootResult.getControllers()) {
                        javas.add(java);
                    }
                    compile(javas, basicPath);
                    for (BootResult.Mapper mapper : bootResult.getMappers()) {
                        String simpleName = CommonUtil.getSimpleClassName(mapper.getMapperId());
                        String mapperFileName = simpleName + "Mapper.xml";
                        File file = new File(basicPath + "resources" +
                                File.separator + "mappers" + File.separator + mapperFileName);
                        if (!file.getParentFile().exists()) {
                            boolean b = file.getParentFile().mkdirs();
                            if (!b) {
                                log.error("创建目录:{} 失败", file.getParentFile().getAbsolutePath());
                                throw new RuntimeException("创建目录:" + file.getParentFile().getAbsolutePath() + "失败");
                            }
                        }
                        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                            byte[] bytes = mapper.getSource().getBytes();
                            fileOutputStream.write(bytes);
                        } catch (IOException e) {
                            throw new RuntimeException("文件:" + file.getName() + "输出异常");
                        }
                    }

                    TransactionFactory transactionFactory = new SpringManagedTransactionFactory();
                    Environment environment = new Environment ("development", transactionFactory, dataSource);
                    Configuration configuration = new Configuration(environment);
                    TypeAliasRegistry aliasRegistry = configuration.getTypeAliasRegistry();
                    for (BootResult.Java java : bootResult.getPo()) {
                        aliasRegistry.registerAlias(java.getClassName().toLowerCase(Locale.ENGLISH), classLoader.findClass(java.getClassName()));
                    }
                    for (BootResult.Java java : bootResult.getDto()) {
                        aliasRegistry.registerAlias(java.getClassName().toLowerCase(Locale.ENGLISH), classLoader.findClass(java.getClassName()));
                    }
                    for (BootResult.Java java : bootResult.getVo()) {
                        aliasRegistry.registerAlias(java.getClassName().toLowerCase(Locale.ENGLISH), classLoader.findClass(java.getClassName()));
                    }
                    for (BootResult.Java java : bootResult.getDao()) {
                        aliasRegistry.registerAlias(java.getClassName().toLowerCase(Locale.ENGLISH), classLoader.findClass(java.getClassName()));
                    }
                    Set<String> daoSet = new HashSet<>();
                    Set<String> serviceSet = new HashSet<>();
                    Set<String> controllerSet = new HashSet<>();
                    String springBeanNamePrefix = bootResult.getId().toString() + "@";
                    try {
                        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
                        for (BootResult.Mapper mapper : bootResult.getMappers()) {
                            String beanId = springBeanNamePrefix + mapper.getMapperId();
                            daoSet.add(beanId);
                            configuration.addMapper(classLoader.findClass(mapper.getMapperId()));
                            ByteArrayInputStream is = new ByteArrayInputStream(mapper.getSource().getBytes());
                            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(is, configuration, beanId, configuration.getSqlFragments());
                            xmlMapperBuilder.parse();

                            MapperFactoryBean mapperFactoryBean = new MapperFactoryBean();
                            mapperFactoryBean.setSqlSessionFactory(sqlSessionFactory);
                            mapperFactoryBean.setMapperInterface(classLoader.findClass(mapper.getMapperId()));
                            Object dao = mapperFactoryBean.getObject();
                            springContextUtil.registerSingleton(beanId, dao);
                            log.info("成功注册Mapper:{}", springContextUtil.getBean(beanId));
                        }

                        for (BootResult.Java java : bootResult.getServiceImpls()) {
                            try {
                                String beanId = springBeanNamePrefix + CommonUtil.getClassNameField(CommonUtil.getSimpleClassName(java.getClassName()));
                                serviceSet.add(beanId);
                                springContextUtil.registerBean(beanId, classLoader.findClass(java.getClassName()));
                                log.info("成功注册Service:{}", springContextUtil.getBean(beanId));
                            } catch (Exception e) {
                                log.error("注册Service异常:\n{}", java.getSource(), e);
                                throw new HandlerException(HttpStatus.SYSTEM_ERROR);
                            }
                        }
                        for (BootResult.Java java : bootResult.getControllers()) {
                            try {
                                String beanId = springBeanNamePrefix + CommonUtil.getClassNameField(CommonUtil.getSimpleClassName(java.getClassName()));
                                controllerSet.add(beanId);
                                springContextUtil.registerController(beanId, classLoader.findClass(java.getClassName()));
                                log.info("成功注册Controller:{}", springContextUtil.getBean(beanId));
                            } catch (Exception e) {
                                log.error("注册Controller异常:\n{}", java.getSource(), e);
                                throw new HandlerException(HttpStatus.SYSTEM_ERROR);
                            }
                        }
                    } catch (Exception e) {
                        for (String beanId : controllerSet) {
                            try {
                                springContextUtil.unregisterController(beanId);
                            } catch (Exception e1) {}
                            try {
                                springContextUtil.removeBean(beanId);
                            } catch (Exception e1) {}
                        }
                        for (String beanId : serviceSet) {
                            try {
                                springContextUtil.removeBean(beanId);
                            } catch (Exception e1) {}
                        }
                        for (String beanId : daoSet) {
                            try {
                                springContextUtil.destroySingleton(beanId);
                            } catch (Exception e1) {}
                        }
                        throw e;
                    }
                    loadMap.put(bootResult.getId(), bootResult);
                }
            }
        }
    }
}
