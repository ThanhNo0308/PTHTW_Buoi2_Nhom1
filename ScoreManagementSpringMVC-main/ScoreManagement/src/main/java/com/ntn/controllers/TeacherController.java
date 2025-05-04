package com.ntn.controllers;

import com.ntn.pojo.Teacher;
import com.ntn.pojo.Department;
import com.ntn.pojo.User;
import com.ntn.service.DepartmentService;
import com.ntn.service.TeacherService;
import com.ntn.service.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controller Giảng viên
@Controller
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserService userService;

    @GetMapping("/admin/teachers")
    public String teachers(Model model, @RequestParam(name = "departmentId", required = false) Integer departmentId, @RequestParam(name = "keyword", required = false) String keyword) {
        List<Teacher> teachers = teacherService.getTeachers();
        Map<Integer, String> teacherUserMap = new HashMap<>();
        List<Department> departments = departmentService.getDepartments();

        for (Teacher teacher : teachers) {
            User user = userService.getUserByEmail(teacher.getEmail());
            if (user != null) {
                teacherUserMap.put(teacher.getId(), user.getUsername());
            }
        }
        if (departmentId != null && keyword != null && !keyword.isEmpty()) {
            // Lọc theo cả khoa và từ khóa
            teachers = teacherService.getTeachersByDepartmentIdAndKeyword(departmentId, keyword);
        } else if (departmentId != null) {
            // Chỉ lọc theo khoa
            teachers = teacherService.getTeachersByDepartmentId(departmentId);
        } else if (keyword != null && !keyword.isEmpty()) {
            // Chỉ tìm kiếm theo từ khóa
            teachers = teacherService.getTeachersByKeyword(keyword);
        } else {
            // Lấy tất cả giảng viên
            teachers = teacherService.getTeachers();
        }

        model.addAttribute("teachers", teachers);
        model.addAttribute("teacherUserMap", teacherUserMap);
        model.addAttribute("departments", departments);
        return "admin/teachers";
    }

    @PostMapping("/admin/teacher-add")
    public String addTeacher(
            @ModelAttribute("teacher") Teacher teacher,
            @RequestParam(value = "departmentIdValue", required = false) Integer departmentIdValue,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra các trường bắt buộc
            if (teacher.getTeacherName() == null || teacher.getTeacherName().isEmpty()
                    || teacher.getEmail() == null || teacher.getEmail().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin bắt buộc");
                return "redirect:/admin/teachers?error=validation";
            }

            // Thiết lập departmentId thủ công
            if (departmentIdValue != null) {
                Department department = departmentService.getDepartmentById(departmentIdValue);
                teacher.setDepartmentId(department);
            }

            // Lưu giảng viên
            boolean success = teacherService.addOrUpdateTeacher(teacher);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Thêm giảng viên thành công");
                return "redirect:/admin/teachers";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm giảng viên");
                return "redirect:/admin/teachers?error=validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/teachers?error=validation";
        }
    }

    @GetMapping("/admin/teacher-delete/{id}")
    public String deleteTeacher(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.deleteTeacher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Giảng viên đã được xóa thành công.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa giảng viên này vì họ đang được phân công cho một lớp học. "
                    + "Hãy gỡ bỏ giảng viên khỏi các lớp học trước khi xóa.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa giảng viên: " + e.getMessage());
        }

        return "redirect:/admin/teachers";
    }

    @PostMapping("/admin/teacher-update")
    public String teacherUpdate(
            @ModelAttribute Teacher teacher,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                // Gửi lỗi qua redirect
                redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ");
                return "redirect:/admin/teachers";
            }

            boolean success = teacherService.addOrUpdateTeacher(teacher);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giảng viên thành công");
                return "redirect:/admin/teachers";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật giảng viên");
                return "redirect:/admin/teachers";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/teachers";
        }
    }
}
