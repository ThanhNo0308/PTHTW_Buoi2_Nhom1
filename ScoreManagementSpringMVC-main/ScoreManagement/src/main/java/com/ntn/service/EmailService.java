package com.ntn.service;

import java.util.List;


public interface EmailService {

    boolean sendEmail(String to, String subject, String text);
    
    boolean sendScoreNotification(int studentId, String subjectName, String teacherName, 
                                String schoolYear, String major);
    
    void sendScoreNotificationsToClass(List<Integer> studentIds, String subjectName, 
                                     String teacherName, String schoolYear, String major);
}