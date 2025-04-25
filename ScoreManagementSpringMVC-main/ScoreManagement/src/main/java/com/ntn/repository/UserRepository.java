/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Student;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import java.util.List;

public interface UserRepository {

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    boolean authUser(String username, String password);

    User addUser(User user);
    boolean saveUser(User user);

    User addTeacherUser(User user);

    boolean findEmail(String email);

    List<Teacher> getTeacherByEmail(String email);

    boolean findTeacherEmail(String email);

    boolean authAdminUser(String username, String password);

    boolean authTeacherUser(String username, String password);

    boolean authStudentUser(String username, String password);

    List<User> getUsers();

    User getUserById(int id);

    boolean updateUser(User user);
    boolean deleteUser(int id);
}
