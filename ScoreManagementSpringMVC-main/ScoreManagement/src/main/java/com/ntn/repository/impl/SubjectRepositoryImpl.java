/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Department;
import com.ntn.pojo.Subject;
import com.ntn.repository.SubjectRepository;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SubjectRepositoryImpl implements SubjectRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Subject> getSubjects() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subject> query = builder.createQuery(Subject.class);
        Root<Subject> root = query.from(Subject.class);

        query.select(root);
        query.orderBy(builder.asc(root.get("subjectName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subject> getSubjectsByDepartmentId(Integer departmentId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subject> query = builder.createQuery(Subject.class);
        Root<Subject> root = query.from(Subject.class);

        query.select(root);

        if (departmentId != null) {
            query.where(builder.equal(root.get("departmentID").get("id"), departmentId));
        }

        query.orderBy(builder.asc(root.get("subjectName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subject> getSubjectsByDepartmentIdAndKeyword(Integer departmentId, String keyword) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subject> query = builder.createQuery(Subject.class);
        Root<Subject> root = query.from(Subject.class);

        List<Predicate> predicates = new ArrayList<>();

        if (departmentId != null) {
            predicates.add(builder.equal(root.get("departmentID").get("id"), departmentId));
        }

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            // Tìm kiếm theo tên môn học
            Predicate nameLike = builder.like(builder.lower(root.get("subjectName")), pattern);

            // Thêm điều kiện tìm theo mã môn nếu keyword là số
            try {
                Integer codeValue = Integer.parseInt(keyword.trim());
                Predicate codeEqual = builder.equal(root.get("id"), codeValue);

                // Thêm điều kiện OR giữa tìm theo tên và tìm theo mã
                predicates.add(builder.or(nameLike, codeEqual));
            } catch (NumberFormatException e) {
                // Nếu không phải số, chỉ tìm theo tên
                predicates.add(nameLike);
            }
        }

        if (!predicates.isEmpty()) {
            query.where(builder.and(predicates.toArray(new Predicate[0])));
        }

        query.orderBy(builder.asc(root.get("subjectName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subject> getSubjectsByKeyword(String keyword) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subject> query = builder.createQuery(Subject.class);
        Root<Subject> root = query.from(Subject.class);

        query.select(root);

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            // Predicate cho tìm kiếm theo tên (sử dụng LIKE)
            Predicate nameLike = builder.like(builder.lower(root.get("subjectName")), pattern);

            // Predicate cho tìm kiếm theo mã môn học (kiểu Integer)
            Predicate codeEqual = null;
            try {
                // Thử chuyển đổi keyword thành Integer để tìm theo mã môn
                Integer codeValue = Integer.parseInt(keyword.trim());
                codeEqual = builder.equal(root.get("id"), codeValue);
            } catch (NumberFormatException e) {
            }

            // Kết hợp các điều kiện tìm kiếm
            if (codeEqual != null) {
                query.where(builder.or(nameLike, codeEqual));
            } else {
                query.where(nameLike);
            }
        }

        query.orderBy(builder.asc(root.get("subjectName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public boolean addOrUpdateSubject(Subject subject) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            // Kiểm tra và cập nhật Department nếu cần
            if (subject.getDepartmentID() != null && subject.getDepartmentID().getId() != null) {
                Department dept = session.get(Department.class, subject.getDepartmentID().getId());
                subject.setDepartmentID(dept);
            }

            // Lưu hoặc cập nhật môn học
            if (subject.getId() == null) {
                // Thêm mới - không cần ID
                session.persist(subject);
            } else {
                // Cập nhật - đã có ID
                session.merge(subject);
            }

            session.flush();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error saving subject: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteSubject(int subjectId) {
        Session session = this.factory.getObject().getCurrentSession();

        try {
            Subject subject = session.get(Subject.class, subjectId);
            if (subject != null) {
                session.remove(subject);
                session.flush();
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public Subject getSubjectById(int subjectId) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Subject.class, subjectId);
    }

}
