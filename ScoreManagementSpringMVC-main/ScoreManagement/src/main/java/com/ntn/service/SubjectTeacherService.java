/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import java.util.List;

public interface SubjectTeacherService {

    List<Subjectteacher> getSubjectTeachers();

    List<Subjectteacher> getAllSubjectTeachers();

    Subjectteacher getSubjectTeacherById(int id);

    boolean addOrUpdateSubjectTeacher(Subjectteacher subjectTeacher);

    boolean deleteSubjectTeacher(int id);

    List<Subjectteacher> getSubjectTeachersByTeacherId(int teacherId);

    List<Subjectteacher> getSubjectTeachersBySubjectId(Integer subjectId);

    List<Subjectteacher> getSubjectTeachersByDepartmentId(int departmentId);

    Subjectteacher getSubJectTeacherById(int id);

    List<Subjectteacher> getSubjectTeacherByTeacherID(int TeacherID);
    
    List<Subjectteacher> getSubjectTeacherByListSubjectTeacherId(List<Studentsubjectteacher> listStudentSubjectTeacher);
}
