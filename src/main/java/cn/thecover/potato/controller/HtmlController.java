package cn.thecover.potato.controller;

import cn.thecover.potato.properties.CoreProperties;
import cn.thecover.potato.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author nihao 2021/11/14
 */
@RequestMapping("/")
public class HtmlController {
    @Autowired
    private CoreProperties properties;

    @RequestMapping("{key}.html")
    public void index(@PathVariable String key,
                      HttpServletResponse response,
                      HttpServletRequest request) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html; charset=utf-8");
        String text = PageUtil.getPage(request, properties.getPath(), key);
        response.getWriter().write(text);
    }

}
