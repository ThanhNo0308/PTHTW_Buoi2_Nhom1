package com.ntn.controllers;

import com.ntn.pojo.Student;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import com.ntn.service.ClassService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.StudentService;
import com.ntn.service.StudentSubjectTeacherService;
import com.ntn.service.SubjectService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TeacherService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controller Đăng ký môn học-giảng viên
@Controller
@PreAuthorize("hasAuthority('Admin')")
public class StudentSubjectTeacherController {

    @Autowired
    private StudentSubjectTeacherService studentSubjectTeacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ClassService classService;

    @GetMapping("/admin/enrollment")
    public String getStudentSubjectTeachers(
            @RequestParam(name = "studentId", required = false) Integer studentId,
            @RequestParam(name = "subjectTeacherId", required = false) Integer subjectTeacherId,
            @RequestParam(name = "teacherId", required = false) Integer teacherId,
            @RequestParam(name = "subjectId", required = false) Integer subjectId,
            @RequestParam(name = "schoolYearId", required = false) Integer schoolYearId,
            @RequestParam(name = "classId", required = false) Integer classId,
            @RequestParam(name = "teachingClassId", required = false) Integer teachingClassId,
            Model model) {

        List<Studentsubjectteacher> enrollments;

        // Logic lọc theo nhiều tiêu chí
        if (studentId != null) {
            // Lọc theo sinh viên
            enrollments = studentSubjectTeacherService.getByStudentId(studentId);
        } else if (subjectTeacherId != null) {
            // Lọc theo phân công giảng dạy
            enrollments = studentSubjectTeacherService.getBySubjectTeacherId(subjectTeacherId);
        } else if (teacherId != null) {
            // Lọc theo giảng viên
            enrollments = studentSubjectTeacherService.getByTeacherId(teacherId);
        } else if (subjectId != null) {
            // Lọc theo môn học
            enrollments = studentSubjectTeacherService.getBySubjectId(subjectId);
        } else if (schoolYearId != null) {
            // Lọc theo năm học - sử dụng phương thức mới
            enrollments = studentSubjectTeacherService.getBySchoolYearIdThroughSubjectTeacher(schoolYearId);
        } else if (classId != null) {
            // Lọc theo lớp học
            enrollments = studentSubjectTeacherService.getByClassId(classId);
        } else if (teachingClassId != null) {
            // Lọc theo lớp dạy của giảng viên
            enrollments = studentSubjectTeacherService.getByTeachingClassId(teachingClassId);
        }   else {
            // Không lọc
            enrollments = studentSubjectTeacherService.getAll();
        }

            // Thêm một đối tượng mới cho modal thêm
            model.addAttribute("enrollment", new Studentsubjectteacher());

            // Thêm dữ liệu cho các bộ lọc
            model.addAttribute("students", studentService.getStudents());
            model.addAttribute("subjectTeachers", subjectTeacherService.getAllSubjectTeachers());
            model.addAttribute("teachers", teacherService.getTeachers());
            model.addAttribute("subjects", subjectService.getSubjects());
            model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());
            model.addAttribute("classes", classService.getClasses());
            model.addAttribute("enrollments", enrollments);

