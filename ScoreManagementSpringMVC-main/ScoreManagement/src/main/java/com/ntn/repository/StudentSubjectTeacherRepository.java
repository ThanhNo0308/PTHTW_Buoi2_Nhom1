/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import java.util.List;

public interface StudentSubjectTeacherRepository {

    List<Studentsubjectteacher> getAll();

    Studentsubjectteacher getById(int id);

    boolean addOrUpdate(Studentsubjectteacher enrollment);

    boolean delete(int id);

    List<Studentsubjectteacher> getByStudentId(int studentId);

    List<Studentsubjectteacher> getBySubjectTeacherId(int subjectTeacherId);

    List<Studentsubjectteacher> getByTeacherId(int teacherId);

    List<Studentsubjectteacher> getBySubjectId(int subjectId);

    List<Studentsubjectteacher> getByClassId(int classId);
    
    List<Studentsubjectteacher> getBySchoolYearId(int schoolYearId);

    boolean checkDuplicate(Integer studentId, Integer subjectTeacherId);

    boolean checkDuplicateExcept(Integer studentId, Integer subjectTeacherId, Integer exceptId);

    boolean hasRelatedScores(int enrollmentId);

    int batchEnrollStudents(int classId, int subjectTeacherId);

    long countEnrollments();
    
    List<Studentsubjectteacher> getEnrollmentsByStudentCode(String studentCode);
}
