/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.pojo.User;
import com.ntn.pojo.User.Role;
import com.ntn.service.ClassService;
import com.ntn.service.DepartmentService;
import com.ntn.service.MajorService;
import com.ntn.service.StudentService;
import com.ntn.service.StudentSubjectTeacherService;
import com.ntn.service.TeacherService;
import com.ntn.service.TrainingTypeService;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ClassService classService;

    @Autowired
    private MajorService majorService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private StudentSubjectTeacherService studentSubjectTeacherService;

    @Autowired
    private TrainingTypeService trainingTypeService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/admin/register")
    public String showRegistrationForm(Model model) {
        return "admin/register";
    }

    @PostMapping("/admin/register")
    public String registerUser(@RequestParam Map<String, String> params) {
        String email = params.get("email");

        // Kiểm tra xem email có nằm trong bảng Teacher
        if (!userService.isTeacherEmailExists(email)) {
            return "redirect:/admin/register?error";
        }

        // Kiểm tra xem email có đúng đuôi "@dh.edu.vn"
        if (!email.endsWith("@dh.edu.vn")) {
            return "redirect:/admin/register?error";
        }

        // Thực hiện đăng ký người dùng giáo viên
        User user = userService.addTeacherUser(params);
        if (user != null) {
            return "redirect:/admin/register?successs";
        } else {
            return "redirect:/admin/register?error";
        }
    }

    @GetMapping("/admin/pageAdmin")
    public String pageAdmin(Model model) {
        int studentCount = studentService.countStudents();
        int teacherCount = teacherService.countTeachers();
        int classCount = classService.countClasses();
        int majorCount = majorService.countMajors();
        int trainingTypeCount = trainingTypeService.countTrainingTypes();
        List<User> accounts = userService.getUsers();

        model.addAttribute("studentCount", studentCount);
        model.addAttribute("teacherCount", teacherCount);
        model.addAttribute("classCount", classCount);
        model.addAttribute("majorCount", majorCount);
        model.addAttribute("departmentCount", departmentService.countDepartments());
        model.addAttribute("enrollmentCount", studentSubjectTeacherService.countEnrollments());
        model.addAttribute("trainingTypeCount", trainingTypeCount);
        model.addAttribute("accountCount", accounts.size());
        return "/admin/dashboard";
    }

    @GetMapping("/registerStudent")
    public String showRegistrationStudentForm(Model model) {
        return "registerStudent"; // Trả về trang đăng ký
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Trả về trang đăng nhập
    }

    @GetMapping("/pageTeacher")
    public String pageTeacher() {
        return "/teacher/dashboard";
    }

    @GetMapping("/pageStudent")
    public String pageStudent() {
        return "/student/dashboard";
    }

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
                redirectUrl = "redirect:/admin/pageAdmin";
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

    @GetMapping("/admin/accounts")
    public String getAccounts(Model model) {
        List<User> accounts = userService.getUsers();
        model.addAttribute("accounts", accounts);

        // Sửa dòng này để dùng phương thức isActive() mới
        long activeCount = accounts.stream().filter(User::isActive).count();
        model.addAttribute("activeCount", activeCount);

        return "admin/accounts";
    }

    @PostMapping("/admin/accounts/update")
    public String updateAccount(@ModelAttribute User user,
            @RequestParam(required = false) String password,
            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra email đã tồn tại với tài khoản khác chưa
            User existingByEmail = userService.getUserByEmail(user.getEmail());
            if (existingByEmail != null && !existingByEmail.getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email '" + user.getEmail() + "' đã được sử dụng bởi tài khoản khác!");
                return "redirect:/admin/accounts";
            }

            // Nếu mật khẩu được cung cấp, mã hóa nó
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            } else {
                // Lấy mật khẩu hiện tại từ DB
                User existingUser = userService.getUserById(user.getId());
                if (existingUser != null) {
                    user.setPassword(existingUser.getPassword());
                }
            }

            boolean success = userService.updateUser(user);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật tài khoản thất bại!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật tài khoản: " + e.getMessage());
        }

        return "redirect:/admin/accounts";
    }

    @GetMapping("/admin/accounts/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản!");
                return "redirect:/admin/accounts";
            }

            // Kiểm tra và xử lý đặc biệt cho Admin
            if ("Admin".equals(user.getRole())) {
                // Đếm số admin đang hoạt động
                long activeAdminCount = userService.getUsers().stream()
                        .filter(u -> "Admin".equals(u.getRole()) && "Active".equals(u.getActive()))
                        .count();

                // Nếu đây là admin cuối cùng và đang hoạt động
                if (activeAdminCount <= 1 && "Active".equals(user.getActive())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể vô hiệu hóa tài khoản Admin cuối cùng!");
                    return "redirect:/admin/accounts";
                }
            }

            // Đảo trạng thái hoạt động - Xử lý đúng trạng thái kiểu String
            String newStatus = "Active".equals(user.getActive()) ? "Inactive" : "Active";
            User updatedUser = new User();
            updatedUser.setId(user.getId());
            updatedUser.setActive(newStatus);

            boolean success = userService.updateUser(updatedUser);

            if (success) {
                String status = "Active".equals(newStatus) ? "kích hoạt" : "vô hiệu hóa";
                redirectAttributes.addFlashAttribute("successMessage", "Đã " + status + " tài khoản!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Thay đổi trạng thái tài khoản thất bại!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thay đổi trạng thái tài khoản: " + e.getMessage());
        }

        return "redirect:/admin/accounts";
    }

    @GetMapping("/admin/accounts/delete/{id}")
    public String deleteAccount(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra tài khoản tồn tại
            User user = userService.getUserById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản!");
                return "redirect:/admin/accounts";
            }

            // Kiểm tra không xóa admin cuối cùng
            if ("Admin".equals(user.getRole())) {
                long adminCount = userService.getUsers().stream()
                        .filter(u -> "Admin".equals(u.getRole()))
                        .count();
                if (adminCount <= 1) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa tài khoản Admin cuối cùng!");
                    return "redirect:/admin/accounts";
                }
            }

            boolean success = userService.deleteUser(id);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Xóa tài khoản thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Xóa tài khoản thất bại!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa tài khoản: " + e.getMessage());
        }

        return "redirect:/admin/accounts";
    }

}
