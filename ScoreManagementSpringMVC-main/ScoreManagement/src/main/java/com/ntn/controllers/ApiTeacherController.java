/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.User;
import com.ntn.pojo.Student;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.service.EmailService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
import com.ntn.service.StudentSubjectTeacherService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TeacherService;
import com.ntn.service.UserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Admin
 */
@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ApiTeacherController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private StudentSubjectTeacherService studentSubjectTeacherService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // Xem chi tiết sinh viên
    @GetMapping("/students/{studentCode}/detail")
    public ResponseEntity<Map<String, Object>> getStudentDetail(@PathVariable("studentCode") String studentCode) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Student> student = studentService.findByStudentCode(studentCode);

            if (student.isPresent()) {
                response.put("success", true);
                response.put("student", student.get());

                // Lấy thông tin về các học kỳ
                List<Schoolyear> allSchoolYears = schoolYearService.getAllSchoolYears();
                List<Schoolyear> relevantSchoolYears = new ArrayList<>();
                Map<Integer, Double> averageScores = new HashMap<>();
                Map<Integer, List<Map<String, Object>>> enrolledSubjects = new HashMap<>();

                // Lấy danh sách đăng ký môn học của sinh viên
                List<Studentsubjectteacher> studentEnrollments
                        = studentSubjectTeacherService.getEnrollmentsByStudentCode(studentCode);

                // Tạo map để lưu trữ học kỳ của mỗi đăng ký
                Map<Integer, Schoolyear> enrollmentSchoolYears = new HashMap<>();
                Map<Integer, Set<Integer>> schoolYearSubjects = new HashMap<>();

                // Xử lý thông tin đăng ký môn học
                for (Studentsubjectteacher enrollment : studentEnrollments) {
                    Subjectteacher subjectTeacher = enrollment.getSubjectTeacherId();
                    if (subjectTeacher != null && subjectTeacher.getSchoolYearId() != null) {
                        Schoolyear schoolYear = subjectTeacher.getSchoolYearId();
                        Integer schoolYearId = schoolYear.getId();

                        // Thêm học kỳ vào danh sách nếu chưa có
                        enrollmentSchoolYears.putIfAbsent(schoolYearId, schoolYear);

                        // Thêm thông tin môn học đã đăng ký
                        schoolYearSubjects.putIfAbsent(schoolYearId, new HashSet<>());
                        schoolYearSubjects.get(schoolYearId).add(subjectTeacher.getSubjectId().getId());

                        // Thêm thông tin môn học đã đăng ký cho frontend
                        enrolledSubjects.putIfAbsent(schoolYearId, new ArrayList<>());
                        Map<String, Object> subject = new HashMap<>();
                        subject.put("id", subjectTeacher.getSubjectId().getId());
                        subject.put("name", subjectTeacher.getSubjectId().getSubjectName());
                        subject.put("credits", subjectTeacher.getSubjectId().getCredits());
                        enrolledSubjects.get(schoolYearId).add(subject);
                    }
                }

                // Thêm các học kỳ đã đăng ký vào danh sách
                for (Schoolyear schoolYear : enrollmentSchoolYears.values()) {
                    if (!relevantSchoolYears.contains(schoolYear)) {
                        relevantSchoolYears.add(schoolYear);
                    }
                }

                Map<Integer, List<Map<String, Object>>> subjectScoresByYear = new HashMap<>();

                // Bổ sung thông tin điểm cho từng học kỳ
                for (Schoolyear schoolYear : allSchoolYears) {
                    List<Score> scores = scoreService.getSubjectScoresByStudentCodeAndSchoolYear(
                            studentCode, schoolYear.getId());

                    if (!scores.isEmpty()) {
                        // Thêm học kỳ vào danh sách
                        if (!relevantSchoolYears.contains(schoolYear)) {
                            relevantSchoolYears.add(schoolYear);
                        }

                        // Nhóm điểm theo môn học
                        Map<Integer, List<Score>> scoresBySubject = new HashMap<>();
                        for (Score score : scores) {
                            int subjectId = score.getSubjectTeacherID().getSubjectId().getId();
                            if (!scoresBySubject.containsKey(subjectId)) {
                                scoresBySubject.put(subjectId, new ArrayList<>());
                            }
                            scoresBySubject.get(subjectId).add(score);
                        }

                        List<Map<String, Object>> subjectScores = new ArrayList<>();
                        for (Map.Entry<Integer, List<Score>> entry : scoresBySubject.entrySet()) {
                            int subjectId = entry.getKey();
                            List<Score> subjectScoreList = entry.getValue();

                            // Tính điểm trung bình môn học
                            double totalWeightedScore = 0;
                            double totalWeight = 0;

                            for (Score score : subjectScoreList) {
                                if (score.getScoreType() != null && score.getScoreValue() != null) {
                                    Float weight = scoreService.getScoreWeight(
                                            score.getSubjectTeacherID().getClassId().getId(),
                                            score.getSubjectTeacherID().getId(),
                                            schoolYear.getId(),
                                            score.getScoreType().getScoreType()
                                    );

                                    if (weight != null) {
                                        totalWeightedScore += score.getScoreValue() * weight;
                                        totalWeight += weight;
                                    }
                                }
                            }

                            double subjectAverage = 0;
                            if (totalWeight > 0) {
                                subjectAverage = totalWeightedScore / totalWeight;
                            }

                            Map<String, Object> subjectScoreInfo = new HashMap<>();
                            subjectScoreInfo.put("id", subjectId);
                            subjectScoreInfo.put("name", subjectScoreList.get(0).getSubjectTeacherID().getSubjectId().getSubjectName());
                            subjectScoreInfo.put("credits", subjectScoreList.get(0).getSubjectTeacherID().getSubjectId().getCredits());
                            subjectScoreInfo.put("averageScore", subjectAverage);

                            subjectScores.add(subjectScoreInfo);
                        }

                        subjectScoresByYear.put(schoolYear.getId(), subjectScores);
                    }
                }

                // Sắp xếp học kỳ theo thứ tự mới nhất trước
                Collections.sort(relevantSchoolYears, (sy1, sy2) -> Integer.compare(sy2.getId(), sy1.getId()));
                response.put("subjectScoresByYear", subjectScoresByYear);
                response.put("schoolYears", relevantSchoolYears);
                response.put("averageScores", averageScores);
                response.put("enrolledSubjects", enrolledSubjects);

                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy sinh viên với mã " + studentCode);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Xem điểm của sinh viên
    @GetMapping("/students/{studentCode}/scores")
    public ResponseEntity<Map<String, Object>> getStudentScores(
            @PathVariable("studentCode") String studentCode,
            @RequestParam(value = "schoolYearId", required = false) Integer schoolYearId) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Student> studentOpt = studentService.findByStudentCode(studentCode);

            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                response.put("student", student);

                // Lấy danh sách đăng ký môn học của sinh viên
                List<Studentsubjectteacher> enrollments
                        = studentSubjectTeacherService.getEnrollmentsByStudentCode(studentCode);

                // Tạo map để lưu trữ học kỳ của mỗi đăng ký
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
                        enrolledSubjects.get(schoolYear.getId()).add(subject);
                    }
                }

                // Lấy danh sách tất cả học kỳ
                List<Schoolyear> allSchoolYears = schoolYearService.getAllSchoolYears();

                // Lọc để chỉ giữ lại học kỳ sinh viên đã đăng ký
                List<Schoolyear> filteredSchoolYears = allSchoolYears.stream()
                        .filter(sy -> enrolledSchoolYearIds.contains(sy.getId()))
                        .collect(Collectors.toList());

                List<Score> scores;
                Schoolyear currentSchoolYear;

                if (schoolYearId != null) {
                    scores = scoreService.getSubjectScoresByStudentCodeAndSchoolYear(studentCode, schoolYearId);
                    currentSchoolYear = schoolYearService.getSchoolYearById(schoolYearId);
                } else {
                    // Lấy năm học hiện tại
                    int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();
                    scores = scoreService.getSubjectScoresByStudentCodeAndSchoolYear(studentCode, currentSchoolYearId);
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

                    for (Score score : scores) {
                        if (score.getAverageScore() != null) {
                            int credits = score.getSubjectTeacherID().getSubjectId().getCredits();
                            totalScore += score.getAverageScore() * credits;
                            totalCredits += credits;
                        }
                    }

                    if (totalCredits > 0) {
                        response.put("semesterAverage", totalScore / totalCredits);
                    } else {
                        response.put("semesterAverage", 0);
                    }
                } else {
                    response.put("semesterAverage", 0);
                }

                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy sinh viên với mã " + studentCode);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Tìm kiếm sinh viên
    @GetMapping("/students/search")
    @Transactional
    public ResponseEntity<Map<String, Object>> searchStudents(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "type", defaultValue = "name") String searchType) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<Student> students = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                if ("code".equals(searchType)) {
                    students = studentService.findStudentsByCode(query);
                } else if ("name".equals(searchType)) {
                    students = studentService.findStudentsByName(query);
                } else {
                    students = studentService.findStudentsByClass(query);
                }
            }

            // Detach objects from the Hibernate session to avoid lazy-loading issues
            for (Student student : students) {
                if (student.getClassId() != null) {
                    // Ensure class information is initialized to prevent lazy loading issues
                    student.getClassId().getClassName(); // Force initialization
                }
            }

            response.put("success", true);
            response.put("students", students);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi tìm kiếm sinh viên: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Tất cả sinh viên trong lớp được phân công
    @GetMapping("/students/assigned")
    public ResponseEntity<Map<String, Object>> getAssignedStudents(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = authentication.getName();
            Teacher teacher = teacherService.getTeacherByUsername(username);

            if (teacher == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin giảng viên");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Lấy danh sách phân công giảng dạy của giáo viên
            List<Subjectteacher> teacherAssignments = subjectTeacherService.getSubjectTeachersByTeacherId(teacher.getId());

            // Lấy danh sách lớp duy nhất từ các phân công (loại bỏ trùng lặp)
            Set<com.ntn.pojo.Class> assignedClassesSet = new HashSet<>();
            for (Subjectteacher assignment : teacherAssignments) {
                if (assignment.getClassId() != null) {
                    assignedClassesSet.add(assignment.getClassId());
                }
            }
            List<com.ntn.pojo.Class> assignedClasses = new ArrayList<>(assignedClassesSet);

            // Lấy danh sách sinh viên từ các lớp được phân công
            Set<Student> allAssignedStudents = new HashSet<>();
            for (com.ntn.pojo.Class cls : assignedClasses) {
                List<Student> studentsInClass = studentService.getStudentByClassId(cls.getId());
                allAssignedStudents.addAll(studentsInClass);
            }

            for (Subjectteacher assignment : teacherAssignments) {
                List<Studentsubjectteacher> enrollments = studentSubjectTeacherService.getBySubjectTeacherId(assignment.getId());
                for (Studentsubjectteacher enrollment : enrollments) {
                    if (enrollment.getStudentId() != null) {
                        allAssignedStudents.add(enrollment.getStudentId());
                    }
                }
            }

            List<Student> assignedStudentsList = new ArrayList<>(allAssignedStudents);

            // Sắp xếp sinh viên theo tên lớp, sau đó theo họ và tên
            Collections.sort(assignedStudentsList, (s1, s2) -> {
                int classCompare = s1.getClassId().getClassName().compareTo(s2.getClassId().getClassName());
                if (classCompare != 0) {
                    return classCompare;
                }

                int lastNameCompare = s1.getLastName().compareTo(s2.getLastName());
                if (lastNameCompare != 0) {
                    return lastNameCompare;
                }

                return s1.getFirstName().compareTo(s2.getFirstName());
            });

            response.put("success", true);
            response.put("students", assignedStudentsList);
            response.put("classCount", assignedClasses.size());
            response.put("studentCount", assignedStudentsList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách sinh viên: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // API lấy danh sách admin để gửi mail
    @GetMapping("/admin-users")
    public ResponseEntity<Map<String, Object>> getAdminUsers() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Nếu không muốn sửa phương thức trong UserRepositoryImp
            // có thể sử dụng trực tiếp từ bảng user và lọc trong code
            List<User> allUsers = userService.getUsers();
            List<Map<String, Object>> adminUsers = allUsers.stream()
                    .filter(user -> user.getRole() == User.Role.Admin)
                    .map(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", user.getId());
                        userMap.put("username", user.getUsername());
                        userMap.put("email", user.getEmail());
                        return userMap;
                    })
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("adminUsers", adminUsers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi để debug
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách admin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // API gửi yêu cầu mở khóa điểm
    @PostMapping("/send-unlock-request")
    public ResponseEntity<Map<String, Object>> sendUnlockRequest(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();

        try {
            String adminEmail = (String) requestData.get("adminEmail");
            // Xử lý teacherId an toàn hơn
            Integer teacherId;
            try {
                if (requestData.get("teacherId") instanceof Integer) {
                    teacherId = (Integer) requestData.get("teacherId");
                } else {
                    teacherId = Integer.parseInt(requestData.get("teacherId").toString());
                }
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "teacherId không hợp lệ");
                return ResponseEntity.badRequest().body(response);
            }

            String schoolYearInfo = (String) requestData.get("schoolYearInfo");
            String department = (String) requestData.get("department");
            String major = (String) requestData.get("major");
            String subjectName = (String) requestData.get("subject");
            String classOrStudent = (String) requestData.get("classOrStudent");
            String reason = (String) requestData.get("reason");

            // Kiểm tra email không rỗng
            if (adminEmail == null || adminEmail.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email quản trị viên không được để trống");
                return ResponseEntity.badRequest().body(response);
            }

            // Lấy thông tin giảng viên
            Teacher teacher = teacherService.getTeacherById(teacherId);

            if (teacher == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin giảng viên");
                return ResponseEntity.badRequest().body(response);
            }

            // Tạo nội dung email
            String subject = "Yêu cầu mở khóa điểm";
            String body = String.format(
                    "Kính gửi Quản trị viên,\n\n"
                    + "Tôi là %s,\n"
                    + "Năm học/Học kỳ: %s\n"
                    + "Khoa: %s\n"
                    + "Ngành: %s\n"
                    + "Môn học: %s\n"
                    + "Lớp/Sinh viên cần mở khóa: %s\n"
                    + "Lý do yêu cầu mở khóa: %s\n\n"
                    + "Xin cảm ơn.",
                    teacher.getTeacherName(), schoolYearInfo, department, major, subjectName, classOrStudent, reason
            );

            // Gửi email
            boolean emailSent = emailService.sendEmail(adminEmail, subject, body);

            if (emailSent) {
                response.put("success", true);
                response.put("message", "Yêu cầu mở khóa đã được gửi thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không thể gửi email yêu cầu");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi gửi yêu cầu: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
