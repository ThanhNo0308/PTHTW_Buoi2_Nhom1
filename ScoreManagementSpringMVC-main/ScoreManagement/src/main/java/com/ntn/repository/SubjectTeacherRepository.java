/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Subjectteacher;
import java.util.List;

/**
 *
 * @author nguye
 */
public interface SubjectTeacherRepository {
    List<Subjectteacher> getSubjectTeachers();
    List<Subjectteacher> getAllSubjectTeachers();
    Subjectteacher getSubjectTeacherById(int id);
    boolean addOrUpdateSubjectTeacher(Subjectteacher subjectTeacher);
    boolean deleteSubjectTeacher(int id);
    List<Subjectteacher> getSubjectTeachersByTeacherId(int teacherId);
    List<Subjectteacher> getSubjectTeachersBySubjectId(Integer subjectId);
    List<Subjectteacher> getSubjectTeachersByDepartmentId(int departmentId);
}
