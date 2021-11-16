package cn.thecover.potato.generate.boot;

import cn.thecover.potato.cache.ResourceCache;
import cn.thecover.potato.cache.SoftResourceCache;
import cn.thecover.potato.dao.BootDao;
import cn.thecover.potato.model.po.Boot;
import cn.thecover.potato.util.CommonUtil;
import cn.thecover.potato.util.DesUtil;
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
    @Autowired(required = false)
    private BootDao bootDao;

    private ResourceCache<String,String> cache;
    private final long expireTime = 1000L * 60 * 60 * 24;

    public HtmlServlet() {
        this.cache = new SoftResourceCache<>();
    }

    void removeCache(String key) {
        cache.remove(key);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String key = request.getRequestURI().replaceFirst(request.getContextPath(), "");
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
            Boot boot = bootDao.selectByMetaIdAndVersion(metaId, version);
            if (boot == null) {
                response.sendError(HttpStatus.NOT_FOUND.value(), "NOT FOUND");
                return;
            }
            BootResult bootResult = CommonUtil.unserialize(boot.getData(), BootResult.class);
            html = bootResult.getHtml().get(key).getSource();
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
