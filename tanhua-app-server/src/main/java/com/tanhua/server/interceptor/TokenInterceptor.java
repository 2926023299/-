package com.tanhua.server.interceptor;

import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.model.domain.User;
import io.jsonwebtoken.Claims;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头
        String token = request.getHeader("Authorization");

        //判断token是否合法
        boolean verifyToken = JwtUtils.verifyToken(token);
        if (!verifyToken) {
            response.setStatus(401);
            return false;
        }

        //解析token存入ThreadLocal
        Claims claims = JwtUtils.getClaims(token);
        Integer id = (Integer) claims.get("id");
        String mobile = (String) claims.get("mobile");

        User user = new User();
        user.setId(Long.valueOf(id));
        user.setMobile(mobile);

        UserHolder.set(user);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.remove();
    }
}
