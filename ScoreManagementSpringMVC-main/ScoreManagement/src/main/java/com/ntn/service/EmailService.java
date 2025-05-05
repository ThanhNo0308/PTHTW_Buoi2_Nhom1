package com.ntn.service;

import java.util.List;

public interface EmailService {

    boolean sendEmail(String to, String subject, String text);

    boolean sendScoreNotification(int studentId, String subjectName, String teacherName,
            String schoolYear, String major);

    void sendScoreNotificationsToClass(List<Integer> studentIds, String subjectName,
            String teacherName, String schoolYear, String major);

    //Gửi cho sinh viên
    boolean sendToStudent(int studentId, String subject, String message);

    int sendToStudentsByClass(int classId, String subject, String message);

    int sendToAllStudents(String subject, String message);

    // Gửi cho giảng viên 
    boolean sendNotificationToTeacher(int teacherId, String subject, String message);
    
    boolean sendToTeacher(int teacherId, String subject, String message);

    int sendToAllTeachers(String subject, String message);
}
