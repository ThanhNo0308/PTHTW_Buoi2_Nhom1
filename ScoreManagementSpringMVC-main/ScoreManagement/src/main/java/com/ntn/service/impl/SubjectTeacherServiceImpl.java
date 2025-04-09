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
    private SubjectTeacherRepository subjTeachRepo;
    @Override
    public List<Subjectteacher> getSubjectTeachers() {
        return this.subjTeachRepo.getSubjectTeachers();
    }
    
}
