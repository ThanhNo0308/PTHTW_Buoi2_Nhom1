/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.repository.StudentSubjectTeacherRepository;
import com.ntn.service.StudentSubjectTeacherService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nguye
 */
@Service
public class StudentSubjectTeacherServiceImpl implements StudentSubjectTeacherService {
    
    @Autowired
    private StudentSubjectTeacherRepository studentSubjectTeacherRepository;
    @Override
    public List<Studentsubjectteacher> getStudsubjteachs() {
        return this.studentSubjectTeacherRepository.getStudsubjteachs();
    }
    
    @Override
    public List<Studentsubjectteacher> getAll() {
        return this.studentSubjectTeacherRepository.getAll();
    }

    @Override
    public Studentsubjectteacher getById(int id) {
        return this.studentSubjectTeacherRepository.getById(id);
    }

    @Override
    public boolean addOrUpdate(Studentsubjectteacher enrollment) {
        return this.studentSubjectTeacherRepository.addOrUpdate(enrollment);
    }

    @Override
    public boolean delete(int id) {
        return this.studentSubjectTeacherRepository.delete(id);
    }

    @Override
    public List<Studentsubjectteacher> getByStudentId(int studentId) {
        return this.studentSubjectTeacherRepository.getByStudentId(studentId);
    }

    @Override
    public List<Studentsubjectteacher> getBySubjectTeacherId(int subjectTeacherId) {
        return this.studentSubjectTeacherRepository.getBySubjectTeacherId(subjectTeacherId);
    }

    @Override
    public List<Studentsubjectteacher> getByTeacherId(int teacherId) {
        return this.studentSubjectTeacherRepository.getByTeacherId(teacherId);
    }

    @Override
    public List<Studentsubjectteacher> getBySubjectId(int subjectId) {
        return this.studentSubjectTeacherRepository.getBySubjectId(subjectId);
    }

    @Override
    public List<Studentsubjectteacher> getBySchoolYearId(int schoolYearId) {
        return this.studentSubjectTeacherRepository.getBySchoolYearId(schoolYearId);
    }

    @Override
    public List<Studentsubjectteacher> getByClassId(int classId) {
        return this.studentSubjectTeacherRepository.getByClassId(classId);
    }

    @Override
    public boolean checkDuplicate(Integer studentId, Integer subjectTeacherId, Integer schoolYearId) {
        return this.studentSubjectTeacherRepository.checkDuplicate(studentId, subjectTeacherId, schoolYearId);
    }

    @Override
    public boolean checkDuplicateExcept(Integer studentId, Integer subjectTeacherId, Integer schoolYearId, Integer exceptId) {
        return this.studentSubjectTeacherRepository.checkDuplicateExcept(studentId, subjectTeacherId, schoolYearId, exceptId);
    }

    @Override
    public boolean hasRelatedScores(int enrollmentId) {
        return this.studentSubjectTeacherRepository.hasRelatedScores(enrollmentId);
    }

    @Override
    public int batchEnrollStudents(int classId, int subjectTeacherId, int schoolYearId) {
        return this.studentSubjectTeacherRepository.batchEnrollStudents(classId, subjectTeacherId, schoolYearId);
    }
    
    @Override
    public long countEnrollments() {
        return this.studentSubjectTeacherRepository.countEnrollments();
    }
    
}
