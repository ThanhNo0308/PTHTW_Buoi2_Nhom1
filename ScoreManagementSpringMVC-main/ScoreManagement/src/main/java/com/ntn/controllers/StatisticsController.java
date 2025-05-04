package com.ntn.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ntn.pojo.Class;
import com.ntn.pojo.Department;
import com.ntn.pojo.Major;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Subject;
import com.ntn.pojo.Teacher;
import com.ntn.service.ClassService;
import com.ntn.service.DepartmentService;
import com.ntn.service.MajorService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.StatisticsService;
import com.ntn.service.SubjectService;
import com.ntn.service.TeacherService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

// Controller báo cáo thống kê biểu đồ
@Controller
@RequestMapping("/admin/statistics")
@PreAuthorize("hasAuthority('Admin')")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ClassService classService;

    @Autowired
    private MajorService majorService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private TeacherService teacherService;

    @GetMapping("/class")
    public String getClassStatistics(
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer schoolYearId,
            Model model) {

        List<Class> classes = classService.getClasses();
        List<Subject> subjects = subjectService.getSubjects();
        List<Schoolyear> schoolYears = schoolYearService.getAllSchoolYears();

        model.addAttribute("classes", classes);
        model.addAttribute("subjects", subjects);
        model.addAttribute("schoolYears", schoolYears);
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedSchoolYearId", schoolYearId);

        if (classId != null) {
            Map<String, Long> scoreDistribution = statisticsService.getScoreDistributionByClass(classId, schoolYearId, subjectId);
            model.addAttribute("scoreDistribution", scoreDistribution);

            Map<String, Object> passFailStats = statisticsService.getPassFailRateByFilter(classId, null, null, schoolYearId, subjectId, null);
            model.addAttribute("passFailStats", passFailStats);

            List<Map<String, Object>> avgScoreBySubject = statisticsService.getAverageScoreBySubject(schoolYearId, null);
            model.addAttribute("avgScoreBySubject", avgScoreBySubject);
        }

        return "admin/statistics/class-statistics";
    }

    @GetMapping("/subject")
    public String getSubjectStatistics(
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer teacherId,
            @RequestParam(required = false) Integer schoolYearId,
            Model model) {

        // Lấy dữ liệu cho các dropdown lọc
        List<Subject> subjects = subjectService.getSubjects();
        List<Teacher> teachers = teacherService.getTeachers();
        List<Schoolyear> schoolYears = schoolYearService.getAllSchoolYears();

        // Thêm dữ liệu dropdown vào model
        model.addAttribute("subjects", subjects);
        model.addAttribute("teachers", teachers);
        model.addAttribute("schoolYears", schoolYears);
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedTeacherId", teacherId);
        model.addAttribute("selectedSchoolYearId", schoolYearId);

        // Lấy dữ liệu điểm trung bình theo môn học
        List<Map<String, Object>> avgScoreBySubject = statisticsService.getAverageScoreBySubject(schoolYearId, teacherId);

        // Đảm bảo không null
        if (avgScoreBySubject == null) {
            avgScoreBySubject = new ArrayList<>();
        }

        model.addAttribute("avgScoreBySubject", avgScoreBySubject);

        // 1. Khởi tạo các thông số thống kê (scoreStats)
        Map<String, Object> scoreStats = new HashMap<>();
        scoreStats.put("studentCount", 0L);
        scoreStats.put("averageScore", 0.0);
        scoreStats.put("passCount", 0L);
        scoreStats.put("failCount", 0L);

        // Tính toán thông số tổng quát từ dữ liệu môn học
        if (!avgScoreBySubject.isEmpty()) {
            long totalStudents = 0;
            double weightedScoreSum = 0;

            for (Map<String, Object> subject : avgScoreBySubject) {
                Number studentCount = (Number) subject.get("studentCount");
                Double avgScore = (Double) subject.get("averageScore");

                if (studentCount != null && avgScore != null) {
                    totalStudents += studentCount.longValue();
                    weightedScoreSum += avgScore * studentCount.longValue();
                }
            }

            if (totalStudents > 0) {
                double overallAvg = weightedScoreSum / totalStudents;
                scoreStats.put("studentCount", totalStudents);
                scoreStats.put("averageScore", overallAvg);

                // Ước tính số lượng đạt/không đạt
                long estimatedPassCount = Math.round(totalStudents * 0.7); // Giả định 70% đạt
                scoreStats.put("passCount", estimatedPassCount);
                scoreStats.put("failCount", totalStudents - estimatedPassCount);
            }
        }

        // 2. Khởi tạo phân phối điểm (scoreDistribution)
        Map<String, Long> scoreDistribution = new LinkedHashMap<>();
        scoreDistribution.put("0-2", 0L);  // Giá trị mặc định để đảm bảo biểu đồ hiển thị
        scoreDistribution.put("2-4", 0L);
        scoreDistribution.put("4-6", 0L);
        scoreDistribution.put("6-8", 0L);
        scoreDistribution.put("8-10", 0L);

        // 3. Khởi tạo điểm theo loại (scoreByType)
        Map<String, Double> scoreByType = new HashMap<>();
        scoreByType.put("Giữa kỳ", 0.0);
        scoreByType.put("Cuối kỳ", 0.0);

        // 4. Khởi tạo danh sách top sinh viên (topStudents)
        List<Map<String, Object>> topStudents = new ArrayList<>();

        // 5. Khởi tạo xu hướng điểm (scoreTrends)
        List<Map<String, Object>> scoreTrends = new ArrayList<>();

        // 6. Khởi tạo thống kê đạt/không đạt (passFailStats)
        Map<String, Object> passFailStats = new HashMap<>();
        passFailStats.put("totalCount", 0L);
        passFailStats.put("passCount", 0L);
        passFailStats.put("failCount", 0L);
        passFailStats.put("passRate", 0.0);
        passFailStats.put("failRate", 0.0);

        // Nếu có lọc theo môn học cụ thể
        if (subjectId != null) {
            // Lấy thông tin chi tiết về đạt/không đạt
            passFailStats = statisticsService.getPassFailRateByFilter(null, null, null, schoolYearId, subjectId, teacherId);
            if (passFailStats == null) {
                passFailStats = new HashMap<>();
                passFailStats.put("totalCount", 0L);
                passFailStats.put("passCount", 0L);
                passFailStats.put("failCount", 0L);
                passFailStats.put("passRate", 0.0);
                passFailStats.put("failRate", 0.0);
            }

            // Cập nhật scoreStats với dữ liệu chính xác hơn
            scoreStats.put("studentCount", passFailStats.get("totalCount"));
            scoreStats.put("passCount", passFailStats.get("passCount"));
            scoreStats.put("failCount", passFailStats.get("failCount"));

            // Tìm thông tin môn học được chọn trong danh sách
            for (Map<String, Object> subject : avgScoreBySubject) {
                if (subject.get("subjectId").equals(subjectId)) {
                    // Cập nhật điểm trung bình
                    scoreStats.put("averageScore", subject.get("averageScore"));

                    // Cập nhật điểm theo loại
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> scoreTypeDetails = (List<Map<String, Object>>) subject.get("scoreTypeDetails");
                    if (scoreTypeDetails != null) {
                        scoreByType.clear(); // Xóa giá trị mặc định
                        for (Map<String, Object> detail : scoreTypeDetails) {
                            String scoreType = (String) detail.get("scoreType");
                            Number avgValue = (Number) detail.get("avgValue");
                            if (scoreType != null && avgValue != null) {
                                scoreByType.put(scoreType, avgValue.doubleValue());
                            }
                        }
                    }
                    break;
                }
            }

            // Lấy phân phối điểm cho môn học
            Map<String, Long> classScoreDist = statisticsService.getScoreDistributionByClass(null, schoolYearId, subjectId);

            if (classScoreDist != null && !classScoreDist.isEmpty()) {
                // Chuyển đổi dữ liệu từ định dạng repository sang định dạng biểu đồ
                long belowFourCount = classScoreDist.getOrDefault("Dưới 4", 0L);
                long range4to5 = classScoreDist.getOrDefault("4 - 5", 0L);
                long range5to65 = classScoreDist.getOrDefault("5 - 6.5", 0L);
                long range65to8 = classScoreDist.getOrDefault("6.5 - 8", 0L);
                long range8to9 = classScoreDist.getOrDefault("8 - 9", 0L);
                long range9to10 = classScoreDist.getOrDefault("9 - 10", 0L);

                // Điền vào map theo khoảng điểm mới
                if (belowFourCount > 0) {
                    // Chia đều cho hai khoảng đầu tiên
                    scoreDistribution.put("0-2", belowFourCount / 2 + (belowFourCount % 2));
                    scoreDistribution.put("2-4", belowFourCount / 2);
                }

                // Khoảng 4-6: gồm "4-5" và nửa đầu của "5-6.5"
                scoreDistribution.put("4-6", range4to5 + range5to65 / 2);

                // Khoảng 6-8: gồm nửa sau của "5-6.5" và "6.5-8"
                scoreDistribution.put("6-8", (range5to65 - range5to65 / 2) + range65to8);

                // Khoảng 8-10: gồm "8-9" và "9-10"
                scoreDistribution.put("8-10", range8to9 + range9to10);
            }

            // Lấy top 5 sinh viên có điểm cao nhất
            topStudents = statisticsService.getTopStudentsBySubject(subjectId, schoolYearId, 5);
            if (topStudents == null) {
                topStudents = new ArrayList<>();
            }

            // Lấy xu hướng điểm qua các kỳ học
            scoreTrends = statisticsService.getScoreTrendsBySubject(subjectId);
            if (scoreTrends == null) {
                scoreTrends = new ArrayList<>();
            }
        }

        // Thêm tất cả dữ liệu vào model
        model.addAttribute("scoreStats", scoreStats);
        model.addAttribute("scoreDistribution", scoreDistribution);
        model.addAttribute("scoreByType", scoreByType);
        model.addAttribute("topStudents", topStudents);
        model.addAttribute("scoreTrends", scoreTrends);
        model.addAttribute("passFailStats", passFailStats);

        // Thêm dữ liệu điểm theo lớp nếu có chọn môn học
        if (subjectId != null) {
            List<Map<String, Object>> avgScoreByClass = statisticsService.getAverageScoreByClass(subjectId, schoolYearId);
            model.addAttribute("avgScoreByClass", avgScoreByClass);
        }

        return "admin/statistics/subject-statistics";
    }

    @GetMapping("/department")
    public String getDepartmentStatistics(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer schoolYearId,
            Model model) {

        List<Department> departments = departmentService.getDepartments();
        List<Schoolyear> schoolYears = schoolYearService.getAllSchoolYears();

        model.addAttribute("departments", departments);
        model.addAttribute("schoolYears", schoolYears);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("selectedSchoolYearId", schoolYearId);

        List<Map<String, Object>> avgScoreByDepartment = statisticsService.getAverageScoreByDepartment(schoolYearId);
        model.addAttribute("avgScoreByDepartment", avgScoreByDepartment);

        if (departmentId != null) {
            List<Map<String, Object>> studentCountByMajor = statisticsService.getStudentCountByMajor(departmentId, schoolYearId);
            model.addAttribute("studentCountByMajor", studentCountByMajor);

            Map<String, Object> passFailStats = statisticsService.getPassFailRateByFilter(null, null, departmentId, schoolYearId, null, null);
            model.addAttribute("passFailStats", passFailStats);
        }

        return "admin/statistics/department-statistics";
    }

    @GetMapping("/major")
    public String getMajorStatistics(
            @RequestParam(required = false) Integer majorId,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer schoolYearId,
            Model model) {

        List<Major> majors = majorService.getMajors();
        List<Department> departments = departmentService.getDepartments();
        List<Schoolyear> schoolYears = schoolYearService.getAllSchoolYears();

        model.addAttribute("majors", majors);
        model.addAttribute("departments", departments);
        model.addAttribute("schoolYears", schoolYears);
        model.addAttribute("selectedMajorId", majorId);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("selectedSchoolYearId", schoolYearId);

        if (departmentId != null) {
            List<Map<String, Object>> studentCountByMajor = statisticsService.getStudentCountByMajor(departmentId, schoolYearId);
            model.addAttribute("studentCountByMajor", studentCountByMajor);
        }

        if (majorId != null) {
            Map<String, Object> passFailStats = statisticsService.getPassFailRateByFilter(null, majorId, null, schoolYearId, null, null);
            model.addAttribute("passFailStats", passFailStats);
        }

        if (majorId != null) {
            Map<String, Long> scoreDistribution = statisticsService.getScoreDistributionByMajor(majorId, schoolYearId);
            // Nếu không có dữ liệu, khởi tạo dữ liệu mẫu để biểu đồ vẫn hiển thị
            if (scoreDistribution == null || scoreDistribution.isEmpty()) {
                scoreDistribution = new LinkedHashMap<>();
                scoreDistribution.put("0-2", 0L);
                scoreDistribution.put("2-4", 0L);
                scoreDistribution.put("4-6", 0L);
                scoreDistribution.put("6-8", 0L);
                scoreDistribution.put("8-10", 0L);
            }
            model.addAttribute("scoreDistribution", scoreDistribution);
        }

        return "admin/statistics/major-statistics";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStatistics(
            @RequestParam String type,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer majorId,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer schoolYearId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer teacherId) {

        byte[] excelContent = statisticsService.exportStatisticsToExcel(type, classId, majorId, departmentId, schoolYearId, subjectId, teacherId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "statistics-" + type + ".xlsx");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
    }
}
