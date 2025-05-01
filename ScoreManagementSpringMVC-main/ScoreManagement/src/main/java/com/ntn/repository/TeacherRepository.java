/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Teacher;
import java.util.List;

public interface TeacherRepository {

    int getidTeacherByEmail(String email);

    int getidStudentByEmail(String email);
    int getTeacherIdByUsername(String username);

    boolean addOrUpdateTeacher(Teacher teacher);

    boolean deleteTeacher(int teacherId);

    Teacher getTeacherByUserId(int userId);

    List<Class> getClassesByTeacherId(int teacherId);

    int getSubjectTeacherIdByTeacherAndClass(int teacherId, int classId);

    List<Teacher> getTeachers();

    int countTeachers();

    Teacher getTeacherById(int teacherId);

    Teacher getTeacherByEmail(String email);

    List<Teacher> getTeachersByDepartmentId(Integer departmentId);

    List<Teacher> getTeachersByKeyword(String keyword);

    List<Teacher> getTeachersByDepartmentIdAndKeyword(Integer departmentId, String keyword);
}
