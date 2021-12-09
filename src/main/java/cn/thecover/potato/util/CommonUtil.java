package cn.thecover.potato.util;

import cn.thecover.potato.model.vo.HttpResult;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.ServerSocket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author nihao 2019/07/03
 */
public class CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static boolean isAjax(HttpServletRequest request) {
        return !request.getServletPath().endsWith(".html");
    }

    public static <T> T parse(String str, Class<T> clazz) {
        try {
            return JSON.parseObject(str, clazz);
        } catch (Exception e) {
            logger.error("json解析异常，json: {}", str, e);
            return null;
        }
    }

    public static String getCookieValue(String key, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(key)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void responseOutWithJson(HttpServletResponse response,
                                           HttpResult result) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.append(result.json());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static List split(List list, Integer pageSize) {
        int listSize = list.size();
        int page = (listSize + (pageSize - 1)) / pageSize;
        List<List> result = new ArrayList<>();
        for (int i = 0; i < page; i++) {
            List subList = new ArrayList();
            for (int j = 0; j < listSize; j++) {
                int pageIndex = ((j + 1) + (pageSize - 1)) / pageSize;
                if (pageIndex == (i + 1)) {
                    subList.add(list.get(j));
                }

                if ((j + 1) == ((j + 1) * pageSize)) {
                    break;
                }
            }
            result.add(subList);
        }
        return result;
    }

    public static File saveFile(String filePath, InputStream in) {
        OutputStream out = null;
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                boolean b = file.getParentFile().mkdirs();
                if (!b) {
                    throw new RuntimeException("保存文件，目录创建异常");
                }
            }
            out = new FileOutputStream(file);
            int length;
            byte[] bytes = new byte[1024];
            while ((length = in.read(bytes)) > 0) {
                out.write(bytes, 0, length);
            }
            return file;
        } catch (Exception e) {
            logger.error("保存文件异常,filePath:{}", filePath, e);
            throw new RuntimeException("文件保存失败");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("close OutputStream error", e);
                }
            }
        }
    }

    public static String getDataFromRequest(HttpServletRequest request) throws Exception {
        InputStream inputStream;
        StringBuffer sb = new StringBuffer();
        inputStream = request.getInputStream();
        String s;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((s = in.readLine()) != null) {
            sb.append(s);
        }
        in.close();
        inputStream.close();
        return sb.toString();
    }

    /**
     * 数据计算结果处理(四舍五入，保留两位小数)
     *
     * @param member      分子
     * @param denominator 分母
     * @return
     */
    public static String dateProcessing(int member, int denominator) {
        float num = (float) member / denominator;
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
    }

    /**
     * 数据计算结果处理(四舍五入，保留两位小数)
     *
     * @param member      分子
     * @param denominator 分母
     * @return
     */
    public static String dateProcessing(float member, float denominator) {
        float num = member / denominator;
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(num);
    }

    public static void buildErrorTip(HttpServletRequest request,
                                     Integer code, String msg) {
        if (code == null) {
            code = 500;
        }
        if (msg == null) {
            msg = "";
        }
        request.setAttribute("code", code);
        request.setAttribute("msg", msg);
        request.setAttribute("fromURI", request.getRequestURI());
    }

    public static void buildErrorTip(HttpServletRequest request) {
        buildErrorTip(request, null, null);
    }

    public static void buildErrorTip(HttpServletRequest request, int code) {
        buildErrorTip(request, code, null);
    }

    public static void buildErrorTip(HttpServletRequest request, String msg) {
        buildErrorTip(request, null, msg);
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("Cdn-Src-Ip");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip == null || ip.isEmpty()) {
            ip = "0.0.0.0";
        }
        return ip;
    }

    public static byte[] getBytes(File file){
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static String getRedirectUrl(HttpServletRequest request, String servletPath, String queryString) {
        String url = request.getRequestURL().toString().replace(request.getServletPath(), servletPath);
        if (queryString != null) {
            if (queryString.startsWith("?")) {
                url = url + queryString;
            } else {
                url = url + "?" + queryString;
            }
        }
        return url;
    }

    public static String getRequestUrl(HttpServletRequest request) {
        String requestUrl= request.getRequestURL().toString()
                + ((request.getQueryString() != null && !request.getQueryString().isEmpty()) ? "" : ("?" + request.getQueryString()));
        return requestUrl;
    }

    public static Integer getAvailablePort() {
        return getAvailablePort(null, null);
    }

    public static Integer getAvailablePort(Integer minPort, Integer maxPort) {
        if (minPort == null) {
            minPort = 1024;
        }
        if (maxPort == null) {
            maxPort = 65535;
        }
        if (minPort < 1024 || maxPort > 65535 || minPort > maxPort) {
            throw new IllegalArgumentException("端口范围:[1024-65535]");
        }
        Random random = new Random();
        int startPort = random.nextInt(maxPort) % (maxPort - minPort + 1) + minPort;
        int port = startPort;
        while (true) {
            if (checkPort(port)) {
                return port;
            }
            if (startPort == minPort) {
                if (port == maxPort) {
                    break;
                }
                port ++;
            } else {
                if (port == startPort - 1) {
                    break;
                }
                if (port < maxPort) {
                    port ++;
                } else {
                    port = minPort;
                }
            }
        }
        throw new RuntimeException("无可用端口");
    }

    private static boolean checkPort(int port) {
        try(ServerSocket serverSocket =  new ServerSocket(port)) {
            serverSocket.getLocalPort();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static byte[] serialize(Serializable object) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally{
            try {
                if (oos != null) {
                    oos.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String serializeJson(Object object) {
        try {
            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T unserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally{
            try {
                if (ois != null) {
                    ois.close();
                }
                if (bais != null) {
                    bais.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static <T> T unserialize(String data, Class<T> clazz) {
        try {
            ObjectMapper om = new ObjectMapper();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return om.readValue(data, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFromResource(String resource) {
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (in == null) {
                in = CommonUtil.class.getResourceAsStream(resource);
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
                } catch (Exception e) {
                    logger.warn("close error", e);
                }
            }
        }
    }

    private static String read(InputStream in) {
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(in, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return read(reader);
    }

    private static String read(Reader reader) {
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

    public static String getPackageName(String className) {
        if (!className.contains(".")) {
            return "";
        }
        return className.substring(0, className.lastIndexOf("."));
    }

    public static String getSimpleClassName(String className) {
        if (className.contains(".")) {
            return className.substring(className.lastIndexOf(".") + 1);
        }
        return className;
    }

    public static String getClassNameField(String className) {
        String simpleClassName = getSimpleClassName(className);
        char[] chars = simpleClassName.toCharArray();
        if (chars[0] >= 'A' && chars[0] <= 'Z') {
            int a = chars[0] + 32;
            chars[0] = (char) a;
        }
        return new String(chars);
    }

}
