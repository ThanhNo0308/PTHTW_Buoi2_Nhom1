package com.ntn.service;


public interface EmailService {

    boolean sendEmail(String to, String subject, String text);
    
    boolean sendScoreNotification(int studentId, String subjectName);
}