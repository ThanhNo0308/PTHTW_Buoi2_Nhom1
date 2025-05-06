/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Teacher;
import java.util.List;

public interface TeacherService {

    Teacher getTeacherById(int teacherId);

    Teacher getTeacherByUsername(String username);

    Teacher getTeacherByEmail(String email);

    List<Teacher> getTeachers();

    List<Teacher> getTeachersByDepartmentId(Integer departmentId);

    List<Teacher> getTeachersByKeyword(String keyword);

    List<Teacher> getTeachersByDepartmentIdAndKeyword(Integer departmentId, String keyword);

    int countTeachers();

    boolean addOrUpdateTeacher(Teacher teacher);

    boolean deleteTeacher(int teacherId);

}
