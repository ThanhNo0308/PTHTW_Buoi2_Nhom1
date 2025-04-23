/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

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
public class SubjectTeacherServiceImpl implements SubjectTeacherService{
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

}
