/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Teacher;
import com.ntn.repository.TeacherRepository;
import com.ntn.service.TeacherService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vhuunghia
 */
@Service
public class TeacherServiceImp implements TeacherService{
    @Autowired
    private TeacherRepository teacherRepository;
    
    
    @Override
    public int getIdTeacherByEmail(String email){
        return this.teacherRepository.getidTeacherByEmail(email);
    }
    
    @Override
    public int getidStudentByEmail(String email){
        return this.teacherRepository.getidStudentByEmail(email);
    }

    @Override
    public boolean addOrUpdateTeacher(Teacher teacher) {
        return this.teacherRepository.addOrUpdateTeacher(teacher);
    }

    @Override
    public boolean deleteTeacher(int teacherId) {
        return this.teacherRepository.deleteTeacher(teacherId);
    }
}
