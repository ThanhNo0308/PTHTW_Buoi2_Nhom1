/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.filters;

import com.ntn.utils.JwtUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String uri = httpRequest.getRequestURI();

        // Luôn thêm CORS headers trước khi xử lý logic
        httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
        httpResponse.setHeader("Vary", "Origin");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // Quan trọng: Xử lý preflight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return; // Quan trọng: return ngay lập tức không filter tiếp
        }

        if (uri.contains("/oauth2/") || uri.contains("/login/oauth2/")) {
            chain.doFilter(request, response);
            return;
        }

        // Kiểm tra URI có chứa "/api/" thay vì bắt đầu bằng "/api/"
        if (uri.contains("/api/login") || uri.contains("/api/register")) {
            chain.doFilter(request, response);
            return;
        }

        // Kiểm tra URI có chứa "/api/"
        if (uri.contains("/api/")) {
            String header = httpRequest.getHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Thiếu token hoặc token không hợp lệ.");
                return;
            }

            String token = header.substring(7);
            try {
                String username = JwtUtils.validateTokenAndGetUsername(token);
                if (username != null) {
                    httpRequest.setAttribute("username", username);
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>())
                    );
                    chain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ.");
                return;
            }
        }

        // Các request khác cho phép đi qua
        chain.doFilter(request, response);
    }

}
