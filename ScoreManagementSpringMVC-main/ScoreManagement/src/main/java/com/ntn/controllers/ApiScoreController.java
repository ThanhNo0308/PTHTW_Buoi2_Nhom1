package com.ntn.controllers;

import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Class;
import com.ntn.pojo.Student;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.Typescore;
import com.ntn.service.ClassService;
import com.ntn.service.EmailService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
import com.ntn.service.StudentSubjectTeacherService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TeacherService;
import com.ntn.service.TypeScoreService;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/scores")
public class ApiScoreController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private TypeScoreService typeScoreService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentSubjectTeacherService studentSubjectTeacherService;

    /**
     * Lấy danh sách loại điểm
     */
    @GetMapping("/score-types/list")
    public ResponseEntity<List<String>> getScoreTypeList() {
        try {
            List<String> types = typeScoreService.getAllScoreTypes().stream()
                    .map(Typescore::getScoreType)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    /**
     * Lấy danh sách loại điểm theo lớp
     */
    @GetMapping("/score-types/by-class")
    public ResponseEntity<List<String>> getScoreTypesByClass(
            @RequestParam("classId") Integer classId,
            @RequestParam("subjectTeacherId") Integer subjectTeacherId,
            @RequestParam("schoolYearId") Integer schoolYearId) {

        try {
            // Lấy các loại điểm cơ bản
            Set<String> types = new HashSet<>();
            types.add("Giữa kỳ");
            types.add("Cuối kỳ");

            // Lấy các loại điểm đã được sử dụng cho lớp này
            List<Score> scores = scoreService.getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(
                    subjectTeacherId, classId, schoolYearId);

            for (Score score : scores) {
                if (score.getScoreType() != null) {
                    types.add(score.getScoreType().getScoreType());
                }
            }

            return ResponseEntity.ok(new ArrayList<>(types));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    /**
     * Lấy trọng số điểm cho các loại điểm
     */
    @GetMapping("/score-types/weights")
    public ResponseEntity<Map<String, Object>> getScoreTypeWeights(
            @RequestParam("classId") Integer classId,
            @RequestParam("subjectTeacherId") Integer subjectTeacherId,
            @RequestParam("schoolYearId") Integer schoolYearId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Lấy danh sách loại điểm
            List<String> scoreTypes = typeScoreService.getScoreTypesByClass(classId, subjectTeacherId, schoolYearId);

            // Đảm bảo luôn có giữa kỳ và cuối kỳ
            if (!scoreTypes.contains("Giữa kỳ")) {
                scoreTypes.add("Giữa kỳ");
            }
            if (!scoreTypes.contains("Cuối kỳ")) {
                scoreTypes.add("Cuối kỳ");
            }

            // Tạo map lưu trọng số
            Map<String, Double> weights = new HashMap<>();
            for (String scoreType : scoreTypes) {
                Float weight = scoreService.getScoreWeight(classId, subjectTeacherId, schoolYearId, scoreType);
                if (weight != null) {
                    weights.put(scoreType, weight.doubleValue());
                }
            }

            response.put("success", true);
            response.put("weights", weights);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Thêm loại điểm mới
     */
    @PostMapping("/score-types/add")
    public ResponseEntity<Map<String, Object>> addScoreType(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String scoreType = payload.get("scoreType");

        try {
            String weightStr = payload.get("weight");
            Float weight = (weightStr != null && !weightStr.isEmpty()) ? Float.parseFloat(weightStr) : 0f;

            Integer classId = Integer.parseInt(payload.get("classId"));
            Integer subjectTeacherId = Integer.parseInt(payload.get("subjectTeacherId"));
            Integer schoolYearId = Integer.parseInt(payload.get("schoolYearId"));

            // 1. Lưu vào bảng typescore
            Typescore typescore = typeScoreService.getScoreTypeByName(scoreType);
            if (typescore == null) {
                typescore = new Typescore(scoreType);
                typeScoreService.addScoreType(typescore);
            }

            // 2. Lưu vào bảng classscoretypes với thông tin lớp học
            boolean success = typeScoreService.addScoreTypeToClass(classId, subjectTeacherId, schoolYearId,
                    scoreType, weight);

            response.put("success", success);
            response.put("message", success ? "Đã thêm loại điểm thành công" : "Không thể thêm loại điểm");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xóa loại điểm
     */
    @PostMapping("/score-types/remove")
    public ResponseEntity<Map<String, Object>> removeScoreType(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            String scoreType = (String) payload.get("scoreType");
            Integer classId = Integer.parseInt(payload.get("classId").toString());
            Integer subjectTeacherId = Integer.parseInt(payload.get("subjectTeacherId").toString());
            Integer schoolYearId = Integer.parseInt(payload.get("schoolYearId").toString());

            boolean success = typeScoreService.removeScoreTypeFromClass(classId, subjectTeacherId, schoolYearId, scoreType);

            response.put("success", success);
            response.put("message", success ? "Loại điểm và các điểm liên quan đã được xóa thành công" : "Không thể xóa loại điểm");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cấu hình trọng số điểm
     */
    @PostMapping("/classes/{classId}/scores/configure-weights")
    public ResponseEntity<Map<String, Object>> configureWeights(
            @PathVariable("classId") Integer classId,
            @RequestParam("subjectTeacherId") Integer subjectTeacherId,
            @RequestParam("schoolYearId") Integer schoolYearId,
            @RequestBody Map<String, Double> weights) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate total weight = 100%
            double totalWeight = weights.values().stream().mapToDouble(w -> w).sum();
            if (Math.abs(totalWeight - 1.0) > 0.01) {
                response.put("success", false);
                response.put("message", "Tổng trọng số phải bằng 100%. Hiện tại: "
                        + Math.round(totalWeight * 100) + "%");
                return ResponseEntity.badRequest().body(response);
            }

            boolean success = typeScoreService.updateScoreTypeWeights(classId, subjectTeacherId, schoolYearId, weights);

            response.put("success", success);
            response.put("message", success ? "Cấu hình trọng số đã được lưu thành công" : "Có lỗi khi lưu cấu hình trọng số");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Khóa/mở khóa điểm của sinh viên
     */
    @PostMapping("/lock")
    public ResponseEntity<Map<String, Object>> lockScore(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer studentId = Integer.parseInt(payload.get("studentId").toString());
            Integer subjectTeacherId = Integer.parseInt(payload.get("subjectTeacherId").toString());
            Integer schoolYearId = Integer.parseInt(payload.get("schoolYearId").toString());
            Boolean lock = Boolean.valueOf(payload.get("lock").toString());

            // Lấy điểm của sinh viên
            List<Score> scores = scoreService.getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(
                    subjectTeacherId, schoolYearId, studentId);

            int updatedCount = 0;
            for (Score score : scores) {
                if (score != null && score.getId() != null) {
                    // Cập nhật trạng thái khóa
                    score.setIsLocked(lock);
                    score.setIsDraft(!lock); // Set draft ngược lại với lock

                    boolean success = scoreService.saveScore(score);
                    if (success) {
                        updatedCount++;
                    }
                }
            }

            response.put("success", updatedCount > 0);
            response.put("message", updatedCount > 0
                    ? "Đã cập nhật " + updatedCount + " điểm"
                    : "Không tìm thấy điểm nào để cập nhật");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Khóa/mở khóa tất cả điểm
     */
    @PostMapping("/lock-all")
    public ResponseEntity<Map<String, Object>> lockAllScores(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer classId = Integer.parseInt(payload.get("classId").toString());
            Integer subjectTeacherId = Integer.parseInt(payload.get("subjectTeacherId").toString());
            Integer schoolYearId = Integer.parseInt(payload.get("schoolYearId").toString());
            Boolean lock = Boolean.valueOf(payload.get("lock").toString());

            // Lấy danh sách sinh viên trong lớp
            List<Student> students = studentService.getStudentByClassId(classId);

            int totalUpdated = 0;
            int totalScores = 0;

            for (Student student : students) {
                // Lấy điểm của sinh viên
                List<Score> scores = scoreService.getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(
                        subjectTeacherId, schoolYearId, student.getId());

                totalScores += scores.size();

                for (Score score : scores) {
                    if (score != null && score.getId() != null) {
                        score.setIsLocked(lock);
                        score.setIsDraft(!lock); // Cập nhật ngược lại với isLocked

                        if (scoreService.saveScore(score)) {
                            totalUpdated++;
                        }
                    }
                }
            }

            response.put("success", totalUpdated > 0);
            response.put("message", "Đã cập nhật " + totalUpdated + "/" + totalScores + " điểm");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lưu điểm
     */
    @PostMapping("/save-scores")
    public ResponseEntity<Map<String, Object>> saveScores(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer subjectTeacherId = Integer.parseInt(requestData.get("subjectTeacherId").toString());
            Integer schoolYearId = Integer.parseInt(requestData.get("schoolYearId").toString());
            Boolean locked = Boolean.valueOf(requestData.get("locked").toString());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> scoresData = (List<Map<String, Object>>) requestData.get("scores");

            List<Score> scoresToSave = new ArrayList<>();

            for (Map<String, Object> scoreData : scoresData) {
                Integer studentId = Integer.parseInt(scoreData.get("studentId").toString());
                String scoreType = (String) scoreData.get("scoreType");
                Float scoreValue = Float.parseFloat(scoreData.get("scoreValue").toString());

                // Kiểm tra giá trị điểm hợp lệ
                if (scoreValue < 0 || scoreValue > 10) {
                    response.put("success", false);
                    response.put("message", "Điểm phải nằm trong khoảng từ 0 đến 10");
                    return ResponseEntity.badRequest().body(response);
                }

                Score score;

                // Kiểm tra ID để xác định đây là cập nhật hay tạo mới
                if (scoreData.containsKey("id") && scoreData.get("id") != null) {
                    Integer scoreId = Integer.parseInt(scoreData.get("id").toString());
                    score = scoreService.getScoreById(scoreId);

                    if (score == null) {
                        // Không tìm thấy điểm => tạo mới
                        score = new Score();
                    }
                } else {
                    // Tạo mới điểm
                    score = new Score();
                }

                // Thiết lập các thuộc tính cho điểm
                if (score.getStudentID() == null) {
                    Student student = studentService.getStudentById(studentId);
                    score.setStudentID(student);
                }

                if (score.getSubjectTeacherID() == null) {
                    score.setSubjectTeacherID(subjectTeacherService.getSubjectTeacherById(subjectTeacherId));
                }

                if (score.getSchoolYearId() == null) {
                    score.setSchoolYearId(schoolYearService.getSchoolYearById(schoolYearId));
                }

                // Thiết lập loại điểm
                Typescore type = typeScoreService.getScoreTypeByName(scoreType);
                if (type == null) {
                    type = new Typescore(scoreType);
                    typeScoreService.addScoreType(type);
                }
                score.setScoreType(type);

                // Thiết lập giá trị và trạng thái
                score.setScoreValue(scoreValue);
                score.setIsLocked(locked);
                score.setIsDraft(!locked);

                scoresToSave.add(score);
            }

            if (scoresToSave.isEmpty()) {
                response.put("success", true);
                response.put("message", "Không có điểm nào để lưu");
                return ResponseEntity.ok(response);
            }

            boolean success = scoreService.saveScores(scoresToSave);

            response.put("success", success);
            response.put("message", success
                    ? (locked ? "Lưu điểm chính thức thành công!" : "Lưu điểm nháp thành công!")
                    : "Có lỗi khi lưu điểm");

            if (success) {
                // Trả về danh sách điểm đã được lưu với ID mới
                List<Map<String, Object>> savedScores = new ArrayList<>();
                for (Score score : scoresToSave) {
                    Map<String, Object> savedScore = new HashMap<>();
                    savedScore.put("id", score.getId());
                    savedScore.put("studentId", score.getStudentID().getId());
                    savedScore.put("scoreType", score.getScoreType().getScoreType());
                    savedScore.put("scoreValue", score.getScoreValue());
                    savedScore.put("isLocked", score.getIsLocked());
                    savedScore.put("isDraft", score.getIsDraft());
                    savedScores.add(savedScore);
                }
                response.put("scores", savedScores);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Gửi thông báo điểm qua email
     */
    @PostMapping("/send-score-notification")
    public ResponseEntity<Map<String, Object>> sendScoreNotification(
            @RequestParam("studentId") int studentId,
            @RequestParam("subjectName") String subjectName) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean success = emailService.sendScoreNotification(studentId, subjectName);

            response.put("success", success);
            response.put("message", success ? "Đã gửi thông báo điểm" : "Không thể gửi thông báo");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/available-school-years")
    public ResponseEntity<Map<String, Object>> getAvailableSchoolYears(
            @RequestParam("subjectTeacherId") Integer subjectTeacherId,
            @RequestParam("classId") Integer classId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Log để debug
            System.out.println("Fetching school years for subjectTeacherId=" + subjectTeacherId + ", classId=" + classId);

            // Lấy trực tiếp SubjectTeacher từ ID
            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);

            if (subjectTeacher == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin phân công giảng dạy");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Kiểm tra xem subjectTeacher có đúng classId không
            if (subjectTeacher.getClassId() == null || subjectTeacher.getClassId().getId() != classId) {
                System.out.println("Warning: ClassId mismatch! SubjectTeacher has classId="
                        + (subjectTeacher.getClassId() != null ? subjectTeacher.getClassId().getId() : "null")
                        + " but requested classId=" + classId);
            }

            // Lấy ra schoolYear từ subjectTeacher
            List<Schoolyear> schoolYears = new ArrayList<>();
            if (subjectTeacher.getSchoolYearId() != null) {
                schoolYears.add(subjectTeacher.getSchoolYearId());
            }

            // Nếu không tìm thấy schoolYear trực tiếp, lấy các năm học được phân công cho giảng viên và lớp này
            if (schoolYears.isEmpty()) {
                List<Subjectteacher> relatedAssignments = subjectTeacherService
                        .getSubjectTeachersBySubjectIdAndClassId(
                                subjectTeacher.getSubjectId().getId(),
                                classId);

                Set<Integer> uniqueSchoolYearIds = new HashSet<>();

                for (Subjectteacher st : relatedAssignments) {
                    if (st.getSchoolYearId() != null && !uniqueSchoolYearIds.contains(st.getSchoolYearId().getId())) {
                        uniqueSchoolYearIds.add(st.getSchoolYearId().getId());
                        schoolYears.add(st.getSchoolYearId());
                    }
                }
            }

            response.put("success", true);
            response.put("schoolYears", schoolYears);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách năm học: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/students/search")
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

            response.put("success", true);
            response.put("students", students);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi tìm kiếm sinh viên: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

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

            // Lấy danh sách lớp được phân công
            List<Class> assignedClasses = classService.getClassesByTeacher(teacher.getId());

            // Lấy danh sách sinh viên từ các lớp được phân công
            Set<Student> allAssignedStudents = new HashSet<>();
            for (Class cls : assignedClasses) {
                List<Student> studentsInClass = studentService.getStudentByClassId(cls.getId());
                allAssignedStudents.addAll(studentsInClass);
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

    @GetMapping("/classes/by-subject")
    public ResponseEntity<Map<String, Object>> getClassesBySubject(
            @RequestParam("subjectId") Integer subjectId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Lấy tất cả Subjectteacher có subjectId trùng khớp
            List<Subjectteacher> subjectTeachers = subjectTeacherService.getSubjectTeachersBySubjectId(subjectId);

            // Lấy danh sách lớp từ các Subjectteacher
            List<Map<String, Object>> classes = new ArrayList<>();
            Set<Integer> uniqueClassIds = new HashSet<>();

            for (Subjectteacher st : subjectTeachers) {
                if (st.getClassId() != null && !uniqueClassIds.contains(st.getClassId().getId())) {
                    uniqueClassIds.add(st.getClassId().getId());

                    // Convert Class entity to map để trả về
                    Map<String, Object> classInfo = new HashMap<>();
                    classInfo.put("id", st.getClassId().getId());
                    classInfo.put("className", st.getClassId().getClassName());

                    classes.add(classInfo);
                }
            }

            response.put("success", true);
            response.put("classes", classes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy danh sách lớp: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API xem chi tiết sinh viên
     */
    @GetMapping("/students/{studentCode}/detail")
    public ResponseEntity<Map<String, Object>> getStudentDetail(@PathVariable String studentCode) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Student> student = studentService.findByStudentId(studentCode);

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

    /**
     * API xem điểm của sinh viên
     */
    @GetMapping("/students/{studentCode}/scores")
    public ResponseEntity<Map<String, Object>> getStudentScores(
            @PathVariable String studentCode,
            @RequestParam(value = "schoolYearId", required = false) Integer schoolYearId) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Student> studentOpt = studentService.findByStudentId(studentCode);

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

    /**
     * API lấy dữ liệu cho form nhập điểm từ file
     */
    @GetMapping("/import-scores-form")
    public ResponseEntity<Map<String, Object>> getImportScoresFormData(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = authentication.getName();
            Teacher teacher = teacherService.getTeacherByUsername(username);

            if (teacher == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin giảng viên");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            List<Subjectteacher> subjectTeachers = subjectTeacherService.getSubjectTeachersByTeacherId(teacher.getId());
            List<Schoolyear> schoolYears = schoolYearService.getAllSchoolYears();
            List<Class> classes = classService.getClassesByTeacher(teacher.getId());

            response.put("success", true);
            response.put("subjectTeachers", subjectTeachers);
            response.put("schoolYears", schoolYears);
            response.put("classes", classes);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API tải file mẫu để nhập điểm
     */
    @GetMapping("/scores/template")
    public ResponseEntity<Map<String, Object>> getScoreTemplate() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Tạo file mẫu CSV
            String csvContent = "MSSV,Họ tên,Điểm bổ sung 1,Điểm bổ sung 2,Điểm bổ sung 3,Giữa kỳ,Cuối kỳ\n"
                    + "SV001,Nguyễn Văn A,8.5,9.0,7.5,8.0,9.5\n"
                    + "SV002,Trần Thị B,7.0,8.0,6.5,7.0,8.5\n";

            // Encode the CSV content as Base64
            String base64Content = Base64.getEncoder().encodeToString(csvContent.getBytes(StandardCharsets.UTF_8));

            response.put("success", true);
            response.put("fileContent", base64Content);
            response.put("fileName", "score_template.csv");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API import điểm từ file CSV
     */
    @PostMapping("/import-scores")
    public ResponseEntity<Map<String, Object>> importScores(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subjectTeacherId") int subjectTeacherId,
            @RequestParam("classId") int classId,
            @RequestParam("schoolYearId") int schoolYearId,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            String username = authentication.getName();
            Teacher teacher = teacherService.getTeacherByUsername(username);

            if (teacher == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin giảng viên");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Sử dụng phương thức findByIdClassIdAndSchoolYearId để tìm subjectTeacher chính xác
            Subjectteacher subjectTeacher = subjectTeacherService.findByIdClassIdAndSchoolYearId(
                    subjectTeacherId, classId, schoolYearId);

            if (subjectTeacher == null) {
                // Nếu không tìm thấy chính xác theo cả 3 tiêu chí, thử tìm theo ID
                subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);

                // Kiểm tra và log cảnh báo nếu không khớp với classId và schoolYearId
                if (subjectTeacher != null) {
                    // Kiểm tra xem giáo viên có đúng không
                    if (!subjectTeacher.getTeacherId().getId().equals(teacher.getId())) {
                        response.put("success", false);
                        response.put("message", "Không có quyền nhập điểm cho môn học này");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                    }
                } else {
                    response.put("success", false);
                    response.put("message", "Không tìm thấy thông tin phân công giảng dạy");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            }

            List<Student> studentsInClass = studentService.getStudentByClassId(classId);

            // Tạo map để tra cứu nhanh với MSSV là key
            Map<String, Student> studentCodeMap = new HashMap<>();
            for (Student student : studentsInClass) {
                studentCodeMap.put(student.getStudentCode(), student);
            }

            // Kiểm tra sinh viên trong file CSV
            List<Map<String, String>> invalidStudents = new ArrayList<>();

            // Kiểm tra xem file CSV có các cột điểm phù hợp với loại điểm trong hệ thống hay không
            try (CSVParser parser = new CSVParser(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                    CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                // Lấy tên các cột trong file CSV
                List<String> headerNames = parser.getHeaderNames();

                for (CSVRecord record : parser) {
                    String studentCode;
                    try {
                        studentCode = record.get("MSSV");
                    } catch (IllegalArgumentException e1) {
                        try {
                            studentCode = record.get("Mã SV");
                        } catch (IllegalArgumentException e2) {
                            System.err.println("Không tìm thấy cột MSSV hoặc Mã SV trong CSV");
                            continue;
                        }
                    }

                    // Bỏ qua nếu không có MSSV
                    if (studentCode == null || studentCode.isEmpty()) {
                        continue;
                    }

                    // Lấy họ tên từ CSV
                    String fullName = "";
                    try {
                        fullName = record.get("Họ tên");
                    } catch (IllegalArgumentException e) {
                        // Nếu không có cột Họ tên
                    }

                    // Kiểm tra sinh viên trong hệ thống
                    Student studentInSystem = studentCodeMap.get(studentCode);
                    if (studentInSystem == null) {
                        Map<String, String> invalidStudent = new HashMap<>();
                        invalidStudent.put("studentCode", studentCode);
                        invalidStudent.put("fullName", fullName);
                        invalidStudent.put("error", "Sinh viên không có trong danh sách lớp này");
                        invalidStudents.add(invalidStudent);
                    }
                }

                // Nếu có sinh viên không hợp lệ, trả về lỗi ngay
                if (!invalidStudents.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "File CSV chứa sinh viên không có trong danh sách lớp");
                    response.put("invalidStudents", invalidStudents);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                // Lấy các loại điểm được cấu hình trong hệ thống từ bảng classscoretypes
                List<String> configuredScoreTypes = typeScoreService.getScoreTypesByClass(classId, subjectTeacherId, schoolYearId);

                // Kiểm tra xem có cột điểm nào trong CSV không có trong hệ thống không
                List<String> missingScoreColumns = new ArrayList<>();
                for (String headerName : headerNames) {
                    if (!headerName.equals("MSSV") && !headerName.equals("Họ tên") && !headerName.equals("Mã SV")) {
                        boolean matched = false;
                        String normalizedHeader = normalizeString(headerName);

                        // Kiểm tra với loại điểm mặc định: Giữa kỳ và Cuối kỳ
                        if (normalizedHeader.equals(normalizeString("Giữa kỳ")) || normalizedHeader.equals(normalizeString("Cuối kỳ"))) {
                            matched = true;
                        } else {
                            // Kiểm tra với các loại điểm đã cấu hình trong hệ thống
                            for (String configType : configuredScoreTypes) {
                                if (normalizeString(configType).equals(normalizedHeader)) {
                                    matched = true;
                                    break;
                                }
                            }
                        }

                        if (!matched) {
                            missingScoreColumns.add(headerName);
                        }
                    }
                }

                // Nếu có cột điểm tùy chỉnh trong CSV nhưng chưa được cấu hình trong hệ thống
                if (!missingScoreColumns.isEmpty()) {
                    StringBuilder missingColumns = new StringBuilder();
                    for (String column : missingScoreColumns) {
                        missingColumns.append(column).append(", ");
                    }

                    if (missingColumns.length() > 2) {
                        missingColumns.setLength(missingColumns.length() - 2);
                    }

                    response.put("success", false);
                    response.put("message", "File CSV chứa các loại điểm chưa được cấu hình: " + missingColumns
                            + ". Vui lòng thêm các loại điểm này vào hệ thống trước khi import.");
                    response.put("missingScoreTypes", missingScoreColumns);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }

            // Nếu tất cả các điều kiện đều thoả mãn, tiến hành import điểm
            // Truyền thêm classId vào phương thức importScoresFromCsv
            boolean success = scoreService.importScoresFromCsv(file, subjectTeacherId, classId, schoolYearId);

            if (success) {
                response.put("success", true);
                response.put("message", "Import điểm thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không thể import điểm. Vui lòng kiểm tra lại file");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

// Hàm chuẩn hóa chuỗi cho việc so sánh không phân biệt hoa/thường và khoảng trắng
    private String normalizeString(String input) {
        if (input == null) {
            return "";
        }

        // Loại bỏ khoảng trắng thừa, chuyển thành chữ thường
        return input.trim().toLowerCase();
    }

    /**
     * API xuất điểm ra file CSV
     */
    @GetMapping("/classes/{classId}/export-csv")
    public ResponseEntity<Map<String, Object>> exportScoresToCsv(
            @PathVariable int classId,
            @RequestParam int subjectTeacherId,
            @RequestParam int schoolYearId,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Xác thực giảng viên
            String username = authentication.getName();
            Teacher teacher = teacherService.getTeacherByUsername(username);

            if (teacher == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin giảng viên");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra giảng viên có quyền xuất điểm lớp này không
            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
            if (subjectTeacher == null || !subjectTeacher.getTeacherId().getId().equals(teacher.getId())) {
                response.put("success", false);
                response.put("message", "Không có quyền xuất điểm cho lớp học này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Xuất điểm ra CSV
            byte[] csvContent = scoreService.exportScoresToCsv(subjectTeacherId, classId, schoolYearId);

            // Convert to Base64
            String base64Content = Base64.getEncoder().encodeToString(csvContent);

            response.put("success", true);
            response.put("fileContent", base64Content);
            response.put("fileName", "scores_class_" + classId + ".csv");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi xuất điểm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API xuất điểm ra file PDF
     */
    @GetMapping("/classes/{classId}/export-pdf")
    public ResponseEntity<Map<String, Object>> exportScoresToPdf(
            @PathVariable int classId,
            @RequestParam int subjectTeacherId,
            @RequestParam int schoolYearId,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Xác thực giảng viên
            String username = authentication.getName();
            Teacher teacher = teacherService.getTeacherByUsername(username);

            if (teacher == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin giảng viên");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra giảng viên có quyền xuất điểm lớp này không
            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
            if (subjectTeacher == null || !subjectTeacher.getTeacherId().getId().equals(teacher.getId())) {
                response.put("success", false);
                response.put("message", "Không có quyền xuất điểm cho lớp học này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Xuất điểm ra PDF
            byte[] pdfContent = scoreService.exportScoresToPdf(subjectTeacherId, classId, schoolYearId);

            // Convert to Base64
            String base64Content = Base64.getEncoder().encodeToString(pdfContent);

            response.put("success", true);
            response.put("fileContent", base64Content);
            response.put("fileName", "scores_class_" + classId + ".pdf");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi xuất điểm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
