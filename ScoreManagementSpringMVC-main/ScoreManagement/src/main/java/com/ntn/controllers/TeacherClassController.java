package com.ntn.controllers;

import com.ntn.pojo.Class;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subject;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.Typescore;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher")
@PreAuthorize("hasAuthority('Teacher') or hasAuthority('Admin')")
public class TeacherClassController {

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
    public String getTeacherClasses(Model model, Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = teacherService.getTeacherByUsername(username);

        if (teacher == null) {
            return "redirect:/error?message=unauthorized";
        }

        // Lấy danh sách các lớp mà giảng viên được phân công dạy thay vì chỉ lớp chủ nhiệm
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

        model.addAttribute("classes", classes);
        model.addAttribute("studentCounts", studentCounts);

        return "teacher/classes";
    }

    @GetMapping("/classes/{classId}")
    public String getClassDetail(@PathVariable("classId") int classId, Model model, Authentication authentication) {
        String username = authentication.getName();
        Teacher teacher = teacherService.getTeacherByUsername(username);

        if (teacher == null) {
            return "redirect:/error?message=unauthorized";
        }

        Class classroom = classService.getClassById(classId);
        if (classroom == null) {
            return "redirect:/teacher/classes?error=class-not-found";
        }

        // Kiểm tra xem giáo viên có được phân công dạy lớp này không
        List<Subjectteacher> teacherClassAssignments = subjectTeacherService.getSubjectTeachersByTeacherIdAndClassId(teacher.getId(), classId);

        if (teacherClassAssignments.isEmpty()) {
            return "redirect:/teacher/classes?error=not-assigned";
        }

        // Tính số lượng sinh viên trong lớp
        int studentCount = studentService.countStudentsByClassId(classroom.getId());

        // Danh sách sinh viên trong lớp
        List<Student> students = studentService.getStudentByClassId(classroom.getId());

        // Lấy năm học hiện tại
        int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();

        // Chỉ lấy các môn học mà giảng viên dạy trong lớp này và học kỳ hiện tại
        List<Subjectteacher> assignedSubjects = subjectTeacherService.getSubjectTeachersByTeacherIdAndClassIdAndSchoolYearId(
                teacher.getId(), classId, currentSchoolYearId);

        // Nếu không có môn học trong học kỳ hiện tại, lấy tất cả phân công của giáo viên cho lớp này
        if (assignedSubjects.isEmpty()) {
            assignedSubjects = subjectTeacherService.getSubjectTeachersByTeacherIdAndClassId(teacher.getId(), classId);
        }

        model.addAttribute("classroom", classroom);
        model.addAttribute("teacher", teacher);
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("students", students);
        model.addAttribute("subjects", assignedSubjects);
        model.addAttribute("schoolYears", schoolYearService.getAllSchoolYears());

        return "teacher/class-detail";
    }

    @GetMapping("/classes/{classId}/scores")
    public String getScoresPage(@PathVariable("classId") int classId,
            @RequestParam("subjectTeacherId") int subjectTeacherId,
            @RequestParam("schoolYearId") int schoolYearId,
            Model model, Authentication authentication) {
        
        String username = authentication.getName();
        Teacher teacher = teacherService.getTeacherByUsername(username);
        
        if (teacher == null) {
            return "redirect:/error?message=unauthorized";
        }

        // Lấy thông tin lớp, giáo viên, môn học và năm học
        Class classroom = classService.getClassById(classId);
        Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
        Subject subject = subjectTeacher.getSubjectId();
        Schoolyear schoolYear = schoolYearService.getSchoolYearById(schoolYearId);
        
        if (subjectTeacher == null || 
            !subjectTeacher.getTeacherId().getId().equals(teacher.getId()) || 
            subjectTeacher.getClassId() == null || 
            !subjectTeacher.getClassId().getId().equals(classId) ||
            subjectTeacher.getSchoolYearId() == null || 
            !subjectTeacher.getSchoolYearId().getId().equals(schoolYearId)) {
            return "redirect:/teacher/classes?error=not-assigned";
        }

        // Lấy danh sách sinh viên trong lớp
        List<Student> students = studentService.getStudentByClassId(classId);

        // Lấy danh sách loại điểm từ bảng classscoretypes (cho lớp cụ thể này)
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

        // Lấy điểm hiện có cho từng sinh viên và tổ chức theo cấu trúc Map
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

        // Thêm các thuộc tính vào model để hiển thị trong view
        model.addAttribute("classroom", classroom);
        model.addAttribute("subject", subject);
        model.addAttribute("schoolYear", schoolYear);
        model.addAttribute("students", students);
        model.addAttribute("studentScores", studentScores);
        model.addAttribute("scoreTypes", orderedScoreTypes);
        model.addAttribute("scoreWeights", scoreWeights);
        model.addAttribute("subjectTeacherId", subjectTeacherId);
        model.addAttribute("schoolYearId", schoolYearId);

        return "teacher/score-management";
    }

