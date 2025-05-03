package com.ntn.repository.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.ntn.repository.StatisticsRepository;
import java.util.Collections;
import java.util.LinkedHashMap;
import org.hibernate.query.Query;

@Repository
@Transactional
public class StatisticsRepositoryImpl implements StatisticsRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Map<String, Long> getScoreDistributionByClass(Integer classId, Integer schoolYearId, Integer subjectId) {
        Session session = this.factory.getObject().getCurrentSession();
        Map<String, Long> result = new LinkedHashMap<>();

        // Khởi tạo các khoảng điểm với giá trị mặc định là 0
        result.put("Dưới 4", 0L);
        result.put("4 - 5", 0L);
        result.put("5 - 6.5", 0L);
        result.put("6.5 - 8", 0L);
        result.put("8 - 9", 0L);
        result.put("9 - 10", 0L);

        StringBuilder sql = new StringBuilder();
        sql.append("WITH StudentScores AS (");
        sql.append("    SELECT ");
        sql.append("        s.StudentID, ");
        sql.append("        stu.ClassId, ");
        sql.append("        st.SubjectId, ");
        sql.append("        s.ScoreType, ");
        sql.append("        s.ScoreValue, ");
        sql.append("        CASE ");
        sql.append("            WHEN cst.Weight IS NOT NULL THEN cst.Weight ");
        sql.append("            WHEN s.ScoreType = 'Giữa kỳ' THEN 0.4 ");
        sql.append("            WHEN s.ScoreType = 'Cuối kỳ' THEN 0.6 ");
        sql.append("            ELSE 0.1 ");
        sql.append("        END as Weight ");
        sql.append("    FROM score s ");
        sql.append("    JOIN subjectteacher st ON s.SubjectTeacherID = st.Id ");
        sql.append("    JOIN student stu ON s.StudentID = stu.Id ");
        sql.append("    LEFT JOIN classscoretypes cst ON stu.ClassId = cst.ClassId ");
        sql.append("        AND s.SubjectTeacherID = cst.SubjectTeacherId ");
        sql.append("        AND s.SchoolYearId = cst.SchoolYearId ");
        sql.append("        AND s.ScoreType = cst.ScoreType ");
        sql.append("    WHERE s.ScoreValue IS NOT NULL ");

        if (classId != null) {
            sql.append("    AND stu.ClassId = :classId ");
        }

        if (subjectId != null) {
            sql.append("    AND st.SubjectId = :subjectId ");
        }
        if (schoolYearId != null) {
            sql.append("    AND s.SchoolYearId = :schoolYearId ");
        }

        sql.append("), StudentAvgScores AS (");
        sql.append("    SELECT ");
        sql.append("        StudentID, ");
        sql.append("        ClassId, ");
        sql.append("        SUM(ScoreValue * Weight) / SUM(Weight) as AvgScore ");
        sql.append("    FROM StudentScores ");
        sql.append("    GROUP BY StudentID, ClassId");
        sql.append(")");
        sql.append("SELECT ");
        sql.append("    CASE ");
        sql.append("        WHEN AvgScore < 4 THEN 'Dưới 4' ");
        sql.append("        WHEN AvgScore >= 4 AND AvgScore < 5 THEN '4 - 5' ");
        sql.append("        WHEN AvgScore >= 5 AND AvgScore < 6.5 THEN '5 - 6.5' ");
        sql.append("        WHEN AvgScore >= 6.5 AND AvgScore < 8 THEN '6.5 - 8' ");
        sql.append("        WHEN AvgScore >= 8 AND AvgScore < 9 THEN '8 - 9' ");
        sql.append("        ELSE '9 - 10' ");
        sql.append("    END AS score_range, ");
        sql.append("    COUNT(*) as student_count ");
        sql.append("FROM StudentAvgScores ");
        sql.append("GROUP BY score_range ");
        sql.append("ORDER BY ");
        sql.append("    CASE score_range ");
        sql.append("        WHEN 'Dưới 4' THEN 1 ");
        sql.append("        WHEN '4 - 5' THEN 2 ");
        sql.append("        WHEN '5 - 6.5' THEN 3 ");
        sql.append("        WHEN '6.5 - 8' THEN 4 ");
        sql.append("        WHEN '8 - 9' THEN 5 ");
        sql.append("        ELSE 6 ");
        sql.append("    END ");

