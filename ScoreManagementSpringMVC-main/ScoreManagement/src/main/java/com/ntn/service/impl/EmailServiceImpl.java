package com.ntn.service.impl;

import com.ntn.pojo.Student;
import com.ntn.repository.StudentRepository;
import com.ntn.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private StudentRepository studentRepo;

    @Override
    public boolean sendEmail(String to, String subject, String text) {
        try {
            if (emailSender == null) {
                return false;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            emailSender.send(message);
            return true;
        } catch (MailSendException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean sendScoreNotification(int studentId, String subjectName, String teacherName, 
                                       String schoolYear, String major) {
        try {
            Student student = this.studentRepo.getStudentById(studentId);

            if (student == null || student.getEmail() == null) {
                return false;
            }

            String to = student.getEmail();
            String subject = "Thông báo điểm môn học " + subjectName;
            String text = "Xin chào " + student.getLastName() + " " + student.getFirstName() + ",\n\n"
                    + "Điểm của bạn cho môn " + subjectName + " đã được giảng viên " + teacherName + " khóa và công bố.\n\n"
                    + "Chi tiết:\n"
                    + "- Môn học: " + subjectName + "\n"
                    + "- Giảng viên: " + teacherName + "\n"
                    + "- Năm học - Học kỳ: " + schoolYear + "\n"
                    + "- Ngành: " + major + "\n\n"
                    + "Vui lòng đăng nhập vào hệ thống để xem chi tiết điểm của bạn.\n\n"
                    + "Trân trọng,\nPhòng Đào tạo";

            return sendEmail(to, subject, text);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    @Async
    public void sendScoreNotificationsToClass(List<Integer> studentIds, String subjectName, 
                                           String teacherName, String schoolYear, String major) {
        int successCount = 0;
        int failCount = 0;
        
        for (Integer studentId : studentIds) {
            try {
                boolean sent = sendScoreNotification(studentId, subjectName, teacherName, schoolYear, major);
                if (sent) {
                    successCount++;
                } else {
                    failCount++;
                }
                // Thêm delay nhỏ để tránh gửi quá nhiều email cùng lúc
                Thread.sleep(100);
            } catch (Exception e) {
                // Log lỗi nhưng vẫn tiếp tục với sinh viên tiếp theo
                System.out.println("Lỗi gửi email cho sinh viên ID " + studentId + ": " + e.getMessage());
                failCount++;
            }
        }
        
        System.out.println("Tổng kết gửi email: Thành công: " + successCount + ", Thất bại: " + failCount);
    }
}