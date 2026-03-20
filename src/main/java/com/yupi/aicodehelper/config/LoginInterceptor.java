package com.yupi.aicodehelper.config;

import com.yupi.aicodehelper.utils.LoginContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 1. header 取 userId
        String userId = request.getHeader("userId");

        // 2. 参数取（SSE）
        if (userId == null) {
            userId = request.getParameter("userId");
        }

        // 3. 有就存，没有就放行
        if (userId != null) {
            LoginContext.set(Long.valueOf(userId));
        }

        // ✅ 一定放最后
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        // ⭐ 防止内存泄漏（必须）
        LoginContext.clear();
    }
}