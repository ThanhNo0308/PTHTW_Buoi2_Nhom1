package com.ntn.controllers;

import com.ntn.pojo.Class;
import com.ntn.pojo.Score;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Student;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subject;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.service.ClassService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
import com.ntn.service.StudentSubjectTeacherService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TeacherService;
import com.ntn.service.TypeScoreService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacherclass")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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
    private TypeScoreService typeScoreService;

    @Autowired
    private StudentSubjectTeacherService studentSubjectTeacherService;

    // Lấy danh sách lớp được phân công
    @GetMapping("/classes")
    public ResponseEntity<?> getTeacherClasses(@RequestParam("username") String username) {
        try {
            Teacher teacher = teacherService.getTeacherByUsername(username);

            if (teacher == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Không tìm thấy thông tin giáo viên"));
            }

            // Lấy danh sách các lớp mà giảng viên được phân công dạy
            Set<Class> assignedClasses = new HashSet<>();
            List<Subjectteacher> teacherAssignments = subjectTeacherService.getSubjectTeachersByTeacherId(teacher.getId());

            // Lưu các học kỳ được phân công theo lớp
            Map<Integer, Set<Schoolyear>> classSchoolYears = new HashMap<>();

            for (Subjectteacher assignment : teacherAssignments) {
                if (assignment.getClassId() != null) {
                    assignedClasses.add(assignment.getClassId());

                    // Lưu thông tin học kỳ được phân công
                    int classId = assignment.getClassId().getId();
                    Schoolyear schoolYear = assignment.getSchoolYearId();

                    if (schoolYear != null) {
                        if (!classSchoolYears.containsKey(classId)) {
                            classSchoolYears.put(classId, new HashSet<>());
                        }
                        classSchoolYears.get(classId).add(schoolYear);
                    }
                }
            }

            // Tạo danh sách lớp với thông tin bổ sung
            List<Map<String, Object>> classesWithDetails = new ArrayList<>();
            for (Class cls : assignedClasses) {
                Map<String, Object> classDetails = new HashMap<>();
                classDetails.put("id", cls.getId());
                classDetails.put("className", cls.getClassName());
                classDetails.put("majorId", cls.getMajorId());

                // Thêm danh sách học kỳ được phân công
                List<Schoolyear> schoolYears = classSchoolYears.containsKey(cls.getId())
                        ? new ArrayList<>(classSchoolYears.get(cls.getId())) : new ArrayList<>();
                classDetails.put("assignedSchoolYears", schoolYears);

                classesWithDetails.add(classDetails);
            }

            Map<Integer, Integer> studentCounts = new HashMap<>();

            // Tính số sinh viên cho mỗi lớp (bao gồm cả sinh viên đăng ký học)
            for (Class cls : assignedClasses) {
                // Lấy danh sách sinh viên thuộc lớp hành chính
                List<Student> administrativeStudents = studentService.getStudentByClassId(cls.getId());
                Set<Student> enrolledStudents = new HashSet<>(administrativeStudents);

                // Lấy các môn học được phân công trong lớp này
                List<Subjectteacher> classAssignments = teacherAssignments.stream()
                        .filter(a -> a.getClassId() != null && a.getClassId().getId().equals(cls.getId()))
                        .collect(java.util.stream.Collectors.toList());

                // Thêm sinh viên đăng ký học các môn này
                for (Subjectteacher st : classAssignments) {
                    List<Studentsubjectteacher> enrollments = studentSubjectTeacherService.getBySubjectTeacherId(st.getId());
                    for (Studentsubjectteacher enrollment : enrollments) {
                        enrolledStudents.add(enrollment.getStudentId());
                    }
                }

                // Lưu số lượng sinh viên thực tế
                studentCounts.put(cls.getId(), enrolledStudents.size());
            }

            int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();

            for (Map<String, Object> classDetails : classesWithDetails) {
                List<Schoolyear> schoolYears = (List<Schoolyear>) classDetails.get("assignedSchoolYears");
                List<Map<String, Object>> enhancedSchoolYears = new ArrayList<>();

                for (Schoolyear sy : schoolYears) {
                    Map<String, Object> syInfo = new HashMap<>();
                    syInfo.put("id", sy.getId());
                    syInfo.put("nameYear", sy.getNameYear());
                    syInfo.put("semesterName", sy.getSemesterName());
                    syInfo.put("yearStart", sy.getYearStart());
                    syInfo.put("yearEnd", sy.getYearEnd());
                    syInfo.put("isCurrentSemester", sy.getId() == currentSchoolYearId);
                    enhancedSchoolYears.add(syInfo);
                }

                classDetails.put("assignedSchoolYears", enhancedSchoolYears);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("classes", classesWithDetails);
            response.put("studentCounts", studentCounts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Lấy thông tin chi tiết lớp học
    @GetMapping("/classes/{classId}")
    public ResponseEntity<?> getClassDetail(
            @PathVariable("classId") int classId,
            @RequestParam("username") String username,
            @RequestParam(value = "schoolYearId", required = false) Integer selectedSchoolYearId) {
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

            // Tạo danh sách các học kỳ mà giáo viên được phân công dạy trong lớp này
            Set<Schoolyear> assignedSchoolYearsSet = new HashSet<>();
            for (Subjectteacher st : teacherClassAssignments) {
                if (st.getSchoolYearId() != null) {
                    assignedSchoolYearsSet.add(st.getSchoolYearId());
                }
            }
            List<Schoolyear> schoolYears = new ArrayList<>(assignedSchoolYearsSet);

            // Sắp xếp học kỳ theo thứ tự mới nhất trước
            Collections.sort(schoolYears, (sy1, sy2) -> Integer.compare(sy2.getId(), sy1.getId()));

            // Nếu không có schoolYearId được chọn, sử dụng học kỳ hiện tại hoặc học kỳ đầu tiên trong danh sách
            int targetSchoolYearId;
            if (selectedSchoolYearId != null) {
                targetSchoolYearId = selectedSchoolYearId;
            } else {
                int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();

                // Kiểm tra xem học kỳ hiện tại có trong danh sách được phân công không
                boolean hasCurrentSchoolYear = schoolYears.stream()
                        .anyMatch(sy -> sy.getId() == currentSchoolYearId);

                if (hasCurrentSchoolYear) {
                    targetSchoolYearId = currentSchoolYearId;
                } else if (!schoolYears.isEmpty()) {
                    // Nếu không có học kỳ hiện tại, lấy học kỳ đầu tiên trong danh sách (mới nhất)
                    targetSchoolYearId = schoolYears.get(0).getId();
                } else {
                    // Nếu không có học kỳ nào, sử dụng học kỳ hiện tại mặc định
                    targetSchoolYearId = currentSchoolYearId;
                }
            }

            // Lấy các môn học mà giảng viên dạy trong lớp này và trong học kỳ được chọn
            List<Subjectteacher> assignedSubjects = subjectTeacherService.getSubjectTeachersByTeacherIdAndClassIdAndSchoolYearId(
                    teacher.getId(), classId, targetSchoolYearId);

            // Danh sách sinh viên trong lớp
            List<Student> administrativeStudents = studentService.getStudentByClassId(classroom.getId());

            Set<Student> enrolledStudents = new HashSet<>(administrativeStudents);
            for (Subjectteacher st : assignedSubjects) {
                List<Studentsubjectteacher> enrollments = studentSubjectTeacherService.getBySubjectTeacherId(st.getId());
                for (Studentsubjectteacher enrollment : enrollments) {
                    enrolledStudents.add(enrollment.getStudentId());
                }
            }

            List<Student> allStudents = new ArrayList<>(enrolledStudents);

            // Thay đổi: Thêm thông tin học kỳ hiện tại vào cấu trúc dữ liệu riêng thay vì sử dụng isCurrent
            int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();

            // Tạo danh sách học kỳ với thông tin bổ sung
            List<Map<String, Object>> enhancedSchoolYears = new ArrayList<>();
            for (Schoolyear sy : schoolYears) {
                Map<String, Object> syInfo = new HashMap<>();
                syInfo.put("id", sy.getId());
                syInfo.put("nameYear", sy.getNameYear());
                syInfo.put("semesterName", sy.getSemesterName());
                syInfo.put("yearStart", sy.getYearStart());
                syInfo.put("yearEnd", sy.getYearEnd());
                syInfo.put("isCurrentSemester", sy.getId() == currentSchoolYearId);
                enhancedSchoolYears.add(syInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("classroom", classroom);
            response.put("teacher", teacher);
            response.put("administrativeStudentCount", administrativeStudents.size());
            response.put("totalStudentCount", enrolledStudents.size());
            response.put("students", allStudents);
            response.put("subjects", assignedSubjects);
            response.put("assignedSchoolYears", enhancedSchoolYears); // Sử dụng danh sách có thông tin bổ sung
            response.put("selectedSchoolYearId", targetSchoolYearId); // Học kỳ được chọn
            response.put("currentSchoolYearId", currentSchoolYearId); // Thêm thông tin học kỳ hiện tại

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Lấy điểm của lớp được phân công
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
            if (!subjectTeacher.getTeacherId().getId().equals(teacher.getId())
                    || subjectTeacher.getClassId() == null
                    || !subjectTeacher.getClassId().getId().equals(classId)
                    || subjectTeacher.getSchoolYearId() == null
                    || !subjectTeacher.getSchoolYearId().getId().equals(schoolYearId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Không có quyền truy cập lớp học này"));
            }

            // Lấy danh sách sinh viên trong lớp
            List<Student> allStudentsInClass = studentService.getStudentByClassId(classId);
            List<Studentsubjectteacher> enrollments = studentSubjectTeacherService.getBySubjectTeacherId(subjectTeacherId);
            Set<Student> uniqueStudents = new HashSet<>(allStudentsInClass);
            for (Studentsubjectteacher enrollment : enrollments) {
                uniqueStudents.add(enrollment.getStudentId());
            }
            List<Student> students = new ArrayList<>(uniqueStudents);

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
}
