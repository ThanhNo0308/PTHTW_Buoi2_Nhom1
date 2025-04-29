package com.ntn.controllers;

import com.ntn.pojo.ListScoreDTO;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Typescore;
import com.ntn.service.ClassService;
import com.ntn.service.EmailService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TeacherService;
import com.ntn.service.TypeScoreService;

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
import org.springframework.web.bind.annotation.*;

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
}
