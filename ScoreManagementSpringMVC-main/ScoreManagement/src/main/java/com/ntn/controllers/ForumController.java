/*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.pojo.Forum;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.User;
import com.ntn.service.ForumService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.UserService;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controller Diễn đàn
@Controller
@PreAuthorize("hasAuthority('Admin')")
public class ForumController {

    @Autowired
    private SubjectTeacherService subjTeachService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    @GetMapping("/admin/forum")
    public String showForumPage(Model model) {
        List<Subjectteacher> subjteachs = subjTeachService.getAllSubjectTeachers();
        List<Forum> forums = forumService.getForums();

        model.addAttribute("subjteachs", subjteachs);
        model.addAttribute("forums", forums);
        return "admin/forum";
    }

    @GetMapping("/admin/forumBySubjectTeacher")
    public String showForumBySubjTeach(Model model, @RequestParam("subjectTeacherId") int subjectTeacherId) {
        List<Subjectteacher> subjteachs = subjTeachService.getAllSubjectTeachers();
        List<Forum> forums = forumService.getForumBySubjectTeacher(subjectTeacherId);

        model.addAttribute("subjteachs", subjteachs);
        model.addAttribute("forums", forums);
        return "admin/forum";

    }

    @PostMapping("/admin/forum/add")
    public String addForum(@ModelAttribute("forum") Forum forum,
            @RequestParam("subjectTeacherId.id") Integer subjectTeacherId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

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
                    redirectAttributes.addFlashAttribute("successMessage", "Bài đăng đã được thêm thành công.");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi thêm bài đăng.");
                    return "redirect:/admin/forum?error=add-validation";
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
                return "redirect:/admin/forum?error=add-validation";
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin.");
            return "redirect:/admin/forum?error=add-validation";
        }
        return "redirect:/admin/forum";
    }

    @PostMapping("/admin/forum/update")
    public String updateForum(@ModelAttribute("forum") Forum forum,
            @RequestParam("subjectTeacherId.id") Integer subjectTeacherId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (forum.getTitle() != null && forum.getDescription() != null && forum.getContent() != null) {
            try {
                // Lấy bài đăng từ DB để giữ lại các thông tin không đổi (như userId, createdAt...)
                Forum existingForum = forumService.getForumById(forum.getId());
                if (existingForum == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bài đăng cần cập nhật.");
                    return "redirect:/admin/forum";
                }

                // Cập nhật những thông tin được thay đổi
                existingForum.setTitle(forum.getTitle());
                existingForum.setDescription(forum.getDescription());
                existingForum.setContent(forum.getContent());

                // Cập nhật subjectTeacherId nếu có thay đổi
                Subjectteacher selectedSubjectTeacher = new Subjectteacher();
                selectedSubjectTeacher.setId(subjectTeacherId);
                existingForum.setSubjectTeacherId(selectedSubjectTeacher);

                // Lưu thay đổi
                if (forumService.updateForum(existingForum)) {
                    redirectAttributes.addFlashAttribute("successMessage", "Bài đăng đã được cập nhật thành công.");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật bài đăng.");
                    return "redirect:/admin/forum?error=update-validation";
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
                return "redirect:/admin/forum?error=update-validation";
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin.");
            return "redirect:/admin/forum?error=update-validation";
        }
        return "redirect:/admin/forum";
    }

    @PostMapping("/admin/deleteForum")
    public String deleteForum(@RequestParam("forumId") int forumId, RedirectAttributes redirectAttributes) {
        if (forumService.deleteForum(forumId)) {
            redirectAttributes.addFlashAttribute("successMessage", "Bài đăng đã được xóa thành công.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa bài đăng.");
        }

        return "redirect:/admin/forum";
    }
    
    @GetMapping("/admin/forum/{id}")
    public Forum getForumById(@PathVariable("id") int forumId) {
        return forumService.getForumById(forumId);
    }
}
