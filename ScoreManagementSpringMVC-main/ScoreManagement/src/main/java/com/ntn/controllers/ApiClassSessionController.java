package com.ntn.controllers;

import com.ntn.pojo.ClassSession;
import com.ntn.pojo.Student;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import com.ntn.service.ClassSessionService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.StudentService;
import com.ntn.service.TeacherService;
import com.ntn.service.UserService;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ApiClassSessionController {
    
    @Autowired
    private ClassSessionService classSessionService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private SchoolYearService schoolYearService;
    
    // API lấy lịch học cho sinh viên theo học kỳ
    @GetMapping("/student/schedule")
    public ResponseEntity<?> getStudentSchedule(
            Principal principal,
            @RequestParam(value = "schoolYearId", required = false) Integer schoolYearId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Bạn cần đăng nhập để sử dụng chức năng này"));
            }
            
            User user = userService.getUserByUn(principal.getName());
            if (user == null || !"Student".equals(user.getRole().name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Bạn không có quyền truy cập tính năng này"));
            }
            
            List<Student> students = studentService.getStudentbyEmail(user.getEmail());
            Student student = students != null && !students.isEmpty() ? students.get(0) : null;
            
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy thông tin sinh viên"));
            }
            
            // Nếu không có schoolYearId, lấy học kỳ hiện tại
            if (schoolYearId == null) {
                schoolYearId = schoolYearService.getCurrentSchoolYearId();
            }
            
            // Lấy danh sách các buổi học
            List<ClassSession> classSessions = classSessionService.getClassSessionsByStudent(student.getId(), schoolYearId);
            
            // Định dạng kết quả để trả về
            List<Map<String, Object>> formattedSessions = formatClassSessions(classSessions);
            
            response.put("success", true);
            response.put("student", Map.of(
                    "id", student.getId(),
                    "studentCode", student.getStudentCode(),
                    "fullName", student.getLastName() + " " + student.getFirstName(),
                    "className", student.getClassId() != null ? student.getClassId().getClassName() : "Chưa có lớp"
            ));
            response.put("classSessions", formattedSessions);
            response.put("schoolYear", schoolYearService.getSchoolYearById(schoolYearId));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // API lấy lịch dạy cho giáo viên theo học kỳ
    @GetMapping("/teacher/schedule")
    public ResponseEntity<?> getTeacherSchedule(
            Principal principal,
            @RequestParam(value = "schoolYearId", required = false) Integer schoolYearId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Bạn cần đăng nhập để sử dụng chức năng này"));
            }
            
            User user = userService.getUserByUn(principal.getName());
            if (user == null || !"Teacher".equals(user.getRole().name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Bạn không có quyền truy cập tính năng này"));
            }
            
            Teacher teacher = teacherService.getTeacherByEmail(user.getEmail());
            
            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy thông tin giáo viên"));
            }
            
            // Nếu không có schoolYearId, lấy học kỳ hiện tại
            if (schoolYearId == null) {
                schoolYearId = schoolYearService.getCurrentSchoolYearId();
            }
            
            // Lấy danh sách các buổi học
            List<ClassSession> classSessions = classSessionService.getClassSessionsByTeacher(teacher.getId(), schoolYearId);
            
            // Định dạng kết quả để trả về
            List<Map<String, Object>> formattedSessions = formatClassSessions(classSessions);
            
            response.put("success", true);
            response.put("teacher", Map.of(
                    "id", teacher.getId(),
                    "teacherName", teacher.getTeacherName(),
                    "department", teacher.getDepartmentId() != null ? teacher.getDepartmentId().getDepartmentName() : "Chưa có khoa"
            ));
            response.put("classSessions", formattedSessions);
            response.put("schoolYear", schoolYearService.getSchoolYearById(schoolYearId));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // Helper method để format thông tin buổi học
    private List<Map<String, Object>> formatClassSessions(List<ClassSession> classSessions) {
        List<Map<String, Object>> formattedSessions = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (ClassSession session : classSessions) {
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("id", session.getId());
            sessionInfo.put("roomId", session.getRoomId());
            sessionInfo.put("dayOfWeek", session.getDayOfWeek());
            sessionInfo.put("dayOfWeekName", getDayOfWeekName(session.getDayOfWeek()));
            sessionInfo.put("startTime", session.getStartTime().format(timeFormatter));
            sessionInfo.put("endTime", session.getEndTime().format(timeFormatter));
            sessionInfo.put("notes", session.getNotes());
            
            Map<String, Object> subjectInfo = new HashMap<>();
            if (session.getSubjectTeacherId() != null) {
                subjectInfo.put("id", session.getSubjectTeacherId().getId());
                
                if (session.getSubjectTeacherId().getSubjectId() != null) {
                    subjectInfo.put("subjectId", session.getSubjectTeacherId().getSubjectId().getId());
                    subjectInfo.put("subjectName", session.getSubjectTeacherId().getSubjectId().getSubjectName());
                    subjectInfo.put("credits", session.getSubjectTeacherId().getSubjectId().getCredits());
                }
                
                if (session.getSubjectTeacherId().getTeacherId() != null) {
                    subjectInfo.put("teacherId", session.getSubjectTeacherId().getTeacherId().getId());
                    subjectInfo.put("teacherName", session.getSubjectTeacherId().getTeacherId().getTeacherName());
                }
                
                if (session.getSubjectTeacherId().getClassId() != null) {
                    subjectInfo.put("classId", session.getSubjectTeacherId().getClassId().getId());
                    subjectInfo.put("className", session.getSubjectTeacherId().getClassId().getClassName());
                }
                
                if (session.getSubjectTeacherId().getSchoolYearId() != null) {
                    subjectInfo.put("schoolYearId", session.getSubjectTeacherId().getSchoolYearId().getId());
                    subjectInfo.put("schoolYearName", session.getSubjectTeacherId().getSchoolYearId().getNameYear() + 
                            " " + session.getSubjectTeacherId().getSchoolYearId().getSemesterName());
                }
            }
            
            sessionInfo.put("subject", subjectInfo);
            formattedSessions.add(sessionInfo);
        }
        
        return formattedSessions;
    }
    
    // Helper method để chuyển đổi số thứ tự ngày trong tuần thành tên
    private String getDayOfWeekName(int dayOfWeek) {
        switch(dayOfWeek) {
            case 1: return "Thứ hai";
            case 2: return "Thứ ba";
            case 3: return "Thứ tư";
            case 4: return "Thứ năm";
            case 5: return "Thứ sáu";
            case 6: return "Thứ bảy";
            case 7: return "Chủ nhật";
            default: return "Không xác định";
        }
    }
}