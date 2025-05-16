package com.ntn.repository;

import com.ntn.pojo.ClassSession;
import java.util.List;

public interface ClassSessionRepository {

    List<ClassSession> getAllClassSessions();

    ClassSession getClassSessionById(int id);

    boolean addOrUpdateClassSession(ClassSession classSession);

    boolean deleteClassSession(int id);

    // Lấy lịch dạy của giáo viên theo semester
    List<ClassSession> getClassSessionsByTeacher(int teacherId, Integer schoolYearId);

    // Lấy lịch học của sinh viên theo semester
    List<ClassSession> getClassSessionsByStudent(int studentId, Integer schoolYearId);

    // Kiểm tra xung đột lịch
    boolean hasScheduleConflict(ClassSession classSession);

    // Lấy các session theo subject-teacher
    List<ClassSession> getClassSessionsBySubjectTeacher(int subjectTeacherId);

    // Lấy lịch theo lớp học
    List<ClassSession> getClassSessionsByClass(int classId, Integer schoolYearId);

    List<ClassSession> getClassSessionsByDepartment(int departmentId, Integer schoolYearId);

    List<ClassSession> getClassSessionsByDayOfWeek(int dayOfWeek, Integer schoolYearId);

    List<ClassSession> getClassSessionsBySchoolYear(int schoolYearId);
}
