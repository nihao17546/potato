package com.appcnd.potato.controller;

import com.appcnd.potato.exception.HandlerException;
import com.appcnd.potato.generate.boot.BootResult;
import com.appcnd.potato.generate.boot.GenerateBoot;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.properties.CoreProperties;
import com.appcnd.potato.util.PageUtil;
import com.appcnd.potato.util.Parser;
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
public class HtmlController {
    @Autowired
    private CoreProperties properties;
    @Autowired
    private GenerateBoot generateBoot;
    private final long expireTime = 1000L * 60 * 60 * 24;

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

    @RequestMapping({"db.html"})
    public void db(HttpServletResponse response,
                   HttpServletRequest request) throws IOException {
        doExtra(response, request, "db");
    }

    @RequestMapping({"operate.html"})
    public void operate(HttpServletResponse response,
                        HttpServletRequest request) throws IOException {
        doExtra(response, request, "operate");
    }

    @RequestMapping({"search.html"})
    public void search(HttpServletResponse response,
                       HttpServletRequest request) throws IOException {
        doExtra(response, request, "search");
    }

    @RequestMapping({"storage.html"})
    public void storage(HttpServletResponse response,
                        HttpServletRequest request) throws IOException {
        doExtra(response, request, "storage");
    }

    @RequestMapping({"table.html"})
    public void table(HttpServletResponse response,
                      HttpServletRequest request) throws IOException {
        doExtra(response, request, "table");
    }

    private void doExtra(HttpServletResponse response,
                         HttpServletRequest request, String key) throws IOException {
        request.setAttribute("potatoPath", properties.getPath());
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html; charset=utf-8");
        Enumeration<String> enu = request.getParameterNames();
        Map<String,String> params = new HashMap<>();
        params.put("potatoPath", properties.getPath());
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

    @RequestMapping("{path}.html")
    public void extra(HttpServletResponse response,
                      HttpServletRequest request,
                      @PathVariable String path) throws IOException {
        String htmlKey = "/" + path + ".html";
        BootResult bootResult = generateBoot.getLoaded(htmlKey);
        if (bootResult == null) {
            throw new HandlerException(HttpStatus.NOT_FOUND);
        }
        String html = bootResult.getHtml().get(htmlKey).getSource();
        html = Parser.parse(html, "contextPath", request.getContextPath());
        html = Parser.parse(html, "potatoPath", properties.getPath());
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html; charset=utf-8");
        // 控制浏览器缓存
//        response.setDateHeader("expires",System.currentTimeMillis() + expireTime);
        response.getWriter().write(html);
    }

}
