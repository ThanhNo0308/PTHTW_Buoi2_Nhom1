/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Department;
import com.ntn.pojo.Major;
import com.ntn.pojo.Trainingtype;
import com.ntn.repository.MajorRepository;
import java.util.List;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class MajorRepositoryImpl implements MajorRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Major> getMajorsByDepartmentId(int departmentId) {
        Session session = factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Major> query = builder.createQuery(Major.class);
        Root<Major> root = query.from(Major.class);

        query.select(root);
        query.where(builder.equal(root.get("departmentId").get("id"), departmentId));
        query.orderBy(builder.asc(root.get("majorName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Major> getMajorsByTrainingTypeId(int trainingtypeId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Major> criteriaQuery = criteriaBuilder.createQuery(Major.class);
        Root<Major> root = criteriaQuery.from(Major.class);

        // Sửa điều kiện tại đây
        criteriaQuery.where(
                criteriaBuilder.equal(root.get("trainingTypeId").get("id"), trainingtypeId)
        );

        criteriaQuery.select(root);
        Query query = session.createQuery(criteriaQuery);
        return query.getResultList();
    }

    @Override
    public List<Major> getMajorsByDepartmentAndTrainingType(Integer departmentId, Integer trainingTypeId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Major> query = builder.createQuery(Major.class);
        Root<Major> root = query.from(Major.class);

        List<Predicate> predicates = new ArrayList<>();

        if (departmentId != null) {
            predicates.add(builder.equal(root.get("departmentId").get("id"), departmentId));
        }

        if (trainingTypeId != null) {
            predicates.add(builder.equal(root.get("trainingTypeId").get("id"), trainingTypeId));
        }

        if (!predicates.isEmpty()) {
            query.where(builder.and(predicates.toArray(new Predicate[0])));
        }

        query.orderBy(builder.asc(root.get("majorName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public boolean addOrUpdateMajor(Major major) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            // Kiểm tra và cập nhật Department nếu cần
            if (major.getDepartmentId() != null && major.getDepartmentId().getId() != null) {
                Department dept = session.get(Department.class, major.getDepartmentId().getId());
                major.setDepartmentId(dept);
            }

            // Kiểm tra và cập nhật TrainingType nếu cần
            if (major.getTrainingTypeId() != null && major.getTrainingTypeId().getId() != null) {
                Trainingtype type = session.get(Trainingtype.class, major.getTrainingTypeId().getId());
                major.setTrainingTypeId(type);
            }

            // Lưu hoặc cập nhật ngành học
            if (major.getId() == null) {
                // Thêm mới - không cần ID
                session.persist(major);
            } else {
                // Cập nhật - đã có ID
                session.merge(major);
            }
            session.flush();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error saving major: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMajor(int majorId) {
        Session s = this.factory.getObject().getCurrentSession();
        Major majorToDelete = s.get(Major.class, majorId);
        if (majorToDelete != null) {
            try {
                s.delete(majorToDelete);
                return true; // Trả về true nếu xóa thành công
            } catch (HibernateException ex) {
                ex.printStackTrace();
            }
        }
        return false; // Trả về false nếu không tìm thấy Major để xóa hoặc có lỗi xảy ra
    }

    @Override
    public int countMajors() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = s.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Major> root = query.from(Major.class);

        query.select(builder.count(root));

        return s.createQuery(query).getSingleResult().intValue();
    }

    @Override
    public List<Major> getMajors() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = s.getCriteriaBuilder();
        CriteriaQuery<Major> query = builder.createQuery(Major.class);
        Root<Major> root = query.from(Major.class);
        query.select(root);

        return s.createQuery(query).getResultList();
    }

    @Override
    public Major getMajorById(int majorId) {
        Session s = this.factory.getObject().getCurrentSession();
        return s.get(Major.class, majorId);
    }

}
