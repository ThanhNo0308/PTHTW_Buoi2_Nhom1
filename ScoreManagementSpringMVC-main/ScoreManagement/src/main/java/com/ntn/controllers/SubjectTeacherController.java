package com.ntn.controllers;

import com.ntn.pojo.Department;
import com.ntn.pojo.Subject;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.Trainingtype;
import com.ntn.service.DepartmentService;
import com.ntn.service.SubjectService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TeacherService;
import com.ntn.service.TrainingTypeService;

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
import org.springframework.web.bind.WebDataBinder;

@Controller
@PreAuthorize("hasAuthority('Admin')")
public class SubjectTeacherController {

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private TrainingTypeService trainingTypeService;

    @GetMapping("/admin/subjTeach")
    public String getSubjectTeachers(
            @RequestParam(name = "teacherId", required = false) Integer teacherId,
            @RequestParam(name = "subjectId", required = false) Integer subjectId,
            @RequestParam(name = "departmentId", required = false) Integer departmentId,
            Model model) {

        List<Subjectteacher> subjectTeachers;

        if (teacherId != null) {
            // Lọc theo giảng viên
            subjectTeachers = subjectTeacherService.getSubjectTeachersByTeacherId(teacherId);
        } else if (subjectId != null) {
            // Lọc theo môn học
            subjectTeachers = subjectTeacherService.getSubjectTeachersBySubjectId(subjectId);
        } else if (departmentId != null) {
            // Lọc theo khoa
            subjectTeachers = subjectTeacherService.getSubjectTeachersByDepartmentId(departmentId);
        } else {
            // Lấy tất cả
            subjectTeachers = subjectTeacherService.getAllSubjectTeachers();
        }

        // Thêm danh sách giảng viên, môn học, khoa và hệ đào tạo vào model
        model.addAttribute("teachers", teacherService.getTeachers());
        model.addAttribute("subjects", subjectService.getSubjects());
        model.addAttribute("departments", departmentService.getDepartments());
        model.addAttribute("subjectTeachers", subjectTeachers);

        return "admin/subject-teachers";
    }

    @GetMapping("/admin/subjTeach/add")
    public String subjectTeacherAddForm(Model model) {
        model.addAttribute("subjectTeacher", new Subjectteacher());
        model.addAttribute("subjects", subjectService.getSubjects());
        model.addAttribute("teachers", teacherService.getTeachers());
        return "admin/subject-teacher-add";
    }

    @PostMapping("/admin/subjTeach/add")
    public String subjectTeacherAdd(
            @ModelAttribute("subjectTeacher") Subjectteacher subjectTeacher,
            BindingResult bindingResult,
            @RequestParam(value = "subjectId.id", required = false) Integer subjectId,
            @RequestParam(value = "teacherId.id", required = false) Integer teacherId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("subjects", subjectService.getSubjects());
                model.addAttribute("teachers", teacherService.getTeachers());
                return "admin/subject-teacher-add";
            }

            // Xử lý Subject và Teacher thủ công
            if (subjectId != null) {
                Subject subject = subjectService.getSubjectById(subjectId);
                subjectTeacher.setSubjectId(subject);
            }

            if (teacherId != null) {
                Teacher teacher = teacherService.getTeacherById(teacherId);
                subjectTeacher.setTeacherId(teacher);
            }

            boolean success = subjectTeacherService.addOrUpdateSubjectTeacher(subjectTeacher);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Phân công giảng dạy thành công");
                return "redirect:/admin/subjTeach";
            } else {
                model.addAttribute("errorMessage", "Không thể phân công giảng dạy");
                model.addAttribute("subjects", subjectService.getSubjects());
                model.addAttribute("teachers", teacherService.getTeachers());
                return "admin/subject-teacher-add";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            model.addAttribute("subjects", subjectService.getSubjects());
            model.addAttribute("teachers", teacherService.getTeachers());
            return "admin/subject-teacher-add";
        }
    }

    @GetMapping("/admin/subjTeach/update/{id}")
    public String subjectTeacherUpdateForm(@PathVariable("id") int subjectTeacherId, Model model) {
        Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);

        if (subjectTeacher == null) {
            return "redirect:/admin/subjTeach?error=not-found";
        }

        model.addAttribute("subjectTeacher", subjectTeacher);
        model.addAttribute("subjects", subjectService.getSubjects());
        model.addAttribute("teachers", teacherService.getTeachers());
        return "admin/subject-teacher-update";
    }

    @PostMapping("/admin/subjTeach/update")
    public String subjectTeacherUpdate(
            @ModelAttribute("subjectTeacher") Subjectteacher subjectTeacher,
            BindingResult bindingResult,
            @RequestParam(value = "subjectId.id", required = false) Integer subjectId,
            @RequestParam(value = "teacherId.id", required = false) Integer teacherId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("subjects", subjectService.getSubjects());
                model.addAttribute("teachers", teacherService.getTeachers());
                return "admin/subject-teacher-update";
            }

            // Xử lý Subject và Teacher thủ công
            if (subjectId != null) {
                Subject subject = subjectService.getSubjectById(subjectId);
                subjectTeacher.setSubjectId(subject);
            }

            if (teacherId != null) {
                Teacher teacher = teacherService.getTeacherById(teacherId);
                subjectTeacher.setTeacherId(teacher);
            }

            boolean success = subjectTeacherService.addOrUpdateSubjectTeacher(subjectTeacher);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phân công giảng dạy thành công");
                return "redirect:/admin/subjTeach";
            } else {
                model.addAttribute("errorMessage", "Không thể cập nhật phân công giảng dạy");
                model.addAttribute("subjects", subjectService.getSubjects());
                model.addAttribute("teachers", teacherService.getTeachers());
                return "admin/subject-teacher-update";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            model.addAttribute("subjects", subjectService.getSubjects());
            model.addAttribute("teachers", teacherService.getTeachers());
            return "admin/subject-teacher-update";
        }
    }

    @GetMapping("/admin/subjTeach/delete/{id}")
    public String subjectTeacherDelete(@PathVariable("id") int subjectTeacherId, RedirectAttributes redirectAttributes) {
        try {
            boolean success = subjectTeacherService.deleteSubjectTeacher(subjectTeacherId);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Xóa phân công giảng dạy thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa phân công giảng dạy. Có thể đã có sinh viên đăng ký hoặc có điểm");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa phân công giảng dạy. Có thể đã có sinh viên đăng ký hoặc có điểm");
        }

        return "redirect:/admin/subjTeach";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Đăng ký PropertyEditor cho Subject
        binder.registerCustomEditor(Subject.class, "subjectId", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                    return;
                }
                try {
                    int id = Integer.parseInt(text);
                    Subject subject = subjectService.getSubjectById(id);
                    setValue(subject);
                } catch (NumberFormatException e) {
                    setValue(null);
                }
            }
        });

        // Đăng ký PropertyEditor cho Teacher
        binder.registerCustomEditor(Teacher.class, "teacherId", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                    return;
                }
                try {
                    int id = Integer.parseInt(text);
                    Teacher teacher = teacherService.getTeacherById(id);
                    setValue(teacher);
                } catch (NumberFormatException e) {
                    setValue(null);
                }
            }
        });
    }
}
