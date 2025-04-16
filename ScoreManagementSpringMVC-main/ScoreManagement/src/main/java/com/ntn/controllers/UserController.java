/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.pojo.User;
import com.ntn.pojo.User.Role;
import com.ntn.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author vhuunghia
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        return "register"; // Trả về trang đăng ký
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Trả về trang đăng nhập
    }

    @GetMapping("/pageAdmin")
    public String pageAdmin() {
        return "pageAdmin";
    }

    @GetMapping("/pageTeacher")
    public String pageTeacher() {
        return "pageTeacher";
    }

    @GetMapping("/pageStudent")
    public String pageStudent() {
        return "pageStudent";
    }

    @GetMapping("/registerStudent")
    public String showRegistrationStudentForm(Model model) {
        return "registerStudent"; // Trả về trang đăng ký
    }

//    @PostMapping("/login")
//    public String loginUser(@RequestParam("username") String username,
//            @RequestParam("password") String password,
//            Model model, HttpSession session) {
//        boolean isAuthenticated = userService.authAdminUser(username, password);
//
//        if (isAuthenticated) {
//            // Lưu thông tin người dùng vào session
//            session.setAttribute("user", username);
//            model.addAttribute("user", username);
//            return "redirect:/register"; // Đăng nhập thành công và có quyền Admin, chuyển hướng đến trang đăng ký người dùng
//        } else {
//            model.addAttribute("error", "Tên người dùng hoặc mật khẩu không chính xác hoặc bạn không có quyền đăng nhập.");
//            return "login"; // Đăng nhập không thành công hoặc không có quyền, hiển thị thông báo lỗi trên trang đăng nhập
//        }
//    }
    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("role") String role,
            Model model, HttpSession session,
            HttpServletRequest request, HttpServletResponse response) {

        // Xác thực người dùng dựa trên role
        boolean isAuthenticated = false;
        String redirectUrl = "";

        switch (role) {
            case "Admin":
                isAuthenticated = userService.authAdminUser(username, password);
                redirectUrl = "redirect:/pageAdmin";
                break;
            case "Teacher":
                isAuthenticated = userService.authTeacherUser(username, password);
                redirectUrl = "redirect:/pageTeacher";
                break;
            case "Student":
                isAuthenticated = userService.authStudentUser(username, password);
                redirectUrl = "redirect:/pageStudent";
                break;
            default:
                // Đảm bảo không có authentication nào được tạo
                SecurityContextHolder.clearContext();
                session.invalidate();
                return "redirect:/login?error=role_invalid";
        }

        if (isAuthenticated) {
            // Lấy User object cho Spring Security
            User user = userService.getUserByUn(username);

            // Cấu hình Spring Security authentication
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(role));

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Lưu thông tin vào session
            session.setAttribute("user", username);
            session.setAttribute("role", role);

            return redirectUrl;
        } else {
            // Đảm bảo xóa mọi thông tin xác thực khi đăng nhập thất bại
            SecurityContextHolder.clearContext();
            session.invalidate();
            return "redirect:/login?error=true";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout=true";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam Map<String, String> params) {
        String email = params.get("email");

        // Kiểm tra xem email có nằm trong bảng Teacher
        if (!userService.isTeacherEmailExists(email)) {
            return "redirect:/register?error"; // Redirect đến trang đăng ký với thông báo lỗi
        }

        // Kiểm tra xem email có đúng đuôi "@dh.edu.vn"
        if (!email.endsWith("@dh.edu.vn")) {
            return "redirect:/register?error"; // Redirect đến trang đăng ký với thông báo lỗi
        }

        // Thực hiện đăng ký người dùng giáo viên
        User user = userService.addTeacherUser(params);
        if (user != null) {
            return "redirect:/register?successs"; // Đăng ký thành công, redirect đến trang đăng nhập
        } else {
            return "redirect:/register?error"; // Đăng ký không thành công, redirect đến trang đăng ký với thông báo lỗi
        }
    }

    @PostMapping("/registerStudent")
    public String registerStudent(@RequestParam Map<String, String> params, Model model) {
        String email = params.get("email");
        String username = params.get("username");

        // Kiểm tra email có nằm trong danh sách sinh viên không
        if (!userService.isEmailExists(email)) {
            model.addAttribute("error", "invalid-email");
            return "redirect:/registerStudent?error=invalid-email";
        }

        // Kiểm tra định dạng email
        if (!email.endsWith("@dh.edu.vn")) {
            model.addAttribute("error", "email-format");
            return "redirect:/registerStudent?error=email-format";
        }

        // Kiểm tra xác nhận mật khẩu
        String password = params.get("password");
        String confirmPassword = params.get("confirmPassword");
        if (password == null || !password.equals(confirmPassword) || password.length() < 6) {
            model.addAttribute("error", "password");
            return "redirect:/registerStudent?error=password";
        }

        // Kiểm tra avatar đã chọn chưa
        if (params.get("avatar") == null || params.get("avatar").isEmpty()) {
            model.addAttribute("error", "avatar-required");
            return "redirect:/registerStudent?error=avatar-required";
        }

        // Thêm username vào params
        params.put("username", username);

        // Đăng ký tài khoản sinh viên
        User user = userService.addUser(params);
        if (user != null) {
            return "redirect:/login?success";
        } else {
            return "redirect:/registerStudent?error=system";
        }
    }

}
