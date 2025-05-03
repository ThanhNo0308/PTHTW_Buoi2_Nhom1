package com.ntn.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    // Thống kê theo khoảng điểm của lớp học
    Map<String, Long> getScoreDistributionByClass(Integer classId, Integer schoolYearId, Integer subjectId);

    // Thống kê điểm trung bình theo môn học
    List<Map<String, Object>> getAverageScoreBySubject(Integer schoolYearId, Integer departmentId);

    // Thống kê tỷ lệ sinh viên đạt/không đạt
    Map<String, Object> getPassFailRateByFilter(Integer classId, Integer majorId, Integer departmentId,
            Integer schoolYearId, Integer subjectId, Integer teacherId);

    // Thống kê số sinh viên theo ngành
    List<Map<String, Object>> getStudentCountByMajor(Integer departmentId, Integer schoolYearId);

    // Thống kê điểm trung bình theo lớp cho một môn học
    List<Map<String, Object>> getAverageScoreByClass(Integer subjectId, Integer schoolYearId);

    // Thống kê điểm trung bình theo khoa
    List<Map<String, Object>> getAverageScoreByDepartment(Integer schoolYearId);

    // Xuất dữ liệu thống kê sang Excel
    byte[] exportStatisticsToExcel(String statType, Integer classId, Integer majorId, Integer departmentId,
            Integer schoolYearId, Integer subjectId, Integer teacherId);

    List<Map<String, Object>> getTopStudentsBySubject(Integer subjectId, Integer schoolYearId, Integer limit);

    List<Map<String, Object>> getScoreTrendsBySubject(Integer subjectId);

    Map<String, Long> getScoreDistributionByMajor(Integer majorId, Integer schoolYearId);
}
