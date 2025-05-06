/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Subject;
import com.ntn.repository.SubjectRepository;
import com.ntn.service.SubjectService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectRepository subjRepo;

    @Override
    public List<Subject> getSubjects() {
        return this.subjRepo.getSubjects();
    }

    @Override
    public List<Subject> getSubjectsByDepartmentId(Integer departmentId) {
        return this.subjRepo.getSubjectsByDepartmentId(departmentId);
    }

    @Override
    public List<Subject> getSubjectsByDepartmentIdAndKeyword(Integer departmentId, String keyword) {
        return this.subjRepo.getSubjectsByDepartmentIdAndKeyword(departmentId, keyword);
    }

    @Override
    public List<Subject> getSubjectsByKeyword(String keyword) {
        return this.subjRepo.getSubjectsByKeyword(keyword);
    }

    @Override
    public boolean addOrUpdateSubject(Subject subject) {
        return this.subjRepo.addOrUpdateSubject(subject);
    }

    @Override
    public boolean deleteSubject(int subjectId) {
        return this.subjRepo.deleteSubject(subjectId);
    }

    @Override
    public Subject getSubjectById(int subjectId) {
        return this.subjRepo.getSubjectById(subjectId);
    }

}
