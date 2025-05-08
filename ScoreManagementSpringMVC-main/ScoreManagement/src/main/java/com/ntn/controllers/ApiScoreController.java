package com.ntn.controllers;

import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Class;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.Typescore;
import com.ntn.service.ClassScoreTypeService;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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
    private ClassScoreTypeService classScoreTypeService;

    @Autowired
    private StudentSubjectTeacherService studentSubjectTeacherService;

    // Cấu hình trọng số điểm
    @PostMapping("/classes/{classId}/scores/configure-weights")
    public ResponseEntity<Map<String, Object>> configureWeights(
            @PathVariable("classId") Integer classId,
            @RequestParam("subjectTeacherId") Integer subjectTeacherId,
            @RequestParam("schoolYearId") Integer schoolYearId,
            @RequestBody Map<String, Float> weights) {

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

            boolean success = classScoreTypeService.updateScoreTypeWeights(classId, subjectTeacherId, schoolYearId, weights);

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

    // Lưu điểm nháp
    @PostMapping("/save-scores-draft")
    public ResponseEntity<Map<String, Object>> saveScoresDraft(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer subjectTeacherId = Integer.parseInt(requestData.get("subjectTeacherId").toString());
            Integer schoolYearId = Integer.parseInt(requestData.get("schoolYearId").toString());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> scoresData = (List<Map<String, Object>>) requestData.get("scores");

            List<Score> scoresToSave = new ArrayList<>();

            for (Map<String, Object> scoreData : scoresData) {
                Integer studentId = Integer.parseInt(scoreData.get("studentId").toString());
                String scoreType = (String) scoreData.get("scoreType");
                Float scoreValue = Float.parseFloat(scoreData.get("scoreValue").toString());

                // Lấy trạng thái khóa hiện tại
                Boolean currentlyLocked = scoreData.containsKey("isLocked")
                        ? Boolean.valueOf(scoreData.get("isLocked").toString()) : false;

                // Kiểm tra giá trị điểm hợp lệ
                if (scoreValue < 0 || scoreValue > 10) {
                    response.put("success", false);
                    response.put("message", "Điểm phải nằm trong khoảng từ 0 đến 10");
                    return ResponseEntity.badRequest().body(response);
                }

                Score score;
                if (scoreData.containsKey("id") && scoreData.get("id") != null
                        && !scoreData.get("id").toString().equals("null")) {
                    Integer scoreId = Integer.parseInt(scoreData.get("id").toString());
                    score = scoreService.getScoreById(scoreId);

                    if (score == null) {
                        score = new Score();
                    }
                } else {
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

                // Luôn giữ nguyên trạng thái khóa nếu đã khóa
                if (currentlyLocked) {
                    score.setIsDraft(false);
                    score.setIsLocked(true);
                } else {
                    score.setIsDraft(true); // Lưu nháp
                    score.setIsLocked(false); // Không khóa
                }

                scoresToSave.add(score);
            }

            if (scoresToSave.isEmpty()) {
                response.put("success", true);
                response.put("message", "Không có điểm nào để lưu");
                return ResponseEntity.ok(response);
            }

            // Sử dụng phương thức mới để lưu nháp (không gửi email)
            boolean success = scoreService.saveScoresDraft(scoresToSave);

            // Phần xử lý kết quả
            response.put("success", success);
            response.put("message", success ? "Lưu điểm nháp thành công!" : "Có lỗi khi lưu điểm nháp");

            if (success) {
                // Trả về thông tin điểm đã lưu
                List<Map<String, Object>> savedScores = new ArrayList<>();
                for (Score score : scoresToSave) {
                    Map<String, Object> savedScore = new HashMap<>();
                    savedScore.put("id", score.getId());
                    savedScore.put("studentId", score.getStudentID().getId());
                    savedScore.put("scoreType", score.getScoreType().getScoreType());
                    savedScore.put("scoreValue", score.getScoreValue());
                    savedScore.put("isLocked", score.getIsLocked());
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

    // Lưu điểm chính thức
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
            Set<Integer> studentIdsToNotify = new HashSet<>(); // Chỉ thông báo cho những sinh viên mới bị khóa

            for (Map<String, Object> scoreData : scoresData) {
                Integer studentId = Integer.parseInt(scoreData.get("studentId").toString());
                String scoreType = (String) scoreData.get("scoreType");
                Float scoreValue = Float.parseFloat(scoreData.get("scoreValue").toString());

                // Lấy trạng thái khóa hiện tại
                Boolean currentlyLocked = scoreData.containsKey("isLocked")
                        ? Boolean.valueOf(scoreData.get("isLocked").toString()) : false;

                // Kiểm tra giá trị điểm hợp lệ
                if (scoreValue < 0 || scoreValue > 10) {
                    response.put("success", false);
                    response.put("message", "Điểm phải nằm trong khoảng từ 0 đến 10");
                    return ResponseEntity.badRequest().body(response);
                }

                Score score;
                // Kiểm tra ID để xác định đây là cập nhật hay tạo mới
                if (scoreData.containsKey("id") && scoreData.get("id") != null
                        && !scoreData.get("id").toString().equals("null")) {
                    Integer scoreId = Integer.parseInt(scoreData.get("id").toString());
                    score = scoreService.getScoreById(scoreId);

                    if (score == null) {
                        score = new Score();
                    }
                } else {
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

                if (locked) {
                    // Nếu đang lưu chính thức, set isDraft = false và isLocked = true
                    score.setIsDraft(false);
                    score.setIsLocked(true);

                    // Chỉ gửi email thông báo cho sinh viên mới bị khóa điểm
                    if (!currentlyLocked) {
                        studentIdsToNotify.add(studentId);
                    }
                } else {
                    // Nếu đang lưu nháp, CHỈ set isDraft = true nếu điểm chưa bị khóa
                    if (!currentlyLocked) {
                        score.setIsDraft(true);
                        score.setIsLocked(false);
                    } else {
                        // Giữ nguyên trạng thái đã khóa nếu điểm đã bị khóa trước đó
                        score.setIsDraft(false);
                        score.setIsLocked(true);
                    }
                }

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

            // Thông báo email chỉ cho sinh viên mới bị khóa điểm
            if (success && locked && !studentIdsToNotify.isEmpty()) {
                Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
                Schoolyear schoolYear = schoolYearService.getSchoolYearById(schoolYearId);

                if (subjectTeacher != null && schoolYear != null) {
                    String subjectName = subjectTeacher.getSubjectId().getSubjectName();
                    String teacherName = subjectTeacher.getTeacherId().getTeacherName();
                    String schoolYearStr = schoolYear.getNameYear() + " " + schoolYear.getSemesterName();
                    String major = "";

                    if (subjectTeacher.getClassId() != null && subjectTeacher.getClassId().getMajorId() != null) {
                        major = subjectTeacher.getClassId().getMajorId().getMajorName();
                    }

                    // Chuyển Set sang List
                    List<Integer> studentIdsList = new ArrayList<>(studentIdsToNotify);

                    // Gửi thông báo email cho những sinh viên mới bị khóa điểm
                    emailService.sendScoreNotificationsToClass(
                            studentIdsList, subjectName, teacherName, schoolYearStr, major);

                    response.put("emailsTriggered", true);
                    response.put("emailCount", studentIdsToNotify.size());
                }
            }

            if (success) {
                // Trả về thông tin điểm đã lưu
                List<Map<String, Object>> savedScores = new ArrayList<>();
                for (Score score : scoresToSave) {
                    Map<String, Object> savedScore = new HashMap<>();
                    savedScore.put("id", score.getId());
                    savedScore.put("studentId", score.getStudentID().getId());
                    savedScore.put("scoreType", score.getScoreType().getScoreType());
                    savedScore.put("scoreValue", score.getScoreValue());
                    savedScore.put("isLocked", score.getIsLocked());
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

    // Lấy danh sách năm học-học kì khi import điểm
    @GetMapping("/available-school-years")
    public ResponseEntity<Map<String, Object>> getAvailableSchoolYears(
            @RequestParam("subjectTeacherId") Integer subjectTeacherId,
            @RequestParam("classId") Integer classId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Lấy trực tiếp SubjectTeacher từ ID
            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);

            if (subjectTeacher == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin phân công giảng dạy");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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

    // Lấy dữ liệu cho form nhập điểm từ file
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

    // Tải file mẫu để nhập điểm
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

    // Import điểm từ file CSV
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

                // Sau khi import điểm thành công, thêm dòng này
                Set<String> processedStudentCodes = new HashSet<>();

                // Trong quá trình đọc file CSV
                for (CSVRecord record : parser) {
                    String studentCode = record.get("MSSV");
                    Student student = studentService.getStudentByCode(studentCode);

                    if (student != null) {
                        processedStudentCodes.add(studentCode);

                        // Kiểm tra xem sinh viên đã đăng ký học chưa
                        boolean alreadyEnrolled = studentSubjectTeacherService.checkDuplicate(student.getId(), subjectTeacherId);

                        // Nếu chưa đăng ký, tự động thêm vào bảng Studentsubjectteacher
                        if (!alreadyEnrolled) {
                            studentSubjectTeacherService.addStudentToSubjectTeacher(
                                    student.getId(), subjectTeacherId, schoolYearId);
                        }
                    }
                }

            } catch (org.apache.commons.csv.CSVException csvEx) {
                // Xử lý lỗi định dạng CSV cụ thể
                String userFriendlyMessage = "File CSV không đúng định dạng. ";

                if (csvEx.getMessage().contains("Invalid character between encapsulated token and delimiter")) {
                    userFriendlyMessage += "Có ký tự không hợp lệ giữa dữ liệu được đặt trong dấu ngoặc kép và dấu phân cách. "
                            + "Vấn đề có thể ở dòng: " + extractLineNumber(csvEx.getMessage());
                    userFriendlyMessage += "\n\nGợi ý: "
                            + "\n1. Kiểm tra các ô chứa dấu phẩy hoặc dấu ngoặc kép trong Excel"
                            + "\n2. Đảm bảo khi lưu file CSV, các ô có chứa dấu phẩy được đặt trong dấu ngoặc kép"
                            + "\n3. Loại bỏ các ký tự đặc biệt không cần thiết trong dữ liệu"
                            + "\n4. Thử mở file bằng Notepad để kiểm tra định dạng";
                } else {
                    userFriendlyMessage += "Lỗi: " + csvEx.getMessage();
                }

                response.put("success", false);
                response.put("message", userFriendlyMessage);
                response.put("error", "csv_format_error");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Truyền thêm classId vào phương thức importScoresFromCsv
            Map<String, Object> importResult = scoreService.importScoresFromCsv(file, subjectTeacherId, classId, schoolYearId);

            response.putAll(importResult);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();

            // Cải thiện thông báo lỗi chung
            String errorMessage = "Lỗi khi import điểm: " + e.getMessage();

            // Kiểm tra nếu là lỗi CSV format
            if (e instanceof org.apache.commons.csv.CSVException
                    || (e.getMessage() != null && e.getMessage().contains("Invalid character"))) {

                errorMessage = "File CSV không đúng định dạng. Vui lòng kiểm tra lại file và đảm bảo: "
                        + "\n- File được lưu với mã UTF-8"
                        + "\n- Không có ký tự đặc biệt không hợp lệ"
                        + "\n- Dữ liệu chứa dấu phẩy đã được đặt trong dấu ngoặc kép"
                        + "\n- Định dạng CSV đúng chuẩn"
                        + "\n\nHãy thử tải file mẫu và làm theo đúng định dạng.";
            }

            response.put("success", false);
            response.put("message", "Lỗi khi import điểm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Xuất điểm ra file CSV
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

    // Xuất điểm ra file PDF
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

    private String extractLineNumber(String errorMessage) {
        if (errorMessage == null) {
            return "không xác định";
        }

        // Tìm số dòng từ thông báo lỗi
        if (errorMessage.contains("at line:")) {
            String[] parts = errorMessage.split("at line:");
            if (parts.length > 1) {
                String[] lineParts = parts[1].trim().split(",");
                if (lineParts.length > 0) {
                    return lineParts[0].trim();
                }
            }
        }

        return "không xác định";
    }

    // Hàm chuẩn hóa chuỗi cho việc so sánh không phân biệt hoa/thường và khoảng trắng
    private String normalizeString(String input) {
        if (input == null) {
            return "";
        }

        // Loại bỏ khoảng trắng thừa, chuyển thành chữ thường
        return input.trim().toLowerCase();
    }
}
