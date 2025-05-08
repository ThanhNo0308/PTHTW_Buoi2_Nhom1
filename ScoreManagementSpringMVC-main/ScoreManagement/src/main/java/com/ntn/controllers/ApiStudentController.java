package com.ntn.controllers;

import com.ntn.pojo.Class;
import com.ntn.pojo.Score;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Student;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.User;
import com.ntn.service.SchoolYearService;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
import com.ntn.service.StudentSubjectTeacherService;
import com.ntn.service.UserService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ApiStudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudentSubjectTeacherService studentSubjectTeacherService;

    // Lấy thông tin sinh viên hiện tại
    @GetMapping("/current-student")
    public ResponseEntity<?> getCurrentStudent(Principal principal) {
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

            response.put("student", student);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lấy điểm của sinh viên đăng nhập
    @GetMapping("/scores")
    public ResponseEntity<?> getStudentScores(
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

            // Lấy tất cả học kỳ
            List<Schoolyear> allSchoolYears = schoolYearService.getAllSchoolYears();

            // Lấy danh sách đăng ký môn học của sinh viên
            List<Studentsubjectteacher> enrollments
                    = studentSubjectTeacherService.getEnrollmentsByStudentCode(student.getStudentCode());

            // Tạo set để lưu trữ các học kỳ đã đăng ký
            Set<Integer> enrolledSchoolYearIds = new HashSet<>();
            Map<Integer, List<Map<String, Object>>> enrolledSubjects = new HashMap<>();

            // Xử lý enrollments để lấy danh sách học kỳ có đăng ký
            for (Studentsubjectteacher enrollment : enrollments) {
                if (enrollment.getSubjectTeacherId() != null
                        && enrollment.getSubjectTeacherId().getSchoolYearId() != null) {

                    Schoolyear schoolYear = enrollment.getSubjectTeacherId().getSchoolYearId();
                    enrolledSchoolYearIds.add(schoolYear.getId());

                    // Thêm thông tin môn học đã đăng ký
                    enrolledSubjects.putIfAbsent(schoolYear.getId(), new ArrayList<>());
                    Map<String, Object> subject = new HashMap<>();
                    subject.put("id", enrollment.getSubjectTeacherId().getSubjectId().getId());
                    subject.put("name", enrollment.getSubjectTeacherId().getSubjectId().getSubjectName());
                    subject.put("credits", enrollment.getSubjectTeacherId().getSubjectId().getCredits());
                    enrolledSubjects.get(schoolYear.getId()).add(subject);
                }
            }

            // Lọc để chỉ giữ lại học kỳ sinh viên đã đăng ký
            List<Schoolyear> filteredSchoolYears = allSchoolYears.stream()
                    .filter(sy -> enrolledSchoolYearIds.contains(sy.getId()))
                    .collect(Collectors.toList());

            List<Score> scores;
            Schoolyear currentSchoolYear;

            if (schoolYearId != null) {
                scores = scoreService.getSubjectScoresByStudentCodeAndSchoolYear(student.getStudentCode(), schoolYearId);
                currentSchoolYear = schoolYearService.getSchoolYearById(schoolYearId);
            } else {
                // Lấy năm học hiện tại
                int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();
                scores = scoreService.getSubjectScoresByStudentCodeAndSchoolYear(student.getStudentCode(), currentSchoolYearId);
                currentSchoolYear = schoolYearService.getSchoolYearById(currentSchoolYearId);
            }

            response.put("enrolledSubjects", enrolledSubjects);
            response.put("schoolYears", filteredSchoolYears);
            response.put("scores", scores);
            response.put("currentSchoolYear", currentSchoolYear);

            // Tính điểm trung bình học kỳ
            if (!scores.isEmpty()) {
                double totalScore = 0;
                int totalCredits = 0;

                // Nhóm điểm theo môn học
                Map<Integer, List<Score>> scoresBySubject = new HashMap<>();
                for (Score score : scores) {
                    int subjectId = score.getSubjectTeacherID().getSubjectId().getId();
                    if (!scoresBySubject.containsKey(subjectId)) {
                        scoresBySubject.put(subjectId, new ArrayList<>());
                    }
                    scoresBySubject.get(subjectId).add(score);
                }

                // Tính điểm trung bình môn học và điểm trung bình học kỳ
                Map<Integer, Double> subjectAverages = new HashMap<>();
                for (Map.Entry<Integer, List<Score>> entry : scoresBySubject.entrySet()) {
                    int subjectId = entry.getKey();
                    List<Score> subjectScores = entry.getValue();

                    double subjectTotalWeightedScore = 0;
                    double subjectTotalWeight = 0;

                    for (Score score : subjectScores) {
                        if (score.getScoreValue() != null && score.getScoreType() != null) {
                            Float weight = scoreService.getScoreWeight(
                                    score.getSubjectTeacherID().getClassId().getId(),
                                    score.getSubjectTeacherID().getId(),
                                    currentSchoolYear.getId(),
                                    score.getScoreType().getScoreType()
                            );

                            if (weight != null) {
                                subjectTotalWeightedScore += score.getScoreValue() * weight;
                                subjectTotalWeight += weight;
                            }
                        }
                    }

                    double subjectAverage = 0;
                    if (subjectTotalWeight > 0) {
                        subjectAverage = subjectTotalWeightedScore / subjectTotalWeight;
                    }

                    int credits = subjectScores.get(0).getSubjectTeacherID().getSubjectId().getCredits();
                    subjectAverages.put(subjectId, subjectAverage);
                    totalScore += subjectAverage * credits;
                    totalCredits += credits;
                }

                double semesterAverage = totalCredits > 0 ? totalScore / totalCredits : 0;
                response.put("subjectAverages", subjectAverages);
                response.put("semesterAverage", semesterAverage);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lấy thông tin lớp và danh sách sinh viên trong lớp
    @GetMapping("/class-info")
    public ResponseEntity<?> getClassInfo(Principal principal) {
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

            // Lấy thông tin lớp của sinh viên
            Class studentClass = student.getClassId();
            if (studentClass == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Sinh viên chưa được phân lớp"));
            }

            // Lấy danh sách sinh viên trong lớp
            List<Student> classmates = studentService.getStudentByClassId(studentClass.getId());

            response.put("class", studentClass);
            response.put("classmates", classmates);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lấy thông tin môn học đã đăng ký của sinh viên
    @GetMapping("/subjects")
    public ResponseEntity<?> getEnrolledSubjects(
            Principal principal,
            @RequestParam(value = "schoolYearId", required = false) Integer schoolYearId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Kiểm tra đăng nhập
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Bạn cần đăng nhập để sử dụng chức năng này"));
            }

            // Kiểm tra quyền
            User user = userService.getUserByUn(principal.getName());
            if (user == null || !"Student".equals(user.getRole().name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Bạn không có quyền truy cập tính năng này"));
            }

            // Lấy thông tin sinh viên
            List<Student> students = studentService.getStudentbyEmail(user.getEmail());
            Student student = students != null && !students.isEmpty() ? students.get(0) : null;

            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy thông tin sinh viên"));
            }

            // Lấy danh sách đăng ký môn học của sinh viên
            List<Studentsubjectteacher> enrollments
                    = studentSubjectTeacherService.getEnrollmentsByStudentCode(student.getStudentCode());

            // Tạo Set để lưu trữ các học kỳ đã đăng ký
            Set<Integer> enrolledSchoolYearIds = new HashSet<>();

            // Thu thập tất cả id học kỳ mà sinh viên có đăng ký môn
            for (Studentsubjectteacher enrollment : enrollments) {
                if (enrollment.getSubjectTeacherId() != null
                        && enrollment.getSubjectTeacherId().getSchoolYearId() != null) {
                    enrolledSchoolYearIds.add(enrollment.getSubjectTeacherId().getSchoolYearId().getId());
                }
            }

            // Lấy tất cả học kỳ từ hệ thống
            List<Schoolyear> allSchoolYears = schoolYearService.getAllSchoolYears();

            // Lọc ra chỉ những học kỳ mà sinh viên có đăng ký
            List<Schoolyear> filteredSchoolYears = allSchoolYears.stream()
                    .filter(sy -> enrolledSchoolYearIds.contains(sy.getId()))
                    .collect(Collectors.toList());

            // Xác định học kỳ hiện tại để lọc môn học
            int currentSchoolYearId;
            if (schoolYearId != null) {
                // Nếu có chọn học kỳ cụ thể
                currentSchoolYearId = schoolYearId;
            } else if (!filteredSchoolYears.isEmpty()) {
                // Nếu không chọn học kỳ nào và có học kỳ đã đăng ký, lấy học kỳ đầu tiên
                currentSchoolYearId = filteredSchoolYears.get(0).getId();
            } else {
                // Nếu không có học kỳ nào đã đăng ký, lấy học kỳ hiện tại của hệ thống
                currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();
            }

            // Lấy thông tin học kỳ hiện tại
            Schoolyear currentSchoolYear = schoolYearService.getSchoolYearById(currentSchoolYearId);

            // Thu thập thông tin môn học của học kỳ đã chọn
            List<Map<String, Object>> subjects = new ArrayList<>();
            Set<Integer> addedSubjectIds = new HashSet<>();
            for (Studentsubjectteacher enrollment : enrollments) {
                if (enrollment.getSubjectTeacherId() != null
                        && enrollment.getSubjectTeacherId().getSchoolYearId() != null
                        && enrollment.getSubjectTeacherId().getSchoolYearId().getId() == currentSchoolYearId) {

                    Integer subjectId = enrollment.getSubjectTeacherId().getSubjectId().getId();

                    // Kiểm tra xem môn học đã được thêm chưa
                    if (!addedSubjectIds.contains(subjectId)) {
                        addedSubjectIds.add(subjectId);

                        Map<String, Object> subjectInfo = new HashMap<>();
                        subjectInfo.put("subjectId", subjectId);
                        subjectInfo.put("subjectName", enrollment.getSubjectTeacherId().getSubjectId().getSubjectName());
                        subjectInfo.put("credits", enrollment.getSubjectTeacherId().getSubjectId().getCredits());
                        subjectInfo.put("teacherName", enrollment.getSubjectTeacherId().getTeacherId().getTeacherName());
                        subjectInfo.put("teacherId", enrollment.getSubjectTeacherId().getTeacherId().getId());

                        subjects.add(subjectInfo);
                    }
                }
            }

            // Đưa dữ liệu vào response
            response.put("subjects", subjects);
            response.put("schoolYear", currentSchoolYear);
            response.put("schoolYears", filteredSchoolYears); // Chỉ trả về các học kỳ đã đăng ký

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
