/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.User;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User getUserById(int id);

    User getUserByUn(String username);

    User getUserByEmail(String email);

    User saveOAuth2User(User user);

    User addUser(Map<String, String> params);

    User addTeacherUser(Map<String, String> params);

    List<User> getUsers();

    List<Map<String, Object>> getUsersByRole(String role);
    
    boolean isUsernameExists(String username);

    boolean isEmailExists(String email);

    boolean isTeacherEmailExists(String email);

    boolean authAdminUser(String username, String password);

    boolean authTeacherUser(String username, String password);

    boolean authStudentUser(String username, String password);

    boolean updateUser(User user);

    boolean deleteUser(int id);

    boolean isEmailExistsInUserTable(String email);

}
