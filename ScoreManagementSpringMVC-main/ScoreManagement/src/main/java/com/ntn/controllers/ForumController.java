/*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.pojo.Forum;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.User;
import com.ntn.service.ClassService;
import com.ntn.service.ForumService;
import com.ntn.service.StudentService;
import com.ntn.service.StudentSubjectTeacherService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.UserService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasAuthority('Admin')")
public class ForumController {

    @Autowired
    private SubjectTeacherService subjTeachService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private StudentService studentService;
    
     @Autowired
    private ClassService classService;

    @GetMapping("/admin/forum")
    public String showForumPage(Model model) {
        List<Subjectteacher> subjteachs = subjTeachService.getSubjectTeachers();
        List<Forum> forums = forumService.getForums();

        model.addAttribute("subjteachs", subjteachs);
        model.addAttribute("forums", forums);
        return "admin/forum";
    }

    @GetMapping("/admin/forumBySubjectTeacher")
    public String showForumBySubjTeach(Model model, @RequestParam("subjectTeacherId") int subjectTeacherId) {
        List<Subjectteacher> subjteachs = subjTeachService.getSubjectTeachers();
        List<Forum> forums = forumService.getForumBySubjectTeacher(subjectTeacherId);

        model.addAttribute("subjteachs", subjteachs);
        model.addAttribute("forums", forums);
        return "admin/forum";

    }

    @GetMapping("/admin/addForum")
    public String showAddForumPage(Model model) {
        List<Subjectteacher> subjteachs = subjTeachService.getSubjectTeachers();
        model.addAttribute("subjteachs", subjteachs);
        model.addAttribute("forum", new Forum()); // Tạo một đối tượng Forum trống để binding dữ liệu
        return "admin/addForum"; // Đưa ra view thêm bài đăng
    }

    @PostMapping("/admin/addForum")
    public String addForum(@ModelAttribute("forum") Forum forum,
            @RequestParam("subjectTeacherId.id") Integer subjectTeacherId,
            Model model, Authentication authentication) {
        if (forum.getTitle() != null && forum.getDescription() != null && forum.getContent() != null) {
            try {
                forum.setCreatedAt(new Date());

                if (authentication != null) {
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    User currentUser = userService.getUserByUn(userDetails.getUsername());
                    forum.setUserId(currentUser);
                }

                Subjectteacher selectedSubjectTeacher = new Subjectteacher();
                selectedSubjectTeacher.setId(subjectTeacherId);
                forum.setSubjectTeacherId(selectedSubjectTeacher);

                if (forumService.addForum(forum)) {
                    model.addAttribute("successMessage", "Bài đăng đã được thêm thành công.");
                } else {
                    model.addAttribute("errorMessage", "Đã xảy ra lỗi khi thêm bài đăng.");
                }
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            }
        } else {
            model.addAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin.");
        }
        return "redirect:/admin/forum";
    }

    @PostMapping("/admin/deleteForum")
    public String deleteForum(@RequestParam("forumId") int forumId, Model model) {
        if (forumService.deleteForum(forumId)) {
            model.addAttribute("successMessage", "Bài đăng đã được xóa thành công.");
        } else {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi xóa bài đăng.");
        }

        return "redirect:/admin/forum";
    }
    
    @GetMapping("/admin/send-notification")
    public String sendNotificationForm(Model model) {
        // Thêm danh sách lớp để chọn (nếu muốn gửi thông báo cho một lớp cụ thể)
        model.addAttribute("classes", classService.getClasses());
        return "/admin/send-notification";
    }

    @PostMapping("/admin/send-notification")
    public String sendNotification(
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam(value = "classId", required = false) Integer classId,
            RedirectAttributes redirectAttributes) {

        int sentCount = 0;

        if (classId != null) {
            // Gửi cho sinh viên trong một lớp cụ thể
            sentCount = studentService.sendNotificationToClass(classId, subject, message);
        } else {
            // Gửi cho tất cả sinh viên
            sentCount = studentService.sendNotificationToAllStudents(subject, message);
        }

        redirectAttributes.addFlashAttribute("successMessage",
                "Đã gửi thông báo đến " + sentCount + " sinh viên");
        return "redirect:/admin/dashboard";
    }
}
