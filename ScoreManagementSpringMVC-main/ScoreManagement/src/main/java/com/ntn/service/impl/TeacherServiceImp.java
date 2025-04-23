/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import com.ntn.repository.TeacherRepository;
import com.ntn.repository.UserRepository;
import com.ntn.service.TeacherService;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vhuunghia
 */
@Service
public class TeacherServiceImp implements TeacherService {

    @Autowired
    private TeacherRepository teacherRepo;

    @Autowired
    private UserRepository userRepo;

    @Override
    public int getIdTeacherByEmail(String email) {
        return this.teacherRepo.getidTeacherByEmail(email);
    }

    @Override
    public int getidStudentByEmail(String email) {
        return this.teacherRepo.getidStudentByEmail(email);
    }

    @Override
    public boolean addOrUpdateTeacher(Teacher teacher) {
        return this.teacherRepo.addOrUpdateTeacher(teacher);
    }

    @Override
    public boolean deleteTeacher(int teacherId) {
        return this.teacherRepo.deleteTeacher(teacherId);
    }

    @Override
    public int getTeacherIdByUsername(String username) {
        // Lấy User từ username
        User user = userRepo.getUserByUsername(username);
        if (user == null) {
            return 0;
        }

        // Lấy teacher từ user
        Teacher teacher = teacherRepo.getTeacherByUserId(user.getId());
        if (teacher == null) {
            return 0;
        }

        return teacher.getId();
    }

    @Override
    public List<Class> getClassesByTeacher(int teacherId) {
        return teacherRepo.getClassesByTeacherId(teacherId);
    }

    @Override
    public int getSubjectTeacherIdByTeacherAndClass(int teacherId, int classId) {
        return teacherRepo.getSubjectTeacherIdByTeacherAndClass(teacherId, classId);
    }

    @Override
    public List<Teacher> getTeachers() {
        return this.teacherRepo.getTeachers();
    }

    @Override
    public int countTeachers() {
        return this.teacherRepo.countTeachers();
    }

    @Override
    public Teacher getTeacherById(int teacherId) {
        return this.teacherRepo.getTeacherById(teacherId);
    }

    @Override
    public Teacher getTeacherByUsername(String username) {
        User user = userRepo.getUserByUsername(username);
        if (user == null) {
            return null;
        }
        return teacherRepo.getTeacherByUserId(user.getId());
    }

    @Override
    public Teacher getTeacherByEmail(String email) {
        return this.teacherRepo.getTeacherByEmail(email);
    }

    @Override
    public List<Teacher> getTeachersByDepartmentId(Integer departmentId) {
        return this.teacherRepo.getTeachersByDepartmentId(departmentId);
    }

    @Override
    public List<Teacher> getTeachersByKeyword(String keyword) {
        return this.teacherRepo.getTeachersByKeyword(keyword);
    }

    @Override
    public List<Teacher> getTeachersByDepartmentIdAndKeyword(Integer departmentId, String keyword) {
        return this.teacherRepo.getTeachersByDepartmentIdAndKeyword(departmentId, keyword);
    }
}
