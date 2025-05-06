/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ntn.pojo.Student;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import com.ntn.repository.StudentRepository;
import com.ntn.repository.UserRepository;
import com.ntn.service.UserService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service("UserDetailsService")
public class UserServiceImp implements UserService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private Cloudinary cloudinary;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    @Override
    public User getUserByUn(String username) {
        return this.userRepo.getUserByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepo.getUserByEmail(email);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userRepo.isUsernameExists(username);
    }

    @Override
    public boolean isEmailExistsInUserTable(String email) {
        return this.userRepo.isEmailExistsInUserTable(email);
    }

    @Override
    public User addUser(Map<String, String> params) {
        String email = params.get("email");
        String username = params.get("username");
        // Tạo EntityManager

        // Lấy danh sách kết quả
        List<Student> students = this.studentRepository.getStudentbyEmail(email);

        if (!students.isEmpty()) {
            // Tìm thấy Student với email tương ứng
            Student foundStudent = students.get(0);
            User user = new User();
            user.setName(foundStudent.getLastName() + " " + foundStudent.getFirstName());
            user.setGender(foundStudent.getGender());
            user.setIdentifyCard(foundStudent.getIdentifyCard());
            user.setHometown(foundStudent.getHometown());
            user.setBirthdate(foundStudent.getBirthdate());
            user.setPhone(foundStudent.getPhone());
            user.setEmail(foundStudent.getEmail());
            user.setUsername(username);
            user.setPassword(this.passwordEncoder.encode(params.get("password")));
            user.setActive(foundStudent.getStatus());
            user.setRole(User.Role.Student);
            String avatarData = params.get("avatar");
            if (avatarData != null && !avatarData.isEmpty()) {
                try {
                    Map uploadResult = this.cloudinary.uploader().upload(
                            "data:image/png;base64," + avatarData,
                            ObjectUtils.asMap("resource_type", "auto")
                    );
                    user.setImage(uploadResult.get("secure_url").toString());
                } catch (IOException ex) {
                    Logger.getLogger(UserServiceImp.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            } else {
                return null; // Return null if no avatar provided
            }

            this.userRepo.addUser(user);
            return user;
        } else {
            // Không tìm thấy Student với email tương ứng
            return null;
        }
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepo.findEmail(email);
    }

    @Override
    public User addTeacherUser(Map<String, String> params) {
        String email = params.get("email");
        // Lấy danh sách kết quả
        List<Teacher> teachers = this.userRepo.getTeacherByEmail(email);
        if (!teachers.isEmpty()) {
            Teacher foundTeacher = teachers.get(0);
            User user = new User();
            user.setEmail(email);
            user.setUsername(email);
            user.setName(foundTeacher.getTeacherName());
            user.setGender(foundTeacher.getGender());
            user.setHometown(foundTeacher.getAddress());
            user.setBirthdate(foundTeacher.getBirthdate());
            user.setPhone(foundTeacher.getPhoneNumber());
            user.setPassword(this.passwordEncoder.encode(params.get("password")));
            user.setActive("Active");
            user.setRole(User.Role.Teacher);

            this.userRepo.addTeacherUser(user);
            return user;
        } else {
            // Không tìm thấy Teacher với email tương ứng
            return null;
        }
    }

    @Override
    public boolean isTeacherEmailExists(String email) {
        return this.userRepo.findTeacherEmail(email);
    }

    @Override
    public boolean authAdminUser(String username, String password) {
        return this.userRepo.authAdminUser(username, password);
    }

    @Override
    public boolean authTeacherUser(String username, String password) {
        return this.userRepo.authTeacherUser(username, password);
    }

    @Override
    public boolean authStudentUser(String username, String password) {
        return this.userRepo.authStudentUser(username, password);
    }

    @Override
    public List<User> getUsers() {
        return this.userRepo.getUsers();
    }

    @Override
    public User getUserById(int id) {
        return this.userRepo.getUserById(id);
    }

    @Override
    public boolean updateUser(User user) {
        return this.userRepo.updateUser(user);
    }

    @Override
    public boolean deleteUser(int id) {
        return this.userRepo.deleteUser(id);
    }

    @Override
    public List<Map<String, Object>> getUsersByRole(String role) {
        return this.userRepo.getUsersByRole(role);
    }

    @Override
    public User saveOAuth2User(User user) {
        // Kiểm tra email
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return null;
        }

        // Thiết lập các giá trị mặc định nếu cần
        if (user.getActive() == null) {
            user.setActive("Active");
        }

        // Lưu người dùng vào cơ sở dữ liệu
        return this.userRepo.addUser(user);
    }
}
