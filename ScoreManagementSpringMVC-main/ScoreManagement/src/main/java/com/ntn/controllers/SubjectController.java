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

import jakarta.validation.Valid;
import java.beans.PropertyEditorSupport;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.WebDataBinder;

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
    @GetMapping("/admin/subject-add")
    public String subjectAddForm(Model model) {
        model.addAttribute("subject", new Subject());
        model.addAttribute("departments", departmentService.getDepartments());
        return "admin/subject-add";
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/admin/subject-add")
    public String subjectAdd(
            @Valid @ModelAttribute("subject") Subject subject,
            BindingResult bindingResult,
            @RequestParam(value = "departmentID.id", required = false) Integer departmentId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("departments", departmentService.getDepartments());
                return "admin/subject-add";
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
                model.addAttribute("errorMessage", "Không thể thêm môn học");
                model.addAttribute("departments", departmentService.getDepartments());
                return "admin/subject-add";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            model.addAttribute("departments", departmentService.getDepartments());
            return "admin/subject-add";
        }
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/admin/subject-update/{id}")
    public String subjectUpdateForm(@PathVariable("id") int subjectId, Model model) {
        Subject subject = subjectService.getSubjectById(subjectId);

        if (subject == null) {
            return "redirect:/admin/subjects?error=subject-not-found";
        }

        model.addAttribute("subject", subject);
        model.addAttribute("departments", departmentService.getDepartments());
        return "admin/subject-update";
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/admin/subject-update")
    public String subjectUpdate(
            @Valid @ModelAttribute("subject") Subject subject,
            BindingResult bindingResult,
            @RequestParam(value = "departmentID.id", required = false) Integer departmentId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("departments", departmentService.getDepartments());
                return "admin/subject-update";
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
                model.addAttribute("errorMessage", "Không thể cập nhật môn học");
                model.addAttribute("departments", departmentService.getDepartments());
                return "admin/subject-update";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            model.addAttribute("departments", departmentService.getDepartments());
            return "admin/subject-update";
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

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Đăng ký PropertyEditor tùy chỉnh để chuyển đổi id thành đối tượng Department
        binder.registerCustomEditor(Department.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                    return;
                }
                try {
                    int id = Integer.parseInt(text);
                    Department dept = departmentService.getDepartmentById(id);
                    setValue(dept);
                } catch (NumberFormatException e) {
                    setValue(null);
                }
            }
        });
    }
}
