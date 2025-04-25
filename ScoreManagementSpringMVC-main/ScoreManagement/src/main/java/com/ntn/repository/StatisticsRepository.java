package com.ntn.repository;

import java.util.List;
import java.util.Map;

public interface StatisticsRepository {
    // 1. Thống kê số lượng sinh viên theo khoảng điểm trong một lớp
    Map<String, Long> getScoreDistributionByClass(Integer classId, Integer schoolYearId, Integer subjectId);
    
    // 2. Thống kê điểm trung bình theo môn học
    List<Object[]> getAverageScoreBySubject(Integer schoolYearId, Integer departmentId);
    
    // 3. Thống kê tỷ lệ sinh viên đạt/không đạt
    Map<String, Object> getPassFailRateByFilter(Integer classId, Integer majorId, Integer departmentId, Integer schoolYearId, Integer subjectId, Integer teacherId);
    
    // 4. Thống kê số sinh viên theo ngành
    List<Object[]> getStudentCountByMajor(Integer departmentId, Integer schoolYearId);
    
    // Thống kê điểm trung bình theo lớp cho một môn học
    List<Object[]> getAverageScoreByClass(Integer subjectId, Integer schoolYearId);
    
    // Thống kê điểm trung bình theo khoa
    List<Object[]> getAverageScoreByDepartment(Integer schoolYearId);
    
    List<Object[]> getAverageScoreByDepartment();
    
    List<Object[]> getTopStudentsBySubject(Integer subjectId, Integer schoolYearId, Integer limit);
    
    List<Object[]> getScoreTrendsBySubject(Integer subjectId);
    
    
    Map<String, Long> getScoreDistributionByMajor(Integer majorId, Integer schoolYearId);
}