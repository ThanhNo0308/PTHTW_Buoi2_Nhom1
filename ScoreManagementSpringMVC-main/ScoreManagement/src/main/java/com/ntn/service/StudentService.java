package com.ntn.service;

import com.ntn.pojo.Student;
import java.util.List;
import java.util.Optional;

public interface StudentService {
    List<Student> getStudentByClassId(int classId);

    boolean addOrUpdateStudent(Student student);

    boolean deleteStudent(int studentId);

    Optional<Student> findByStudentId(String studentId);

    List<Student> findByFullNameContaining(String name);

    List<Student> getStudentsByTeacherId(int teacherId);

    List<Student> getStudentsBySubjectTeacherId(int subjectTeacherId);

    List<Student> getStudentsBySubjectTeacherAndSchoolYear(int subjectTeacherId, int schoolYearId);

    List<Student> getStudents();

    Student getStudentByUsername(String username);

    Student getStudentById(int studentId);

    Student getStudentByStudentCode(String studentCode);

    int countStudents();

    int sendNotificationToClass(int classId, String subject, String message);

    int sendNotificationToAllStudents(String subject, String message);

    List<Student> findStudentsByCode(String code);

    List<Student> findStudentsByName(String name);

    List<Student> findStudentsByClass(String className);

    int countStudentsByClassId(int classId);

    List<Student> getStudentsByKeyword(String keyword);

    List<Student> getStudentsByClassIdAndKeyword(Integer classId, String keyword);
}
