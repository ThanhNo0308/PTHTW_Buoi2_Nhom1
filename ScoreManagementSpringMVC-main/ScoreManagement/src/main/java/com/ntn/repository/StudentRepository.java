package com.ntn.repository;

import com.ntn.pojo.Student;
import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    List<Student> getStudentByClassId(int classId);
    boolean addOrUpdateStudent(Student student);
    boolean deleteStudent(int studentId);
    Student getStudentById(int studentId);
    
    Optional<Student> findByStudentCode(String studentCode);
    List<Student> findByFullNameContaining(String name);
    List<Student> getStudentsByTeacherId(int teacherId);
    List<Student> getStudentsBySubjectTeacherId(int subjectTeacherId);
    List<Student> getStudentsBySubjectTeacherAndSchoolYear(int subjectTeacherId, int schoolYearId);
    
    int countStudents();
    List<Student> getStudents();
    Student getStudentByUsername(String username);
    
    int countStudentsByClassId(int classId);
    
    List<Student> getStudentsByKeyword(String keyword);
    List<Student> getStudentsByClassIdAndKeyword(Integer classId, String keyword);
    List<Student> getStudentbyEmail(String email);
}