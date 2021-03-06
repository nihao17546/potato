package com.appcnd.potato.generate.boot;

import com.appcnd.potato.exception.ExceptionAssert;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.properties.CoreProperties;
import com.appcnd.potato.util.CommonUtil;
import com.appcnd.potato.util.SpringContextUtil;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author nihao 2021/07/16
 */
@Slf4j
public class GenerateBoot {
    @Autowired
    private SpringContextUtil springContextUtil;
    @Autowired
    private CoreProperties coreProperties;
    private DataSource dataSource;

    public GenerateBoot(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private final Map<Integer,BootResult> loadMap = new ConcurrentHashMap<>();
    private final Map<String,Integer> loadKey = new ConcurrentHashMap<>();
    private final Map<String,Integer> loadApiKey = new ConcurrentHashMap<>();

    public BootResult getLoaded(Integer metaId) {
        return loadMap.get(metaId);
    }

    public BootResult getLoaded(String htmlKey) {
        Integer metaId = loadKey.get(htmlKey);
        if (metaId == null) {
            return null;
        }
        return getLoaded(metaId);
    }

    public BootResult getLoadedByApi(String apiPrefix) {
        Integer metaId = loadApiKey.get(apiPrefix);
        if (metaId == null) {
            return null;
        }
        return getLoaded(metaId);
    }

    public void unLoad(Integer metaId) {
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
                    String path = coreProperties.getClassPath() + File.separator + metaId;
                    File file = new File(path);
                    if (file.exists()) {
                        boolean b = deleteDir(file);
                        if (!b) {
                            log.error("??????????????????: {}", path);
                        } else {
                            log.info("??????:{}?????????", path);
                        }
                    }
                    Set<String> htmls = bootResult.getHtml().keySet();
                    for (String htmlKey : htmls) {
                        loadKey.remove(htmlKey);
                    }
                    for (String httpRequest : bootResult.getHttpRequest()) {
                        loadApiKey.remove(httpRequest);
                    }
                    bootResult.clear();
                }
            }
        }
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //????????????????????????????????????
            for (int i=0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // ?????????????????????????????????
        return dir.delete();
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

    private String getClassPath(Set<String> needLoadClasses, Integer id) {
        Set<String> classPaths = new HashSet<>();
        Map<String,Set<String>> map = new HashMap<>();
        for (String className : needLoadClasses) {
            ClassLoader classLoader = null;
            try {
                classLoader = Class.forName(className).getClassLoader();
            } catch (ClassNotFoundException e) {
                continue;
            } catch (Exception e) {
                log.error("??????class: {} ??????", className, e);
                ExceptionAssert.throwException(HttpStatus.SYSTEM_ERROR.getCode(), "??????class:" + className + "??????");
            }
            if (classLoader != null) {
                if (classLoader instanceof URLClassLoader) {
                    URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                    for (URL url : urlClassLoader.getURLs()) {
                        String path = url.getPath();
                        try {
                            if (path.startsWith("file:")) {
                                path = path.replace("file:", "");
                                String[] strings = path.split("!/");
                                if (map.containsKey(strings[0])) {
                                    map.get(strings[0]).add(strings[1]);
                                } else {
                                    Set<String> set = new HashSet<>();
                                    set.add(strings[1]);
                                    map.put(strings[0], set);
                                }
                            } else {
                                classPaths.add(url.getFile());
                            }
                        } catch (Exception e) {
                            log.error("??????classpath: {} ??????", path, e);
                            ExceptionAssert.throwException(HttpStatus.SYSTEM_ERROR.getCode(), "??????classpath:" + path + "??????");
                        }
                    }
                }
            }
        }
        if (!map.isEmpty()) {
            for (String jar : map.keySet()) {
                Set<String> set = map.get(jar);
                JarFile jarFile = null;
                try {
                    jarFile = new JarFile(jar);
                } catch (Exception e) {
                    log.error("??????jar: {} ??????", jar, e);
                    ExceptionAssert.throwException(HttpStatus.SYSTEM_ERROR.getCode(), "??????jar:" + jar + "??????");
                }
                for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
                    JarEntry jarEntry = e.nextElement();
                    if (set.contains(jarEntry.getName())) {
                        String path = coreProperties.getClassPath() + File.separator + id + File.separator
                                + "classpath" + File.separator + jar + File.separator + jarEntry.getName();
                        try {
                            writeFile(jarFile, jarEntry, new File(path));
                            classPaths.add(path);
                        } catch (Exception ex) {
                            log.error("??????jar: {}->{} ??????: {}:{} ????????????: {}",
                                    jar, jarEntry.getName(), ex.getClass().getName(), ex.getMessage(), path);
                        }
                    }
                }
            }
        }
        if (classPaths.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String classPath : classPaths) {
            sb.append(classPath).append(File.pathSeparator);
        }
        return sb.toString();
    }

    private void writeFile(JarFile jarFile, JarEntry jarEntry, File file) throws Exception {
        InputStream in = jarFile.getInputStream(jarEntry);
        OutputStream os = null;
        try {
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new RuntimeException(file.getParentFile().getPath() + " ??????????????????");
                }
            }
            os = new BufferedOutputStream(new FileOutputStream(file));
            byte[] bytes = new byte[2048];
            int len;
            while((len = in.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void compile(List<BootResult.Java> javas, String basicPath, Set<String> needLoadClasses, Integer id) {
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
        try {
            List<String> options = new ArrayList<>();
            if (needLoadClasses != null) {
                String classPath = getClassPath(needLoadClasses, id);
                if (classPath != null) {
                    options.add("-classpath");
                    options.add(classPath);
                }
            }
            options.add("-d");
            options.add(basicPath + "java");
            JavaCompiler.CompilationTask task = javaCompiler.getTask(null, standardFileManager, null, options, null, files);
            Boolean result = task.call();
            if (result) {
                log.info("class????????????");
            } else {
                log.info("class????????????");
                throw new RuntimeException("????????????");
            }
        } finally {
            try {
                standardFileManager.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void boot(BootResult bootResult, Set<String> needLoadClasses) throws Exception {
        log.info("??????{}", bootResult.getId());
        File classPathFile = new File(coreProperties.getClassPath() + File.separator + bootResult.getId());
        if (classPathFile.exists()) {
            deleteDir(classPathFile);
        }
        String basicPath = coreProperties.getClassPath() + File.separator + bootResult.getId() + File.separator;
        File f = new File(basicPath + "java");
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new RuntimeException(f.getAbsolutePath() + " ??????????????????");
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
        for (BootResult.Java java : bootResult.getParam()) {
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
        compile(javas, basicPath, needLoadClasses, bootResult.getId());
        for (BootResult.Mapper mapper : bootResult.getMappers()) {
            String simpleName = CommonUtil.getSimpleClassName(mapper.getMapperId());
            String mapperFileName = simpleName + "Mapper.xml";
            File file = new File(basicPath + "resources" +
                    File.separator + "mappers" + File.separator + mapperFileName);
            if (!file.getParentFile().exists()) {
                boolean b = file.getParentFile().mkdirs();
                if (!b) {
                    log.error("????????????:{} ??????", file.getParentFile().getAbsolutePath());
                    throw new RuntimeException("????????????:" + file.getParentFile().getAbsolutePath() + "??????");
                }
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                byte[] bytes = mapper.getSource().getBytes();
                fileOutputStream.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException("??????:" + file.getName() + "????????????");
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
            bootResult.setSqlSessionFactory(sqlSessionFactory);
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
                log.info("????????????Mapper:{}", springContextUtil.getBean(beanId));
            }

            for (BootResult.Java java : bootResult.getServiceImpls()) {
                try {
                    String beanId = springBeanNamePrefix + CommonUtil.getClassNameField(CommonUtil.getSimpleClassName(java.getClassName()));
                    serviceSet.add(beanId);
                    springContextUtil.registerBean(beanId, classLoader.findClass(java.getClassName()));
                    log.info("????????????Service:{}", springContextUtil.getBean(beanId));
                } catch (Exception e) {
                    log.error("??????Service??????:\n{}", java.getSource(), e);
                    ExceptionAssert.throwException(HttpStatus.SYSTEM_ERROR);
                }
            }
            for (BootResult.Java java : bootResult.getControllers()) {
                try {
                    String beanId = springBeanNamePrefix + CommonUtil.getClassNameField(CommonUtil.getSimpleClassName(java.getClassName()));
                    controllerSet.add(beanId);
                    springContextUtil.registerController(beanId, classLoader.findClass(java.getClassName()));
                    log.info("????????????Controller:{}", springContextUtil.getBean(beanId));
                } catch (Exception e) {
                    log.error("??????Controller??????:\n{}", java.getSource(), e);
                    ExceptionAssert.throwException(HttpStatus.SYSTEM_ERROR);
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
        for (String htmlKey : bootResult.getHtml().keySet()) {
            loadKey.put(htmlKey, bootResult.getId());
        }
        for (String httpRequest : bootResult.getHttpRequest()) {
            loadApiKey.put(httpRequest, bootResult.getId());
        }
    }
}
