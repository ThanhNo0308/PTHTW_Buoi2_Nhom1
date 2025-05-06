package com.ntn.service.impl;

import com.ntn.pojo.Student;
import com.ntn.repository.StudentRepository;
import com.ntn.service.StudentService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studRepo;

    @Override
    public List<Student> getStudentByClassId(int classId) {
        return this.studRepo.getStudentByClassId(classId);
    }

    @Override
    public List<Student> getStudentbyEmail(String email) {
        return this.studRepo.getStudentbyEmail(email);
    }
    

    @Override
    public boolean addOrUpdateStudent(Student student) {
        return this.studRepo.addOrUpdateStudent(student);
    }

    @Override
    public boolean deleteStudent(int studentId) {
        return this.studRepo.deleteStudent(studentId);
    }

    @Override
    public Optional<Student> findByStudentCode(String studentCode) {
        return this.studRepo.findByStudentCode(studentCode);
    }

    @Override
    public List<Student> getStudents() {
        return this.studRepo.getStudents();
    }

    @Override
    public Student getStudentById(int studentId) {
        return this.studRepo.getStudentById(studentId);
    }

    @Override
    public int countStudents() {
        return this.studRepo.countStudents();
    }

    @Override
    public List<Student> findStudentsByCode(String code) {
        return this.studRepo.findStudentsByCode(code);
    }

    @Override
    public List<Student> findStudentsByName(String name) {
        return this.studRepo.findStudentsByName(name);
    }

    @Override
    public List<Student> findStudentsByClass(String className) {
        return this.studRepo.findStudentsByClass(className);
    }

    @Override
    public int countStudentsByClassId(int classId) {
        return this.studRepo.countStudentsByClassId(classId);
    }

    @Override
    public List<Student> getStudentsByKeyword(String keyword) {
        return this.studRepo.getStudentsByKeyword(keyword);
    }

    @Override
    public List<Student> getStudentsByClassIdAndKeyword(Integer classId, String keyword) {
        return this.studRepo.getStudentsByClassIdAndKeyword(classId, keyword);
    }

    @Override
    public Student getStudentByEmail(String email) {
        return this.studRepo.getStudentByEmail(email);
    }

    @Override
    public Student getStudentByCode(String studentCode) {
        return this.studRepo.getStudentByCode(studentCode);
    }
}