    // Phương thức xử lý lưu điểm
    @PostMapping("/classes/{classId}/scores/save")
    @Transactional
    public String saveScores(
            @PathVariable("classId") Integer classId,
            @RequestParam("subjectTeacherId") Integer subjectTeacherId,
            @RequestParam("schoolYearId") Integer schoolYearId,
            @RequestParam(value = "saveMode", defaultValue = "final") String saveMode,
            @RequestParam Map<String, Object> formData,
            RedirectAttributes redirectAttributes) {

        List<Score> scoresToSave = new ArrayList<>();

        // Xử lý formData để lấy điểm
        for (String key : formData.keySet()) {
            if (key.startsWith("scores[") && !key.endsWith("].id")) {
                Pattern pattern = Pattern.compile("scores\\[(\\d+)\\]\\[(.+?)\\]");
                Matcher matcher = pattern.matcher(key);

                if (matcher.find()) {
                    int studentId = Integer.parseInt(matcher.group(1));
                    String scoreType = matcher.group(2);
                    String scoreValue = (String) formData.get(key);

                    // Bỏ qua nếu không có giá trị điểm
                    if (scoreValue == null || scoreValue.trim().isEmpty()) {
                        continue;
                    }

                    // Lấy ID điểm nếu đã tồn tại
                    String scoreIdParam = "scores[" + studentId + "][" + scoreType + "].id";
                    String scoreId = (String) formData.get(scoreIdParam);

                    Score score;
                    if (scoreId != null && !scoreId.trim().isEmpty()) {
                        // Cập nhật điểm đã tồn tại
                        score = scoreService.getScoreById(Integer.parseInt(scoreId));
                        if (score == null) {
                            System.out.println("WARNING: Score with ID " + scoreId + " not found, creating new one");
                            score = new Score();
                            // Thiết lập các tham chiếu cho điểm mới
                            Student student = studentService.getStudentById(studentId);
                            score.setStudentID(student);
                            score.setSubjectTeacherID(subjectTeacherService.getSubjectTeacherById(subjectTeacherId));
                            score.setSchoolYearId(schoolYearService.getSchoolYearById(schoolYearId));

                            Typescore type = typeScoreService.getScoreTypeByName(scoreType);
                            if (type == null) {
                                type = new Typescore(scoreType);
                                typeScoreService.addScoreType(type);
                            }
                            score.setScoreType(type);
                        }
                    } else {
                        // Tạo điểm mới
                        score = new Score();
                        Student student = studentService.getStudentById(studentId);
                        score.setStudentID(student);
                        score.setSubjectTeacherID(subjectTeacherService.getSubjectTeacherById(subjectTeacherId));
                        score.setSchoolYearId(schoolYearService.getSchoolYearById(schoolYearId));

                        Typescore type = typeScoreService.getScoreTypeByName(scoreType);
                        if (type == null) {
                            type = new Typescore();
                            type.setScoreType(scoreType);
                            typeScoreService.addScoreType(type);
                        }
                        score.setScoreType(type);
                    }

                    try {
                        float value = Float.parseFloat(scoreValue);
                        if (value >= 0 && value <= 10) {
                            score.setScoreValue(value);
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

                            if (score.getScoreType() == null) {
                                Typescore type = typeScoreService.getScoreTypeByName(scoreType);
                                if (type == null) {
                                    type = new Typescore(scoreType);
                                    typeScoreService.addScoreType(type);
                                }
                                score.setScoreType(type);
                            }
                            scoresToSave.add(score);
                        } else {
                            redirectAttributes.addFlashAttribute("errorMessage",
                                    "Điểm cho học sinh " + score.getStudentID().getFirstName()
                                    + " " + score.getStudentID().getLastName()
                                    + " phải nằm trong khoảng 0-10!");
                            return "redirect:/teacher/classes/" + classId + "/scores?subjectTeacherId="
                                    + subjectTeacherId + "&schoolYearId=" + schoolYearId;
                        }
                    } catch (NumberFormatException e) {
                        // Bỏ qua các giá trị không phải số
                    }
                }
            }
        }

        if (!scoresToSave.isEmpty()) {
            boolean isFinalSave = "final".equals(saveMode);

            for (Score score : scoresToSave) {
                if (isFinalSave) {
                    score.setIsLocked(true);
                    score.setIsDraft(false);
                } else {
                    score.setIsLocked(false);
                    score.setIsDraft(true);
                }
            }

            boolean success = scoreService.saveScores(scoresToSave);
            if (success) {
                String message = isFinalSave
                        ? "Lưu điểm chính thức thành công!"
                        : "Lưu điểm nháp thành công!";
                redirectAttributes.addFlashAttribute("successMessage", message);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi khi lưu điểm!");
            }
        }

        return "redirect:/teacher/classes/" + classId + "/scores?subjectTeacherId="
                + subjectTeacherId + "&schoolYearId=" + schoolYearId;
    }

    @GetMapping("/classes/{classId}/scores/export-csv")
    public ResponseEntity<byte[]> exportScoresToCsv(
            @PathVariable("classId") int classId,
            @RequestParam("subjectTeacherId") int subjectTeacherId,
            @RequestParam("schoolYearId") int schoolYearId) {

        try {
            byte[] csvBytes = scoreService.exportScoresToCsv(subjectTeacherId, classId, schoolYearId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "scores.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/classes/{classId}/scores/export-pdf")
    public ResponseEntity<byte[]> exportScoresToPdf(
            @PathVariable("classId") int classId,
            @RequestParam("subjectTeacherId") int subjectTeacherId,
            @RequestParam("schoolYearId") int schoolYearId) {

        try {
            byte[] pdfBytes = scoreService.exportScoresToPdf(subjectTeacherId, classId, schoolYearId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "scores.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