        Query query = session.createNativeQuery(sql.toString());

        if (classId != null) {
            query.setParameter("classId", classId);
        }

        if (subjectId != null) {
            query.setParameter("subjectId", subjectId);
        }
        if (schoolYearId != null) {
            query.setParameter("schoolYearId", schoolYearId);
        }

        List<Object[]> rows = query.getResultList();

        // Cập nhật kết quả từ cơ sở dữ liệu
        for (Object[] row : rows) {
            String range = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            result.put(range, count);
        }

        return result;
    }

    @Override
    public List<Object[]> getAverageScoreBySubject(Integer schoolYearId, Integer teacherId) {
        Session session = this.factory.getObject().getCurrentSession();

        StringBuilder sql = new StringBuilder();
        sql.append("WITH StudentScores AS (");
        sql.append("    SELECT ");
        sql.append("        s.StudentID, ");
        sql.append("        st.SubjectId, ");
        sql.append("        s.ScoreType, ");
        sql.append("        s.ScoreValue, ");
        sql.append("        CASE ");
        sql.append("            WHEN cst.Weight IS NOT NULL THEN cst.Weight ");
        sql.append("            WHEN s.ScoreType = 'Giữa kỳ' THEN 0.4 ");
        sql.append("            WHEN s.ScoreType = 'Cuối kỳ' THEN 0.6 ");
        sql.append("            ELSE 0.1 ");
        sql.append("        END as Weight ");
        sql.append("    FROM score s ");
        sql.append("    JOIN subjectteacher st ON s.SubjectTeacherID = st.Id ");
        sql.append("    JOIN student stu ON s.StudentID = stu.Id ");
        sql.append("    JOIN class c ON stu.ClassId = c.Id ");
        sql.append("    LEFT JOIN classscoretypes cst ON c.Id = cst.ClassId ");
        sql.append("        AND s.SubjectTeacherID = cst.SubjectTeacherId ");
        sql.append("        AND s.SchoolYearId = cst.SchoolYearId ");
        sql.append("        AND s.ScoreType = cst.ScoreType ");
        sql.append("    WHERE s.ScoreValue IS NOT NULL ");

        if (schoolYearId != null) {
            sql.append("    AND s.SchoolYearId = :schoolYearId ");
        }
        if (teacherId != null) {
            sql.append("    AND st.TeacherId = :teacherId ");
        }

        sql.append("), StudentAvgScores AS (");
        sql.append("    SELECT ");
        sql.append("        StudentID, ");
        sql.append("        SubjectId, ");
        sql.append("        SUM(ScoreValue * Weight) / SUM(Weight) as AvgScore ");
        sql.append("    FROM StudentScores ");
        sql.append("    GROUP BY StudentID, SubjectId");
        sql.append("), ScoreTypeValues AS (");
        sql.append("    SELECT ");
        sql.append("        st.SubjectId, ");
        sql.append("        s.ScoreType, ");
        sql.append("        AVG(s.ScoreValue) as TypeAvgScore ");
        sql.append("    FROM score s ");
        sql.append("    JOIN subjectteacher st ON s.SubjectTeacherID = st.Id ");
        sql.append("    WHERE s.ScoreValue IS NOT NULL ");

        if (schoolYearId != null) {
            sql.append("    AND s.SchoolYearId = :schoolYearId ");
        }
        if (teacherId != null) {
            sql.append("    AND st.TeacherId = :teacherId ");
        }

        sql.append("    GROUP BY st.SubjectId, s.ScoreType");
        sql.append(")");

        // Sử dụng JSON_ARRAYAGG hoặc GROUP_CONCAT tùy phiên bản MySQL
        try {
            sql.append("SELECT ");
            sql.append("    sub.Id as subjectId, ");
            sql.append("    sub.SubjectName as subjectName, ");
            sql.append("    AVG(sas.AvgScore) as avgScore, ");
            sql.append("    COUNT(DISTINCT sas.StudentID) as studentCount, ");
            sql.append("    (");
            sql.append("        SELECT JSON_ARRAYAGG(");
            sql.append("            JSON_OBJECT(");
            sql.append("                'scoreType', stv.ScoreType,");
            sql.append("                'avgValue', stv.TypeAvgScore");
            sql.append("            )");
            sql.append("        ) ");
            sql.append("        FROM ScoreTypeValues stv ");
            sql.append("        WHERE stv.SubjectId = sub.Id");
            sql.append("    ) as scoreTypeDetails ");
            sql.append("FROM StudentAvgScores sas ");
            sql.append("JOIN subject sub ON sas.SubjectId = sub.Id ");
            sql.append("GROUP BY sub.Id, sub.SubjectName ");
            sql.append("ORDER BY avgScore DESC");

            Query query = session.createNativeQuery(sql.toString());

            if (schoolYearId != null) {
                query.setParameter("schoolYearId", schoolYearId);
            }
            if (teacherId != null) {
                query.setParameter("teacherId", teacherId);
            }

            return query.getResultList();
        } catch (Exception e) {
            // Fallback nếu MySQL không hỗ trợ JSON_ARRAYAGG
            System.err.println("JSON_ARRAYAGG không được hỗ trợ: " + e.getMessage());

            // Sử dụng truy vấn thay thế không có JSON
            sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append("    sub.Id as subjectId, ");
            sql.append("    sub.SubjectName as subjectName, ");
            sql.append("    AVG(sas.AvgScore) as avgScore, ");
            sql.append("    COUNT(DISTINCT sas.StudentID) as studentCount, ");
            sql.append("    NULL as scoreTypeDetails ");
            sql.append("FROM StudentAvgScores sas ");
            sql.append("JOIN subject sub ON sas.SubjectId = sub.Id ");
            sql.append("GROUP BY sub.Id, sub.SubjectName ");
            sql.append("ORDER BY avgScore DESC");

            Query query = session.createNativeQuery(sql.toString());

            if (schoolYearId != null) {
                query.setParameter("schoolYearId", schoolYearId);
            }
            if (teacherId != null) {
                query.setParameter("teacherId", teacherId);
            }

            return query.getResultList();
        }
    }

    @Override
    public Map<String, Object> getPassFailRateByFilter(Integer classId, Integer majorId, Integer departmentId,
            Integer schoolYearId, Integer subjectId, Integer teacherId) {
        Session session = this.factory.getObject().getCurrentSession();
        Map<String, Object> result = new HashMap<>();

        StringBuilder sql = new StringBuilder();
        sql.append("WITH StudentScores AS (");
        sql.append("    SELECT ");
        sql.append("        s.StudentID, ");
        sql.append("        stu.ClassId, ");
        sql.append("        c.MajorId, ");
        sql.append("        m.DepartmentId, ");
        sql.append("        s.SchoolYearId, ");
        sql.append("        st.SubjectId, ");
        sql.append("        st.TeacherId, ");
        sql.append("        s.ScoreType, ");
        sql.append("        s.ScoreValue, ");
        sql.append("        CASE ");
        sql.append("            WHEN cst.Weight IS NOT NULL THEN cst.Weight ");
        sql.append("            WHEN s.ScoreType = 'Giữa kỳ' THEN 0.4 ");
        sql.append("            WHEN s.ScoreType = 'Cuối kỳ' THEN 0.6 ");
        sql.append("            ELSE 0.1 ");
        sql.append("        END as Weight ");
        sql.append("    FROM score s ");
        sql.append("    JOIN subjectteacher st ON s.SubjectTeacherID = st.Id ");
        sql.append("    JOIN student stu ON s.StudentID = stu.Id ");
        sql.append("    JOIN class c ON stu.ClassId = c.Id ");
        sql.append("    JOIN major m ON c.MajorId = m.Id ");
        sql.append("    JOIN department d ON m.DepartmentId = d.Id ");
        sql.append("    LEFT JOIN classscoretypes cst ON c.Id = cst.ClassId ");
        sql.append("        AND s.SubjectTeacherID = cst.SubjectTeacherId ");
        sql.append("        AND s.SchoolYearId = cst.SchoolYearId ");
        sql.append("        AND s.ScoreType = cst.ScoreType ");
        sql.append("    WHERE s.ScoreValue IS NOT NULL ");

        if (classId != null) {
            sql.append("    AND stu.ClassId = :classId ");
        }
        if (majorId != null) {
            sql.append("    AND c.MajorId = :majorId ");
        }
        if (departmentId != null) {
            sql.append("    AND m.DepartmentId = :departmentId ");
        }
        if (schoolYearId != null) {
            sql.append("    AND s.SchoolYearId = :schoolYearId ");
        }
        if (subjectId != null) {
            sql.append("    AND st.SubjectId = :subjectId ");
        }
        if (teacherId != null) {
            sql.append("    AND st.TeacherId = :teacherId ");
        }

        sql.append("), StudentAvgScores AS (");
        sql.append("    SELECT ");
        sql.append("        StudentID, ");
        sql.append("        ClassId, ");
        sql.append("        MajorId, ");
        sql.append("        DepartmentId, ");
        sql.append("        SUM(ScoreValue * Weight) / SUM(Weight) as AvgScore ");
        sql.append("    FROM StudentScores ");
        sql.append("    GROUP BY StudentID, ClassId, MajorId, DepartmentId");
        sql.append(")");
        sql.append("SELECT ");
        sql.append("    SUM(CASE WHEN AvgScore >= 5.0 THEN 1 ELSE 0 END) as pass_count, ");
        sql.append("    SUM(CASE WHEN AvgScore < 5.0 THEN 1 ELSE 0 END) as fail_count, ");
        sql.append("    COUNT(*) as total_count ");
        sql.append("FROM StudentAvgScores ");

        Query query = session.createNativeQuery(sql.toString());

        if (classId != null) {
            query.setParameter("classId", classId);
        }
        if (majorId != null) {
            query.setParameter("majorId", majorId);
        }
        if (departmentId != null) {
            query.setParameter("departmentId", departmentId);
        }
        if (schoolYearId != null) {
            query.setParameter("schoolYearId", schoolYearId);
        }
        if (subjectId != null) {
            query.setParameter("subjectId", subjectId);
        }
        if (teacherId != null) {
            query.setParameter("teacherId", teacherId);
        }

        Object[] resultRow = (Object[]) query.getSingleResult();

        Long passCount = resultRow[0] != null ? ((Number) resultRow[0]).longValue() : 0L;
        Long failCount = resultRow[1] != null ? ((Number) resultRow[1]).longValue() : 0L;
        Long totalCount = resultRow[2] != null ? ((Number) resultRow[2]).longValue() : 0L;

        double passRate = totalCount > 0 ? (double) passCount / totalCount : 0;
        double failRate = totalCount > 0 ? (double) failCount / totalCount : 0;

        result.put("passCount", passCount);
        result.put("failCount", failCount);
        result.put("totalCount", totalCount);
        result.put("passRate", passRate);
        result.put("failRate", failRate);

        return result;
    }

    @Override
    public List<Object[]> getStudentCountByMajor(Integer departmentId, Integer schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();

        StringBuilder queryStr = new StringBuilder(
                "SELECT "
                + "  m.id, "
                + "  m.majorName, "
                + "  t.trainingTypeName, "
                + "  d.departmentName, "
                + "  COUNT(s.id) as studentCount "
                + "FROM Student s "
                + "JOIN s.classId c "
                + "JOIN c.majorId m "
                + "JOIN m.trainingTypeId t "
                + "JOIN m.departmentId d "
                + "WHERE 1=1");

        if (departmentId != null) {
            queryStr.append(" AND d.id = :departmentId");
        }

        queryStr.append(" GROUP BY m.id, m.majorName, t.trainingTypeName, d.departmentName");

        org.hibernate.query.Query query = session.createQuery(queryStr.toString());

        if (departmentId != null) {
            query.setParameter("departmentId", departmentId);
        }

        return query.getResultList();
    }

    @Override
    public List<Object[]> getAverageScoreByClass(Integer subjectId, Integer schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();

        StringBuilder sql = new StringBuilder();
        sql.append("WITH StudentScores AS (");
        sql.append("    SELECT ");
        sql.append("        s.StudentID, ");
        sql.append("        stu.ClassId, ");
        sql.append("        st.SubjectId, ");
        sql.append("        s.ScoreType, ");
        sql.append("        s.ScoreValue, ");
        sql.append("        CASE ");
        sql.append("            WHEN cst.Weight IS NOT NULL THEN cst.Weight ");
        sql.append("            WHEN s.ScoreType = 'Giữa kỳ' THEN 0.4 ");
        sql.append("            WHEN s.ScoreType = 'Cuối kỳ' THEN 0.6 ");
        sql.append("            ELSE 0.1 ");
        sql.append("        END as Weight ");
        sql.append("    FROM score s ");
        sql.append("    JOIN subjectteacher st ON s.SubjectTeacherID = st.Id ");
        sql.append("    JOIN student stu ON s.StudentID = stu.Id ");
        sql.append("    JOIN class c ON stu.ClassId = c.Id ");
        sql.append("    LEFT JOIN classscoretypes cst ON c.Id = cst.ClassId ");
        sql.append("        AND s.SubjectTeacherID = cst.SubjectTeacherId ");
        sql.append("        AND s.SchoolYearId = cst.SchoolYearId ");
        sql.append("        AND s.ScoreType = cst.ScoreType ");
        sql.append("    WHERE s.ScoreValue IS NOT NULL ");

        if (subjectId != null) {
            sql.append("    AND st.SubjectId = :subjectId ");
        }
        if (schoolYearId != null) {
            sql.append("    AND s.SchoolYearId = :schoolYearId ");
        }

        sql.append("), StudentAvgScores AS (");
        sql.append("    SELECT ");
        sql.append("        StudentID, ");
        sql.append("        ClassId, ");
        sql.append("        SUM(ScoreValue * Weight) / SUM(Weight) as AvgScore ");
        sql.append("    FROM StudentScores ");
        sql.append("    GROUP BY StudentID, ClassId");
        sql.append(")");
        sql.append("SELECT ");
        sql.append("    c.Id as classId, ");
        sql.append("    c.ClassName as className, ");
        sql.append("    m.MajorName as majorName, ");
        sql.append("    AVG(sas.AvgScore) as avgScore, ");
        sql.append("    COUNT(DISTINCT sas.StudentID) as studentCount ");
        sql.append("FROM StudentAvgScores sas ");
        sql.append("JOIN class c ON sas.ClassId = c.Id ");
        sql.append("JOIN major m ON c.MajorId = m.Id ");
        sql.append("GROUP BY c.Id, c.ClassName, m.MajorName ");
        sql.append("ORDER BY avgScore DESC");

        Query query = session.createNativeQuery(sql.toString());

        if (subjectId != null) {
            query.setParameter("subjectId", subjectId);
        }
        if (schoolYearId != null) {
            query.setParameter("schoolYearId", schoolYearId);
        }

        return query.getResultList();
    }

    @Override
    public List<Object[]> getAverageScoreByDepartment() {
        return getAverageScoreByDepartment(null);
    }

    @Override
    public List<Object[]> getAverageScoreByDepartment(Integer schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();

        StringBuilder sql = new StringBuilder();
        sql.append("WITH StudentScores AS (");
        sql.append("    SELECT ");
        sql.append("        s.StudentID, ");
        sql.append("        m.DepartmentId, ");
        sql.append("        s.ScoreType, ");
        sql.append("        s.ScoreValue, ");
        sql.append("        CASE ");
        sql.append("            WHEN cst.Weight IS NOT NULL THEN cst.Weight ");
        sql.append("            WHEN s.ScoreType = 'Giữa kỳ' THEN 0.4 ");
        sql.append("            WHEN s.ScoreType = 'Cuối kỳ' THEN 0.6 ");
        sql.append("            ELSE 0.1 ");
        sql.append("        END as Weight ");
        sql.append("    FROM score s ");
        sql.append("    JOIN student stu ON s.StudentID = stu.Id ");
        sql.append("    JOIN class c ON stu.ClassId = c.Id ");
        sql.append("    JOIN major m ON c.MajorId = m.Id ");
        sql.append("    JOIN department d ON m.DepartmentId = d.Id ");
        sql.append("    LEFT JOIN classscoretypes cst ON c.Id = cst.ClassId ");
        sql.append("        AND s.SubjectTeacherID = cst.SubjectTeacherId ");
        sql.append("        AND s.SchoolYearId = cst.SchoolYearId ");
        sql.append("        AND s.ScoreType = cst.ScoreType ");
        sql.append("    WHERE s.ScoreValue IS NOT NULL ");

        if (schoolYearId != null) {
            sql.append("    AND s.SchoolYearId = :schoolYearId ");
        }

        sql.append("), StudentAvgScores AS (");
        sql.append("    SELECT ");
        sql.append("        StudentID, ");
        sql.append("        DepartmentId, ");
        sql.append("        SUM(ScoreValue * Weight) / SUM(Weight) as AvgScore ");
        sql.append("    FROM StudentScores ");
        sql.append("    GROUP BY StudentID, DepartmentId");
        sql.append(")");
        sql.append("SELECT ");
        sql.append("    d.Id as departmentId, ");
        sql.append("    d.DepartmentName as departmentName, ");
        sql.append("    AVG(sas.AvgScore) as avgScore, ");
        sql.append("    COUNT(DISTINCT sas.StudentID) as studentCount ");
        sql.append("FROM StudentAvgScores sas ");
        sql.append("JOIN department d ON sas.DepartmentId = d.Id ");
        sql.append("GROUP BY d.Id, d.DepartmentName ");
        sql.append("ORDER BY avgScore DESC");

        Query query = session.createNativeQuery(sql.toString());

        if (schoolYearId != null) {
            query.setParameter("schoolYearId", schoolYearId);
        }

        return query.getResultList();
    }

    @Override
    public List<Object[]> getTopStudentsBySubject(Integer subjectId, Integer schoolYearId, Integer limit) {
        Session session = this.factory.getObject().getCurrentSession();

        StringBuilder sql = new StringBuilder();
        sql.append("WITH StudentScores AS (");
        sql.append("    SELECT ");
        sql.append("        s.StudentID, ");
        sql.append("        st.SubjectId, ");
        sql.append("        s.ScoreType, ");
        sql.append("        s.ScoreValue, ");
        sql.append("        CASE ");
        sql.append("            WHEN cst.Weight IS NOT NULL THEN cst.Weight ");
        sql.append("            WHEN s.ScoreType = 'Giữa kỳ' THEN 0.4 ");
        sql.append("            WHEN s.ScoreType = 'Cuối kỳ' THEN 0.6 ");
        sql.append("            ELSE 0.1 ");
        sql.append("        END as Weight ");
        sql.append("    FROM score s ");
        sql.append("    JOIN subjectteacher st ON s.SubjectTeacherID = st.Id ");
        sql.append("    JOIN student stu ON s.StudentID = stu.Id ");
        sql.append("    JOIN class c ON stu.ClassId = c.Id ");
        sql.append("    LEFT JOIN classscoretypes cst ON c.Id = cst.ClassId ");
        sql.append("        AND s.SubjectTeacherID = cst.SubjectTeacherId ");
        sql.append("        AND s.SchoolYearId = cst.SchoolYearId ");
        sql.append("        AND s.ScoreType = cst.ScoreType ");
        sql.append("    WHERE s.ScoreValue IS NOT NULL ");

        if (subjectId != null) {
            sql.append("    AND st.SubjectId = :subjectId ");
        }
        if (schoolYearId != null) {
            sql.append("    AND s.SchoolYearId = :schoolYearId ");
        }

        sql.append("), StudentAvgScores AS (");
        sql.append("    SELECT ");
        sql.append("        s.StudentID, ");
        sql.append("        stu.FirstName, ");
        sql.append("        stu.LastName, ");
        sql.append("        stu.StudentCode, ");
        sql.append("        st.SubjectId, ");
        sql.append("        SUM(s.ScoreValue * ss.Weight) / SUM(ss.Weight) as AvgScore ");
        sql.append("    FROM ");
        sql.append("        score s ");
        sql.append("    JOIN ");
        sql.append("        StudentScores ss ON s.StudentID = ss.StudentID AND s.ScoreType = ss.ScoreType ");
        sql.append("    JOIN ");
        sql.append("        student stu ON s.StudentID = stu.Id ");
        sql.append("    JOIN ");
        sql.append("        subjectteacher st ON s.SubjectTeacherID = st.Id ");
        sql.append("    WHERE s.ScoreValue IS NOT NULL ");

        if (subjectId != null) {
            sql.append("    AND st.SubjectId = :subjectId ");
        }
        if (schoolYearId != null) {
            sql.append("    AND s.SchoolYearId = :schoolYearId ");
        }

        sql.append("    GROUP BY ");
        sql.append("        s.StudentID, stu.FirstName, stu.LastName, stu.StudentCode, st.SubjectId ");
        sql.append(")");
        sql.append("SELECT ");
        sql.append("    sas.StudentID as studentId, ");
        sql.append("    CONCAT(sas.FirstName, ' ', sas.LastName) as studentName, ");
        sql.append("    sas.StudentCode as studentCode, ");
        sql.append("    sas.AvgScore as averageScore ");
        sql.append("FROM ");
        sql.append("    StudentAvgScores sas ");
        sql.append("ORDER BY ");
        sql.append("    sas.AvgScore DESC ");
        sql.append("LIMIT :limit");

        Query query = session.createNativeQuery(sql.toString());

        if (subjectId != null) {
            query.setParameter("subjectId", subjectId);
        }
        if (schoolYearId != null) {
            query.setParameter("schoolYearId", schoolYearId);
        }
        query.setParameter("limit", limit == null ? 5 : limit);

        return query.getResultList();
    }

    @Override
    public List<Object[]> getScoreTrendsBySubject(Integer subjectId) {
        if (subjectId == null) {
            return Collections.emptyList();
        }

        Session session = this.factory.getObject().getCurrentSession();

        StringBuilder sql = new StringBuilder();
        sql.append("WITH SemesterScores AS (");
        sql.append("    SELECT ");
        sql.append("        sy.Id as SchoolYearId, ");
        sql.append("        sy.SemesterName as SemesterName, ");
        sql.append("        CONCAT(sy.YearStart, '-', sy.YearEnd) as AcademicYear, ");
        sql.append("        s.StudentID, ");
        sql.append("        st.SubjectId, ");
        sql.append("        s.ScoreType, ");
        sql.append("        s.ScoreValue, ");
        sql.append("        CASE ");
        sql.append("            WHEN cst.Weight IS NOT NULL THEN cst.Weight ");
        sql.append("            WHEN s.ScoreType = 'Giữa kỳ' THEN 0.4 ");
        sql.append("            WHEN s.ScoreType = 'Cuối kỳ' THEN 0.6 ");
        sql.append("            ELSE 0.1 ");
        sql.append("        END as Weight ");
        sql.append("    FROM score s ");
        sql.append("    JOIN schoolyear sy ON s.SchoolYearId = sy.Id ");
        sql.append("    JOIN subjectteacher st ON s.SubjectTeacherID = st.Id ");
        sql.append("    JOIN student stu ON s.StudentID = stu.Id ");
        sql.append("    JOIN class c ON stu.ClassId = c.Id ");
        sql.append("    LEFT JOIN classscoretypes cst ON c.Id = cst.ClassId ");
        sql.append("        AND s.SubjectTeacherID = cst.SubjectTeacherId ");
        sql.append("        AND s.SchoolYearId = cst.SchoolYearId ");
        sql.append("        AND s.ScoreType = cst.ScoreType ");
        sql.append("    WHERE s.ScoreValue IS NOT NULL ");
        sql.append("    AND st.SubjectId = :subjectId ");
        sql.append("), StudentSemesterScores AS (");
        sql.append("    SELECT ");
        sql.append("        SchoolYearId, ");
        sql.append("        SemesterName, ");
        sql.append("        AcademicYear, ");
        sql.append("        StudentID, ");
        sql.append("        SubjectId, ");
        sql.append("        SUM(ScoreValue * Weight) / SUM(Weight) as AvgScore ");
        sql.append("    FROM SemesterScores ");
        sql.append("    GROUP BY SchoolYearId, SemesterName, AcademicYear, StudentID, SubjectId");
        sql.append(")");
        sql.append("SELECT ");
        sql.append("    SchoolYearId, ");
        sql.append("    CONCAT(SemesterName, ' ', AcademicYear) as semester, ");
        sql.append("    AVG(AvgScore) as averageScore, ");
        sql.append("    COUNT(DISTINCT StudentID) as studentCount ");
        sql.append("FROM StudentSemesterScores ");
        sql.append("GROUP BY SchoolYearId, SemesterName, AcademicYear ");
        sql.append("ORDER BY SchoolYearId");

        Query query = session.createNativeQuery(sql.toString());
        query.setParameter("subjectId", subjectId);

        return query.getResultList();
    }

    @Override
    public Map<String, Long> getScoreDistributionByMajor(Integer majorId, Integer schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        Map<String, Long> result = new LinkedHashMap<>();

        // Khởi tạo các khoảng điểm với giá trị mặc định là 0
        result.put("0-2", 0L);
        result.put("2-4", 0L);
        result.put("4-6", 0L);
        result.put("6-8", 0L);
        result.put("8-10", 0L);

        if (majorId == null) {
            return result;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("WITH StudentScores AS (");
        sql.append("    SELECT ");
        sql.append("        s.StudentID, ");
        sql.append("        m.Id AS MajorId, ");
        sql.append("        s.ScoreType, ");
        sql.append("        s.ScoreValue, ");
        sql.append("        CASE ");
        sql.append("            WHEN cst.Weight IS NOT NULL THEN cst.Weight ");
        sql.append("            WHEN s.ScoreType = 'Giữa kỳ' THEN 0.4 ");
        sql.append("            WHEN s.ScoreType = 'Cuối kỳ' THEN 0.6 ");
        sql.append("            ELSE 0.1 ");
        sql.append("        END as Weight ");
        sql.append("    FROM score s ");
        sql.append("    JOIN student stu ON s.StudentID = stu.Id ");
        sql.append("    JOIN class c ON stu.ClassId = c.Id ");
        sql.append("    JOIN major m ON c.MajorId = m.Id ");
        sql.append("    LEFT JOIN classscoretypes cst ON c.Id = cst.ClassId ");
        sql.append("        AND s.SubjectTeacherID = cst.SubjectTeacherId ");
        sql.append("        AND s.SchoolYearId = cst.SchoolYearId ");
        sql.append("        AND s.ScoreType = cst.ScoreType ");
        sql.append("    WHERE s.ScoreValue IS NOT NULL ");
        sql.append("    AND m.Id = :majorId ");

        if (schoolYearId != null) {
            sql.append("    AND s.SchoolYearId = :schoolYearId ");
        }

        sql.append("), StudentAvgScores AS (");
        sql.append("    SELECT ");
        sql.append("        StudentID, ");
        sql.append("        MajorId, ");
        sql.append("        SUM(ScoreValue * Weight) / SUM(Weight) as AvgScore ");
        sql.append("    FROM StudentScores ");
        sql.append("    GROUP BY StudentID, MajorId");
        sql.append(")");
        sql.append("SELECT ");
        sql.append("    CASE ");
        sql.append("        WHEN AvgScore >= 0 AND AvgScore < 2 THEN '0-2' ");
        sql.append("        WHEN AvgScore >= 2 AND AvgScore < 4 THEN '2-4' ");
        sql.append("        WHEN AvgScore >= 4 AND AvgScore < 6 THEN '4-6' ");
        sql.append("        WHEN AvgScore >= 6 AND AvgScore < 8 THEN '6-8' ");
        sql.append("        ELSE '8-10' ");
        sql.append("    END AS score_range, ");
        sql.append("    COUNT(*) as student_count ");
        sql.append("FROM StudentAvgScores ");
        sql.append("GROUP BY score_range ");
        sql.append("ORDER BY score_range");

        Query query = session.createNativeQuery(sql.toString());
        query.setParameter("majorId", majorId);

        if (schoolYearId != null) {
            query.setParameter("schoolYearId", schoolYearId);
        }

        List<Object[]> rows = query.getResultList();

        for (Object[] row : rows) {
            String range = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            result.put(range, count);
        }

        return result;
    }
}
