/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import java.util.List;
import java.util.Map;

public interface UserRepository {

    User getUserById(int id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User addUser(User user);

    User addTeacherUser(User user);

    List<User> getUsers();

    List<Map<String, Object>> getUsersByRole(String role);

    List<Teacher> getTeacherByEmail(String email);
    
    boolean isUsernameExists(String username);

    boolean isEmailExistsInUserTable(String email);

    boolean saveUser(User user);

    boolean findEmail(String email);

    boolean findTeacherEmail(String email);

    boolean authAdminUser(String username, String password);

    boolean authTeacherUser(String username, String password);

    boolean authStudentUser(String username, String password);

    boolean updateUser(User user);

    boolean deleteUser(int id);

}
