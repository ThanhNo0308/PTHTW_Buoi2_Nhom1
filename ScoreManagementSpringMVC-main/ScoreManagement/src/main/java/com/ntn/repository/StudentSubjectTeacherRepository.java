/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import java.util.List;

public interface StudentSubjectTeacherRepository {

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

    List<Studentsubjectteacher> getStudentsubjectteacherBySubjectTeacherID(List<Subjectteacher> listsubjectteacher, int schoolYearID);

    List<Studentsubjectteacher> getListStudentsubjectteacher(int subjectteacherID, int selectedSchoolYearId);

    List<Studentsubjectteacher> getListStudentsubjectteacherByStudentID(int studentID, int schoolyearID);
}
