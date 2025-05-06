/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Class;
import com.ntn.pojo.Major;
import com.ntn.pojo.Teacher;
import com.ntn.repository.ClassRepository;
import java.util.List;
import jakarta.persistence.Query;
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
public class ClassRepositoryImpl implements ClassRepository {

    @Autowired
    LocalSessionFactoryBean factory;

    @Override
    public List<Class> getClasses() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Class> q = b.createQuery(Class.class);
        Root root = q.from(Class.class);
        q.select(root);
        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Class> getClassesByMajorId(int majorId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Class> q = b.createQuery(Class.class);
        Root root = q.from(Class.class);
        q.select(root);
        q.where(b.equal(root.get("majorId").get("id"), majorId));
        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Class> getClassesByKeyword(String keyword) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Class> query = builder.createQuery(Class.class);
        Root<Class> root = query.from(Class.class);

        query.select(root);

        if (keyword != null && !keyword.isEmpty()) {
            Predicate nameLike = builder.like(
                    builder.lower(root.get("className")),
                    "%" + keyword.toLowerCase() + "%"
            );
            query.where(nameLike);
        }

        query.orderBy(builder.asc(root.get("className")));
        return session.createQuery(query).getResultList();
    }

    @Override
    public boolean deleteClass(int classId) {
        Session s = this.factory.getObject().getCurrentSession();
        Class classToDelete = s.get(Class.class, classId);
        if (classToDelete != null) {
            s.delete(classToDelete);
            return true; // Trả về true nếu xóa thành công
        }
        return false; // Trả về false nếu không tìm thấy lớp để xóa
    }

    @Override
    public boolean addOrUpdateClass(Class classObj) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            // Kiểm tra và cập nhật Major nếu cần
            if (classObj.getMajorId() != null && classObj.getMajorId().getId() != null) {
                Major major = session.get(Major.class, classObj.getMajorId().getId());
                classObj.setMajorId(major);
            }

            // Kiểm tra và cập nhật Teacher nếu cần
            if (classObj.getTeacherId() != null && classObj.getTeacherId().getId() != null) {
                Teacher teacher = session.get(Teacher.class, classObj.getTeacherId().getId());
                classObj.setTeacherId(teacher);
            }

            // Lưu hoặc cập nhật lớp học
            if (classObj.getId() == null) {
                // Thêm mới - không cần ID
                session.persist(classObj);
            } else {
                // Cập nhật - đã có ID
                session.merge(classObj);
            }
            session.flush();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error saving class: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public Class getClassById(int classId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Class> query = builder.createQuery(Class.class);
        Root<Class> root = query.from(Class.class);

        query.where(builder.equal(root.get("id"), classId));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Class> getClassesByTeacher(int teacherId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Class> query = builder.createQuery(Class.class);
        Root<Class> root = query.from(Class.class);

        // Lọc các lớp có teacherId phù hợp
        query.where(builder.equal(root.get("teacherId").get("id"), teacherId));
        query.orderBy(builder.asc(root.get("className")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public int countClasses() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Class> root = query.from(Class.class);

        query.select(builder.count(root));

        return session.createQuery(query).getSingleResult().intValue();
    }

}
