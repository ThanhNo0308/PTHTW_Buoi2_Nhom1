package com.ntn.service.impl;

import com.ntn.pojo.Student;
import com.ntn.repository.StudentRepository;
import com.ntn.service.EmailService;
import com.ntn.service.StudentService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Service;

/**
 * Triển khai dịch vụ sinh viên
 */
@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studRepo;

    @Autowired
    private EmailService emailService;

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
    public Optional<Student> findByStudentId(String studentCode) {
        return this.studRepo.findByStudentCode(studentCode);
    }

    @Override
    public List<Student> findByFullNameContaining(String name) {
        return this.studRepo.findByFullNameContaining(name);
    }

    @Override
    public List<Student> getStudentsByTeacherId(int teacherId) {
        return this.studRepo.getStudentsByTeacherId(teacherId);
    }

    @Override
    public List<Student> getStudentsBySubjectTeacherId(int subjectTeacherId) {
        return this.studRepo.getStudentsBySubjectTeacherId(subjectTeacherId);
    }

    @Override
    public List<Student> getStudentsBySubjectTeacherAndSchoolYear(int subjectTeacherId, int schoolYearId) {
        return this.studRepo.getStudentsBySubjectTeacherAndSchoolYear(subjectTeacherId, schoolYearId);
    }

    @Override
    public List<Student> getStudents() {
        return this.studRepo.getStudents();
    }

    @Override
    public Student getStudentByUsername(String username) {
        return this.studRepo.getStudentByUsername(username);
    }

    @Override
    public Student getStudentById(int studentId) {
        return this.studRepo.getStudentById(studentId);
    }

    @Override
    public Student getStudentByStudentCode(String studentCode) {
        Optional<Student> student = this.studRepo.findByStudentCode(studentCode);
        return student.orElse(null);
    }

    @Override
    public int countStudents() {
        return this.studRepo.countStudents();
    }

    @Override
    public int sendNotificationToClass(int classId, String subject, String message) {
        List<Student> students = this.getStudentByClassId(classId);
        int sentCount = 0;

        for (Student student : students) {
            if (student.getEmail() != null && !student.getEmail().isEmpty()) {
                boolean sent = emailService.sendEmail(student.getEmail(), subject, message);
                if (sent) {
                    sentCount++;
                }
            }
        }

        return sentCount;
    }

    @Override
    public int sendNotificationToAllStudents(String subject, String message) {
        List<Student> students = this.getStudents();
        int sentCount = 0;

        for (Student student : students) {
            if (student.getEmail() != null && !student.getEmail().isEmpty()) {
                boolean sent = emailService.sendEmail(student.getEmail(), subject, message);
                if (sent) {
                    sentCount++;
                }
            }
        }

        return sentCount;
    }

    @Override
    public List<Student> findStudentsByCode(String code) {
        Optional<Student> student = this.studRepo.findByStudentCode(code);
        List<Student> result = new ArrayList<>();
        student.ifPresent(result::add);
        return result;
    }

    @Override
    public List<Student> findStudentsByName(String name) {
        return this.studRepo.findByFullNameContaining(name);
    }
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Student> findStudentsByClass(String className) {
        // Triển khai tìm kiếm sinh viên theo tên lớp
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        // Join từ Student đến Class
        Join<Student, Class> classJoin = root.join("classId", JoinType.INNER);

        // Điều kiện tìm kiếm - tên lớp chứa chuỗi tìm kiếm
        Predicate classNamePredicate = builder.like(
                builder.lower(classJoin.get("className")),
                "%" + className.toLowerCase() + "%"
        );

        query.where(classNamePredicate);
        query.orderBy(builder.asc(root.get("lastName")));

        return session.createQuery(query).getResultList();
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
}
