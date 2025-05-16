/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.pojo.Department;
import com.ntn.pojo.Major;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Student;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.User;
import com.ntn.service.DepartmentService;
import com.ntn.service.MajorService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.StudentService;
import com.ntn.service.StudentSubjectTeacherService;
import com.ntn.service.SubjectService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.UserService;
import java.security.Principal;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Admin
 */
@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ApiStudentSubjectTeacherController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private MajorService majorService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private StudentSubjectTeacherService studentSubjectTeacherService;

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @GetMapping("/course-registration")
    public ResponseEntity<?> getAvailableCourses(Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Bạn cần đăng nhập để sử dụng chức năng này"));
            }

            // Lấy thông tin sinh viên
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

            // Lấy thông tin khoa/ngành của sinh viên
            if (student.getClassId() == null || student.getClassId().getMajorId() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Chưa có thông tin về lớp hoặc ngành học của sinh viên"));
            }

            Major studentMajor = student.getClassId().getMajorId();
            Department studentDepartment = studentMajor.getDepartmentId();

            // Lấy thông tin về học kỳ tiếp theo
            Schoolyear currentSchoolYear = getSchoolYearById(schoolYearService.getCurrentSchoolYearId());
            Schoolyear nextSemester = getNextSemester();

            if (nextSemester == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy thông tin về học kỳ tiếp theo"));
            }

            // Lấy danh sách môn học đã đăng ký trong học kỳ tiếp theo
            List<Studentsubjectteacher> registeredCourses = studentSubjectTeacherService.getEnrollmentsByStudentCode(student.getStudentCode())
                    .stream()
                    .filter(e -> e.getSubjectTeacherId().getSchoolYearId().getId().equals(nextSemester.getId()))
                    .collect(Collectors.toList());

            // Tính tổng số tín chỉ đã đăng ký
            int registeredCredits = registeredCourses.stream()
                    .mapToInt(e -> e.getSubjectTeacherId().getSubjectId().getCredits())
                    .sum();

            // Lấy danh sách các lớp học phần có thể đăng ký (thuộc khoa của sinh viên)
            List<Subjectteacher> availableCourses = subjectTeacherService.getSubjectTeachersBySchoolYearId(nextSemester.getId())
                    .stream()
                    .filter(st -> st.getSubjectId() != null
                    && st.getSubjectId().getDepartmentID() != null
                    && st.getSubjectId().getDepartmentID().getId().equals(studentDepartment.getId()))
                    .collect(Collectors.toList());

            // Loại bỏ những môn học đã đăng ký trước đó
            List<Integer> enrolledSubjectIds = registeredCourses.stream()
                    .map(rc -> rc.getSubjectTeacherId().getSubjectId().getId())
                    .collect(Collectors.toList());

            List<Subjectteacher> filteredCourses = availableCourses.stream()
                    .filter(st -> !enrolledSubjectIds.contains(st.getSubjectId().getId()))
                    .collect(Collectors.toList());

            // Kiểm tra thời gian đăng ký
            Map<String, Object> registrationPeriod = getRegistrationPeriodInfo(nextSemester);

            // Format dữ liệu môn học để hiển thị
            List<Map<String, Object>> formattedCourses = formatCoursesForResponse(filteredCourses, registeredCourses);
            List<Map<String, Object>> formattedRegisteredCourses = mapRegisteredCourses(registeredCourses);

            // Trả về kết quả
            response.put("student", student);
            response.put("studentMajor", studentMajor);
            response.put("studentDepartment", studentDepartment);
            response.put("currentSemester", currentSchoolYear);
            response.put("nextSemester", nextSemester);
            response.put("availableCourses", formattedCourses);
            response.put("registeredCourses", formattedRegisteredCourses);
            response.put("registeredCredits", registeredCredits);
            response.put("maxCredits", 17);
            response.put("remainingCredits", 17 - registeredCredits);
            response.put("registrationPeriod", registrationPeriod);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API đăng ký môn học
     */
    @PostMapping("/course-registration/register")
    public ResponseEntity<?> registerCourse(
            Principal principal,
            @RequestParam("subjectTeacherId") Integer subjectTeacherId) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Bạn cần đăng nhập để sử dụng chức năng này"));
            }

            // Lấy thông tin sinh viên
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

            // Lấy thông tin về môn học cần đăng ký
            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
            if (subjectTeacher == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy thông tin môn học"));
            }

            // Lấy thông tin về học kỳ tiếp theo
            Schoolyear nextSemester = getNextSemester();
            if (nextSemester == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy thông tin về học kỳ tiếp theo"));
            }

            // Kiểm tra xem môn học có thuộc học kỳ tiếp theo không
            if (!subjectTeacher.getSchoolYearId().getId().equals(nextSemester.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Môn học không thuộc học kỳ đăng ký"));
            }

            // Kiểm tra thời gian đăng ký
            Map<String, Object> registrationPeriod = getRegistrationPeriodInfo(nextSemester);
            if (!(Boolean) registrationPeriod.get("canRegister")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "error", "Không trong thời gian đăng ký học",
                                "registrationPeriod", registrationPeriod
                        ));
            }

            // Kiểm tra khoa của môn học có phù hợp với khoa của sinh viên không
            if (student.getClassId() != null && student.getClassId().getMajorId() != null
                    && student.getClassId().getMajorId().getDepartmentId() != null
                    && subjectTeacher.getSubjectId().getDepartmentID() != null) {

                Integer studentDepartmentId = student.getClassId().getMajorId().getDepartmentId().getId();
                Integer subjectDepartmentId = subjectTeacher.getSubjectId().getDepartmentID().getId();

                if (!studentDepartmentId.equals(subjectDepartmentId)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Môn học không thuộc khoa của bạn"));
                }
            }

            // Kiểm tra xem sinh viên đã đăng ký môn này chưa
            boolean isDuplicate = studentSubjectTeacherService.checkDuplicate(student.getId(), subjectTeacherId);
            if (isDuplicate) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Bạn đã đăng ký môn học này"));
            }

            // Kiểm tra giới hạn tín chỉ
            List<Studentsubjectteacher> registeredCourses = studentSubjectTeacherService.getEnrollmentsByStudentCode(student.getStudentCode())
                    .stream()
                    .filter(e -> e.getSubjectTeacherId().getSchoolYearId().getId().equals(nextSemester.getId()))
                    .collect(Collectors.toList());

            int registeredCredits = registeredCourses.stream()
                    .mapToInt(e -> e.getSubjectTeacherId().getSubjectId().getCredits())
                    .sum();

            int courseCredits = subjectTeacher.getSubjectId().getCredits();

            if (registeredCredits + courseCredits > 17) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Vượt quá giới hạn 17 tín chỉ cho phép đăng ký"));
            }

            // Đăng ký môn học
            Studentsubjectteacher enrollment = new Studentsubjectteacher();
            enrollment.setStudentId(student);
            enrollment.setSubjectTeacherId(subjectTeacher);

            boolean success = studentSubjectTeacherService.addOrUpdate(enrollment);

            if (success) {
                // Cập nhật danh sách môn đã đăng ký
                registeredCourses = studentSubjectTeacherService.getEnrollmentsByStudentCode(student.getStudentCode())
                        .stream()
                        .filter(e -> e.getSubjectTeacherId().getSchoolYearId().getId().equals(nextSemester.getId()))
                        .collect(Collectors.toList());

                registeredCredits = registeredCourses.stream()
                        .mapToInt(e -> e.getSubjectTeacherId().getSubjectId().getCredits())
                        .sum();

                response.put("success", true);
                response.put("message", "Đăng ký môn học thành công");
                response.put("registeredCourses", mapRegisteredCourses(registeredCourses));
                response.put("registeredCredits", registeredCredits);
                response.put("remainingCredits", 17 - registeredCredits);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Không thể đăng ký môn học. Vui lòng thử lại sau."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API hủy đăng ký môn học
     */
    @DeleteMapping("/course-registration/drop")
    public ResponseEntity<?> dropCourse(
            Principal principal,
            @RequestParam("enrollmentId") Integer enrollmentId) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Bạn cần đăng nhập để sử dụng chức năng này"));
            }

            // Lấy thông tin sinh viên
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

            // Lấy thông tin đăng ký cần hủy
            Studentsubjectteacher enrollment = studentSubjectTeacherService.getById(enrollmentId);
            if (enrollment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy thông tin đăng ký môn học"));
            }

            // Kiểm tra xem đăng ký có phải của sinh viên này không
            if (!enrollment.getStudentId().getId().equals(student.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Không có quyền hủy đăng ký môn học này"));
            }

            // Kiểm tra thời gian hủy đăng ký
            Schoolyear semester = enrollment.getSubjectTeacherId().getSchoolYearId();
            Map<String, Object> registrationPeriod = getRegistrationPeriodInfo(semester);

            if (!(Boolean) registrationPeriod.get("canDrop")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "error", "Đã hết thời gian hủy đăng ký học",
                                "registrationPeriod", registrationPeriod
                        ));
            }

            // Kiểm tra xem có điểm liên quan không
            boolean hasScores = studentSubjectTeacherService.hasRelatedScores(enrollmentId);
            if (hasScores) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Không thể hủy đăng ký vì đã có điểm cho môn học này"));
            }

            // Thực hiện hủy đăng ký
            boolean success = studentSubjectTeacherService.delete(enrollmentId);

            if (success) {
                // Cập nhật danh sách môn đã đăng ký
                List<Studentsubjectteacher> registeredCourses = studentSubjectTeacherService.getEnrollmentsByStudentCode(student.getStudentCode())
                        .stream()
                        .filter(e -> e.getSubjectTeacherId().getSchoolYearId().getId().equals(semester.getId()))
                        .collect(Collectors.toList());

                int registeredCredits = registeredCourses.stream()
                        .mapToInt(e -> e.getSubjectTeacherId().getSubjectId().getCredits())
                        .sum();

                response.put("success", true);
                response.put("message", "Hủy đăng ký môn học thành công");
                response.put("registeredCourses", mapRegisteredCourses(registeredCourses));
                response.put("registeredCredits", registeredCredits);
                response.put("remainingCredits", 17 - registeredCredits);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Không thể hủy đăng ký môn học. Vui lòng thử lại sau."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thông tin về học kỳ tiếp theo
     */
    private Schoolyear getNextSemester() {
        // Lấy tất cả học kỳ, sắp xếp theo thời gian bắt đầu
        List<Schoolyear> allSemesters = schoolYearService.getAllSchoolYears();

        if (allSemesters.isEmpty()) {
            return null;
        }

        // Lấy thời gian hiện tại
        Date now = new Date();

        // Tìm học kỳ hiện tại
        Schoolyear currentSemester = allSemesters.stream()
                .filter(semester -> semester.getYearStart() != null
                && semester.getYearEnd() != null
                && now.after(semester.getYearStart())
                && now.before(semester.getYearEnd()))
                .findFirst()
                .orElse(null);

        if (currentSemester == null) {
            // Nếu không tìm thấy học kỳ hiện tại, lấy học kỳ gần nhất trong tương lai
            return allSemesters.stream()
                    .filter(semester -> semester.getYearStart() != null && semester.getYearStart().after(now))
                    .min(Comparator.comparing(Schoolyear::getYearStart))
                    .orElse(null);
        } else {
            // Nếu tìm thấy học kỳ hiện tại, tìm học kỳ tiếp theo
            return allSemesters.stream()
                    .filter(semester -> semester.getYearStart() != null
                    && semester.getYearStart().after(currentSemester.getYearEnd()))
                    .min(Comparator.comparing(Schoolyear::getYearStart))
                    .orElse(null);
        }
    }

    /**
     * Lấy thông tin học kỳ từ ID
     */
    private Schoolyear getSchoolYearById(int schoolYearId) {
        return schoolYearService.getSchoolYearById(schoolYearId);
    }

    /**
     * Format thông tin môn học cho response
     */
    private List<Map<String, Object>> formatCoursesForResponse(
            List<Subjectteacher> availableCourses,
            List<Studentsubjectteacher> registeredCourses) {

        Set<Integer> registeredSubjectTeacherIds = registeredCourses.stream()
                .map(rc -> rc.getSubjectTeacherId().getId())
                .collect(Collectors.toSet());

        return availableCourses.stream().map(course -> {
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("id", course.getId());
            courseMap.put("subjectName", course.getSubjectId().getSubjectName());
            courseMap.put("subjectId", course.getSubjectId().getId());
            courseMap.put("credits", course.getSubjectId().getCredits());
            courseMap.put("teacherName", course.getTeacherId().getTeacherName());
            courseMap.put("teacherId", course.getTeacherId().getId());
            courseMap.put("classId", course.getClassId() != null ? course.getClassId().getId() : null);
            courseMap.put("className", course.getClassId() != null ? course.getClassId().getClassName() : "Chưa phân lớp");
            courseMap.put("schoolYearId", course.getSchoolYearId().getId());
            courseMap.put("schoolYearName", course.getSchoolYearId().getNameYear() + " " + course.getSchoolYearId().getSemesterName());
            courseMap.put("isRegistered", registeredSubjectTeacherIds.contains(course.getId()));

            return courseMap;
        }).collect(Collectors.toList());
    }

    /**
     * Map thông tin các môn đã đăng ký
     */
    private List<Map<String, Object>> mapRegisteredCourses(List<Studentsubjectteacher> registeredCourses) {
        return registeredCourses.stream().map(enrollment -> {
            Map<String, Object> courseMap = new HashMap<>();
            Subjectteacher subjectTeacher = enrollment.getSubjectTeacherId();

            courseMap.put("enrollmentId", enrollment.getId());
            courseMap.put("subjectTeacherId", subjectTeacher.getId());
            courseMap.put("subjectName", subjectTeacher.getSubjectId().getSubjectName());
            courseMap.put("subjectId", subjectTeacher.getSubjectId().getId());
            courseMap.put("credits", subjectTeacher.getSubjectId().getCredits());
            courseMap.put("teacherName", subjectTeacher.getTeacherId().getTeacherName());
            courseMap.put("teacherId", subjectTeacher.getTeacherId().getId());
            courseMap.put("classId", subjectTeacher.getClassId() != null ? subjectTeacher.getClassId().getId() : null);
            courseMap.put("className", subjectTeacher.getClassId() != null ? subjectTeacher.getClassId().getClassName() : "Chưa phân lớp");
            courseMap.put("schoolYearId", subjectTeacher.getSchoolYearId().getId());
            courseMap.put("schoolYearName", subjectTeacher.getSchoolYearId().getNameYear() + " " + subjectTeacher.getSchoolYearId().getSemesterName());

            return courseMap;
        }).collect(Collectors.toList());
    }

    /**
     * Kiểm tra và trả về thông tin về thời gian đăng ký
     */
    private Map<String, Object> getRegistrationPeriodInfo(Schoolyear semester) {
        Map<String, Object> result = new HashMap<>();
        Date now = new Date();

        if (semester.getYearStart() == null) {
            result.put("canRegister", false);
            result.put("canDrop", false);
            result.put("message", "Không có thông tin về ngày bắt đầu học kỳ");
            return result;
        }

        // Thời gian bắt đầu học kỳ
        Date semesterStart = semester.getYearStart();

        // Thời gian kết thúc học kỳ
        Date semesterEnd = semester.getYearEnd();

        // Thời gian bắt đầu đăng ký (1 tháng trước khi học kỳ bắt đầu)
        Calendar registrationStartCal = Calendar.getInstance();
        registrationStartCal.setTime(semesterStart);
        registrationStartCal.add(Calendar.MONTH, -1);
        Date registrationStart = registrationStartCal.getTime();

        // Thời gian kết thúc đăng ký (15 ngày trước khi học kỳ bắt đầu)
        Calendar registrationEndCal = Calendar.getInstance();
        registrationEndCal.setTime(semesterStart);
        registrationEndCal.add(Calendar.DAY_OF_MONTH, -17);
        Date registrationEnd = registrationEndCal.getTime();

        // Thời gian bắt đầu hủy đăng ký (14 ngày trước khi học kỳ bắt đầu)
        Calendar dropStartCal = Calendar.getInstance();
        dropStartCal.setTime(semesterStart);
        dropStartCal.add(Calendar.DAY_OF_MONTH, -16);
        Date dropStart = dropStartCal.getTime();

        // Thời gian kết thúc hủy đăng ký (7 ngày trước khi học kỳ bắt đầu)
        Calendar dropEndCal = Calendar.getInstance();
        dropEndCal.setTime(semesterStart);
        dropEndCal.add(Calendar.DAY_OF_MONTH, -9);
        Date dropEnd = dropEndCal.getTime();

        // Kiểm tra thời gian hiện tại có nằm trong khoảng thời gian đăng ký hoặc hủy đăng ký không
        boolean canRegister = now.after(registrationStart) && now.before(registrationEnd);
        boolean canDrop = now.after(dropStart) && now.before(dropEnd);

        result.put("semesterStart", semesterStart);
        result.put("semesterEnd", semesterEnd);
        result.put("registrationStart", registrationStart);
        result.put("registrationEnd", registrationEnd);
        result.put("dropStart", dropStart);
        result.put("dropEnd", dropEnd);
        result.put("canRegister", canRegister);
        result.put("canDrop", canDrop);

        // Thêm thông báo tương ứng với các mốc thời gian mới
        if (now.before(registrationStart)) {
            result.put("message", "Thời gian đăng ký chưa bắt đầu");
        } else if (now.after(registrationEnd) && now.before(dropStart)) {
            result.put("message", "Đã kết thúc thời gian đăng ký, chưa đến thời gian hủy đăng ký");
        } else if (now.after(dropEnd) && now.before(semesterStart)) {
            result.put("message", "Đã kết thúc thời gian đăng ký và hủy đăng ký");
        } else if (canRegister) {
            result.put("message", "Đang trong thời gian đăng ký môn học");
        } else if (canDrop) {
            result.put("message", "Đang trong thời gian hủy đăng ký môn học");
        }

        return result;
    }
}
