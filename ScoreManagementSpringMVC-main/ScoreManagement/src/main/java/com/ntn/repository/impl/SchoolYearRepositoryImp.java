package com.ntn.repository.impl;

import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import com.ntn.repository.SchoolYearRepository;
import jakarta.persistence.NoResultException;
import java.util.List;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SchoolYearRepositoryImp implements SchoolYearRepository {

    @Autowired
    private LocalSessionFactoryBean factory;
    @Autowired
    private Environment env;

    @Override
    public List<Schoolyear> getListSchoolYear(String currentYear) {
        Session s = this.factory.getObject().getCurrentSession();
        Query q = s.createQuery("FROM Schoolyear WHERE NameYear = :currentYear");
        q.setParameter("currentYear", currentYear);
        List<Schoolyear> schoolYears = q.getResultList();
        return schoolYears;
    }

    @Override
    public Schoolyear getSchoolYearById(int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        Query query = session.createQuery("FROM Schoolyear WHERE id = :schoolYearId");
        query.setParameter("schoolYearId", schoolYearId);

        List<Schoolyear> schoolYears = query.getResultList();

        if (!schoolYears.isEmpty()) {
            return schoolYears.get(0);
        } else {
            return null;
        }
    }

    @Override
    public int getCurrentSchoolYearId() {
        int currentYear = java.time.Year.now().getValue();

        Session session = this.factory.getObject().getCurrentSession();
        try {
            // Tìm năm học hiện tại dựa trên ngày hiện tại
            java.util.Date now = new java.util.Date();
            
            Query query = session.createQuery(
                "FROM Schoolyear s WHERE :now BETWEEN s.yearStart AND s.yearEnd ORDER BY s.id DESC");
            query.setParameter("now", now);
            query.setMaxResults(1);
            
            List<Schoolyear> results = query.getResultList();
            
            if (!results.isEmpty()) {
                return results.get(0).getId();
            }
            
            // Nếu không tìm thấy năm học đang hoạt động, lấy năm học gần nhất
            query = session.createQuery("FROM Schoolyear ORDER BY id DESC");
            query.setMaxResults(1);
            results = query.getResultList();
            
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
    public Subjectteacher getSubJectTeacherById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Subjectteacher.class, id);
    }

    @Override
    public Typescore getScoreTypeByName(String name) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Typescore> query = builder.createQuery(Typescore.class);
        Root<Typescore> root = query.from(Typescore.class);

        query.where(builder.equal(root.get("scoreType"), name));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            // Nếu không tìm thấy, tạo loại điểm mới
            Typescore newType = new Typescore(name);
            session.save(newType);
            return newType;
        }
    }

    @Override
    public boolean addOrUpdateSchoolYear(Schoolyear schoolYear) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            System.out.println("=== SCHOOL YEAR INFO BEFORE SAVE ===");
            System.out.println("ID: " + schoolYear.getId());
            
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
                
                System.out.println("Generated nameYear: " + nameYear);
                System.out.println("Start Date: " + new SimpleDateFormat("yyyy-MM-dd").format(schoolYear.getYearStart()));
                System.out.println("End Date: " + new SimpleDateFormat("yyyy-MM-dd").format(schoolYear.getYearEnd()));
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
            System.out.println("Saved school year with ID: " + schoolYear.getId());
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
            Query checkScoresQuery = session.createQuery("SELECT COUNT(s) FROM Score s WHERE s.schoolYearId.id = :schoolYearId");
            checkScoresQuery.setParameter("schoolYearId", schoolYearId);
            Long scoreCount = (Long) checkScoresQuery.getSingleResult();

            if (scoreCount > 0) {
                return false;
            }

            org.hibernate.query.Query query = session.createNativeQuery("DELETE FROM schoolyear WHERE Id = :id");
            query.setParameter("id", schoolYearId);

            int result = query.executeUpdate();
            return result > 0;
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
            // Kiểm tra điểm số
            Query checkScoresQuery = session.createQuery("SELECT COUNT(s) FROM Score s WHERE s.schoolYearId.id = :schoolYearId");
            checkScoresQuery.setParameter("schoolYearId", schoolYearId);
            Long scoreCount = (Long) checkScoresQuery.getSingleResult();

            if (scoreCount > 0) {
                return true;
            }

            // Kiểm tra lịch học
            Query checkSST = session.createQuery("SELECT COUNT(s) FROM Studentsubjectteacher s WHERE s.schoolYearId.id = :schoolYearId");
            checkSST.setParameter("schoolYearId", schoolYearId);
            Long sstCount = (Long) checkSST.getSingleResult();

            return sstCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Nếu có lỗi, giả định là có dữ liệu liên quan để tránh xóa không an toàn
        }
    }
}