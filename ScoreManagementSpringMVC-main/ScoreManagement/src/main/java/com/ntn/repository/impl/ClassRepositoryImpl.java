/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntn.pojo.Class;
import com.ntn.pojo.Major;
import com.ntn.pojo.Student;
import com.ntn.pojo.Teacher;
import com.ntn.repository.ClassRepository;
import java.util.List;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nguye
 */
@Repository
@Transactional
public class ClassRepositoryImpl implements ClassRepository {

    @Autowired
    LocalSessionFactoryBean factory;
    @Autowired
    private Environment env;

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
    @Transactional
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
        return session.get(Class.class, classId);
    }

    @Override
    public boolean updateScoreColumns(int classId, int additionalColumns,
            String column3Name, String column4Name, String column5Name) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            // Lấy thông tin lớp
            Class classObj = session.get(Class.class, classId);
            if (classObj == null) {
                return false;
            }

            // Lưu cấu hình cột điểm vào bảng class_config hoặc một trường khác trong Class
            // Đây là giả định, cần điều chỉnh theo cấu trúc thực tế của bạn
            Map<String, Object> configs = new HashMap<>();
            configs.put("additionalColumns", additionalColumns);

            if (additionalColumns >= 1 && column3Name != null && !column3Name.isEmpty()) {
                configs.put("column3Name", column3Name);
            }

            if (additionalColumns >= 2 && column4Name != null && !column4Name.isEmpty()) {
                configs.put("column4Name", column4Name);
            }

            if (additionalColumns >= 3 && column5Name != null && !column5Name.isEmpty()) {
                configs.put("column5Name", column5Name);
            }

            // Lưu cấu hình (giả định có trường configs kiểu JSON hoặc Text)
            // classObj.setConfigs(convertConfigsToString(configs));
            session.update(classObj);
            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
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
        Session s = this.factory.getObject().getCurrentSession();
        Query query = s.createQuery("SELECT COUNT(c) FROM Class c");
        return ((Long) query.getSingleResult()).intValue();
    }

    @Override
    public boolean updateClassConfiguration(int classId, boolean enableAttendance,
            boolean enableActivityScoring, String gradingPolicy) {
        Session s = this.factory.getObject().getCurrentSession();
        try {
            Class classObj = s.get(Class.class, classId);
            if (classObj == null) {
                return false;
            }

            // Giả sử có các thuộc tính này trong entity Class
            // Nếu không, bạn có thể tạo một bảng riêng để lưu cấu hình
            // Lưu cấu hình dưới dạng JSON
            Map<String, Object> config = new HashMap<>();
            config.put("enableAttendance", enableAttendance);
            config.put("enableActivityScoring", enableActivityScoring);
            config.put("gradingPolicy", gradingPolicy);

            String configJson = new ObjectMapper().writeValueAsString(config);

            // Giả sử có trường classConfig kiểu String trong entity Class
            // classObj.setClassConfig(configJson);
            // Hoặc lưu các giá trị riêng biệt nếu có các trường tương ứng
            // classObj.setEnableAttendance(enableAttendance);
            // classObj.setEnableActivityScoring(enableActivityScoring);
            // classObj.setGradingPolicy(gradingPolicy);
            s.update(classObj);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
