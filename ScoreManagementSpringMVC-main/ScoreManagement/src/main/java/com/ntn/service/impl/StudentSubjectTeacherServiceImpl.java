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
    private StudentSubjectTeacherRepository studsubjteachRepo;
    @Override
    public List<Studentsubjectteacher> getStudsubjteachs() {
        return this.studsubjteachRepo.getStudsubjteachs();
    }
    
}
