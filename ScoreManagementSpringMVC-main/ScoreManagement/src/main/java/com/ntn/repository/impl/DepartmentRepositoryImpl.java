/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Department;
import com.ntn.repository.DepartmentRepository;
import java.util.List;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class DepartmentRepositoryImpl implements DepartmentRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Department> getDepartments() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Department> query = builder.createQuery(Department.class);
        Root<Department> root = query.from(Department.class);
        query.select(root);
        query.orderBy(builder.asc(root.get("departmentName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public boolean deleteDepartment(int departmentId) {
        try {
            Session session = this.factory.getObject().getCurrentSession();

            // Kiểm tra xem có dữ liệu liên quan không
            if (hasRelatedData(departmentId)) {
                return false;
            }

            // Sử dụng SQL thuần để xóa, tránh StackOverflowError
            org.hibernate.query.NativeQuery query = session.createNativeQuery("DELETE FROM department WHERE Id = :id");
            query.setParameter("id", departmentId);

            int result = query.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addOrUpdateDepartment(Department department) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            if (department.getId() == null) {
                session.persist(department);
            } else {
                session.merge(department);
            }

            session.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Department getDepartmentById(int departmentId) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Department.class, departmentId);
    }

    @Override
    public boolean hasRelatedData(int departmentId) {
        Session session = this.factory.getObject().getCurrentSession();

        try {
            // Kiểm tra có ngành học nào thuộc khoa không
            Query checkMajor = session.createQuery("SELECT COUNT(m) FROM Major m WHERE m.departmentId.id = :departmentId");
            checkMajor.setParameter("departmentId", departmentId);
            Long majorCount = (Long) checkMajor.getSingleResult();

            if (majorCount > 0) {
                return true;
            }

            // Kiểm tra có giảng viên nào thuộc khoa không
            Query checkTeacher = session.createQuery("SELECT COUNT(t) FROM Teacher t WHERE t.departmentId.id = :departmentId");
            checkTeacher.setParameter("departmentId", departmentId);
            Long teacherCount = (Long) checkTeacher.getSingleResult();

            if (teacherCount > 0) {
                return true;
            }

            // Kiểm tra có môn học nào thuộc khoa không
            Query checkSubject = session.createQuery("SELECT COUNT(s) FROM Subject s WHERE s.departmentID.id = :departmentId");
            checkSubject.setParameter("departmentId", departmentId);
            Long subjectCount = (Long) checkSubject.getSingleResult();

            return subjectCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Nếu có lỗi, giả định là có dữ liệu liên quan để tránh xóa không an toàn
        }
    }

    @Override
    public long countDepartments() {
        Session session = this.factory.getObject().getCurrentSession();
        Query q = session.createQuery("SELECT COUNT(*) FROM Department");
        return (long) q.getSingleResult();
    }

}
