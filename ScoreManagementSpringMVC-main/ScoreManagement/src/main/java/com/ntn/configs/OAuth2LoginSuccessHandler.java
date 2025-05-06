package com.ntn.configs;

import com.ntn.pojo.Student;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import com.ntn.service.StudentService;
import com.ntn.service.TeacherService;
import com.ntn.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

// Custem Role khi đăng nhập của OAuth2
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        // Lấy thông tin từ OAuth2User
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = token.getPrincipal();

        // Lấy các thông tin cần thiết từ attributes
        HttpSession session = request.getSession();
        String provider = oauth2User.getAttribute("provider");
        Boolean needAdditionalInfo = oauth2User.getAttribute("needAdditionalInfo");

        if (Boolean.TRUE.equals(needAdditionalInfo)) {
            // Lưu các thông tin cần thiết vào session để form bổ sung thông tin có thể sử dụng
            session.setAttribute("oauth2Provider", provider);
            session.setAttribute("oauth2Name", oauth2User.getAttribute("name"));
            session.setAttribute("oauth2Picture", oauth2User.getAttribute("picture"));

            response.sendRedirect(request.getContextPath() + "/oauth2/additional-info");
            return;
        }

        // Lấy thông tin từ OAuth2 attributes
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        // Xử lý logic đăng nhập dựa trên email
        processOAuth2Login(email, name, picture, request, response);
    }

    private void processOAuth2Login(String email, String name, String picture,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession();

        if (email.endsWith("@gmail.com")) {
            // Tìm user trong database
            User existingUser = userService.getUserByEmail(email);

            if (existingUser == null) {
                // Tạo user Admin mới
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setUsername(email);
                newUser.setImage(picture);
                newUser.setPassword(UUID.randomUUID().toString());
                newUser.setActive("Active");
                newUser.setRole(User.Role.Admin);

                existingUser = userService.saveOAuth2User(newUser);
            }

            SecurityContext securityContext = SecurityContextHolder.getContext();
            UsernamePasswordAuthenticationToken auth
                    = new UsernamePasswordAuthenticationToken(existingUser.getUsername(), null,
                            Collections.singletonList(new SimpleGrantedAuthority(existingUser.getRole().toString())));
            securityContext.setAuthentication(auth);
            session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext);

            // Đặt thông tin user vào session
            session.setAttribute("user", existingUser.getUsername());
            session.setAttribute("role", existingUser.getRole().toString());
            String redirectUrl = request.getContextPath() + "/admin/pageAdmin";
            response.sendRedirect(redirectUrl);
        } else if (email.endsWith("@dh.edu.vn")) {
            // Kiểm tra trong bảng Teacher
            Teacher teacher = teacherService.getTeacherByEmail(email);
            if (teacher != null) {
                // Tìm user trong database
                User existingUser = userService.getUserByEmail(email);

                if (existingUser == null) {
                    // Tạo user Teacher mới
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(teacher.getTeacherName());
                    newUser.setGender(teacher.getGender());
                    newUser.setHometown(teacher.getAddress());
                    newUser.setBirthdate(teacher.getBirthdate());
                    newUser.setPhone(teacher.getPhoneNumber());
                    newUser.setUsername(email);
                    newUser.setImage(picture);
                    newUser.setPassword(UUID.randomUUID().toString());
                    newUser.setActive("Active");
                    newUser.setRole(User.Role.Teacher);

                    existingUser = userService.saveOAuth2User(newUser);
                }

                // Đặt thông tin user vào session
                session.setAttribute("user", existingUser.getUsername());
                session.setAttribute("role", existingUser.getRole().toString());
                response.sendRedirect(request.getContextPath() + "/pageTeacher");
            } // Kiểm tra trong bảng Student
            else {
                Student student = studentService.getStudentByEmail(email);
                if (student != null) {
                    // Tìm user trong database
                    User existingUser = userService.getUserByEmail(email);

                    if (existingUser == null) {
                        // Tạo user Student mới
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setName(student.getLastName() + " " + student.getFirstName());
                        newUser.setGender(student.getGender());
                        newUser.setIdentifyCard(student.getIdentifyCard());
                        newUser.setHometown(student.getHometown());
                        newUser.setBirthdate(student.getBirthdate());
                        newUser.setPhone(student.getPhone());
                        newUser.setUsername(email);
                        newUser.setImage(picture);
                        newUser.setPassword(UUID.randomUUID().toString());
                        newUser.setActive("Active");
                        newUser.setRole(User.Role.Student);

                        existingUser = userService.saveOAuth2User(newUser);
                    }

                    // Đặt thông tin user vào session
                    session.setAttribute("user", existingUser.getUsername());
                    session.setAttribute("role", existingUser.getRole().toString());
                    response.sendRedirect(request.getContextPath() + "/pageStudent");
                } else {
                    // Email không tồn tại trong cả Teacher và Student
                    session.setAttribute("errorMessage", "Email không được đăng ký trong hệ thống");
                    response.sendRedirect(request.getContextPath() + "/login?error=email_not_found");
                }
            }
        } else {
            // Email không hợp lệ (không phải @gmail.com hoặc @dh.edu.vn)
            session.setAttribute("errorMessage", "Email không hợp lệ");
            response.sendRedirect(request.getContextPath() + "/login?error=invalid_email");
        }
    }
}
