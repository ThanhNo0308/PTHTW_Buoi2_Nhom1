/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import com.ntn.repository.SubjectTeacherRepository;
import java.util.List;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SubjectTeacherRepositoryImpl implements SubjectTeacherRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Subjectteacher> getAllSubjectTeachers() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subjectteacher> query = builder.createQuery(Subjectteacher.class);
        Root<Subjectteacher> root = query.from(Subjectteacher.class);
        query.select(root);

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersBySubjectIdAndClassId(int subjectId, int classId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subjectteacher> query = builder.createQuery(Subjectteacher.class);
        Root<Subjectteacher> root = query.from(Subjectteacher.class);

        // Tạo hai điều kiện: subjectId.id = ? AND classId.id = ?
        Predicate subjectPredicate = builder.equal(root.get("subjectId").get("id"), subjectId);
        Predicate classPredicate = builder.equal(root.get("classId").get("id"), classId);

        // Kết hợp hai điều kiện với AND
        query.where(builder.and(subjectPredicate, classPredicate));

        // Sắp xếp kết quả theo ID
        query.orderBy(builder.asc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public Subjectteacher getSubjectTeacherById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Subjectteacher.class, id);
    }

    @Override
    public boolean addOrUpdateSubjectTeacher(Subjectteacher subjectTeacher) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            if (subjectTeacher.getId() == null) {
                session.persist(subjectTeacher);
            } else {
                session.merge(subjectTeacher);
            }
            session.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteSubjectTeacher(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            org.hibernate.query.NativeQuery query = session.createNativeQuery("DELETE FROM subjectteacher WHERE Id = :id");
            query.setParameter("id", id);

            int result = query.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByTeacherId(int teacherId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subjectteacher> query = builder.createQuery(Subjectteacher.class);
        Root<Subjectteacher> root = query.from(Subjectteacher.class);

        query.where(builder.equal(root.get("teacherId").get("id"), teacherId));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersBySubjectId(Integer subjectId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subjectteacher> query = builder.createQuery(Subjectteacher.class);
        Root<Subjectteacher> root = query.from(Subjectteacher.class);

        // Tạo điều kiện where subjectId.id = ?
        if (subjectId != null) {
            query.where(builder.equal(root.get("subjectId").get("id"), subjectId));
        }

        // Sắp xếp kết quả theo ID của môn học
        query.orderBy(builder.asc(root.get("id")));

        // Thực thi truy vấn và trả về kết quả
        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByDepartmentId(int departmentId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subjectteacher> query = builder.createQuery(Subjectteacher.class);
        Root<Subjectteacher> root = query.from(Subjectteacher.class);

        // Lấy các giảng viên thuộc khoa
        Predicate teacherPredicate = builder.equal(root.get("teacherId").get("departmentId").get("id"), departmentId);

        // Lấy các môn học thuộc khoa
        Predicate subjectPredicate = builder.equal(root.get("subjectId").get("departmentID").get("id"), departmentId);

        // Kết hợp hai điều kiện với OR
        query.where(builder.or(teacherPredicate, subjectPredicate));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersBySchoolYearId(int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subjectteacher> query = builder.createQuery(Subjectteacher.class);
        Root<Subjectteacher> root = query.from(Subjectteacher.class);

        // Tạo điều kiện where schoolYearId.id = ?
        query.where(builder.equal(root.get("schoolYearId").get("id"), schoolYearId));

        // Sắp xếp kết quả theo ID
        query.orderBy(builder.asc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByTeacherIdAndSchoolYearId(int teacherId, int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subjectteacher> query = builder.createQuery(Subjectteacher.class);
        Root<Subjectteacher> root = query.from(Subjectteacher.class);

        // Tạo hai điều kiện: teacherId.id = ? AND schoolYearId.id = ?
        Predicate teacherPredicate = builder.equal(root.get("teacherId").get("id"), teacherId);
        Predicate schoolYearPredicate = builder.equal(root.get("schoolYearId").get("id"), schoolYearId);

        // Kết hợp hai điều kiện với AND
        query.where(builder.and(teacherPredicate, schoolYearPredicate));

        // Sắp xếp kết quả theo ID
        query.orderBy(builder.asc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByClassId(int classId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subjectteacher> query = builder.createQuery(Subjectteacher.class);
        Root<Subjectteacher> root = query.from(Subjectteacher.class);

        // Tạo điều kiện where classId.id = ?
        query.where(builder.equal(root.get("classId").get("id"), classId));

        // Sắp xếp kết quả theo ID
        query.orderBy(builder.asc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByTeacherIdAndClassId(int teacherId, int classId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subjectteacher> query = builder.createQuery(Subjectteacher.class);
        Root<Subjectteacher> root = query.from(Subjectteacher.class);

        // Tạo hai điều kiện: teacherId.id = ? AND classId.id = ?
        Predicate teacherPredicate = builder.equal(root.get("teacherId").get("id"), teacherId);
        Predicate classPredicate = builder.equal(root.get("classId").get("id"), classId);

        // Kết hợp hai điều kiện với AND
        query.where(builder.and(teacherPredicate, classPredicate));

        // Sắp xếp kết quả theo ID
        query.orderBy(builder.asc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subjectteacher> getSubjectTeachersByTeacherIdAndClassIdAndSchoolYearId(int teacherId, int classId, int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subjectteacher> query = builder.createQuery(Subjectteacher.class);
        Root<Subjectteacher> root = query.from(Subjectteacher.class);

        // Tạo ba điều kiện: teacherId.id = ? AND classId.id = ? AND schoolYearId.id = ?
        Predicate teacherPredicate = builder.equal(root.get("teacherId").get("id"), teacherId);
        Predicate classPredicate = builder.equal(root.get("classId").get("id"), classId);
        Predicate schoolYearPredicate = builder.equal(root.get("schoolYearId").get("id"), schoolYearId);

        // Kết hợp các điều kiện với AND
        query.where(builder.and(teacherPredicate, classPredicate, schoolYearPredicate));

        // Sắp xếp kết quả theo ID
        query.orderBy(builder.asc(root.get("id")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public Subjectteacher findByIdClassIdAndSchoolYearId(int id, int classId, int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            String hql = "FROM Subjectteacher s WHERE s.id = :id "
                    + "AND s.classId.id = :classId "
                    + "AND s.schoolYearId.id = :schoolYearId";

            // Sửa dòng lỗi - sử dụng TypedQuery thay vì Query thông thường
            jakarta.persistence.TypedQuery<Subjectteacher> query
                    = session.createQuery(hql, Subjectteacher.class);
            query.setParameter("id", id);
            query.setParameter("classId", classId);
            query.setParameter("schoolYearId", schoolYearId);

            // Sử dụng getResultList() và kiểm tra kết quả thay vì uniqueResult()
            List<Subjectteacher> results = query.getResultList();
            Subjectteacher result = results.isEmpty() ? null : results.get(0);

            if (result != null) {
                System.out.println("Found SubjectTeacher with ID: " + result.getId());
            } else {
                System.out.println("No matching SubjectTeacher found");
            }

            return result;
        } catch (Exception e) {
            System.err.println("Error finding SubjectTeacher: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
