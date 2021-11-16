package cn.thecover.potato.generate.boot;

import cn.thecover.potato.util.CommonUtil;
import cn.thecover.potato.util.SpringContextUtil;
import javassist.util.proxy.DefineClassHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private final Map<Integer,BootResult> loadMap = new ConcurrentHashMap<>();

    public BootResult getLoaded(Integer metaId) {
        return loadMap.get(metaId);
    }

    public void unLoad(Integer metaId, List<String> htmls) {
        if (loadMap.containsKey(metaId)) {
            synchronized(metaId.toString().intern()) {
                if (loadMap.containsKey(metaId)) {
                    BootResult bootResult = loadMap.get(metaId);
                    for (BootResult.Java java : bootResult.getControllers()) {
                        String beanId = CommonUtil.getClassNameField(CommonUtil.getSimpleClassName(java.getClassName()));
                        springContextUtil.unregisterController(beanId);
                        springContextUtil.removeBean(beanId);
                    }
                    for (BootResult.Java java : bootResult.getServiceImpls()) {
                        springContextUtil.removeBean(CommonUtil.getClassNameField(CommonUtil.getSimpleClassName(java.getClassName())));
                    }
                    for (BootResult.Mapper mapper : bootResult.getMappers()) {
                        springContextUtil.destroySingleton(mapper.getMapperId());
                    }
                    loadMap.remove(metaId);
                    for (String html : htmls) {
                        htmlServlet.removeCache(html);
                    }
                }
            }
        }
    }

    public void boot(BootResult bootResult) {
        if (!loadMap.containsKey(bootResult.getId())) {
            synchronized (bootResult.getId().toString().intern()) {
                if (!loadMap.containsKey(bootResult.getId())) {
                    log.info("加载{}", bootResult.getId());
                    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                    StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
                    MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager);
                    List<JavaFileObject> fileObjectList = new ArrayList<>();
                    for (BootResult.Java java : bootResult.getAllJava()) {
                        JavaFileObject javaFileObject = new MemoryInputJavaFileObject(java.getClassName(), java.getSource());
                        fileObjectList.add(javaFileObject);
                    }
                    Map<String, byte[]> mapBytes = null;
                    try {
                        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null,
                                null, null, fileObjectList);
                        Boolean result = task.call();
                        if (result == null || !result.booleanValue()) {
                            throw new RuntimeException("Compilation failed.");
                        }
                        mapBytes =  manager.getClassBytes();
                    } finally {
                        try {
                            manager.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Map<String,byte[]> needLoad = new LinkedHashMap<>();
                    for (BootResult.Java java : bootResult.getPo()) {
                        byte[] bytes = mapBytes.get(java.getClassName());
                        needLoad.put(java.getClassName(), bytes);
                    }
                    for (BootResult.Java java : bootResult.getDto()) {
                        byte[] bytes = mapBytes.get(java.getClassName());
                        needLoad.put(java.getClassName(), bytes);
                    }
                    for (BootResult.Java java : bootResult.getVo()) {
                        byte[] bytes = mapBytes.get(java.getClassName());
                        needLoad.put(java.getClassName(), bytes);
                    }
                    for (BootResult.Java java : bootResult.getDao()) {
                        byte[] bytes = mapBytes.get(java.getClassName());
                        needLoad.put(java.getClassName(), bytes);
                    }
                    for (BootResult.Java java : bootResult.getServices()) {
                        byte[] bytes = mapBytes.get(java.getClassName());
                        needLoad.put(java.getClassName(), bytes);
                    }
                    for (BootResult.Java java : bootResult.getServiceImpls()) {
                        byte[] bytes = mapBytes.get(java.getClassName());
                        needLoad.put(java.getClassName(), bytes);
                    }
                    for (BootResult.Java java : bootResult.getControllers()) {
                        byte[] bytes = mapBytes.get(java.getClassName());
                        needLoad.put(java.getClassName(), bytes);
                    }

                    for (String className : needLoad.keySet()) {
                        byte[] bytes = needLoad.get(className);
                        try {
                            Class.forName(className);
                        } catch (ClassNotFoundException e1) {
                            try {
                                DefineClassHelper.toClass(className, null, this.getClass().getClassLoader(), null, bytes);
                                log.info("加载类:{}", className);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    for (BootResult.Mapper mapper : bootResult.getMappers()) {
                        MapperBoot.addMapper(mapper.getMapperId(), mapper.getSource());
                    }
                    for (BootResult.Java java : bootResult.getServiceImpls()) {
                        springContextUtil.addBean(java.getClassName(), CommonUtil.getClassNameField(CommonUtil.getSimpleClassName(java.getClassName())));
                    }
                    for (BootResult.Java java : bootResult.getControllers()) {
                        System.out.println(java.getSource());
                        String beanId = CommonUtil.getClassNameField(CommonUtil.getSimpleClassName(java.getClassName()));
                        springContextUtil.registerController(beanId, java.getClassName());
                    }

                    bootResult.clear();
                    loadMap.put(bootResult.getId(), bootResult);
                }
            }
        }
    }
}
