package com.ntn.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ntn.pojo.Student;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import com.ntn.service.StudentService;
import com.ntn.service.TeacherService;
import com.ntn.service.UserService;
import com.ntn.utils.JwtUtils;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ApiUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private Cloudinary cloudinary;

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestBody) {
        String username = requestBody.get("username");
        String password = requestBody.get("password");
        String role = requestBody.get("role");

        boolean isAuthenticated = false;
        Map<String, Object> response = new HashMap<>();

        User user = userService.getUserByUn(username);

        // Kiểm tra tài khoản tồn tại và đang active
        if (user != null && !user.isActive()) {
            response.put("status", "error");
            response.put("message", "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        switch (role) {
            case "Admin":
                isAuthenticated = userService.authAdminUser(username, password);
                break;
            case "Teacher":
                isAuthenticated = userService.authTeacherUser(username, password);
                break;
            case "Student":
                isAuthenticated = userService.authStudentUser(username, password);
                break;
            default:
                response.put("status", "error");
                response.put("message", "Vai trò không hợp lệ");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (isAuthenticated) {
            response.put("status", "success");
            response.put("message", "Đăng nhập thành công");

            // Loại bỏ thông tin nhạy cảm trước khi gửi đi
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("name", user.getName());
            userMap.put("image", user.getImage());
            userMap.put("role", user.getRole());
            userMap.put("active", user.getActive());

            // Tạo JWT token
            String token = null;
            try {
                token = JwtUtils.generateToken(username);
                userMap.put("token", token);
            } catch (Exception e) {
                response.put("status", "error");
                response.put("message", "Lỗi tạo token: " + e.getMessage());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            response.put("user", userMap);
            response.put("role", user.getRole());
            response.put("token", token);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("status", "error");
            response.put("message", "Tên đăng nhập hoặc mật khẩu không đúng");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    // Đăng ký tài khoản sinh viên
    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String username = params.get("username");
        String password = params.get("password");
        String confirmPassword = params.get("confirmPassword");
        Map<String, Object> response = new HashMap<>();

        // Kiểm tra email có nằm trong danh sách sinh viên không
        if (!userService.isEmailExists(email)) {
            response.put("status", "error");
            response.put("message", "Email không tồn tại trong hệ thống");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra định dạng email
        if (!email.endsWith("@dh.edu.vn")) {
            response.put("status", "error");
            response.put("message", "Email phải có định dạng @dh.edu.vn");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        //Kiểm tra email đã được đăng ký chưa
        if (userService.isEmailExistsInUserTable(email)) {
            response.put("status", "error");
            response.put("message", "Email đã tồn tại trong hệ thống. Vui lòng sử dụng email khác.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra username đã tồn tại chưa
        if (userService.isUsernameExists(username)) {
            response.put("status", "error");
            response.put("message", "Tên đăng nhập đã tồn tại trong hệ thống. Vui lòng chọn tên đăng nhập khác.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra xác nhận mật khẩu
        if (password == null || !password.equals(confirmPassword) || password.length() < 6) {
            response.put("status", "error");
            response.put("message", "Mật khẩu không khớp hoặc có ít hơn 6 ký tự");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Thêm username vào params
        params.put("username", email.split("@")[0]);

        // Đăng ký tài khoản sinh viên
        User user = userService.addUser(params);
        if (user != null) {
            response.put("status", "success");
            response.put("message", "Đăng ký thành công");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else {
            response.put("status", "error");
            response.put("message", "Đăng ký thất bại");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Lấy thông tin người dùng hiện tại
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("Không có người dùng đăng nhập", HttpStatus.UNAUTHORIZED);
        }

        User user = userService.getUserByUn(principal.getName());
        if (user == null) {
            return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);
        }

        // Tạo DTO thay vì trả về entity trực tiếp
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("username", user.getUsername());
        userMap.put("image", user.getImage());
        userMap.put("role", user.getRole());
        userMap.put("gender", user.getGender());
        userMap.put("hometown", user.getHometown());
        userMap.put("identifyCard", user.getIdentifyCard());
        userMap.put("birthdate", user.getBirthdate());
        userMap.put("phone", user.getPhone());
        userMap.put("active", user.getActive());

        response.put("user", userMap);

        // Thêm thông tin chi tiết theo vai trò
        if (user.getRole() == User.Role.Teacher) {
            Teacher teacher = teacherService.getTeacherByEmail(user.getEmail());
            if (teacher != null) {
                Map<String, Object> teacherMap = new HashMap<>();
                teacherMap.put("id", teacher.getId());
                teacherMap.put("teacherName", teacher.getTeacherName());
                teacherMap.put("email", teacher.getEmail());

                // Chỉ lấy thông tin cần thiết từ Department
                if (teacher.getDepartmentId() != null) {
                    Map<String, Object> deptMap = new HashMap<>();
                    deptMap.put("id", teacher.getDepartmentId().getId());
                    deptMap.put("departmentName", teacher.getDepartmentId().getDepartmentName());
                    teacherMap.put("departmentId", deptMap);
                }

                response.put("roleSpecificInfo", teacherMap);
            }
        } else if (user.getRole() == User.Role.Student) {
            // Xử lý tương tự cho Student
            List<Student> students = studentService.getStudentbyEmail(user.getEmail());
            Student student = students != null && !students.isEmpty() ? students.get(0) : null;
            if (student != null) {
                Map<String, Object> studentMap = new HashMap<>();
                studentMap.put("id", student.getId());
                studentMap.put("studentName", student.getFirstName());
                studentMap.put("email", student.getEmail());
                studentMap.put("studentId", student.getStudentCode());
                // Thêm các thông tin cần thiết khác

                response.put("roleSpecificInfo", studentMap);
            }
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Cập nhật thông tin người dùng
    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> profileData) {
        Integer userId = (Integer) profileData.get("id");
        Map<String, Object> response = new HashMap<>();

        try {
            User existingUser = userService.getUserById(userId);
            if (existingUser == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy người dùng");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Cập nhật thông tin người dùng
            if (profileData.get("name") != null) {
                existingUser.setName((String) profileData.get("name"));
            }
            if (profileData.get("hometown") != null) {
                existingUser.setHometown((String) profileData.get("hometown"));
            }
            if (profileData.get("phone") != null) {
                existingUser.setPhone((String) profileData.get("phone"));
            }
            if (profileData.get("identifyCard") != null) {
                existingUser.setIdentifyCard((String) profileData.get("identifyCard"));
            }

            // Xử lý giới tính
            if (profileData.get("gender") != null) {
                String gender = (String) profileData.get("gender");
                Short genderValue = null;
                if ("Nam".equals(gender)) {
                    genderValue = 0;
                } else if ("Nữ".equals(gender)) {
                    genderValue = 1;
                } else {
                    genderValue = -1;
                }
                existingUser.setGender(genderValue);
            }
            if (profileData.get("birthdate") != null) {
                String birthdateStr = (String) profileData.get("birthdate");
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date birthdate = dateFormat.parse(birthdateStr);
                    existingUser.setBirthdate(birthdate);
                } catch (ParseException e) {
                    // Log lỗi nếu định dạng không hợp lệ
                    System.err.println("Error parsing birthdate: " + e.getMessage());
                }
            }

            boolean success = userService.updateUser(existingUser);
            if (success) {
                response.put("status", "success");
                response.put("message", "Cập nhật thông tin cá nhân thành công");
                response.put("user", existingUser);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("status", "error");
                response.put("message", "Cập nhật thông tin thất bại");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Cập nhật avatar người dùng
    @PostMapping("/profile/upload-avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("id") int userId,
            @RequestParam("image") MultipartFile imageFile) {

        Map<String, Object> response = new HashMap<>();

        try {
            User existingUser = userService.getUserById(userId);
            if (existingUser == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy người dùng");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                Map<String, Object> result = cloudinary.uploader().upload(
                        imageFile.getBytes(),
                        ObjectUtils.asMap("resource_type", "auto")
                );
                existingUser.setImage((String) result.get("secure_url"));

                boolean success = userService.updateUser(existingUser);
                if (success) {
                    response.put("status", "success");
                    response.put("message", "Cập nhật avatar thành công");
                    response.put("imageUrl", existingUser.getImage());
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
            }

            response.put("status", "error");
            response.put("message", "Cập nhật avatar thất bại");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Đổi mật khẩu
    @PostMapping("/profile/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        int userId = Integer.parseInt(passwordData.get("id"));
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        String confirmPassword = passwordData.get("confirmPassword");

        Map<String, Object> response = new HashMap<>();

        try {
            User existingUser = userService.getUserById(userId);
            if (existingUser == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy người dùng");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Kiểm tra mật khẩu hiện tại
            if (!passwordEncoder.matches(currentPassword, existingUser.getPassword())) {
                response.put("status", "error");
                response.put("message", "Mật khẩu hiện tại không chính xác");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Kiểm tra mật khẩu mới và xác nhận
            if (!newPassword.equals(confirmPassword)) {
                response.put("status", "error");
                response.put("message", "Mật khẩu xác nhận không khớp với mật khẩu mới");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Kiểm tra độ phức tạp của mật khẩu
            if (newPassword.length() < 6) {
                response.put("status", "error");
                response.put("message", "Mật khẩu phải có ít nhất 6 ký tự");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Mã hóa và cập nhật mật khẩu mới
            existingUser.setPassword(passwordEncoder.encode(newPassword));
            boolean success = userService.updateUser(existingUser);

            if (success) {
                response.put("status", "success");
                response.put("message", "Đổi mật khẩu thành công");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("status", "error");
                response.put("message", "Đổi mật khẩu thất bại");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Kiểm tra email sinh viên tồn tại
    @GetMapping("/check-student-email")
    public ResponseEntity<?> checkStudentEmail(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userService.isEmailExists(email);

        response.put("exists", exists);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Kiểm tra email giảng viên tồn tại
    @GetMapping("/check-teacher-email")
    public ResponseEntity<?> checkTeacherEmail(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userService.isTeacherEmailExists(email);

        response.put("exists", exists);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = userService.isEmailExistsInUserTable(email);
        response.put("exists", exists);
        response.put("message", exists ? "Email đã tồn tại trong hệ thống" : "Email hợp lệ");
        return ResponseEntity.ok(response);
    }
}
