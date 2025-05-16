package com.ntn.service.impl;

import com.ntn.pojo.ClassSession;
import com.ntn.repository.ClassSessionRepository;
import com.ntn.service.ClassSessionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClassSessionServiceImpl implements ClassSessionService {

    @Autowired
    private ClassSessionRepository classSessionRepository;

    @Override
    public List<ClassSession> getAllClassSessions() {
        return this.classSessionRepository.getAllClassSessions();
    }

    @Override
    public ClassSession getClassSessionById(int id) {
        return this.classSessionRepository.getClassSessionById(id);
    }

    @Override
    public boolean addOrUpdateClassSession(ClassSession classSession) {
        return this.classSessionRepository.addOrUpdateClassSession(classSession);
    }

    @Override
    public boolean deleteClassSession(int id) {
        return this.classSessionRepository.deleteClassSession(id);
    }

    @Override
    public List<ClassSession> getClassSessionsByTeacher(int teacherId, Integer schoolYearId) {
        return this.classSessionRepository.getClassSessionsByTeacher(teacherId, schoolYearId);
    }

    @Override
    public List<ClassSession> getClassSessionsByStudent(int studentId, Integer schoolYearId) {
        return this.classSessionRepository.getClassSessionsByStudent(studentId, schoolYearId);
    }

    @Override
    public boolean hasScheduleConflict(ClassSession classSession) {
        return this.classSessionRepository.hasScheduleConflict(classSession);
    }

    @Override
    public List<ClassSession> getClassSessionsBySubjectTeacher(int subjectTeacherId) {
        return this.classSessionRepository.getClassSessionsBySubjectTeacher(subjectTeacherId);
    }

    @Override
    public List<ClassSession> getClassSessionsByClass(int classId, Integer schoolYearId) {
        return this.classSessionRepository.getClassSessionsByClass(classId, schoolYearId);
    }

    @Override
    public List<ClassSession> getClassSessionsByDepartment(int departmentId, Integer schoolYearId) {
        return this.classSessionRepository.getClassSessionsByDepartment(departmentId, schoolYearId);
    }

    @Override
    public List<ClassSession> getClassSessionsByDayOfWeek(int dayOfWeek, Integer schoolYearId) {
        return this.classSessionRepository.getClassSessionsByDayOfWeek(dayOfWeek, schoolYearId);
    }

    @Override
    public List<ClassSession> getClassSessionsBySchoolYear(int schoolYearId) {
        return this.classSessionRepository.getClassSessionsBySchoolYear(schoolYearId);
    }
}
