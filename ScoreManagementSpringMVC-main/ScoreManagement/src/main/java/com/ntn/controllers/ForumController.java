/*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.pojo.Forum;
import com.ntn.pojo.Forumcomment;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.User;
import com.ntn.service.ForumCommentService;
import com.ntn.service.ForumService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.UserService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private ForumCommentService forumCommentService;

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

    @GetMapping("/admin/forum-comments/{forumId}")
    public String showForumComments(@PathVariable("forumId") int forumId, Model model) {
        try {
            // Lấy thông tin diễn đàn
            Forum forum = forumService.getForumById(forumId);
            if (forum == null) {
                model.addAttribute("errorMessage", "Không tìm thấy diễn đàn yêu cầu.");
                return "redirect:/admin/forum";
            }

            // Lấy danh sách bình luận
            List<Forumcomment> comments = forumCommentService.getCommentsByForumId(forumId);

            // Phân loại bình luận thành gốc và con
            List<Forumcomment> rootComments = new ArrayList<>();
            Map<Integer, List<Forumcomment>> childComments = new HashMap<>();

            for (Forumcomment comment : comments) {
                if (comment.getParentCommentId() == null) {
                    // Đây là bình luận gốc
                    rootComments.add(comment);
                } else {
                    // Đây là bình luận con (phản hồi)
                    int parentId = comment.getParentCommentId().getId();
                    if (!childComments.containsKey(parentId)) {
                        childComments.put(parentId, new ArrayList<>());
                    }
                    childComments.get(parentId).add(comment);
                }
            }

            model.addAttribute("forum", forum);
            model.addAttribute("comments", comments);
            model.addAttribute("rootComments", rootComments);
            model.addAttribute("childComments", childComments);

            return "admin/forum-comments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/forum";
        }
    }

    @PostMapping("/admin/forum-comments/add")
    public String addComment(
            @RequestParam("forumId") int forumId,
            @RequestParam(value = "parentCommentId", required = false) Integer parentCommentId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Forumcomment comment = new Forumcomment();
            comment.setTitle(title);
            comment.setContent(content);
            comment.setCreatedAt(new Date());

            // Thiết lập forum
            Forum forum = new Forum();
            forum.setId(forumId);
            comment.setForumId(forum);

            // Thiết lập parentComment nếu có
            if (parentCommentId != null) {
                Forumcomment parentComment = new Forumcomment();
                parentComment.setId(parentCommentId);
                comment.setParentCommentId(parentComment);
            }

            // Thiết lập người dùng
            if (authentication != null) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User currentUser = userService.getUserByUn(userDetails.getUsername());
                comment.setUserId(currentUser);
            }

            // Lưu bình luận
            boolean success = forumCommentService.addComment(comment);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Bình luận đã được thêm thành công.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm bình luận.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        }

        return "redirect:/admin/forum-comments/" + forumId;
    }

    @PostMapping("/admin/forum-comments/delete")
    public String deleteComment(
            @RequestParam("commentId") int commentId,
            @RequestParam("forumId") int forumId,
            RedirectAttributes redirectAttributes) {

        try {
            boolean success = forumCommentService.deleteComment(commentId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Bình luận đã được xóa thành công.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa bình luận.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        }

        return "redirect:/admin/forum-comments/" + forumId;
    }

    @GetMapping("/admin/forum/{id}")
    public Forum getForumById(@PathVariable("id") int forumId) {
        return forumService.getForumById(forumId);
    }
}
