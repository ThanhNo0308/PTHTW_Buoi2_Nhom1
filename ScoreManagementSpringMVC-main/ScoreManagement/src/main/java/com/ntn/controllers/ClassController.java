package com.ntn.controllers;

import com.ntn.pojo.Class;
import com.ntn.pojo.Major;
import com.ntn.pojo.Teacher;
import com.ntn.service.ClassService;
import com.ntn.service.MajorService;
import com.ntn.service.TeacherService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.dao.DataIntegrityViolationException;

// Controller Lớp học
@Controller
@PreAuthorize("hasAuthority('Admin')")
public class ClassController {

    @Autowired
    private ClassService classService;

    @Autowired
    private MajorService majorService;

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/admin/classes")
    public String getClasses(
            @RequestParam(name = "majorId", required = false) Integer majorId,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {

        List<Class> classes;
        if (majorId != null) {
            classes = classService.getClassesByMajorId(majorId);
            model.addAttribute("selectedMajor", majorService.getMajorById(majorId));
        } else if (keyword != null && !keyword.isEmpty()) {
            classes = classService.getClassesByKeyword(keyword);
        } else {
            classes = classService.getClasses();
        }

        model.addAttribute("classes", classes);
        model.addAttribute("majors", majorService.getMajors());
        model.addAttribute("teachers", teacherService.getTeachers());
        return "/admin/classes";
    }
    
    @PostMapping("/admin/class-add")
    public String addClass(
            @ModelAttribute("class") Class classObj,
            BindingResult bindingResult,
            @RequestParam(value = "majorId.id", required = false) Integer majorId,
            @RequestParam(value = "teacherId.id", required = false) Integer teacherId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // Kiểm tra lỗi validation
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin lớp học");
                return "redirect:/admin/classes?error=validation";
            }

            // Xử lý Major và Teacher thủ công
            if (majorId != null) {
                Major major = majorService.getMajorById(majorId);
                classObj.setMajorId(major);
            }

            if (teacherId != null) {
                Teacher teacher = teacherService.getTeacherById(teacherId);
                classObj.setTeacherId(teacher);
            }

            // Lưu lớp học
            boolean success = classService.addOrUpdateClass(classObj);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Thêm lớp học thành công");
                return "redirect:/admin/classes";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm lớp học");
                return "redirect:/admin/classes?error=validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/classes?error=validation";
        }
    }

    @PostMapping("/admin/class-update")
    public String classUpdate(
            @ModelAttribute("class") Class classObj,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin lớp học");
            return "redirect:/admin/classes";
        }

        boolean success = classService.addOrUpdateClass(classObj);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật lớp học thành công");
            return "redirect:/admin/classes";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật lớp học");
            return "redirect:/admin/classes";
        }
    }

    @GetMapping("/admin/class-delete/{id}")
    public String deleteClass(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            classService.deleteClass(id);
            redirectAttributes.addFlashAttribute("successMessage", "Lớp học đã được xóa thành công.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa lớp học này vì đã có sinh viên trong lớp. "
                    + "Hãy chuyển sinh viên sang lớp khác trước khi xóa.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa lớp học: " + e.getMessage());
        }

        return "redirect:/admin/classes";
    }
}
