package com.ntn.service;

/**
 * Service gửi email thông báo
 */
public interface EmailService {
    /**
     * Gửi email thông báo
     * 
     * @param to Địa chỉ email người nhận
     * @param subject Tiêu đề email
     * @param text Nội dung email
     * @return true nếu gửi thành công, false nếu thất bại
     */
    boolean sendEmail(String to, String subject, String text);
    
    /**
     * Gửi thông báo điểm cho sinh viên
     * 
     * @param studentId ID sinh viên
     * @param subjectName Tên môn học
     * @return true nếu gửi thành công, false nếu thất bại
     */
    boolean sendScoreNotification(int studentId, String subjectName);
}