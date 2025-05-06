package com.ntn.repository;

import com.ntn.pojo.Student;
import java.util.List;
import java.util.Optional;

public interface StudentRepository {

    Student getStudentById(int studentId);

    Student getStudentByEmail(String email);

    Student getStudentByCode(String studentCode);

    List<Student> getStudents();

    List<Student> getStudentByClassId(int classId);

    List<Student> getStudentsByKeyword(String keyword);

    List<Student> getStudentsByClassIdAndKeyword(Integer classId, String keyword);

    List<Student> getStudentbyEmail(String email);

    List<Student> findStudentsByCode(String code);

    List<Student> findStudentsByName(String name);

    List<Student> findStudentsByClass(String className);

    Optional<Student> findByStudentCode(String studentCode);

    int countStudents();

    int countStudentsByClassId(int classId);

    boolean addOrUpdateStudent(Student student);

    boolean deleteStudent(int studentId);
}
