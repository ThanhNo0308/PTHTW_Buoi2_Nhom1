package com.ntn.repository.impl;

import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Subjectteacher;
import com.ntn.repository.SchoolYearRepository;
import java.util.List;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.Calendar;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SchoolYearRepositoryImp implements SchoolYearRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Schoolyear getSchoolYearById(int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Schoolyear> query = builder.createQuery(Schoolyear.class);
        Root<Schoolyear> root = query.from(Schoolyear.class);

        query.select(root);
        query.where(builder.equal(root.get("id"), schoolYearId));

        List<Schoolyear> schoolYears = session.createQuery(query).getResultList();

        if (!schoolYears.isEmpty()) {
            return schoolYears.get(0);
        } else {
            return null;
        }
    }

    @Override
    public int getCurrentSchoolYearId() {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            // Tìm năm học hiện tại dựa trên ngày hiện tại
            java.util.Date now = new java.util.Date();

            // Tìm năm học hiện tại
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Schoolyear> query = builder.createQuery(Schoolyear.class);
            Root<Schoolyear> root = query.from(Schoolyear.class);

            // Tìm năm học có khoảng thời gian chứa ngày hiện tại
            query.select(root);
            query.where(
                    builder.and(
                            builder.lessThanOrEqualTo(root.get("yearStart"), now),
                            builder.greaterThanOrEqualTo(root.get("yearEnd"), now)
                    )
            );
            query.orderBy(builder.desc(root.get("id")));

            List<Schoolyear> results = session.createQuery(query)
                    .setMaxResults(1)
                    .getResultList();

            if (!results.isEmpty()) {
                return results.get(0).getId();
            }

            // Nếu không tìm thấy năm học đang hoạt động, lấy năm học gần nhất
            CriteriaQuery<Schoolyear> latestQuery = builder.createQuery(Schoolyear.class);
            Root<Schoolyear> latestRoot = latestQuery.from(Schoolyear.class);
            latestQuery.select(latestRoot);
            latestQuery.orderBy(builder.desc(latestRoot.get("id")));

            results = session.createQuery(latestQuery)
                    .setMaxResults(1)
                    .getResultList();

            if (!results.isEmpty()) {
                return results.get(0).getId();
            }

            // Nếu không có năm học nào trong DB
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<Schoolyear> getAllSchoolYears() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Schoolyear> query = builder.createQuery(Schoolyear.class);
        Root<Schoolyear> root = query.from(Schoolyear.class);

        query.orderBy(builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public boolean addOrUpdateSchoolYear(Schoolyear schoolYear) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            // Tự động tạo nameYear từ yearStart và yearEnd (từ Date)
            if (schoolYear.getYearStart() != null && schoolYear.getYearEnd() != null) {
                Calendar calStart = Calendar.getInstance();
                Calendar calEnd = Calendar.getInstance();

                calStart.setTime(schoolYear.getYearStart());
                calEnd.setTime(schoolYear.getYearEnd());

                int startYear = calStart.get(Calendar.YEAR);
                int endYear = calEnd.get(Calendar.YEAR);

                String nameYear = startYear + "-" + endYear;
                schoolYear.setNameYear(nameYear);

            }

            // Lưu hoặc cập nhật năm học
            if (schoolYear.getId() == null) {
                // Thêm mới
                session.persist(schoolYear);
            } else {
                // Cập nhật
                session.merge(schoolYear);
            }
            session.flush();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error saving school year: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteSchoolYear(int schoolYearId) {
        try {
            Session session = this.factory.getObject().getCurrentSession();

            // Kiểm tra xem có dữ liệu liên quan không
            if (hasRelatedData(schoolYearId)) {
                return false;
            }

            // Sử dụng CriteriaBuilder để lấy đối tượng Schoolyear cần xóa
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Schoolyear> query = builder.createQuery(Schoolyear.class);
            Root<Schoolyear> root = query.from(Schoolyear.class);

            query.select(root);
            query.where(builder.equal(root.get("id"), schoolYearId));

            List<Schoolyear> schoolYears = session.createQuery(query).getResultList();

            if (!schoolYears.isEmpty()) {
                Schoolyear schoolYearToDelete = schoolYears.get(0);
                session.delete(schoolYearToDelete);
                return true;
            }

            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Schoolyear> getSchoolYearsByNameYear(String nameYear) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Schoolyear> query = builder.createQuery(Schoolyear.class);
        Root<Schoolyear> root = query.from(Schoolyear.class);

        query.where(builder.equal(root.get("nameYear"), nameYear));
        query.orderBy(builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Schoolyear> getSchoolYearsBySemester(String semesterName) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Schoolyear> query = builder.createQuery(Schoolyear.class);
        Root<Schoolyear> root = query.from(Schoolyear.class);

        query.where(builder.equal(root.get("semesterName"), semesterName));
        query.orderBy(builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Schoolyear> getSchoolYearsByNameYearAndSemester(String nameYear, String semesterName) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Schoolyear> query = builder.createQuery(Schoolyear.class);
        Root<Schoolyear> root = query.from(Schoolyear.class);

        query.where(
                builder.and(
                        builder.equal(root.get("nameYear"), nameYear),
                        builder.equal(root.get("semesterName"), semesterName)
                )
        );
        query.orderBy(builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public boolean hasRelatedData(int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();

        try {
            // Kiểm tra điểm (giữ nguyên vì đúng)
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> scoreQuery = builder.createQuery(Long.class);
            Root<Score> scoreRoot = scoreQuery.from(Score.class);

            scoreQuery.select(builder.count(scoreRoot));
            scoreQuery.where(builder.equal(scoreRoot.get("schoolYearId").get("id"), schoolYearId));

            Long scoreCount = session.createQuery(scoreQuery).getSingleResult();
            if (scoreCount > 0) {
                return true;
            }

            // Kiểm tra trong SubjectTeacher thay vì StudentSubjectTeacher
            CriteriaQuery<Long> stQuery = builder.createQuery(Long.class);
            Root<Subjectteacher> stRoot = stQuery.from(Subjectteacher.class);

            stQuery.select(builder.count(stRoot));
            stQuery.where(builder.equal(stRoot.get("schoolYearId").get("id"), schoolYearId));

            Long stCount = session.createQuery(stQuery).getSingleResult();
            return stCount > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra dữ liệu liên quan: " + e.getMessage());
            e.printStackTrace();
            return false; // Thay đổi thành false để cho phép xóa khi có lỗi không xác định
        }
    }
}
