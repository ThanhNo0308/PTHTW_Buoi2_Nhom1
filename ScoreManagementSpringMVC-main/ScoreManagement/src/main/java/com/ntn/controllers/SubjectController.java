package com.ntn.controllers;

import com.ntn.pojo.Department;
import com.ntn.pojo.Subject;
import com.ntn.service.DepartmentService;
import com.ntn.service.SubjectService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;

// Controller Môn học
@Controller
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/admin/subjects")
    public String getSubjects(Model model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "departmentId", required = false) Integer departmentId) {

        List<Subject> subjects;

        if (departmentId != null && departmentId > 0) {
            // Nếu chỉ lọc theo khoa
            if (keyword == null || keyword.trim().isEmpty()) {
                subjects = subjectService.getSubjectsByDepartmentId(departmentId);
            } else {
                // Nếu lọc theo cả khoa và từ khóa
                subjects = subjectService.getSubjectsByDepartmentIdAndKeyword(departmentId, keyword);
            }
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // Nếu chỉ lọc theo từ khóa
            subjects = subjectService.getSubjectsByKeyword(keyword);
        } else {
            subjects = subjectService.getSubjects();
        }

        List<Department> departments = departmentService.getDepartments();

        model.addAttribute("subjects", subjects);
        model.addAttribute("departments", departments);
        return "admin/subjects";
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/admin/subject-add")
    public String subjectAdd(
            @ModelAttribute("subject") Subject subject,
            BindingResult bindingResult,
            @RequestParam(value = "departmentID.id", required = false) Integer departmentId,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin môn học");
                return "redirect:/admin/subjects?error=add-validation";
            }

            // Xử lý Department thủ công
            if (departmentId != null) {
                Department dept = departmentService.getDepartmentById(departmentId);
                subject.setDepartmentID(dept);
            }

            boolean success = subjectService.addOrUpdateSubject(subject);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Thêm môn học thành công");
                return "redirect:/admin/subjects";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm môn học");
                return "redirect:/admin/subjects?error=add-validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/subjects?error=add-validation";
        }
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/admin/subject-update")
    public String subjectUpdate(
            @ModelAttribute("subject") Subject subject,
            BindingResult bindingResult,
            @RequestParam(value = "departmentID.id", required = false) Integer departmentId,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin môn học");
                return "redirect:/admin/subjects?error=update-validation";
            }

            // Xử lý Department thủ công
            if (departmentId != null) {
                Department dept = departmentService.getDepartmentById(departmentId);
                subject.setDepartmentID(dept);
            }

            boolean success = subjectService.addOrUpdateSubject(subject);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật môn học thành công");
                return "redirect:/admin/subjects";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật môn học");
                return "redirect:/admin/subjects?error=update-validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/subjects?error=update-validation";
        }
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/admin/subject-delete/{id}")
    public String subjectDelete(@PathVariable("id") int subjectId, RedirectAttributes redirectAttributes) {
        try {
            boolean success = subjectService.deleteSubject(subjectId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Xóa môn học thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa môn học này vì đã phân công giảng viên hoặc sinh viên có điểm liên quan. Hãy xóa dữ liệu liên quan trước.");
            }
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa môn học này vì đã phân công giảng viên hoặc sinh viên có điểm liên quan. Hãy xóa dữ liệu liên quan trước.");
        } catch (StackOverflowError e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi hệ thống khi xóa môn học.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa môn học: " + e.getMessage());
        }

        return "redirect:/admin/subjects";
    }
}
