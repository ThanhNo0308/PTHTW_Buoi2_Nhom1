/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Major;
import com.ntn.repository.MajorRepository;
import java.util.List;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.HibernateException;
import org.hibernate.Session;
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
public class MajorRepositoryImpl implements MajorRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Major> getMajorsByDepartmentId(int departmentId) {
        Session session = factory.getObject().getCurrentSession();
        Query query = session.createQuery("FROM Major WHERE departmentId.id = :departmentId");
        query.setParameter("departmentId", departmentId);
        return query.getResultList();
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
    public boolean addOrUpdateMajor(Major major) {
        Session s = this.factory.getObject().getCurrentSession();
        try {
            if (major.getId() == null) {
                s.save(major);
            } else {
                s.update(major);
            }

            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
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

}
