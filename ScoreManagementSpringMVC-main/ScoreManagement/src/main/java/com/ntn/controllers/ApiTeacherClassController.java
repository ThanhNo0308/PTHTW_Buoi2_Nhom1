package com.ntn.controllers;

import com.ntn.pojo.Class;
import com.ntn.pojo.Score;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subject;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import com.ntn.service.ClassService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TeacherService;
import com.ntn.service.TypeScoreService;
import com.ntn.service.UserService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
public class ApiTeacherClassController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ClassService classService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    @Autowired
    private TypeScoreService typeScoreService;

    @GetMapping("/classes")
    public ResponseEntity<?> getTeacherClasses(@RequestParam String username) {
        try {
            Teacher teacher = teacherService.getTeacherByUsername(username);

            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Không tìm thấy thông tin giáo viên"));
            }

            // Lấy danh sách các lớp mà giảng viên được phân công dạy
            Set<Class> assignedClasses = new HashSet<>();
            List<Subjectteacher> teacherAssignments = subjectTeacherService.getSubjectTeachersByTeacherId(teacher.getId());

            for (Subjectteacher assignment : teacherAssignments) {
                if (assignment.getClassId() != null) {
                    assignedClasses.add(assignment.getClassId());
                }
            }

            List<Class> classes = new ArrayList<>(assignedClasses);
            Map<Integer, Integer> studentCounts = new HashMap<>();

            // Tính số sinh viên cho mỗi lớp
            for (Class cls : classes) {
                int count = studentService.countStudentsByClassId(cls.getId());
                studentCounts.put(cls.getId(), count);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("classes", classes);
            response.put("studentCounts", studentCounts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/classes/{classId}")
    public ResponseEntity<?> getClassDetail(
            @PathVariable("classId") int classId,
            @RequestParam String username) {
        try {
            Teacher teacher = teacherService.getTeacherByUsername(username);

            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Không tìm thấy thông tin giáo viên"));
            }

            Class classroom = classService.getClassById(classId);
            if (classroom == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy lớp học"));
            }

            // Kiểm tra xem giáo viên có được phân công dạy lớp này không
            List<Subjectteacher> teacherClassAssignments = subjectTeacherService.getSubjectTeachersByTeacherIdAndClassId(teacher.getId(), classId);

            if (teacherClassAssignments.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Không có quyền truy cập lớp này"));
            }

            // Tính số lượng sinh viên trong lớp
            int studentCount = studentService.countStudentsByClassId(classroom.getId());

            // Danh sách sinh viên trong lớp
            List<Student> students = studentService.getStudentByClassId(classroom.getId());

            // Lấy năm học hiện tại
            int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();

            // Lấy các môn học mà giảng viên dạy trong lớp này
            List<Subjectteacher> assignedSubjects = subjectTeacherService.getSubjectTeachersByTeacherIdAndClassIdAndSchoolYearId(
                    teacher.getId(), classId, currentSchoolYearId);

            // Nếu không có môn học trong học kỳ hiện tại, lấy tất cả phân công
            if (assignedSubjects.isEmpty()) {
                assignedSubjects = subjectTeacherService.getSubjectTeachersByTeacherIdAndClassId(teacher.getId(), classId);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("classroom", classroom);
            response.put("teacher", teacher);
            response.put("studentCount", studentCount);
            response.put("students", students);
            response.put("subjects", assignedSubjects);
            response.put("schoolYears", schoolYearService.getAllSchoolYears());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @GetMapping("/classes/{classId}/scores")
    public ResponseEntity<?> getClassScores(
            @PathVariable("classId") int classId,
            @RequestParam("subjectTeacherId") int subjectTeacherId,
            @RequestParam("schoolYearId") int schoolYearId,
            @RequestParam String username) {
        try {
            Teacher teacher = teacherService.getTeacherByUsername(username);
            
            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Không tìm thấy thông tin giáo viên"));
            }

            // Lấy thông tin lớp, giáo viên, môn học và năm học
            Class classroom = classService.getClassById(classId);
            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
            
            if (subjectTeacher == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy thông tin phân công giảng dạy"));
            }
            
            Subject subject = subjectTeacher.getSubjectId();
            Schoolyear schoolYear = schoolYearService.getSchoolYearById(schoolYearId);
            
            // Kiểm tra quyền truy cập
            if (!subjectTeacher.getTeacherId().getId().equals(teacher.getId()) || 
                subjectTeacher.getClassId() == null || 
                !subjectTeacher.getClassId().getId().equals(classId) ||
                subjectTeacher.getSchoolYearId() == null || 
                !subjectTeacher.getSchoolYearId().getId().equals(schoolYearId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Không có quyền truy cập lớp học này"));
            }

            // Lấy danh sách sinh viên trong lớp
            List<Student> students = studentService.getStudentByClassId(classId);

            // Lấy danh sách loại điểm từ bảng classscoretypes
            List<String> scoreTypes = typeScoreService.getScoreTypesByClass(classId, subjectTeacherId, schoolYearId);

            // Đảm bảo luôn có loại điểm giữa kỳ và cuối kỳ
            if (!scoreTypes.contains("Giữa kỳ")) {
                scoreTypes.add("Giữa kỳ");
            }
            if (!scoreTypes.contains("Cuối kỳ")) {
                scoreTypes.add("Cuối kỳ");
            }

            // Sắp xếp lại: điểm tùy chỉnh trước, sau đó là giữa kỳ và cuối kỳ
            List<String> orderedScoreTypes = new ArrayList<>();

            // Thêm các loại điểm tùy chỉnh trước
            for (String type : scoreTypes) {
                if (!type.equals("Giữa kỳ") && !type.equals("Cuối kỳ")) {
                    orderedScoreTypes.add(type);
                }
            }

            // Thêm giữa kỳ và cuối kỳ vào cuối
            if (scoreTypes.contains("Giữa kỳ")) {
                orderedScoreTypes.add("Giữa kỳ");
            }
            if (scoreTypes.contains("Cuối kỳ")) {
                orderedScoreTypes.add("Cuối kỳ");
            }

            // Lấy điểm hiện có cho từng sinh viên
            Map<Integer, Map<String, Score>> studentScores = new HashMap<>();
            for (Student student : students) {
                Map<String, Score> scoreMap = new HashMap<>();
                studentScores.put(student.getId(), scoreMap);

                List<Score> scores = scoreService.getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(
                        subjectTeacherId, schoolYearId, student.getId());

                // Lưu điểm vào map theo loại điểm
                for (Score score : scores) {
                    if (score.getScoreType() != null) {
                        scoreMap.put(score.getScoreType().getScoreType(), score);
                    }
                }
            }

            // Lấy trọng số cho các loại điểm
            Map<String, Double> scoreWeights = new HashMap<>();
            for (String scoreType : scoreTypes) {
                Float weight = scoreService.getScoreWeight(classId, subjectTeacherId, schoolYearId, scoreType);
                if (weight != null) {
                    scoreWeights.put(scoreType, weight.doubleValue());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("classroom", classroom);
            response.put("subject", subject);
            response.put("schoolYear", schoolYear);
            response.put("students", students);
            response.put("studentScores", studentScores);
            response.put("scoreTypes", orderedScoreTypes);
            response.put("scoreWeights", scoreWeights);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PostMapping("/classes/{classId}/scores/save")
    public ResponseEntity<?> saveScores(
            @PathVariable("classId") Integer classId,
            @RequestParam("subjectTeacherId") Integer subjectTeacherId,
            @RequestParam("schoolYearId") Integer schoolYearId,
            @RequestParam(value = "saveMode", defaultValue = "final") String saveMode,
            @RequestBody Map<String, Object> requestData,
            @RequestParam String username) {
        try {
            Teacher teacher = teacherService.getTeacherByUsername(username);
            
            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Không tìm thấy thông tin giáo viên"));
            }

            // Kiểm tra quyền truy cập
            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
            if (subjectTeacher == null || !subjectTeacher.getTeacherId().getId().equals(teacher.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Không có quyền cập nhật điểm cho lớp này"));
            }
            
            // Xử lý dữ liệu điểm từ request
            Map<String, Object> scores = (Map<String, Object>) requestData.get("scores");
            List<Score> scoresToSave = new ArrayList<>();
            
            for (String studentIdStr : scores.keySet()) {
                int studentId = Integer.parseInt(studentIdStr);
                Map<String, Object> studentScores = (Map<String, Object>) scores.get(studentIdStr);
                
                for (String scoreType : studentScores.keySet()) {
                    Map<String, Object> scoreData = (Map<String, Object>) studentScores.get(scoreType);
                    String scoreValue = (String) scoreData.get("value");
                    String scoreIdStr = (String) scoreData.get("id");
                    
                    // Bỏ qua nếu không có giá trị điểm
                    if (scoreValue == null || scoreValue.trim().isEmpty()) {
                        continue;
                    }
                    
                    Score score;
                    if (scoreIdStr != null && !scoreIdStr.trim().isEmpty()) {
                        // Cập nhật điểm đã tồn tại
                        int scoreId = Integer.parseInt(scoreIdStr);
                        score = scoreService.getScoreById(scoreId);
                        if (score == null) {
                            score = new Score();
                            // Thiết lập các tham chiếu cho điểm mới
                            Student student = studentService.getStudentById(studentId);
                            score.setStudentID(student);
                            score.setSubjectTeacherID(subjectTeacher);
                            score.setSchoolYearId(schoolYearService.getSchoolYearById(schoolYearId));
                        }
                    } else {
                        // Tạo điểm mới
                        score = new Score();
                        Student student = studentService.getStudentById(studentId);
                        score.setStudentID(student);
                        score.setSubjectTeacherID(subjectTeacher);
                        score.setSchoolYearId(schoolYearService.getSchoolYearById(schoolYearId));
                    }
                    
                    // Thiết lập loại điểm
                    com.ntn.pojo.Typescore type = typeScoreService.getScoreTypeByName(scoreType);
                    if (type == null) {
                        type = new com.ntn.pojo.Typescore(scoreType);
                        typeScoreService.addScoreType(type);
                    }
                    score.setScoreType(type);
                    
                    // Thiết lập giá trị điểm
                    try {
                        float value = Float.parseFloat(scoreValue);
                        if (value >= 0 && value <= 10) {
                            score.setScoreValue(value);
                            
                            // Thiết lập trạng thái draft/final
                            boolean isFinalSave = "final".equals(saveMode);
                            if (isFinalSave) {
                                score.setIsLocked(true);
                                score.setIsDraft(false);
                            } else {
                                score.setIsLocked(false);
                                score.setIsDraft(true);
                            }
                            
                            scoresToSave.add(score);
                        } else {
                            return ResponseEntity.badRequest()
                                    .body(Map.of("error", "Điểm phải nằm trong khoảng từ 0 đến 10"));
                        }
                    } catch (NumberFormatException e) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Định dạng điểm không hợp lệ"));
                    }
                }
            }
            
            if (scoresToSave.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "Không có điểm nào được cập nhật"));
            }
            
            boolean success = scoreService.saveScores(scoresToSave);
            
            if (success) {
                String message = "final".equals(saveMode) 
                        ? "Lưu điểm chính thức thành công!"
                        : "Lưu điểm nháp thành công!";
                return ResponseEntity.ok(Map.of("message", message));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Có lỗi khi lưu điểm"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}