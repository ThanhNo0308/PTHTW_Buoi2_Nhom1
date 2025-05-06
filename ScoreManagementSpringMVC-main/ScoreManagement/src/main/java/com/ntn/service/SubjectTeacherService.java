/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Subjectteacher;
import java.util.List;

public interface SubjectTeacherService {

    Subjectteacher getSubjectTeacherById(int id);

    Subjectteacher findByIdClassIdAndSchoolYearId(int id, int classId, int schoolYearId);

    List<Subjectteacher> getAllSubjectTeachers();

    List<Subjectteacher> getSubjectTeachersByTeacherId(int teacherId);

    List<Subjectteacher> getSubjectTeachersBySubjectId(Integer subjectId);

    List<Subjectteacher> getSubjectTeachersByDepartmentId(int departmentId);

    List<Subjectteacher> getSubjectTeachersBySchoolYearId(int schoolYearId);

    List<Subjectteacher> getSubjectTeachersByTeacherIdAndSchoolYearId(int teacherId, int schoolYearId);

    List<Subjectteacher> getSubjectTeachersByClassId(int classId);

    List<Subjectteacher> getSubjectTeachersByTeacherIdAndClassId(int teacherId, int classId);

    List<Subjectteacher> getSubjectTeachersByTeacherIdAndClassIdAndSchoolYearId(int teacherId, int classId, int schoolYearId);

    List<Subjectteacher> getSubjectTeachersBySubjectIdAndClassId(int subjectId, int classId);

    List<Subjectteacher> getSubjectTeachersByClassAndSchoolYear(int classId, int schoolYearId);

    boolean addOrUpdateSubjectTeacher(Subjectteacher subjectTeacher);

    boolean deleteSubjectTeacher(int id);
}
