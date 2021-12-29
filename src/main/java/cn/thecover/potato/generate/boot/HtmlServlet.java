package cn.thecover.potato.generate.boot;

import cn.thecover.potato.cache.ResourceCache;
import cn.thecover.potato.cache.SoftResourceCache;
import cn.thecover.potato.properties.CoreProperties;
import cn.thecover.potato.util.DesUtil;
import cn.thecover.potato.util.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * created by nihao 2020/07/07
 */
@Slf4j
public class HtmlServlet extends HttpServlet {
    @Autowired
    private GenerateBoot generateBoot;
    @Autowired
    private CoreProperties coreProperties;

    private ResourceCache<String,String> cache;
    private final long expireTime = 1000L * 60 * 60 * 24;

    public HtmlServlet() {
        this.cache = new SoftResourceCache<>();
    }

    public void removeCache(String key) {
        cache.remove(key);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String key = request.getRequestURI().replaceFirst(request.getContextPath() + coreProperties.getPath(), "");
        String html = cache.get(key);
        if (html == null) {
            String path = key.replaceFirst("/boot/page/", "");
            if (path.endsWith(".html")) {
                path = path.substring(0, path.length() - ".html".length());
            } else {
                response.sendError(HttpStatus.NOT_FOUND.value(), "NOT FOUND");
                return;
            }
            String string = null;
            try {
                string = DesUtil.decrypt(path);
            } catch (Exception e) {
                response.sendError(HttpStatus.NOT_FOUND.value(), "NOT FOUND");
                return;
            }

            Integer metaId = null;
            Integer version = null;
            try {
                String[] strings = string.split(",");
                metaId = Integer.parseInt(strings[0]);
                version = Integer.parseInt(strings[1]);
            } catch (Exception e) {
                response.sendError(HttpStatus.NOT_FOUND.value(), "NOT FOUND");
                return;
            }
            BootResult bootResult = generateBoot.getLoaded(metaId);
            if (bootResult == null || !bootResult.getVersion().equals(version)) {
                response.sendError(HttpStatus.NOT_FOUND.value(), "NOT FOUND");
                return;
            }
            html = bootResult.getHtml().get(key).getSource();
            html = Parser.parse(html, "contextPath", request.getContextPath());
            html = Parser.parse(html, "potatoPath", coreProperties.getPath());
            log.debug("补充静态资源缓存:{}", key);
            cache.set(key, html);
        }

        // 控制浏览器缓存
        response.setDateHeader("expires",System.currentTimeMillis() + expireTime);
        response.setContentType("text/html; charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(html);
    }

}
