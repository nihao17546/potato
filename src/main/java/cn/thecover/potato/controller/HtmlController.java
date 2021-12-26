package cn.thecover.potato.controller;

import cn.thecover.potato.exception.HandlerException;
import cn.thecover.potato.model.vo.HttpStatus;
import cn.thecover.potato.properties.CoreProperties;
import cn.thecover.potato.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author nihao 2021/11/14
 */
@RequestMapping("/")
public class HtmlController extends BaseController {
    @Autowired
    private CoreProperties properties;

    @RequestMapping("index.html")
    public void index(HttpServletResponse response,
                      HttpServletRequest request) throws IOException {
        request.setAttribute("potatoPath", properties.getPath());
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html; charset=utf-8");
        Map<String,String> params = new HashMap<>();
        params.put("random", (new Random().nextInt(10) + 1) + "");
        String text = PageUtil.getPage(request, properties.getPath(), "index", params);
        response.getWriter().write(text);
    }

    @RequestMapping("error.html")
    public void error(HttpServletResponse response,
                      HttpServletRequest request) throws IOException {
        request.setAttribute("potatoPath", properties.getPath());
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html; charset=utf-8");
        String text = PageUtil.getPage(request, properties.getPath(), "error", null);
        response.getWriter().write(text);
    }

    @RequestMapping("{key}.html")
    public void index(@PathVariable String key,
                      HttpServletResponse response,
                      HttpServletRequest request) throws IOException {
        request.setAttribute("potatoPath", properties.getPath());
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html; charset=utf-8");
        Enumeration<String> enu = request.getParameterNames();
        Map<String,String> params = new HashMap<>();
        while(enu.hasMoreElements()){
            String k = enu.nextElement();
            String v = request.getParameter(k);
            params.put(k, v);
        }
        if (!params.containsKey("id")) {
            throw new HandlerException(HttpStatus.PARAM_ERROR);
        }
        String text = PageUtil.getPage(request, properties.getPath(), key, params);
        if (text == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        response.getWriter().write(text);
    }

}
