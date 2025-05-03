/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import java.util.List;

public interface StudentSubjectTeacherService {

    List<Studentsubjectteacher> getStudsubjteachs();

    List<Studentsubjectteacher> getAll();

    Studentsubjectteacher getById(int id);

    boolean addOrUpdate(Studentsubjectteacher enrollment);

    boolean delete(int id);

    List<Studentsubjectteacher> getByStudentId(int studentId);

    List<Studentsubjectteacher> getBySubjectTeacherId(int subjectTeacherId);

    List<Studentsubjectteacher> getByTeacherId(int teacherId);

    List<Studentsubjectteacher> getBySubjectId(int subjectId);

    List<Studentsubjectteacher> getByClassId(int classId);

    boolean checkDuplicate(Integer studentId, Integer subjectTeacherId);

    boolean checkDuplicateExcept(Integer studentId, Integer subjectTeacherId, Integer exceptId);

    int batchEnrollStudents(int classId, int subjectTeacherId);

    List<Studentsubjectteacher> getBySchoolYearIdThroughSubjectTeacher(int schoolYearId);

    boolean hasRelatedScores(int enrollmentId);

    long countEnrollments();

    List<Studentsubjectteacher> getStudentsubjectteacherBySubjectTeacherID(List<Subjectteacher> listsubjectteacher);

    List<Studentsubjectteacher> getListStudentsubjectteacher(int subjectteacherID);

    List<Studentsubjectteacher> getListStudentsubjectteacherByStudentID(int studentID);

    List<Studentsubjectteacher> getEnrollmentsByStudentCode(String studentCode);

}
