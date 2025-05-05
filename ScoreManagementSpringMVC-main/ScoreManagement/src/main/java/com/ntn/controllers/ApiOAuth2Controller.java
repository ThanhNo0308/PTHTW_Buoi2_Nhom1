package com.ntn.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ntn.pojo.Student;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import com.ntn.service.StudentService;
import com.ntn.service.TeacherService;
import com.ntn.service.UserService;
import com.ntn.utils.JwtUtils;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.facebook.GraphUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/oauth2")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ApiOAuth2Controller {

    @Autowired
    private UserService userService;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookAppId;
    
    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookAppSecret;

    /**
     * Xử lý đăng nhập OAuth2 từ client
     */
    @PostMapping("/login")
    public ResponseEntity<?> oauth2Login(@RequestBody Map<String, String> body) {
        String provider = body.get("provider");
        String token = body.get("token");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if ("google".equals(provider)) {
                return processGoogleLogin(token);
            } else if ("facebook".equals(provider)) {
                return processFacebookLogin(token);
            } else {
                response.put("status", "error");
                response.put("message", "Provider không được hỗ trợ");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi xử lý đăng nhập: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Xử lý thông tin bổ sung
     */
    @PostMapping("/additional-info")
    public ResponseEntity<?> submitAdditionalInfo(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String provider = body.get("provider");
        String name = body.get("name");
        String picture = body.get("picture");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Kiểm tra email trong database
            User existingUser = userService.getUserByEmail(email);
            if (existingUser != null) {
                response.put("status", "error");
                response.put("message", "Email đã được đăng ký trong hệ thống");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            if (email.endsWith("@gmail.com")) {
                // Tài khoản Admin
                return processAdminRegistration(email, name, picture);
            } else if (email.endsWith("@dh.edu.vn")) {
                // Kiểm tra trong bảng Teacher
                Teacher teacher = teacherService.getTeacherByEmail(email);
                if (teacher != null) {
                    return processTeacherRegistration(email, name, picture, teacher);
                } 
                
                // Kiểm tra trong bảng Student
                Student student = studentService.getStudentByEmail(email);
                if (student != null) {
                    return processStudentRegistration(email, name, picture, student);
                }
                
                response.put("status", "error");
                response.put("message", "Email không tồn tại trong hệ thống nhà trường");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                response.put("status", "error");
                response.put("message", "Email không hợp lệ. Vui lòng sử dụng email @gmail.com hoặc @dh.edu.vn");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi xử lý đăng ký: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Xử lý đăng nhập bằng Google
     */
    private ResponseEntity<?> processGoogleLogin(String idToken) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        
        GoogleIdToken googleIdToken = verifier.verify(idToken);
        if (googleIdToken == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Google ID token không hợp lệ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        Payload payload = googleIdToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");
        
        // Kiểm tra email
        if (email.endsWith("@gmail.com")) {
            // Admin - @gmail.com
            User existingUser = userService.getUserByEmail(email);
            if (existingUser == null) {
                // Tạo user mới
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
            
            // Tạo JWT token
            String token = jwtUtils.generateToken(existingUser.getUsername(), 
                                                existingUser.getRole().toString());
            
            // Tạo phản hồi
            Map<String, Object> userMap = createUserResponse(existingUser, token);
            
            return ResponseEntity.ok(userMap);
        } else if (email.endsWith("@dh.edu.vn")) {
            // Kiểm tra Teacher/Student
            Teacher teacher = teacherService.getTeacherByEmail(email);
            if (teacher != null) {
                return processTeacherLogin(email, name, picture);
            } 
            
            Student student = studentService.getStudentByEmail(email);
            if (student != null) {
                return processStudentLogin(email, name, picture);
            }
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Email không tồn tại trong hệ thống nhà trường");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Email không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Xử lý đăng nhập bằng Facebook
     */
    private ResponseEntity<?> processFacebookLogin(String accessToken) throws Exception {
        try {
            // Xác thực token với Facebook
            FacebookClient facebookClient = new DefaultFacebookClient(
                    accessToken, facebookAppSecret, Version.VERSION_12_0);
            
            // Lấy thông tin cơ bản từ Facebook
            GraphUser user = facebookClient.fetchObject("me", GraphUser.class, 
                    Parameter.with("fields", "id,name,picture"));
            
            // Facebook không luôn trả về email, nên phải yêu cầu thông tin bổ sung
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("needAdditionalInfo", true);
            response.put("provider", "facebook");
            response.put("name", user.getName());
            
            // Lấy URL ảnh đại diện
            String pictureUrl = "";
            if (user.getPicture() != null && user.getPicture().getUrl() != null) {
                pictureUrl = user.getPicture().getUrl();
            } else {
                pictureUrl = "https://graph.facebook.com/" + user.getId() + "/picture?type=large";
            }
            response.put("picture", pictureUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi xác thực Facebook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    
    /**
     * Xử lý đăng nhập Teacher
     */
    private ResponseEntity<?> processTeacherLogin(String email, String name, String picture) {
        User existingUser = userService.getUserByEmail(email);
        if (existingUser == null) {
            // Lấy thông tin từ bảng Teacher
            Teacher teacher = teacherService.getTeacherByEmail(email);
            
            // Tạo user mới
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
        
        // Tạo JWT token
        String token = jwtUtils.generateToken(existingUser.getUsername(), 
                                            existingUser.getRole().toString());
        
        // Tạo phản hồi
        Map<String, Object> userMap = createUserResponse(existingUser, token);
        
        return ResponseEntity.ok(userMap);
    }
    
    /**
     * Xử lý đăng nhập Student
     */
    private ResponseEntity<?> processStudentLogin(String email, String name, String picture) {
        User existingUser = userService.getUserByEmail(email);
        if (existingUser == null) {
            // Lấy thông tin từ bảng Student
            Student student = studentService.getStudentByEmail(email);
            
            // Tạo user mới
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
        
        // Tạo JWT token
        String token = jwtUtils.generateToken(existingUser.getUsername(), 
                                            existingUser.getRole().toString());
        
        // Tạo phản hồi
        Map<String, Object> userMap = createUserResponse(existingUser, token);
        
        return ResponseEntity.ok(userMap);
    }
    
    /**
     * Xử lý đăng ký Admin
     */
    private ResponseEntity<?> processAdminRegistration(String email, String name, String picture) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setUsername(email);
        newUser.setImage(picture);
        newUser.setPassword(UUID.randomUUID().toString());
        newUser.setActive("Active");
        newUser.setRole(User.Role.Admin);
        
        User savedUser = userService.saveOAuth2User(newUser);
        
        // Tạo JWT token
        String token = jwtUtils.generateToken(savedUser.getUsername(), 
                                            savedUser.getRole().toString());
        
        // Tạo phản hồi
        Map<String, Object> userMap = createUserResponse(savedUser, token);
        
        return ResponseEntity.ok(userMap);
    }
    
    /**
     * Xử lý đăng ký Teacher
     */
    private ResponseEntity<?> processTeacherRegistration(String email, String name, String picture, Teacher teacher) {
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
        
        User savedUser = userService.saveOAuth2User(newUser);
        
        // Tạo JWT token
        String token = jwtUtils.generateToken(savedUser.getUsername(), 
                                            savedUser.getRole().toString());
        
        // Tạo phản hồi
        Map<String, Object> userMap = createUserResponse(savedUser, token);
        
        return ResponseEntity.ok(userMap);
    }
    
    /**
     * Xử lý đăng ký Student
     */
    private ResponseEntity<?> processStudentRegistration(String email, String name, String picture, Student student) {
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
        
        User savedUser = userService.saveOAuth2User(newUser);
        
        // Tạo JWT token
        String token = jwtUtils.generateToken(savedUser.getUsername(), 
                                            savedUser.getRole().toString());
        
        // Tạo phản hồi
        Map<String, Object> userMap = createUserResponse(savedUser, token);
        
        return ResponseEntity.ok(userMap);
    }
    
    /**
     * Tạo phản hồi chuẩn cho client
     */
    private Map<String, Object> createUserResponse(User user, String token) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("status", "success");
        userMap.put("message", "Đăng nhập thành công");
        userMap.put("id", user.getId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("name", user.getName());
        userMap.put("image", user.getImage());
        userMap.put("role", user.getRole());
        userMap.put("token", token);
        
        return userMap;
    }
}