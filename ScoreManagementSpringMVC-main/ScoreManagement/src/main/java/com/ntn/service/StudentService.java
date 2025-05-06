package com.ntn.service;

import com.ntn.pojo.Student;
import java.util.List;
import java.util.Optional;

public interface StudentService {

    List<Student> getStudentByClassId(int classId);

    boolean addOrUpdateStudent(Student student);

    boolean deleteStudent(int studentId);

    Optional<Student> findByStudentCode(String studentId);

    List<Student> getStudents();

    Student getStudentById(int studentId);
    
    Student getStudentByCode(String studentCode);

    int countStudents();

    List<Student> findStudentsByCode(String code);

    List<Student> findStudentsByName(String name);

    List<Student> findStudentsByClass(String className);

    int countStudentsByClassId(int classId);

    List<Student> getStudentsByKeyword(String keyword);

    List<Student> getStudentsByClassIdAndKeyword(Integer classId, String keyword);

    List<Student> getStudentbyEmail(String email);
    
    Student getStudentByEmail(String email);
}
