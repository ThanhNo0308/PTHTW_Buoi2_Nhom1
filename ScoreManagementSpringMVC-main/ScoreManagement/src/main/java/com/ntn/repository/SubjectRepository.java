/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subject;
import com.ntn.pojo.Subjectteacher;
import java.util.List;

/**
 *
 * @author nguye
 */
public interface SubjectRepository {

    List<Subject> getSubjects();

    List<Subject> getListSubjectById(List<Integer> listSubjectID);
    
    List<Subject> getSubjectsByKeyword(String keyword);
    List<Subject> getSubjectsByDepartmentId(Integer departmentId);
    List<Subject> getSubjectsByDepartmentIdAndKeyword(Integer departmentId, String keyword);

    List<Subjectteacher> getSubjectTeacherByTeacherID(int TeacherID);

    List<Studentsubjectteacher> getStudentsubjectteacherBySubjectTeacherID(List<Subjectteacher> listsubjectteacher, int schoolYearID);

    List<Integer> getSubjectTeacherId(List<Studentsubjectteacher> studentSubjectTeacher);

    List<Integer> getSubjectIdByListSubjectTeacherId(List<Integer> listSubjectTeacherId);

    List<Studentsubjectteacher> getListStudentsubjectteacher(int subjectteacherID, int selectedSchoolYearId);

    List<Studentsubjectteacher> getListStudentsubjectteacherByStudentID(int studentID, int schoolyearID);

    List<Subjectteacher> getSubjectTeacherByListSubjectTeacherId(List<Studentsubjectteacher> listStudentSubjectTeacher);
          
    boolean addOrUpdateSubject(Subject subject);
    
    boolean deleteSubject(int subjectId);
    
    Subject getSubjectById(int subjectId);
    
}
