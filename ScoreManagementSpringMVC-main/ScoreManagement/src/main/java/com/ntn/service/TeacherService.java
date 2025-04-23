/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Teacher;
import java.util.List;

/**
 *
 * @author vhuunghia
 */
public interface TeacherService {

    int getIdTeacherByEmail(String email);

    int getidStudentByEmail(String email);
    
    boolean addOrUpdateTeacher(Teacher teacher);
    boolean deleteTeacher(int teacherId);
    
    int getTeacherIdByUsername(String username);
    List<Class> getClassesByTeacher(int teacherId);
    int getSubjectTeacherIdByTeacherAndClass(int teacherId, int classId);
    
    List<Teacher> getTeachers();
    int countTeachers();
    Teacher getTeacherById(int teacherId);
    Teacher getTeacherByUsername(String username);
    Teacher getTeacherByEmail(String email);
    List<Teacher> getTeachersByDepartmentId(Integer departmentId);
    List<Teacher> getTeachersByKeyword(String keyword);
    List<Teacher> getTeachersByDepartmentIdAndKeyword(Integer departmentId, String keyword);
}
