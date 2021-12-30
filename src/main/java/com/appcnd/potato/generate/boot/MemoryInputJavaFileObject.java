package com.appcnd.potato.generate.boot;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;
import java.nio.CharBuffer;

/**
 * @author nihao 2021/07/16
 */
public class MemoryInputJavaFileObject extends SimpleJavaFileObject {

    final String code;

    public MemoryInputJavaFileObject(String className, String code) {
        super(URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
        return CharBuffer.wrap(code);
    }
}