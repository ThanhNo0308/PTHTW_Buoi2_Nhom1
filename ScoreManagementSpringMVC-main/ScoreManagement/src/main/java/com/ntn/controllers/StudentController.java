package com.ntn.controllers;

import com.ntn.pojo.Student;
import com.ntn.service.ClassService;
import com.ntn.service.StudentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;

// Controller xử lý Sinh viên trong lớp
@Controller
@PreAuthorize("hasAuthority('Admin')")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private ClassService classService;

    @GetMapping("/admin/students")
    public String getStudents(
            @RequestParam(name = "classId", required = false) Integer classId,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {

        List<Student> students;

        if (classId != null && keyword != null && !keyword.isEmpty()) {
            // Lọc theo cả lớp và từ khóa
            students = studentService.getStudentsByClassIdAndKeyword(classId, keyword);
        } else if (classId != null) {
            // Chỉ lọc theo lớp
            students = studentService.getStudentByClassId(classId);
        } else if (keyword != null && !keyword.isEmpty()) {
            // Chỉ tìm kiếm theo từ khóa
            students = studentService.getStudentsByKeyword(keyword);
        } else {
            // Lấy tất cả sinh viên
            students = studentService.getStudents();
        }

        List<com.ntn.pojo.Class> classes = classService.getClasses();

        model.addAttribute("students", students);
        model.addAttribute("classes", classes);

        return "admin/students";
    }
 
    @GetMapping("/admin/class-students/{classId}")
    public String getStudentsByClass(
            @PathVariable("classId") int classId,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {

        List<Student> students;
        com.ntn.pojo.Class classObj = classService.getClassById(classId);

        if (classObj == null) {
            return "redirect:/admin/classes?error=class-not-found";
        }

        if (keyword != null && !keyword.isEmpty()) {
            // Tìm kiếm sinh viên trong lớp theo từ khóa
            students = studentService.getStudentsByClassIdAndKeyword(classId, keyword);
        } else {
            // Lấy tất cả sinh viên của lớp
            students = studentService.getStudentByClassId(classId);
        }

        model.addAttribute("class", classObj);
        model.addAttribute("students", students);
        model.addAttribute("student", new Student());
        return "admin/class-students";
    }

    @PostMapping("/admin/student-add")
    public String studentAdd(
            @ModelAttribute("student") Student student,
            @RequestParam(value = "classId.id", required = false) Integer classId,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin sinh viên");
                return "redirect:/admin/class-students/" + classId + "?error=add-validation";
            }

            // Xử lý Class trước khi lưu
            if (classId != null) {
                // Lấy đối tượng Class từ ID
                com.ntn.pojo.Class cls = classService.getClassById(classId);

                if (cls != null) {
                    // Gán Class cho Student
                    student.setClassId(cls);
                }
            }
            if (student.getStatus() == null) {
                student.setStatus("Active");
            }
            boolean success = studentService.addOrUpdateStudent(student);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Thêm sinh viên thành công");
                return "redirect:/admin/class-students/" + classId;
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm sinh viên");
                return "redirect:/admin/class-students/" + classId + "?error=add-validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/class-students/" + classId + "?error=add-validation";
        }
    }

    @PostMapping("/admin/student-update")
    public String studentUpdate(
            @ModelAttribute("student") Student student,
            @RequestParam(value = "classId.id", required = false) Integer classId,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin sinh viên");
                return "redirect:/admin/class-students/" + classId + "?error=update-validation";
            }

            // Xử lý Class thủ công
            if (classId != null) {
                com.ntn.pojo.Class cls = classService.getClassById(classId);
                if (cls != null) {
                    student.setClassId(cls);
                }
            }

            boolean success = studentService.addOrUpdateStudent(student);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sinh viên thành công");
                return "redirect:/admin/class-students/" + classId;
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật sinh viên");
                return "redirect:/admin/class-students/" + classId + "?error=update-validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/class-students/" + classId + "?error=update-validation";
        }
    }

    @GetMapping("/admin/student-delete/{id}")
    public String deleteStudent(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        Integer classId = null;
        try {
            Student student = studentService.getStudentById(id);

            if (student != null && student.getClassId() != null) {
                classId = student.getClassId().getId();
            }

            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Sinh viên đã được xóa thành công.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa sinh viên này vì còn dữ liệu điểm số liên quan. "
                    + "Hãy xóa điểm số của sinh viên trước khi xóa.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa sinh viên: " + e.getMessage());
        }

        if (classId != null) {
            return "redirect:/admin/class-students/" + classId;
        } else {
            return "redirect:/admin/classes";
        }
    }
}
