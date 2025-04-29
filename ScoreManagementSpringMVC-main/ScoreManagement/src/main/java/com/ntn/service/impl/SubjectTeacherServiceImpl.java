/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import com.ntn.repository.SubjectTeacherRepository;
import com.ntn.service.SubjectTeacherService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nguye
 */
@Service
public class SubjectTeacherServiceImpl implements SubjectTeacherService {

    @Autowired
    private SubjectTeacherRepository subjectTeacherRepo;

    @Override
    public List<Subjectteacher> getSubjectTeachers() {
        return this.subjectTeacherRepo.getSubjectTeachers();
    }

    @Override
    public List<Subjectteacher> getAllSubjectTeachers() {
        return this.subjectTeacherRepo.getAllSubjectTeachers();
    }

    @Override
    public Subjectteacher getSubjectTeacherById(int id) {
        return this.subjectTeacherRepo.getSubjectTeacherById(id);
    }

    @Override
    public boolean addOrUpdateSubjectTeacher(Subjectteacher subjectTeacher) {
        return this.subjectTeacherRepo.addOrUpdateSubjectTeacher(subjectTeacher);
    }

    @Override
    public boolean deleteSubjectTeacher(int id) {
        return this.subjectTeacherRepo.deleteSubjectTeacher(id);
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByTeacherId(int teacherId) {
        return this.subjectTeacherRepo.getSubjectTeachersByTeacherId(teacherId);
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersBySubjectId(Integer subjectId) {
        return this.subjectTeacherRepo.getSubjectTeachersBySubjectId(subjectId);
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByDepartmentId(int departmentId) {
        return this.subjectTeacherRepo.getSubjectTeachersByDepartmentId(departmentId);
    }

    @Override
    public Subjectteacher getSubJectTeacherById(int id) {
        return this.subjectTeacherRepo.getSubJectTeacherById(id);
    }

    @Override
    public List<Subjectteacher> getSubjectTeacherByTeacherID(int TeacherID) {
        return this.subjectTeacherRepo.getSubjectTeacherByTeacherID(TeacherID);
    }

    @Override
    public List<Subjectteacher> getSubjectTeacherByListSubjectTeacherId(List<Studentsubjectteacher> listStudentSubjectTeacher) {
        return this.subjectTeacherRepo.getSubjectTeacherByListSubjectTeacherId(listStudentSubjectTeacher);
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersBySchoolYearId(int schoolYearId) {
        return this.subjectTeacherRepo.getSubjectTeachersBySchoolYearId(schoolYearId);
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByTeacherIdAndSchoolYearId(int teacherId, int schoolYearId) {
        return this.subjectTeacherRepo.getSubjectTeachersByTeacherIdAndSchoolYearId(teacherId, schoolYearId);
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByClassId(int classId) {
        return this.subjectTeacherRepo.getSubjectTeachersByClassId(classId);
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByTeacherIdAndClassId(int teacherId, int classId) {
        return this.subjectTeacherRepo.getSubjectTeachersByTeacherIdAndClassId(teacherId, classId);
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByTeacherIdAndClassIdAndSchoolYearId(int teacherId, int classId, int schoolYearId) {
        return this.subjectTeacherRepo.getSubjectTeachersByTeacherIdAndClassIdAndSchoolYearId(teacherId, classId, schoolYearId);
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersBySubjectIdAndClassId(int subjectId, int classId) {
        return this.subjectTeacherRepo.getSubjectTeachersBySubjectIdAndClassId(subjectId, classId);
    }

}
