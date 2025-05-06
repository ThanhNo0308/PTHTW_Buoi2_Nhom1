package com.ntn.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

// Custem Role khi đăng nhập
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String selectedRole = request.getParameter("role"); // role người dùng chọn
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean roleMatched = false;

        for (GrantedAuthority authority : authorities) {
            String actualRole = authority.getAuthority();

            if (actualRole.equalsIgnoreCase(selectedRole)) {
                roleMatched = true;
                // Vai trò đúng → redirect theo vai trò
                HttpSession session = request.getSession();
                session.setAttribute("user", authentication.getName());
                session.setAttribute("role", actualRole);
                
                switch (actualRole) {
                    case "Admin":
                        response.sendRedirect(request.getContextPath() + "/admin/pageAdmin");
                        return;
                    case "Teacher":
                        response.sendRedirect(request.getContextPath() + "/pageTeacher");
                        return;
                    case "Student":
                        response.sendRedirect(request.getContextPath() + "/pageStudent");
                        return;
                }
            }
        }

        // Nếu không khớp vai trò → đăng nhập thất bại và xóa authentication
        if (!roleMatched) {
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession();
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?roleError=true");
        }
    }
}