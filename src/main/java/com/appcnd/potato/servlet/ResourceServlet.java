package com.appcnd.potato.servlet;

import com.appcnd.potato.cache.ResourceCache;
import com.appcnd.potato.cache.SoftResourceCache;
import com.appcnd.potato.cache.WeakResourceCache;
import com.appcnd.potato.util.Parser;
import com.appcnd.potato.util.ResourceUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * created by nihao 2020/07/07
 */
@Slf4j
public class ResourceServlet extends HttpServlet {

    protected final String resourcePath;
    protected final String contextPath;

    private ResourceCache<String,String> textCache = null;
    private ResourceCache<String,byte[]> byteCache = null;
    private final long expireTime = 1000L * 60 * 60 * 24;

    public ResourceServlet(String resourcePath, String contextPath) {
        this.resourcePath = resourcePath;
        this.contextPath = contextPath;
        this.textCache = new SoftResourceCache<>();
        this.byteCache = new WeakResourceCache<>();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();

        response.setCharacterEncoding("utf-8");

        if (contextPath == null) { // root context
            contextPath = "";
        }
        String uri = contextPath + servletPath;
        String path = requestURI.substring(contextPath.length() + servletPath.length());
        returnResourceFile(path, uri, request, response);
    }

    protected String getFilePath(String fileName) {
        return resourcePath + fileName;
    }

    protected void returnResourceFile(String fileName, String uri, HttpServletRequest request, HttpServletResponse response)
            throws ServletException,
            IOException {
        // 控制浏览器缓存
        response.setDateHeader("expires",System.currentTimeMillis() + expireTime);
        boolean isByte = false;
        if (fileName.endsWith(".html")) {
            response.setContentType("text/html; charset=utf-8");
        } else if (fileName.endsWith(".css")) {
            response.setContentType("text/css;charset=utf-8");
        } else if (fileName.endsWith(".js")) {
            response.setContentType("text/javascript;charset=utf-8");
        } else {
            isByte = true;
        }
        if (isByte) {
            returnBytes(fileName, request, response);
        } else {
            returnText(fileName, request, response);
        }
    }

    private void returnBytes(String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        byte[] bytes = byteCache.get(fileName);
        if (bytes == null) {
            String filePath = getFilePath(fileName);
            bytes = ResourceUtil.readByteArrayFromResource(filePath);
            if (bytes == null) {
                response.sendError(404, "资源不存在");
                return;
            }
            // 补充缓存
            log.debug("补充静态资源缓存:{}", fileName);
            byteCache.set(fileName, bytes);
        }
        response.getOutputStream().write(bytes);
    }

    private void returnText(String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String text = textCache.get(fileName);
        if (text == null) {
            String filePath = getFilePath(fileName);
            text = ResourceUtil.readFromResource(filePath);
            if (text == null) {
                if (fileName.endsWith(".html")) {
                    request.setAttribute("code", 404);
                    request.setAttribute("msg", "错误信息：不存在");
                    request.getRequestDispatcher(contextPath + "/static/htmls/error.html").forward(request, response);
                } else {
                    response.sendError(404, "资源不存在");
                }
                return;
            }
            // 补充缓存
            log.debug("补充静态资源缓存:{}", fileName);
            textCache.set(fileName, text);
        }
        if (fileName.endsWith(".html")) {
            text = Parser.parse(text, "contextPath", request.getContextPath() + contextPath);
            text = Parser.parse(text, "code", request.getAttribute("code"));
            text = Parser.parse(text, "msg", request.getAttribute("code"));
        }
        response.getWriter().write(text);
    }
}
