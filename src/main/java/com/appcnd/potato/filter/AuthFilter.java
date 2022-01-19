package com.appcnd.potato.filter;

import com.appcnd.potato.model.vo.HttpResult;
import com.appcnd.potato.model.vo.HttpStatus;
import com.appcnd.potato.util.CommonUtil;
import com.appcnd.potato.util.DesUtil;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

/**
 * @author nihao 2019/10/24
 */
public class AuthFilter implements Filter {
    private final String TOKEN = "POTATO";
    private String loginname;
    private String password;
    Base64.Decoder decoder = Base64.getDecoder();
    public AuthFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        loginname = filterConfig.getInitParameter("loginname");
        password = filterConfig.getInitParameter("password");
    }

    private String decode(String encodedText) {
        String text = null;
        try {
            text = new String(decoder.decode(encodedText), "UTF-8");
        } catch (Exception e) {
        }
        return text;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        boolean login = false;
        try {
            String token = CommonUtil.getCookieValue(TOKEN, request);
            if (token != null) {
                Long time = Long.parseLong(DesUtil.decrypt(token));
                if (time != null && System.currentTimeMillis() - time < 60L * 60 * 1000) {
                    login = true;
                }
            }
            if (!login) {
                String authorization = request.getHeader("Authorization");
                if (authorization != null && authorization.startsWith("Basic ")) {
                    authorization = authorization.replace("Basic ", "");
                    authorization = decode(authorization);
                    if (authorization != null) {
                        String[] ss = authorization.split(":");
                        if (ss != null && ss.length >= 2) {
                            if (ss[0].equals(loginname) && ss[1].equals(password)) {
                                Cookie cookie = new Cookie(TOKEN, DesUtil.encrypt(System.currentTimeMillis() + ""));
                                cookie.setPath("/");
                                response.addCookie(cookie);
                                login = true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}
        if (!login) {
            handlerNeedLogin(request, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void handlerNeedLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (CommonUtil.isAjax(request)) {
            CommonUtil.responseOutWithJson(response, HttpResult.build(HttpStatus.NEED_LOGIN));
        } else {
            response.addHeader("WWW-Authenticate", "Basic realm=\"POTATO\"");
            response.setStatus(HttpStatus.NEED_LOGIN.getCode());
        }
    }
}
