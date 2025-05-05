package com.ntn.repository;

import com.ntn.pojo.Student;
import java.util.List;
import java.util.Optional;

public interface StudentRepository {

    List<Student> getStudentByClassId(int classId);
    
    Student getStudentByEmail(String email);

    boolean addOrUpdateStudent(Student student);

    boolean deleteStudent(int studentId);

    Student getStudentById(int studentId);

    Optional<Student> findByStudentCode(String studentCode);

    int countStudents();

    List<Student> getStudents();

    int countStudentsByClassId(int classId);

    List<Student> getStudentsByKeyword(String keyword);

    List<Student> getStudentsByClassIdAndKeyword(Integer classId, String keyword);

    List<Student> getStudentbyEmail(String email);

    List<Student> findStudentsByCode(String code);

    List<Student> findStudentsByName(String name);

    List<Student> findStudentsByClass(String className);
}
