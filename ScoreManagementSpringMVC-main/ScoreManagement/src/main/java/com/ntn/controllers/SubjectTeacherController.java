package com.ntn.controllers;

import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Subject;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.Class;
import com.ntn.service.ClassService;
import com.ntn.service.DepartmentService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.SubjectService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TeacherService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controller Phân công giảng dạy
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
    private SchoolYearService schoolYearService;
    
    @Autowired
    private ClassService classService;

    @GetMapping("/admin/subjTeach")
    public String getSubjectTeachers(
            @RequestParam(name = "teacherId", required = false) Integer teacherId,
            @RequestParam(name = "subjectId", required = false) Integer subjectId,
            @RequestParam(name = "departmentId", required = false) Integer departmentId,
            @RequestParam(name = "schoolYearId", required = false) Integer schoolYearId,
            @RequestParam(name = "classId", required = false) Integer classId,
            Model model) {

        List<Subjectteacher> subjectTeachers;
         if (classId != null) {
            // Lọc theo lớp học
            subjectTeachers = subjectTeacherService.getSubjectTeachersByClassId(classId);
        }else if (teacherId != null && schoolYearId != null) {
            // Lọc theo giảng viên và học kỳ
            subjectTeachers = subjectTeacherService.getSubjectTeachersByTeacherIdAndSchoolYearId(teacherId, schoolYearId);
        } else if (teacherId != null) {
            // Lọc theo giảng viên
            subjectTeachers = subjectTeacherService.getSubjectTeachersByTeacherId(teacherId);
        } else if (subjectId != null) {
            // Lọc theo môn học
            subjectTeachers = subjectTeacherService.getSubjectTeachersBySubjectId(subjectId);
        } else if (departmentId != null) {
            // Lọc theo khoa
            subjectTeachers = subjectTeacherService.getSubjectTeachersByDepartmentId(departmentId);
        } else if (schoolYearId != null) {
            // Lọc theo học kỳ
            subjectTeachers = subjectTeacherService.getSubjectTeachersBySchoolYearId(schoolYearId);
        } else {
            // Lấy tất cả
            subjectTeachers = subjectTeacherService.getAllSubjectTeachers();
        }

        model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());
        model.addAttribute("classes", classService.getClasses());
        model.addAttribute("teachers", teacherService.getTeachers());
        model.addAttribute("subjects", subjectService.getSubjects());
        model.addAttribute("departments", departmentService.getDepartments());
        model.addAttribute("subjectTeachers", subjectTeachers);
        model.addAttribute("subjectTeacher", new Subjectteacher());

        return "admin/subject-teachers";
    }

    @PostMapping("/admin/subjTeach/add")
    public String subjectTeacherAdd(
            @ModelAttribute("subjectTeacher") Subjectteacher subjectTeacher,
            BindingResult bindingResult,
            @RequestParam(value = "subjectId.id", required = false) Integer subjectId,
            @RequestParam(value = "teacherId.id", required = false) Integer teacherId,
            @RequestParam(value = "schoolYearId.id", required = false) Integer schoolYearId,
            @RequestParam(value = "classId.id", required = false) Integer classId,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin phân công");
                return "redirect:/admin/subjTeach?error=add-validation";
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

            if (schoolYearId != null) {
                Schoolyear schoolYear = schoolYearService.getSchoolYearById(schoolYearId);
                subjectTeacher.setSchoolYearId(schoolYear);
            }
            
            if (classId != null) {
                Class classObj = classService.getClassById(classId);
                subjectTeacher.setClassId(classObj);
            }

            boolean success = subjectTeacherService.addOrUpdateSubjectTeacher(subjectTeacher);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Phân công giảng dạy thành công");
                return "redirect:/admin/subjTeach";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể phân công giảng dạy");
                return "redirect:/admin/subjTeach?error=add-validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/subjTeach?error=add-validation";
        }
    }

    @PostMapping("/admin/subjTeach/update")
    public String subjectTeacherUpdate(
            @ModelAttribute("subjectTeacher") Subjectteacher subjectTeacher,
            BindingResult bindingResult,
            @RequestParam(value = "subjectId.id", required = false) Integer subjectId,
            @RequestParam(value = "teacherId.id", required = false) Integer teacherId,
            @RequestParam(value = "schoolYearId.id", required = false) Integer schoolYearId,
            @RequestParam(value = "classId.id", required = false) Integer classId,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin phân công");
                return "redirect:/admin/subjTeach?error=update-validation";
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
            
            if (schoolYearId != null) {
                Schoolyear schoolYear = schoolYearService.getSchoolYearById(schoolYearId);
                subjectTeacher.setSchoolYearId(schoolYear);
            }
            
            if (classId != null) {
                Class classObj = classService.getClassById(classId);
                subjectTeacher.setClassId(classObj);
            }

            boolean success = subjectTeacherService.addOrUpdateSubjectTeacher(subjectTeacher);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phân công giảng dạy thành công");
                return "redirect:/admin/subjTeach";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật phân công giảng dạy");
                return "redirect:/admin/subjTeach?error=update-validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/subjTeach?error=update-validation";
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
}
