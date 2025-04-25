package com.ntn.controllers;

import com.ntn.pojo.ListScoreDTO;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Typescore;
import com.ntn.repository.TypeScoreRepository;
import com.ntn.service.ClassService;
import com.ntn.service.EmailService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/scores")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private TypeScoreService typeScoreService;

    // Thêm vào ScoreController.java
    @PostMapping("/classes/{classId}/scores/configure-weights")
    @ResponseBody
    public Map<String, Object> configureWeights(
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
                return response;
            }

            // Chỉ cần cập nhật trọng số trong bảng classscoretypes
            boolean success = typeScoreService.updateScoreTypeWeights(classId, subjectTeacherId, schoolYearId, weights);

            response.put("success", success);
            if (success) {
                response.put("message", "Cấu hình trọng số đã được lưu thành công");
            } else {
                response.put("message", "Có lỗi khi lưu cấu hình trọng số");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/score-types/remove")
    @ResponseBody
    public Map<String, Object> removeScoreType(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            String scoreType = (String) payload.get("scoreType");
            Integer classId = Integer.parseInt(payload.get("classId").toString());
            Integer subjectTeacherId = Integer.parseInt(payload.get("subjectTeacherId").toString());
            Integer schoolYearId = Integer.parseInt(payload.get("schoolYearId").toString());

            boolean success = typeScoreService.removeScoreTypeFromClass(classId, subjectTeacherId, schoolYearId, scoreType);

            response.put("success", success);
            if (success) {
                response.put("message", "Loại điểm đã được xóa thành công");
            } else {
                response.put("message", "Không thể xóa loại điểm");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/score-types/weights")
    @ResponseBody
    public Map<String, Object> getScoreTypeWeights(
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
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/score-types/list")
    @ResponseBody
    public List<String> getScoreTypeList() {
        try {
            return typeScoreService.getAllScoreTypes().stream()
                    .map(Typescore::getScoreType)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @GetMapping("/score-types/by-class")
    @ResponseBody
    public List<String> getScoreTypesByClass(
            @RequestParam("classId") Integer classId,
            @RequestParam("subjectTeacherId") Integer subjectTeacherId,
            @RequestParam("schoolYearId") Integer schoolYearId) {

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

        return new ArrayList<>(types);
    }

    /**
     * Hiển thị trang tìm kiếm điểm
     */
    @GetMapping("/search")
    public String searchScoresForm(Model model) {
        model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());
        return "scores/search";
    }

    /**
     * Tìm kiếm điểm theo mã sinh viên
     */
    @GetMapping("/search-by-student")
    public String searchByStudent(
            @RequestParam("studentCode") String studentCode,
            @RequestParam(value = "schoolYearId", required = false) Integer schoolYearId,
            Model model) {

        Student student = studentService.getStudentByStudentCode(studentCode);

        if (student == null) {
            model.addAttribute("errorMessage", "Không tìm thấy sinh viên với mã: " + studentCode);
            model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());
            return "scores/search";
        }

        List<Score> scores;
        if (schoolYearId != null) {
            scores = scoreService.getSubjectScoresByStudentCodeAndSchoolYear(studentCode, schoolYearId);
        } else {
            scores = scoreService.getSubjectScoresByStudentCode(studentCode);
        }

        model.addAttribute("student", student);
        model.addAttribute("scores", scores);
        model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());

        return "scores/results";
    }

    /**
     * Hiển thị form cập nhật điểm cho lớp học
     */
    @PreAuthorize("hasAuthority('Teacher') or hasAuthority('Admin')")
    @GetMapping("/class-scores/{classId}")
    public String classScoresForm(@PathVariable("classId") int classId, Model model) {
        model.addAttribute("classroom", classService.getClassById(classId));
        model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());
        return "scores/class-scores";
    }

    /**
     * Lấy danh sách điểm của lớp học
     */
    @PreAuthorize("hasAuthority('Teacher') or hasAuthority('Admin')")
    @GetMapping("/api/class-scores")
    @ResponseBody
    public ResponseEntity<ListScoreDTO> getClassScores(
            @RequestParam("classId") int classId,
            @RequestParam("subjectTeacherId") int subjectTeacherId,
            @RequestParam("schoolYearId") int schoolYearId) {

        List<Score> scores = scoreService.getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(
                subjectTeacherId, classId, schoolYearId);

        ListScoreDTO listScoreDTO = scoreService.createListScoreDTO(scores, subjectTeacherId, schoolYearId);

        if (listScoreDTO == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(listScoreDTO, HttpStatus.OK);
    }

    /**
     * Lưu điểm cho lớp học
     */
    @PreAuthorize("hasAuthority('Teacher') or hasAuthority('Admin')")
    @PostMapping("/api/save-scores")
    @ResponseBody
    public ResponseEntity<String> saveScores(@RequestBody ListScoreDTO listScoreDTO) {
        try {
            boolean success = scoreService.saveListScoreByListScoreDTO(listScoreDTO);

            if (success) {
                return new ResponseEntity<>("Lưu điểm thành công", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Không thể lưu điểm", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Thêm loại điểm mới
     */
    @PreAuthorize("hasAuthority('Teacher') or hasAuthority('Admin')")
    @PostMapping("/api/add-score-type")
    @ResponseBody
    public ResponseEntity<String> addScoreType(
            @RequestParam("typeName") String typeName,
            @RequestParam("subjectTeacherId") int subjectTeacherId) {

        try {
            boolean success = typeScoreService.addScoreType(typeName, subjectTeacherId);

            if (success) {
                return new ResponseEntity<>("Thêm loại điểm thành công", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Không thể thêm loại điểm. Đã đạt số lượng tối đa hoặc lỗi khác.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gửi thông báo điểm đến sinh viên
     */
    @PreAuthorize("hasAuthority('Teacher') or hasAuthority('Admin')")
    @PostMapping("/api/send-score-notification")
    @ResponseBody
    public ResponseEntity<String> sendScoreNotification(
            @RequestParam("studentId") int studentId,
            @RequestParam("subjectName") String subjectName) {

        try {
            boolean success = emailService.sendScoreNotification(studentId, subjectName);

            if (success) {
                return new ResponseEntity<>("Đã gửi thông báo điểm", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Không thể gửi thông báo", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/show-search")
    public String showSearchForm(Model model) {
        model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());
        return "scores/search";
    }

    @PostMapping("/score-types/add")
    @ResponseBody
    public Map<String, Object> addScoreType(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String scoreType = payload.get("scoreType");

        try {
            // Sửa lại dòng lỗi
            String weightStr = payload.get("weight");
            Float weight = (weightStr != null && !weightStr.isEmpty()) ? Float.parseFloat(weightStr) : 0f;

            // Lấy thông tin lớp, giáo viên, năm học
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

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/lock")
    @ResponseBody
    public Map<String, Object> lockScore(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer studentId = Integer.parseInt(payload.get("studentId").toString());
            Integer subjectTeacherId = Integer.parseInt(payload.get("subjectTeacherId").toString());
            Integer schoolYearId = Integer.parseInt(payload.get("schoolYearId").toString());
            Boolean lock = Boolean.valueOf(payload.get("lock").toString());

            // Get scores for the student
            List<Score> scores = scoreService.getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(
                    subjectTeacherId, schoolYearId, studentId);

            int updatedCount = 0;
            for (Score score : scores) {
                if (score != null && score.getId() != null) {
                    // Update lock status AND draft status
                    score.setIsLocked(lock);
                    score.setIsDraft(!lock); // Set draft opposite to lock status

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
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/lock-all")
    @ResponseBody
    public Map<String, Object> lockAllScores(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer classId = Integer.parseInt(payload.get("classId").toString());
            Integer subjectTeacherId = Integer.parseInt(payload.get("subjectTeacherId").toString());
            Integer schoolYearId = Integer.parseInt(payload.get("schoolYearId").toString());
            Boolean lock = Boolean.valueOf(payload.get("lock").toString());

            // Lấy danh sách sinh viên trong lớp
            List<Student> students = studentService.getStudentByClassId(classId);

            int totalUpdated = 0; // Đổi tên biến từ updatedCount thành totalUpdated
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
                            totalUpdated++; // Biến này đã đúng
                        }
                    }
                }
            }

            response.put("success", totalUpdated > 0); // Sửa từ updatedCount thành totalUpdated
            response.put("message", "Đã cập nhật " + totalUpdated + "/" + totalScores + " điểm"); // Sửa từ updatedCount thành totalUpdated

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }

        return response;
    }

}
