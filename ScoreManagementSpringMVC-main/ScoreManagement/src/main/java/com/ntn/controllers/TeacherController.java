package com.ntn.controllers;

import com.ntn.pojo.ListScoreDTO;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.Class;
import com.ntn.pojo.Department;
import com.ntn.pojo.User;
import com.ntn.service.ClassService;
import com.ntn.service.DepartmentService;
import com.ntn.service.ScoreService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.StudentService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TeacherService;
import com.ntn.service.UserService;
import jakarta.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TeacherController {

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SchoolYearService schoolYearService;

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

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // 1. Đăng ký CustomDateEditor để xử lý chuyển đổi String sang Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));

        // 2. Đăng ký PropertyEditor tùy chỉnh để chuyển đổi id thành đối tượng Department
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

    @PostMapping("/classes/{id}/scores/import")
    public String importScores(@PathVariable("id") int classId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin giảng viên đang đăng nhập
            String username = authentication.getName();
            int teacherId = teacherService.getTeacherIdByUsername(username);

            // Lấy subjectTeacherId từ teacherId và classId
            int subjectTeacherId = teacherService.getSubjectTeacherIdByTeacherAndClass(teacherId, classId);

            // Lấy năm học hiện tại
            int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();

            // Nhập điểm từ file CSV
            boolean success = scoreService.importScoresFromCsv(file, subjectTeacherId, currentSchoolYearId);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Nhập điểm thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể nhập điểm");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi nhập điểm: " + e.getMessage());
        }

        return "redirect:/teacher/classes/" + classId + "/scores";
    }

    /**
     * Xuất điểm ra file CSV
     */
    @GetMapping("/classes/{id}/scores/export/csv")
    public ResponseEntity<byte[]> exportScoresToCsv(@PathVariable("id") int classId,
            Authentication authentication) {
        try {
            // Lấy thông tin giảng viên đang đăng nhập
            String username = authentication.getName();
            int teacherId = teacherService.getTeacherIdByUsername(username);

            // Lấy subjectTeacherId từ teacherId và classId
            int subjectTeacherId = teacherService.getSubjectTeacherIdByTeacherAndClass(teacherId, classId);

            // Lấy năm học hiện tại
            int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();

            // Xuất điểm ra CSV
            byte[] csvContent = scoreService.exportScoresToCsv(subjectTeacherId, classId, currentSchoolYearId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            String filename = "scores_class_" + classId + ".csv";
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Xuất điểm ra file PDF
     */
    @GetMapping("/classes/{id}/scores/export/pdf")
    public ResponseEntity<byte[]> exportScoresToPdf(@PathVariable("id") int classId,
            Authentication authentication) {
        try {
            // Lấy thông tin giảng viên đang đăng nhập
            String username = authentication.getName();
            int teacherId = teacherService.getTeacherIdByUsername(username);

            // Lấy subjectTeacherId từ teacherId và classId
            int subjectTeacherId = teacherService.getSubjectTeacherIdByTeacherAndClass(teacherId, classId);

            // Lấy năm học hiện tại
            int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();

            // Xuất điểm ra PDF
            byte[] pdfContent = scoreService.exportScoresToPdf(subjectTeacherId, classId, currentSchoolYearId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "scores_class_" + classId + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Hiển thị trang tìm kiếm sinh viên
     */
    @GetMapping("/search-students")
    public String searchStudentsForm(Model model) {
        model.addAttribute("searchQuery", "");
        return "teacher/search-students";
    }

    /**
     * Xử lý tìm kiếm sinh viên
     */
    @PostMapping("/search-students")
    public String searchStudents(@RequestParam("searchQuery") String searchQuery,
            @RequestParam("searchType") String searchType,
            Model model) {
        List<Student> students;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            if ("code".equals(searchType)) {
                students = studentService.findStudentsByCode(searchQuery);
            } else if ("name".equals(searchType)) {
                students = studentService.findStudentsByName(searchQuery);
            } else {
                students = studentService.findStudentsByClass(searchQuery);
            }
        } else {
            students = new ArrayList<>();
        }

        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("searchType", searchType);
        model.addAttribute("students", students);
        return "teacher/search-students";
    }

    /**
     * Xem chi tiết sinh viên
     */
    @GetMapping("/student-detail/{id}")
    public String viewStudentDetail(@PathVariable("id") String studentId, Model model) {
        Optional<Student> student = studentService.findByStudentId(studentId);

        if (student.isPresent()) {
            model.addAttribute("student", student.get());
            return "teacher/student-detail";
        } else {
            return "redirect:/teacher/search-students?error=student-not-found";
        }
    }

    /**
     * Xem điểm của sinh viên
     */
    @GetMapping("/student-scores/{id}")
    public String viewStudentScores(@PathVariable("id") String studentId, Model model) {
        Optional<Student> student = studentService.findByStudentId(studentId);

        if (student.isPresent()) {
            Student studentObj = student.get();
            model.addAttribute("student", studentObj);

            // Lấy danh sách điểm của sinh viên
            List<Score> scores = scoreService.findByStudent(studentObj);
            model.addAttribute("scores", scores);

            return "teacher/student-scores";
        } else {
            return "redirect:/teacher/search-students?error=student-not-found";
        }
    }

    /**
     * Hiển thị form nhập điểm từ CSV
     */
    @GetMapping("/scores/import")
    public String showImportScoresForm(Model model, Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = teacherService.getTeacherByUsername(username);

        if (teacher == null) {
            return "redirect:/teacher/error?message=teacher-not-found";
        }

        List<Subjectteacher> subjectTeachers = subjectTeacherService.getSubjectTeachersByTeacherId(teacher.getId());
        List<Schoolyear> schoolYears = schoolYearService.getAllSchoolYears();
        List<Class> classes = classService.getClassesByTeacher(teacher.getId());

        model.addAttribute("subjectTeachers", subjectTeachers);
        model.addAttribute("schoolYears", schoolYears);
        model.addAttribute("classes", classes);

        return "teacher/import-scores";
    }

    @PostMapping("/scores/import")
    public String importScores(@RequestParam("file") MultipartFile file,
            @RequestParam("subjectTeacherId") int subjectTeacherId,
            @RequestParam("classId") int classId,
            @RequestParam("schoolYearId") int schoolYearId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Teacher teacher = teacherService.getTeacherByUsername(username);

            if (teacher == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin giảng viên");
                return "redirect:/teacher/scores/import";
            }

            // Kiểm tra xem giảng viên có được phân công môn học này không
            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
            if (subjectTeacher == null || !subjectTeacher.getTeacherId().getId().equals(teacher.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không có quyền nhập điểm cho môn học này");
                return "redirect:/teacher/scores/import";
            }

            boolean success = scoreService.importScoresFromCsv(file, subjectTeacherId, schoolYearId);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Nhập điểm thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể nhập điểm. Vui lòng kiểm tra lại file");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/teacher/scores/import";
    }

    /**
     * Tải về file mẫu để nhập điểm
     */
    @GetMapping("/scores/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            // Tạo file mẫu CSV
            String csvContent = "MSSV,Họ tên,Giữa kỳ,Cuối kỳ,Cột điểm 1,Cột điểm 2,Cột điểm 3\n"
                    + "SV001,Nguyễn Văn A,8.5,9.0,7.5,8.0,9.5\n"
                    + "SV002,Trần Thị B,7.0,8.0,6.5,7.0,8.5\n";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "score_template.csv");

            return new ResponseEntity<>(csvContent.getBytes(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
