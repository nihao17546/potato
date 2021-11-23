package cn.thecover.potato.util;

import cn.thecover.potato.model.constant.BasicConstant;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Map;

/**
 * created by nihao 2020/07/08
 */
public class PageUtil {

    public static String getPage(HttpServletRequest request, String commonCmsUrl, String page) throws IOException {
        return getPage(request, commonCmsUrl, page, null);
    }

    public static String getPage(HttpServletRequest request, String commonCmsUrl, String page, Map<String,String> params) throws IOException {
        String filePath = BasicConstant.resourcePath
                + request.getRequestURI().substring(request.getContextPath().length() + request.getServletPath().length())
                + "/htmls/" + page + ".html";
        String text = readFromResource(filePath);
        if (text == null) {
            return text;
        }
        text = Parser.parse(text, "contextPath", request.getContextPath() + commonCmsUrl);
        if (params != null && !params.isEmpty()) {
            for(Map.Entry<String, String> entry : params.entrySet()){
                text = Parser.parse(text, entry.getKey(), entry.getValue());
            }
        }
        return text;
    }

    private static String readFromResource(String resource) {
        if (resource == null
                || resource.isEmpty()
                || resource.contains("..")
                || resource.contains("?")
                || resource.contains(":")) {
            return null;
        }

        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (in == null) {
                in = PageUtil.class.getResourceAsStream(resource);
            }

            if (in == null) {
                return null;
            }

            String text = read(in);
            return text;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String read(InputStream in) {
        if (in == null) {
            return null;
        }

        InputStreamReader reader;
        try {
            reader = new InputStreamReader(in, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return read(reader);
    }

    private static String read(Reader reader) {
        if (reader == null) {
            return null;
        }

        try {
            StringWriter writer = new StringWriter();

            char[] buffer = new char[1024 * 4];
            int n = 0;
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }

            return writer.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("read error", ex);
        }
    }

}
