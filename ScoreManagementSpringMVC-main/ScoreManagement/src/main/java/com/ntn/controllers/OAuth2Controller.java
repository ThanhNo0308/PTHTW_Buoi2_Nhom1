package com.ntn.controllers;

import com.ntn.pojo.Student;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import com.ntn.service.StudentService;
import com.ntn.service.TeacherService;
import com.ntn.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Controller
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @GetMapping("/additional-info")
    public String getAdditionalInfo(HttpSession session, Model model) {
        // Kiểm tra xem có đến từ quá trình OAuth2 không
        String provider = (String) session.getAttribute("oauth2Provider");
        if (provider == null) {
            return "redirect:/login";
        }

        model.addAttribute("name", session.getAttribute("oauth2Name"));
        model.addAttribute("provider", provider);

        // Lấy thông báo lỗi từ session nếu có
        if (session.getAttribute("errorMessage") != null) {
            model.addAttribute("errorMessage", session.getAttribute("errorMessage"));
            session.removeAttribute("errorMessage");
        }

        return "AddInformation";
    }

    @PostMapping("/submit-additional-info")
    public String submitAdditionalInfo(
            @RequestParam("email") String email,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) throws IOException {

        // Lấy thông tin từ session
        String name = (String) session.getAttribute("oauth2Name");
        String picture = (String) session.getAttribute("oauth2Picture");

        if (!email.trim().toLowerCase().endsWith("@dh.edu.vn")) {
            session.setAttribute("errorMessage", "Email không hợp lệ. Vui lòng sử dụng email có định dạng @dh.edu.vn.");
            return "redirect:/oauth2/additional-info";
        }

        // Kiểm tra email trong database
        if (email.endsWith("@dh.edu.vn")) {
            // Kiểm tra email đã đăng ký user chưa
            User existingUser = userService.getUserByEmail(email);
            if (existingUser != null) {
                session.setAttribute("errorMessage", "Email này đã được đăng ký. Vui lòng đăng nhập trực tiếp.");
                return "redirect:/oauth2/additional-info";
            }

            // Kiểm tra trong bảng Teacher
            Teacher teacher = teacherService.getTeacherByEmail(email);
            if (teacher != null) {
                // Tạo user Teacher mới với thông tin đầy đủ từ bảng Teacher
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(teacher.getTeacherName());
                newUser.setGender(teacher.getGender());
                newUser.setHometown(teacher.getAddress()); // Lưu ý: teacher dùng Address, user dùng Hometown
                newUser.setBirthdate(teacher.getBirthdate());
                newUser.setPhone(teacher.getPhoneNumber()); // Lưu ý: teacher dùng PhoneNumber, user dùng Phone
                newUser.setUsername(email);
                newUser.setImage(picture);
                newUser.setPassword(UUID.randomUUID().toString());
                newUser.setActive("Active");
                newUser.setRole(User.Role.Teacher);

                existingUser = userService.saveOAuth2User(newUser);

                // Thiết lập SecurityContext
                setupSecurityContext(existingUser, session);

                // Chuyển hướng đến trang teacher
                response.sendRedirect(request.getContextPath() + "/pageTeacher");
                return null;
            } // Kiểm tra trong bảng Student
            else {
                Student student = studentService.getStudentByEmail(email);
                if (student != null) {
                    // Tạo user Student mới với thông tin đầy đủ từ bảng Student
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

                    // Thiết lập SecurityContext
                    setupSecurityContext(existingUser, session);

                    // Chuyển hướng đến trang student
                    response.sendRedirect(request.getContextPath() + "/pageStudent");
                    return null;
                } else {
                    session.setAttribute("errorMessage", "Email không tồn tại trong hệ thống nhà trường.");
                    return "redirect:/oauth2/additional-info";
                }
            }
        } else {
            session.setAttribute("errorMessage", "Email không hợp lệ. Nếu bạn là sinh viên hoặc giảng viên, vui lòng sử dụng email @dh.edu.vn");
            return "redirect:/oauth2/additional-info";
        }
    }

    // Phương thức hỗ trợ thiết lập SecurityContext
    private void setupSecurityContext(User user, HttpSession session) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        UsernamePasswordAuthenticationToken auth
                = new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString())));
        auth.setDetails(user);
        securityContext.setAuthentication(auth);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext);

        // Lưu thông tin vào session
        session.setAttribute("user", user.getUsername());
        session.setAttribute("role", user.getRole().toString());

        // Dọn dẹp thông tin OAuth2 tạm thời
        session.removeAttribute("oauth2Provider");
        session.removeAttribute("oauth2Name");
        session.removeAttribute("oauth2Picture");
    }
}
