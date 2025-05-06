/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.pojo.Score;
import com.ntn.pojo.Typescore;
import com.ntn.service.ClassScoreTypeService;
import com.ntn.service.ScoreService;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/typescores")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ApiTypeScoreController {

    @Autowired
    private TypeScoreService typeScoreService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ClassScoreTypeService classScoreTypeService;

    // Lấy danh sách loại điểm
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

    // Lấy danh sách loại điểm theo lớp
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

    // Lấy trọng số điểm cho các loại điểm
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

    // Thêm loại điểm mới
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
            boolean success = classScoreTypeService.addScoreTypeToClass(classId, subjectTeacherId, schoolYearId,
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

    // Xóa loại điểm
    @PostMapping("/score-types/remove")
    public ResponseEntity<Map<String, Object>> removeScoreType(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            String scoreType = (String) payload.get("scoreType");
            Integer classId = Integer.parseInt(payload.get("classId").toString());
            Integer subjectTeacherId = Integer.parseInt(payload.get("subjectTeacherId").toString());
            Integer schoolYearId = Integer.parseInt(payload.get("schoolYearId").toString());

            boolean success = classScoreTypeService.removeScoreTypeFromClass(classId, subjectTeacherId, schoolYearId, scoreType);

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
}