            return "admin/enrollments";
        }

        @PostMapping("/admin/enrollment/add")
        public String enrollmentAdd
        (
            @ModelAttribute("enrollment")
        Studentsubjectteacher enrollment,
                BindingResult bindingResult
        ,
            @RequestParam(value = "studentId.id", required = false)
        Integer studentId,
            @RequestParam(value = "subjectTeacherId.id", required = false) Integer subjectTeacherId,
                Model model
        ,
            RedirectAttributes redirectAttributes
        
            ) {

        try {
                if (bindingResult.hasErrors()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ");
                    return "redirect:/admin/enrollment";
                }

                // Xử lý thủ công các đối tượng quan hệ
                if (studentId != null) {
                    Student student = studentService.getStudentById(studentId);
                    enrollment.setStudentId(student);
                }

                if (subjectTeacherId != null) {
                    Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
                    enrollment.setSubjectTeacherId(subjectTeacher);

                    // Không cần set schoolYearId nữa vì đã bỏ trường này
                }

                // Kiểm tra trùng lặp (đã sửa không còn schoolYearId)
                boolean isDuplicate = studentSubjectTeacherService.checkDuplicate(
                        studentId, subjectTeacherId);

                if (isDuplicate) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Sinh viên đã đăng ký môn học này");
                    return "redirect:/admin/enrollment";
                }

                // Thực hiện thêm
                boolean success = studentSubjectTeacherService.addOrUpdate(enrollment);

                if (success) {
                    redirectAttributes.addFlashAttribute("successMessage", "Thêm đăng ký học thành công");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm đăng ký học");
                }
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            }

            return "redirect:/admin/enrollment";
        }

        @GetMapping("/admin/enrollment/{id}")
        @ResponseBody
        public ResponseEntity<?> getEnrollment
        (@PathVariable
        int id
        
            ) {
        Studentsubjectteacher studentsubjectteacher = studentSubjectTeacherService.getById(id);
            if (studentsubjectteacher != null) {
                return new ResponseEntity<>(studentsubjectteacher, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }

        @PostMapping("/admin/enrollment/update")
        public String enrollmentUpdate
        (
            @ModelAttribute("enrollment")
        Studentsubjectteacher enrollment,
                BindingResult bindingResult
        ,
            @RequestParam(value = "studentId.id", required = false)
        Integer studentId,
            @RequestParam(value = "subjectTeacherId.id", required = false) Integer subjectTeacherId,
                Model model
        ,
            RedirectAttributes redirectAttributes
        
            ) {

        try {
                if (bindingResult.hasErrors()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ");
                    return "redirect:/admin/enrollment";
                }

                // Xử lý thủ công các đối tượng quan hệ
                if (studentId != null) {
                    Student student = studentService.getStudentById(studentId);
                    enrollment.setStudentId(student);
                }

                if (subjectTeacherId != null) {
                    Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
                    enrollment.setSubjectTeacherId(subjectTeacher);

                    // Không cần set schoolYearId nữa
                }

                // Kiểm tra trùng lặp (ngoại trừ chính nó) - đã cập nhật tham số
                boolean isDuplicate = studentSubjectTeacherService.checkDuplicateExcept(
                        studentId, subjectTeacherId, enrollment.getId());

                if (isDuplicate) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Sinh viên đã đăng ký môn học này");
                    return "redirect:/admin/enrollment";
                }

                // Thực hiện cập nhật
                boolean success = studentSubjectTeacherService.addOrUpdate(enrollment);

                if (success) {
                    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đăng ký học thành công");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật đăng ký học");
                }
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            }

            return "redirect:/admin/enrollment";
        }

        @GetMapping("/admin/enrollment/delete/{id}")
        public String enrollmentDelete
        (@PathVariable("id")
        int enrollmentId, RedirectAttributes redirectAttributes
        
            ) {
        try {
                // Kiểm tra xem có điểm liên quan không
                boolean hasScores = studentSubjectTeacherService.hasRelatedScores(enrollmentId);

                if (hasScores) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Không thể xóa đăng ký học này vì đã có điểm số liên quan");
                    return "redirect:/admin/enrollment";
                }

                boolean success = studentSubjectTeacherService.delete(enrollmentId);

                if (success) {
                    redirectAttributes.addFlashAttribute("successMessage", "Xóa đăng ký học thành công");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa đăng ký học");
                }
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            }

            return "redirect:/admin/enrollment";
        }

        @GetMapping("/admin/enrollment/batch-add")
        public String batchAddForm
        (Model model
        
            ) {
        model.addAttribute("classes", classService.getClasses());
            model.addAttribute("subjectTeachers", subjectTeacherService.getAllSubjectTeachers());
            model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());
            return "admin/enrollment-batch-add";
        }

        @PostMapping("/admin/enrollment/batch-add")
        public String batchAdd
        (
            @RequestParam(value = "classId", required = false)
        Integer classId,
            @RequestParam(value = "subjectTeacherId", required = false) Integer subjectTeacherId,
                Model model
        ,
            RedirectAttributes redirectAttributes
        
            ) {

        try {
                if (classId == null || subjectTeacherId == null) {
                    model.addAttribute("errorMessage", "Vui lòng chọn đầy đủ thông tin lớp và môn học-giảng viên");
                    model.addAttribute("classes", classService.getClasses());
                    model.addAttribute("subjectTeachers", subjectTeacherService.getAllSubjectTeachers());
                    model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());
                    return "admin/enrollment-batch-add";
                }

                // SchoolYearId không còn được sử dụng ở đây
                int count = studentSubjectTeacherService.batchEnrollStudents(classId, subjectTeacherId);

                if (count > 0) {
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Đã đăng ký thành công cho " + count + " sinh viên");
                } else {
                    redirectAttributes.addFlashAttribute("warningMessage",
                            "Không có sinh viên nào được đăng ký (có thể đã đăng ký trước đó hoặc lớp không có sinh viên)");
                }

                return "redirect:/admin/enrollment";

            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
                model.addAttribute("classes", classService.getClasses());
                model.addAttribute("subjectTeachers", subjectTeacherService.getAllSubjectTeachers());
                model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());
                return "admin/enrollment-batch-add";
            }
        }
    }
