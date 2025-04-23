/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Studentsubjectteacher;
import java.util.List;

/**
 *
 * @author nguye
 */
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

    List<Studentsubjectteacher> getBySchoolYearId(int schoolYearId);

    List<Studentsubjectteacher> getByClassId(int classId);

    boolean checkDuplicate(Integer studentId, Integer subjectTeacherId, Integer schoolYearId);

    boolean checkDuplicateExcept(Integer studentId, Integer subjectTeacherId, Integer schoolYearId, Integer exceptId);

    boolean hasRelatedScores(int enrollmentId);

    int batchEnrollStudents(int classId, int subjectTeacherId, int schoolYearId);

    long countEnrollments();
}
