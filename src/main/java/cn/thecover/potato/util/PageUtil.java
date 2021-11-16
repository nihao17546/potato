package cn.thecover.potato.util;

import cn.thecover.potato.model.constant.BasicConstant;
import com.alibaba.druid.util.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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
        String text = Utils.readFromResource(filePath);
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

}
