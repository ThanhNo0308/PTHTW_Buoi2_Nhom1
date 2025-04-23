package com.ntn.service.impl;

import com.ntn.pojo.Student;
import com.ntn.repository.StudentRepository;
import com.ntn.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Triển khai dịch vụ gửi email
 */
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
                // Log de error o return false si no hay configuración de correo
                return false;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            emailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean sendScoreNotification(int studentId, String subjectName) {
        try {
            Student student = this.studentRepo.getStudentById(studentId);

            if (student == null || student.getEmail() == null) {
                return false;
            }

            String to = student.getEmail();
            String subject = "Thông báo có điểm môn học " + subjectName;
            String text = "Xin chào " + student.getFirstName() + " " + student.getLastName() + ",\n\n"
                    + "Điểm của bạn cho môn " + subjectName + " đã được giảng viên cập nhật.\n"
                    + "Vui lòng đăng nhập vào hệ thống để xem chi tiết.\n\n"
                    + "Trân trọng,\nPhòng Đào tạo";

            return sendEmail(to, subject, text);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
