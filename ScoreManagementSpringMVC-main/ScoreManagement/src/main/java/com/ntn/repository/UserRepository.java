/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Student;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import java.util.List;

/**
 *
 * @author vhuunghia
 */
public interface UserRepository {

//    List<User> getUsers();

    User getUserByUsername(String username);

    boolean authUser(String username, String password);

    User addUser(User user);
    User addTeacherUser(User user);
    boolean findEmail(String email);
    List<Student> getStudentbyEmail(String email);
    List<Teacher> getTeacherByEmail(String email);
    boolean findTeacherEmail(String email);
    boolean authAdminUser(String username, String password);
    boolean authTeacherUser(String username, String password);
    boolean authStudentUser(String username, String password);
}
