/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.service.ClassService;
import com.ntn.service.EmailService;
import com.ntn.service.StudentService;
import com.ntn.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controller thông báo
@Controller
@PreAuthorize("hasAuthority('Admin')")
public class NotificationController {
    
    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ClassService classService;
    
    @Autowired
    private EmailService emailService;
    
    @GetMapping("/admin/send-notification")
    public String sendNotificationForm(Model model) {
        model.addAttribute("classes", classService.getClasses());
        model.addAttribute("students", studentService.getStudents());
        model.addAttribute("teachers", teacherService.getTeachers());
        return "/admin/send-notification";
    }

    @PostMapping("/admin/send-notification")
    public String sendNotification(
            @RequestParam("recipientType") String recipientType,
            @RequestParam(value = "studentRecipient", required = false) String studentRecipient,
            @RequestParam(value = "teacherRecipient", required = false) String teacherRecipient,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            RedirectAttributes redirectAttributes) {

        int sentCount = 0;

        try {
            if ("student".equals(recipientType)) {
                // Gửi cho sinh viên
                if (studentRecipient.equals("all")) {
                    // Gửi cho tất cả sinh viên
                    sentCount = emailService.sendToAllStudents(subject, message);
                } else if (studentRecipient.startsWith("class-")) {
                    // Gửi cho sinh viên theo lớp
                    int classId = Integer.parseInt(studentRecipient.substring(6));
                    sentCount = emailService.sendToStudentsByClass(classId, subject, message);
                } else if (studentRecipient.startsWith("student-")) {
                    // Gửi cho sinh viên cụ thể
                    int studentId = Integer.parseInt(studentRecipient.substring(8));
                    boolean sent = emailService.sendToStudent(studentId, subject, message);
                    sentCount = sent ? 1 : 0;
                }
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã gửi thông báo thành công đến " + sentCount + " sinh viên");
            } else {
                // Gửi cho giảng viên
                if (teacherRecipient.equals("all")) {
                    // Gửi cho tất cả giảng viên
                    sentCount = emailService.sendToAllTeachers(subject, message);
                } else {
                    // Gửi cho giảng viên cụ thể
                    int teacherId = Integer.parseInt(teacherRecipient);
                    boolean sent = emailService.sendToTeacher(teacherId, subject, message);
                    sentCount = sent ? 1 : 0;
                }
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã gửi thông báo thành công đến " + sentCount + " giảng viên");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi gửi thông báo: " + e.getMessage());
        }

        return "redirect:/admin/send-notification";
    }
}
