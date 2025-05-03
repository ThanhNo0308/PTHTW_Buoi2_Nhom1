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

    User getUserByUn(String username);

    User getUserByEmail(String email);

    boolean authUser(String username, String password);

    User addUser(Map<String, String> params);

    boolean isEmailExists(String email);

    User addTeacherUser(Map<String, String> params);

    boolean isTeacherEmailExists(String email);

    boolean authAdminUser(String username, String password);

    boolean authTeacherUser(String username, String password);

    boolean authStudentUser(String username, String password);

    List<User> getUsers();

    User getUserById(int id);

    boolean updateUser(User user);

    boolean deleteUser(int id);
    
    boolean isEmailExistsInUserTable(String email);
}
