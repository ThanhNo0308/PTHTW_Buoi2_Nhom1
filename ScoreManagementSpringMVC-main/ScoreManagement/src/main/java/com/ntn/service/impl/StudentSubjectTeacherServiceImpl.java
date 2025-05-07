/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Student;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import com.ntn.repository.StudentSubjectTeacherRepository;
import com.ntn.service.SchoolYearService;
import com.ntn.service.StudentService;
import com.ntn.service.StudentSubjectTeacherService;
import com.ntn.service.SubjectTeacherService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentSubjectTeacherServiceImpl implements StudentSubjectTeacherService {

    @Autowired
    private StudentSubjectTeacherRepository studentSubjectTeacherRepository;

    @Autowired
    private StudentService studentService;
    
    @Autowired
    private SubjectTeacherService subjectTeacherService;
    
    @Autowired
    private SchoolYearService schoolYearService;

    @Override
    public List<Studentsubjectteacher> getBySchoolYearIdThroughSubjectTeacher(int schoolYearId) {
        return this.studentSubjectTeacherRepository.getBySchoolYearId(schoolYearId);
    }

    @Override
    public List<Studentsubjectteacher> getAll() {
        return this.studentSubjectTeacherRepository.getAll();
    }

    @Override
    public Studentsubjectteacher getById(int id) {
        return this.studentSubjectTeacherRepository.getById(id);
    }

    @Override
    public boolean addOrUpdate(Studentsubjectteacher enrollment) {
        return this.studentSubjectTeacherRepository.addOrUpdate(enrollment);
    }

    @Override
    public boolean delete(int id) {
        return this.studentSubjectTeacherRepository.delete(id);
    }

    @Override
    public List<Studentsubjectteacher> getByStudentId(int studentId) {
        return this.studentSubjectTeacherRepository.getByStudentId(studentId);
    }

    @Override
    public List<Studentsubjectteacher> getBySubjectTeacherId(int subjectTeacherId) {
        return this.studentSubjectTeacherRepository.getBySubjectTeacherId(subjectTeacherId);
    }

    @Override
    public List<Studentsubjectteacher> getByTeacherId(int teacherId) {
        return this.studentSubjectTeacherRepository.getByTeacherId(teacherId);
    }

    @Override
    public List<Studentsubjectteacher> getBySubjectId(int subjectId) {
        return this.studentSubjectTeacherRepository.getBySubjectId(subjectId);
    }

    @Override
    public List<Studentsubjectteacher> getByClassId(int classId) {
        return this.studentSubjectTeacherRepository.getByClassId(classId);
    }

    @Override
    public boolean checkDuplicate(Integer studentId, Integer subjectTeacherId) {
        return this.studentSubjectTeacherRepository.checkDuplicate(studentId, subjectTeacherId);
    }

    @Override
    public boolean checkDuplicateExcept(Integer studentId, Integer subjectTeacherId, Integer exceptId) {
        return this.studentSubjectTeacherRepository.checkDuplicateExcept(studentId, subjectTeacherId, exceptId);
    }

    @Override
    public boolean hasRelatedScores(int enrollmentId) {
        return this.studentSubjectTeacherRepository.hasRelatedScores(enrollmentId);
    }

    @Override
    public int batchEnrollStudents(int classId, int subjectTeacherId) {
        return this.studentSubjectTeacherRepository.batchEnrollStudents(classId, subjectTeacherId);
    }

    @Override
    public long countEnrollments() {
        return this.studentSubjectTeacherRepository.countEnrollments();
    }

    @Override
    public List<Studentsubjectteacher> getEnrollmentsByStudentCode(String studentCode) {
        return this.studentSubjectTeacherRepository.getEnrollmentsByStudentCode(studentCode);
    }

    @Override
    public List<Studentsubjectteacher> getByTeachingClassId(int teachingClassId) {
        return this.studentSubjectTeacherRepository.getByTeachingClassId(teachingClassId);
    }

    @Override
    public boolean addStudentToSubjectTeacher(int studentId, int subjectTeacherId, int schoolYearId) {
        try {
            // Lấy các đối tượng cần thiết
            Student student = studentService.getStudentById(studentId);
            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);

            if (student == null || subjectTeacher == null) {
                return false;
            }

            // Tạo đối tượng đăng ký học
            Studentsubjectteacher enrollment = new Studentsubjectteacher();
            enrollment.setStudentId(student);
            enrollment.setSubjectTeacherId(subjectTeacher);

            // Lưu vào database
            return studentSubjectTeacherRepository.addStudentSubjectTeacher(enrollment);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
