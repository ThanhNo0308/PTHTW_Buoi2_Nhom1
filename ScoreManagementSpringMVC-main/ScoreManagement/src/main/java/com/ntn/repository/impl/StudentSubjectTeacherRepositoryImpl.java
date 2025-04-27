/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Department;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Student;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subject;
import com.ntn.pojo.Subjectteacher;
import com.ntn.repository.StudentSubjectTeacherRepository;
import java.util.List;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nguye
 */
@Repository
@Transactional
public class StudentSubjectTeacherRepositoryImpl implements StudentSubjectTeacherRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Studentsubjectteacher> getStudsubjteachs() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Studentsubjectteacher> q = b.createQuery(Studentsubjectteacher.class);
        Root root = q.from(Studentsubjectteacher.class);
        q.select(root);
        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Studentsubjectteacher> getAll() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Studentsubjectteacher> query = builder.createQuery(Studentsubjectteacher.class);
        Root<Studentsubjectteacher> root = query.from(Studentsubjectteacher.class);

        query.select(root);
        query.orderBy(builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public Studentsubjectteacher getById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Studentsubjectteacher.class, id);
    }

    @Override
    @Transactional
    public boolean addOrUpdate(Studentsubjectteacher enrollment) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            if (enrollment.getId() == null) {
                session.persist(enrollment);
            } else {
                session.merge(enrollment);
            }
            session.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            Studentsubjectteacher enrollment = session.get(Studentsubjectteacher.class, id);
            if (enrollment != null) {
                session.remove(enrollment);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Studentsubjectteacher> getByStudentId(int studentId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Studentsubjectteacher> query = builder.createQuery(Studentsubjectteacher.class);
        Root<Studentsubjectteacher> root = query.from(Studentsubjectteacher.class);

        query.where(builder.equal(root.get("studentId").get("id"), studentId));
        query.orderBy(builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Studentsubjectteacher> getBySubjectTeacherId(int subjectTeacherId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Studentsubjectteacher> query = builder.createQuery(Studentsubjectteacher.class);
        Root<Studentsubjectteacher> root = query.from(Studentsubjectteacher.class);

        query.where(builder.equal(root.get("subjectTeacherId").get("id"), subjectTeacherId));
        query.orderBy(builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Studentsubjectteacher> getByTeacherId(int teacherId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Studentsubjectteacher> query = builder.createQuery(Studentsubjectteacher.class);
        Root<Studentsubjectteacher> root = query.from(Studentsubjectteacher.class);

        // Join từ Studentsubjectteacher -> SubjectTeacher -> Teacher
        Join<Studentsubjectteacher, Subjectteacher> subjectTeacherJoin = root.join("subjectTeacherId");

        query.where(builder.equal(subjectTeacherJoin.get("teacherId").get("id"), teacherId));
        query.orderBy(builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Studentsubjectteacher> getBySubjectId(int subjectId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Studentsubjectteacher> query = builder.createQuery(Studentsubjectteacher.class);
        Root<Studentsubjectteacher> root = query.from(Studentsubjectteacher.class);

        // Join từ Studentsubjectteacher -> SubjectTeacher -> Subject
        Join<Studentsubjectteacher, Subjectteacher> subjectTeacherJoin = root.join("subjectTeacherId");

        query.where(builder.equal(subjectTeacherJoin.get("subjectId").get("id"), subjectId));
        query.orderBy(builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Studentsubjectteacher> getByClassId(int classId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Studentsubjectteacher> query = builder.createQuery(Studentsubjectteacher.class);
        Root<Studentsubjectteacher> root = query.from(Studentsubjectteacher.class);

        // Join từ Studentsubjectteacher -> Student -> Class
        Join<Studentsubjectteacher, Student> studentJoin = root.join("studentId");

        query.where(builder.equal(studentJoin.get("classId").get("id"), classId));
        query.orderBy(builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }
    
    @Override
    public List<Studentsubjectteacher> getBySchoolYearId(int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Studentsubjectteacher> query = builder.createQuery(Studentsubjectteacher.class);
        Root<Studentsubjectteacher> root = query.from(Studentsubjectteacher.class);
        
        // Join từ Studentsubjectteacher -> Subjectteacher -> SchoolYear
        Join<Studentsubjectteacher, Subjectteacher> subjectTeacherJoin = root.join("subjectTeacherId");
        
        query.where(builder.equal(subjectTeacherJoin.get("schoolYearId").get("id"), schoolYearId));
        query.orderBy(builder.desc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public boolean checkDuplicate(Integer studentId, Integer subjectTeacherId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Studentsubjectteacher> root = query.from(Studentsubjectteacher.class);

        List<Predicate> predicates = new ArrayList<>();

        if (studentId != null) {
            predicates.add(builder.equal(root.get("studentId").get("id"), studentId));
        }

        if (subjectTeacherId != null) {
            predicates.add(builder.equal(root.get("subjectTeacherId").get("id"), subjectTeacherId));
        }

        query.select(builder.count(root))
                .where(predicates.toArray(new Predicate[0]));

        Long count = session.createQuery(query).getSingleResult();
        return count > 0;
    }

    @Override
    public boolean checkDuplicateExcept(Integer studentId, Integer subjectTeacherId, Integer exceptId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Studentsubjectteacher> root = query.from(Studentsubjectteacher.class);

        List<Predicate> predicates = new ArrayList<>();

        if (studentId != null) {
            predicates.add(builder.equal(root.get("studentId").get("id"), studentId));
        }

        if (subjectTeacherId != null) {
            predicates.add(builder.equal(root.get("subjectTeacherId").get("id"), subjectTeacherId));
        }

        // Loại trừ chính nó
        if (exceptId != null) {
            predicates.add(builder.notEqual(root.get("id"), exceptId));
        }

        query.select(builder.count(root))
                .where(predicates.toArray(new Predicate[0]));

        Long count = session.createQuery(query).getSingleResult();
        return count > 0;
    }

    @Override
    public boolean hasRelatedScores(int enrollmentId) {
        Session session = this.factory.getObject().getCurrentSession();

        try {
            Query checkScores = session.createQuery(
                    "SELECT COUNT(s) FROM Score s WHERE s.subjectTeacherID.id = :enrollmentId");
            checkScores.setParameter("enrollmentId", enrollmentId);
            Long scoreCount = (Long) checkScores.getSingleResult();

            return scoreCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Nếu có lỗi, giả định là có dữ liệu liên quan để tránh xóa không an toàn
        }
    }

    @Override
    public int batchEnrollStudents(int classId, int subjectTeacherId) {
        Session session = this.factory.getObject().getCurrentSession();
        Transaction tx = null;
        int enrolledCount = 0;

        try {
            // Lấy danh sách sinh viên của lớp
            Query studentQuery = session.createQuery("FROM Student s WHERE s.classId.id = :classId");
            studentQuery.setParameter("classId", classId);
            List<Student> students = studentQuery.getResultList();

            // Lấy đối tượng SubjectTeacher
            Subjectteacher subjectTeacher = session.get(Subjectteacher.class, subjectTeacherId);

            if (students.isEmpty() || subjectTeacher == null) {
                return 0;
            }

            // Đăng ký cho từng sinh viên
            for (Student student : students) {
                // Kiểm tra trùng lặp
                Query checkQuery = session.createQuery(
                        "SELECT COUNT(sst) FROM Studentsubjectteacher sst "
                        + "WHERE sst.studentId.id = :studentId "
                        + "AND sst.subjectTeacherId.id = :subjectTeacherId");

                checkQuery.setParameter("studentId", student.getId());
                checkQuery.setParameter("subjectTeacherId", subjectTeacherId);

                Long count = (Long) checkQuery.getSingleResult();

                // Nếu chưa đăng ký, thêm mới
                if (count == 0) {
                    Studentsubjectteacher enrollment = new Studentsubjectteacher();
                    enrollment.setStudentId(student);
                    enrollment.setSubjectTeacherId(subjectTeacher);

                    session.persist(enrollment);
                    enrolledCount++;
                }
            }

            session.flush();
            return enrolledCount;

        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            return 0;
        }
    }

    @Override
    public long countEnrollments() {
        Session session = this.factory.getObject().getCurrentSession();
        Query query = session.createQuery("SELECT COUNT(*) FROM Studentsubjectteacher");
        return (long) query.getSingleResult();
    }

    @Override
    public List<Studentsubjectteacher> getStudentsubjectteacherBySubjectTeacherID(List<Subjectteacher> listsubjectteacher) {
        Session s = this.factory.getObject().getCurrentSession();

        // Lấy danh sách id của teacher từ listsubjectteacher
        List<Integer> teacherIds = listsubjectteacher.stream()
                .map(subjectTeacher -> subjectTeacher.getId())
                .collect(Collectors.toList());

        // Tạo danh sách tham số
        List<Integer> parameterList = new ArrayList<>();

        // Tạo chuỗi HQL cho phần IN
        String parameterHql = " WHERE subjectTeacherId.id IN (";
        for (int i = 0; i < teacherIds.size(); i++) {
            parameterList.add(teacherIds.get(i));
            parameterHql += ":teacherId" + i;
            if (i < teacherIds.size() - 1) {
                parameterHql += ", ";
            }
        }
        parameterHql += ")";

        // Tạo câu truy vấn HQL hoàn chỉnh
        String queryString = "FROM Studentsubjectteacher" + parameterHql;

        Query q = s.createQuery(queryString);

        // Đặt giá trị cho từng tham số teacherId
        for (int i = 0; i < teacherIds.size(); i++) {
            q.setParameter("teacherId" + i, teacherIds.get(i));
        }

        List<Studentsubjectteacher> resultList = q.getResultList();
        return resultList;
    }

    @Override
    public List<Studentsubjectteacher> getListStudentsubjectteacher(int subjectteacherID) {
        Session s = this.factory.getObject().getCurrentSession();
        Query q = s.createQuery("FROM Studentsubjectteacher WHERE subjectTeacherId.id = :subjectteacherID");

        q.setParameter("subjectteacherID", subjectteacherID);
        List<Studentsubjectteacher> listStudentsubjectteacher = q.getResultList();
        return listStudentsubjectteacher;
    }

    @Override
    public List<Studentsubjectteacher> getListStudentsubjectteacherByStudentID(int studentID) {
        Session s = this.factory.getObject().getCurrentSession();
        // Sửa câu query
        Query q = s.createQuery("FROM Studentsubjectteacher WHERE studentId.id = :studentID");
        q.setParameter("studentID", studentID);
        List<Studentsubjectteacher> listStudentsubjectteacher = q.getResultList();
        return listStudentsubjectteacher;
    }

}
