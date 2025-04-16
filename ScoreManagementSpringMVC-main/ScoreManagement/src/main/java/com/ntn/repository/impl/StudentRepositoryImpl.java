/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Student;
import com.ntn.repository.StudentRepository;
import java.util.List;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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
public class StudentRepositoryImpl implements StudentRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Student> getStudentByClassId(int classId) {
        // Lấy phiên hiện tại từ LocalSessionFactoryBean
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Student> criteriaQuery = criteriaBuilder.createQuery(Student.class);

        Root<Student> root = criteriaQuery.from(Student.class);

        // Thêm điều kiện để lọc các Major theo trainingtypeId
        criteriaQuery.where(criteriaBuilder.equal(root.get("classId").get("id"), classId));

        criteriaQuery.select(root);

        Query query = session.createQuery(criteriaQuery);
        return query.getResultList();
    }

    @Override
    public boolean addOrUpdateStudent(Student student) {
        Session s = this.factory.getObject().getCurrentSession();
        try {
            if (student.getId() == null) {
                s.save(student);
            } else {
                s.update(student);
            }

            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteStudent(int studentId) {
        Session s = this.factory.getObject().getCurrentSession();
        Student studentToDelete = s.get(Student.class, studentId);
        if (studentToDelete != null) {
            try {
                s.delete(studentToDelete);
                return true; // Trả về true nếu xóa thành công
            } catch (HibernateException ex) {
                ex.printStackTrace();
            }
        }
        return false; // Trả về false nếu không tìm thấy Student để xóa hoặc có lỗi xảy ra
    }

}
