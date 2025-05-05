package com.ntn.controllers;

import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.service.ClassService;
import com.ntn.service.EmailService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TypeScoreService;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controller Điểm của lớp học
@Controller
@RequestMapping("/scores")
@PreAuthorize("hasAuthority('Admin')")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TypeScoreService typeScoreService;

    @GetMapping("/admin/class/{classId}")
    public String viewClassScores(@PathVariable("classId") int classId,
            @RequestParam(required = false) Integer schoolYearId,
            @RequestParam(required = false) Integer subjectTeacherId,
            Model model) {

        try {
            // Lấy thông tin lớp học
            com.ntn.pojo.Class classroom = classService.getClassById(classId);
            if (classroom == null) {
                return "redirect:/admin/classes?error=class-not-found";
            }

            // Lấy danh sách năm học
            List<Schoolyear> schoolYears = schoolYearService.getAllSchoolYears();
            Schoolyear currentSchoolYear = null;

            if (schoolYearId != null) {
                currentSchoolYear = schoolYearService.getSchoolYearById(schoolYearId);
            } else if (!schoolYears.isEmpty()) {
                currentSchoolYear = schoolYears.get(0); // Lấy năm học đầu tiên mặc định
                schoolYearId = currentSchoolYear.getId();
            }

            // Danh sách môn học của lớp
            List<Subjectteacher> subjectTeachers = new ArrayList<>();
            if (currentSchoolYear != null) {
                subjectTeachers = subjectTeacherService.getSubjectTeachersByClassAndSchoolYear(classId, schoolYearId);
            }

            // Nếu có tham số subjectTeacherId, lấy chi tiết điểm
            List<Score> scores = new ArrayList<>();
            Subjectteacher selectedSubject = null;

            if (subjectTeacherId != null && schoolYearId != null) {
                scores = scoreService.getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(
                        subjectTeacherId, classId, schoolYearId);
                selectedSubject = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
            }

            // Lấy danh sách loại điểm
            List<String> scoreTypes = new ArrayList<>();
            if (subjectTeacherId != null && schoolYearId != null) {
                scoreTypes = typeScoreService.getScoreTypesByClass(classId, subjectTeacherId, schoolYearId);

                // Đảm bảo có "Giữa kỳ" và "Cuối kỳ" trong danh sách
                if (!scoreTypes.contains("Giữa kỳ")) {
                    scoreTypes.add("Giữa kỳ");
                }
                if (!scoreTypes.contains("Cuối kỳ")) {
                    scoreTypes.add("Cuối kỳ");
                }

                // Sắp xếp để "Giữa kỳ" và "Cuối kỳ" luôn ở cuối
                List<String> customTypes = scoreTypes.stream()
                        .filter(type -> !type.equals("Giữa kỳ") && !type.equals("Cuối kỳ"))
                        .collect(Collectors.toList());
                List<String> defaultTypes = new ArrayList<>();
                defaultTypes.add("Giữa kỳ");
                defaultTypes.add("Cuối kỳ");

                scoreTypes = new ArrayList<>(customTypes);
                scoreTypes.addAll(defaultTypes);
            }

            // Lấy trọng số điểm
            Map<String, Float> scoreWeights = new HashMap<>();
            if (subjectTeacherId != null && schoolYearId != null) {
                for (String type : scoreTypes) {
                    Float weight = scoreService.getScoreWeight(classId, subjectTeacherId, schoolYearId, type);
                    scoreWeights.put(type, weight != null ? weight : 0.0f);
                }
            }

            // Lấy danh sách sinh viên của lớp
            List<Student> students = studentService.getStudentByClassId(classId);

            // Nhóm điểm theo sinh viên
            Map<Integer, Map<String, Score>> studentScores = new HashMap<>();
            for (Student student : students) {
                studentScores.put(student.getId(), new HashMap<>());
            }

            for (Score score : scores) {
                if (score.getStudentID() != null && score.getScoreType() != null) {
                    int studentId = score.getStudentID().getId();
                    String scoreType = score.getScoreType().getScoreType();

                    if (!studentScores.containsKey(studentId)) {
                        studentScores.put(studentId, new HashMap<>());
                    }

                    studentScores.get(studentId).put(scoreType, score);
                }
            }

            // Đưa dữ liệu vào model
            model.addAttribute("classroom", classroom);
            model.addAttribute("schoolYears", schoolYears);
            model.addAttribute("currentSchoolYear", currentSchoolYear);
            model.addAttribute("subjectTeachers", subjectTeachers);
            model.addAttribute("selectedSubject", selectedSubject);
            model.addAttribute("scoreTypes", scoreTypes);
            model.addAttribute("scoreWeights", scoreWeights);
            model.addAttribute("students", students);
            model.addAttribute("studentScores", studentScores);

            return "admin/class-scores";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "error";
        }
    }
    
    @PostMapping("/admin/toggle-lock")
    public String toggleScoreLock(@RequestParam("scoreId") int scoreId,
            @RequestParam("classId") int classId,
            @RequestParam("schoolYearId") int schoolYearId,
            @RequestParam("subjectTeacherId") int subjectTeacherId,
            @RequestParam(value = "unlock", defaultValue = "false") boolean unlock,
            RedirectAttributes redirectAttributes) {

        try {
            Score score = scoreService.getScoreById(scoreId);
            if (score == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy điểm với ID: " + scoreId);
                return "redirect:/scores/admin/class/" + classId
                        + "?schoolYearId=" + schoolYearId
                        + "&subjectTeacherId=" + subjectTeacherId;
            }

            boolean success = scoreService.toggleScoreLock(scoreId, unlock);

            if (success) {
                String action = unlock ? "mở khóa" : "khóa";
                String studentName = score.getStudentID().getLastName() + " " + score.getStudentID().getFirstName();
                String scoreType = score.getScoreType().getScoreType();

                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã " + action + " điểm " + scoreType + " của sinh viên " + studentName + " thành công.");

                // Thông báo cho giảng viên nếu mở khóa
                if (unlock) {
                    emailService.sendNotificationToTeacher(
                            score.getSubjectTeacherID().getTeacherId().getId(),
                            "Thông báo mở khóa điểm",
                            "Điểm " + scoreType + " của sinh viên " + studentName + " đã được mở khóa. "
                            + "Bạn có thể cập nhật điểm này trong hệ thống."
                    );
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể thay đổi trạng thái khóa điểm.");
            }

            return "redirect:/scores/admin/class/" + classId
                    + "?schoolYearId=" + schoolYearId
                    + "&subjectTeacherId=" + subjectTeacherId;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/scores/admin/class/" + classId;
        }
    }
}
