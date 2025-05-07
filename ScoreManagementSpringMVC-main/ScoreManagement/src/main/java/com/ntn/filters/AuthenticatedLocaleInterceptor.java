package com.ntn.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

public class AuthenticatedLocaleInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Nếu người dùng đã đăng nhập (không phải anonymous)
        if (authentication != null && authentication.isAuthenticated() 
                && !authentication.getPrincipal().equals("anonymousUser")) {
            
            // Lấy LocaleResolver từ request
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            
            if (localeResolver != null) {
                // Đặt ngôn ngữ thành tiếng Việt
                localeResolver.setLocale(request, response, new Locale("vi"));
            }
        }
        
        return true;
    }
}