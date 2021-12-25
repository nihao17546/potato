package cn.thecover.potato.controller;

import cn.thecover.potato.properties.CoreProperties;
import cn.thecover.potato.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author nihao 2021/11/14
 */
@RequestMapping("/")
public class HtmlController extends BaseController {
    @Autowired
    private CoreProperties properties;

    @RequestMapping("{key}.html")
    public void index(@PathVariable String key,
                      HttpServletResponse response,
                      HttpServletRequest request) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html; charset=utf-8");
        Enumeration enu = request.getParameterNames();
        Map<String,String> params = new HashMap<>();
        while(enu.hasMoreElements()){
            String k = (String) enu.nextElement();
            String v = request.getParameter(k);
            params.put(k, v);
        }
        params.put("random", (new Random().nextInt(10) + 1) + "");
        String text = PageUtil.getPage(request, properties.getPath(), key, params);
        response.getWriter().write(text);
    }

}
