package com.ntn.service;

import com.ntn.pojo.Student;
import java.util.List;
import java.util.Optional;

public interface StudentService {

    Student getStudentById(int studentId);

    Student getStudentByCode(String studentCode);

    Student getStudentByEmail(String email);

    List<Student> getStudents();

    List<Student> getStudentByClassId(int classId);

    List<Student> findStudentsByCode(String code);

    List<Student> findStudentsByName(String name);

    List<Student> findStudentsByClass(String className);

    Optional<Student> findByStudentCode(String studentId);

    List<Student> getStudentsByKeyword(String keyword);

    List<Student> getStudentsByClassIdAndKeyword(Integer classId, String keyword);

    List<Student> getStudentbyEmail(String email);

    int countStudents();

    int countStudentsByClassId(int classId);

    boolean addOrUpdateStudent(Student student);

    boolean deleteStudent(int studentId);

}
