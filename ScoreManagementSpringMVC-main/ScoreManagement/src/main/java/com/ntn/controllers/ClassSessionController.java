package com.ntn.controllers;

import com.ntn.pojo.ClassSession;
import com.ntn.pojo.Subjectteacher;
import com.ntn.service.ClassService;
import com.ntn.service.ClassSessionService;
import com.ntn.service.DepartmentService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TeacherService;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasAuthority('Admin')")
public class ClassSessionController {

    @Autowired
    private ClassSessionService classSessionService;

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Autowired
    private SchoolYearService schoolYearService;
    
    @Autowired
    private ClassService classService;
    
     @Autowired
    private DepartmentService departmentService;
     
     @Autowired
     private TeacherService teacherService;

    // Hiển thị danh sách các buổi học
    @GetMapping("/admin/class-sessions")
    public String classSessionList(
            Model model,
            @RequestParam(name = "subjectTeacherId", required = false) Integer subjectTeacherId,
            @RequestParam(name = "schoolYearId", required = false) Integer schoolYearId,
            @RequestParam(name = "classId", required = false) Integer classId,
            @RequestParam(name = "teacherId", required = false) Integer teacherId,
            @RequestParam(name = "departmentId", required = false) Integer departmentId,
            @RequestParam(name = "dayOfWeek", required = false) Integer dayOfWeek) {

        List<ClassSession> classSessions;

        // Lọc theo nhiều điều kiện
        if (subjectTeacherId != null) {
            classSessions = classSessionService.getClassSessionsBySubjectTeacher(subjectTeacherId);
            model.addAttribute("selectedSubjectTeacher", subjectTeacherService.getSubjectTeacherById(subjectTeacherId));
        } else if (classId != null) {
            classSessions = classSessionService.getClassSessionsByClass(classId, schoolYearId);
        } else if (teacherId != null) {
            classSessions = classSessionService.getClassSessionsByTeacher(teacherId, schoolYearId);
        } else if (departmentId != null) {
            classSessions = classSessionService.getClassSessionsByDepartment(departmentId, schoolYearId);
        } else if (dayOfWeek != null) {
            classSessions = classSessionService.getClassSessionsByDayOfWeek(dayOfWeek, schoolYearId);
        } else if (schoolYearId != null) {
            classSessions = classSessionService.getClassSessionsBySchoolYear(schoolYearId);
        } else {
            classSessions = classSessionService.getAllClassSessions();
        }

        // Thêm các danh sách cho bộ lọc
        model.addAttribute("classSessions", classSessions);
        model.addAttribute("subjectTeachers", subjectTeacherService.getAllSubjectTeachers());
        model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());
        model.addAttribute("classes", classService.getClasses());
        model.addAttribute("teachers", teacherService.getTeachers());
        model.addAttribute("departments", departmentService.getDepartments());
        model.addAttribute("classSession", new ClassSession());

        // Thêm Map cho ngày trong tuần
        Map<Integer, String> dayOfWeekNames = new HashMap<>();
        dayOfWeekNames.put(1, "Thứ hai");
        dayOfWeekNames.put(2, "Thứ ba");
        dayOfWeekNames.put(3, "Thứ tư");
        dayOfWeekNames.put(4, "Thứ năm");
        dayOfWeekNames.put(5, "Thứ sáu");
        dayOfWeekNames.put(6, "Thứ bảy");
        dayOfWeekNames.put(7, "Chủ nhật");
        model.addAttribute("dayOfWeekNames", dayOfWeekNames);

        // Thêm các giá trị đã chọn cho bộ lọc
        model.addAttribute("selectedSubjectTeacherId", subjectTeacherId);
        model.addAttribute("selectedSchoolYearId", schoolYearId);
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("selectedTeacherId", teacherId);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("selectedDayOfWeek", dayOfWeek);

        return "admin/class-sessions";
    }

    // Thêm buổi học mới
    @PostMapping("/admin/class-session-add")
    public String classSessionAdd(
            @ModelAttribute("classSession") ClassSession classSession,
            @RequestParam(value = "subjectTeacherId.id", required = false) Integer subjectTeacherId,
            @RequestParam(value = "startHour") Integer startHour,
            @RequestParam(value = "startMinute") Integer startMinute,
            @RequestParam(value = "endHour") Integer endHour,
            @RequestParam(value = "endMinute") Integer endMinute,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin buổi học");
                return "redirect:/admin/class-sessions";
            }

            if (subjectTeacherId != null) {
                Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
                classSession.setSubjectTeacherId(subjectTeacher);
            }

            // Thiết lập thời gian bắt đầu và kết thúc
            classSession.setStartTime(LocalTime.of(startHour, startMinute));
            classSession.setEndTime(LocalTime.of(endHour, endMinute));

            // Kiểm tra xung đột lịch học
            if (classSessionService.hasScheduleConflict(classSession)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Phòng học đã được sử dụng trong khung giờ này!");
                return "redirect:/admin/class-sessions";
            }

            boolean success = classSessionService.addOrUpdateClassSession(classSession);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Thêm buổi học thành công");
                return "redirect:/admin/class-sessions";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm buổi học");
                return "redirect:/admin/class-sessions";
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/class-sessions";
        }
    }

    // Cập nhật buổi học
    @PostMapping("/admin/class-session-update")
    public String classSessionUpdate(
            @ModelAttribute("classSession") ClassSession classSession,
            @RequestParam(value = "subjectTeacherId.id", required = false) Integer subjectTeacherId,
            @RequestParam(value = "startHour") Integer startHour,
            @RequestParam(value = "startMinute") Integer startMinute,
            @RequestParam(value = "endHour") Integer endHour,
            @RequestParam(value = "endMinute") Integer endMinute,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin buổi học");
                return "redirect:/admin/class-sessions";
            }

            if (subjectTeacherId != null) {
                Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
                classSession.setSubjectTeacherId(subjectTeacher);
            }

            // Thiết lập thời gian bắt đầu và kết thúc
            classSession.setStartTime(LocalTime.of(startHour, startMinute));
            classSession.setEndTime(LocalTime.of(endHour, endMinute));

            // Kiểm tra xung đột lịch học
            if (classSessionService.hasScheduleConflict(classSession)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Phòng học đã được sử dụng trong khung giờ này!");
                return "redirect:/admin/class-sessions";
            }

            boolean success = classSessionService.addOrUpdateClassSession(classSession);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật buổi học thành công");
                return "redirect:/admin/class-sessions";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật buổi học");
                return "redirect:/admin/class-sessions";
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/class-sessions";
        }
    }

    // Lấy thông tin buổi học theo ID
    @GetMapping("/admin/class-session/{id}")
    public String getClassSession(@PathVariable("id") int id, Model model) {
        ClassSession classSession = classSessionService.getClassSessionById(id);
        model.addAttribute("classSession", classSession);
        return "admin/class-session-detail";
    }

    // Xóa buổi học
    @GetMapping("/admin/class-session/delete/{id}")
    public String classSessionDelete(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            boolean success = classSessionService.deleteClassSession(id);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Xóa buổi học thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa buổi học");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        }

        return "redirect:/admin/class-sessions";
    }
}
