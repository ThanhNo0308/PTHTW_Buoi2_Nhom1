/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Studentsubjectteacher;
import java.util.List;

public interface StudentSubjectTeacherService {

    Studentsubjectteacher getById(int id);

    List<Studentsubjectteacher> getAll();

    List<Studentsubjectteacher> getByStudentId(int studentId);

    List<Studentsubjectteacher> getBySubjectTeacherId(int subjectTeacherId);

    List<Studentsubjectteacher> getByTeacherId(int teacherId);

    List<Studentsubjectteacher> getBySubjectId(int subjectId);

    List<Studentsubjectteacher> getByClassId(int classId);

    List<Studentsubjectteacher> getByTeachingClassId(int teachingClassId);

    List<Studentsubjectteacher> getBySchoolYearIdThroughSubjectTeacher(int schoolYearId);

    List<Studentsubjectteacher> getEnrollmentsByStudentCode(String studentCode);

    int batchEnrollStudents(int classId, int subjectTeacherId);

    long countEnrollments();

    boolean checkDuplicate(Integer studentId, Integer subjectTeacherId);

    boolean checkDuplicateExcept(Integer studentId, Integer subjectTeacherId, Integer exceptId);

    boolean addOrUpdate(Studentsubjectteacher enrollment);

    boolean delete(int id);

    boolean hasRelatedScores(int enrollmentId);

    boolean addStudentToSubjectTeacher(int studentId, int subjectTeacherId, int schoolYearId);

}
