package cn.thecover.potato.generate.boot;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nihao 2021/11/24
 */
@Slf4j
public class PotatoClassLoader extends ClassLoader {
    private String path;
    private ClassLoader parent;
    private final Map<String,Class> classMap;
    public PotatoClassLoader(ClassLoader parent, String classPath) {
        super(parent);
        this.path = classPath;
        this.parent = parent;
        this.classMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void finalize() throws Throwable {
        log.info("PotatoClassLoader 卸载: {}", path);
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        if (classMap.containsKey(className)) {
            return classMap.get(className);
        }

        //这个classLoader的主要方法
        String classPath = className.replace(".", File.separator) + ".class";//将包转为目录
        String classFile = path + File.separator + classPath;//拼接完整的目录
        Class clazz = null;
        byte[] data = null;
        try {
            data = getClassFileBytes(classFile);
        } catch (Exception e) {}
        if (data == null) {
            clazz = parent.loadClass(className);
        } else {
            try {
                clazz = defineClass(className, data, 0, data.length);
                if (null == clazz) {//如果在这个类加载器中都不能找到这个类的话，就真的找不到了
                    throw new ClassNotFoundException(className);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        classMap.put(className, clazz);
        return clazz;

    }

    private byte[] getClassFileBytes(String classFile) throws Exception {
        //采用NIO读取
        FileInputStream fis = new FileInputStream(classFile);
        FileChannel fileC = fis.getChannel();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel outC = Channels.newChannel(baos);
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        while (true) {
            int i = fileC.read(buffer);
            if (i == 0 || i == -1) {
                break;
            }
            buffer.flip();
            outC.write(buffer);
            buffer.clear();
        }
        fis.close();
        return baos.toByteArray();
    }
}
