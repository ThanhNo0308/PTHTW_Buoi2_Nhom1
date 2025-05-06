/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Department;
import com.ntn.pojo.Major;
import com.ntn.pojo.Subject;
import com.ntn.pojo.Teacher;
import com.ntn.repository.DepartmentRepository;
import java.util.List;
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
            // Kiểm tra ngành học thuộc khoa 
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> majorQuery = builder.createQuery(Long.class);
            Root<Major> majorRoot = majorQuery.from(Major.class);

            majorQuery.select(builder.count(majorRoot));
            majorQuery.where(builder.equal(majorRoot.get("departmentId").get("id"), departmentId));

            Long majorCount = session.createQuery(majorQuery).getSingleResult();
            if (majorCount > 0) {
                return true;
            }

            // Kiểm tra giảng viên thuộc khoa 
            CriteriaQuery<Long> teacherQuery = builder.createQuery(Long.class);
            Root<Teacher> teacherRoot = teacherQuery.from(Teacher.class);

            teacherQuery.select(builder.count(teacherRoot));
            teacherQuery.where(builder.equal(teacherRoot.get("departmentId").get("id"), departmentId));

            Long teacherCount = session.createQuery(teacherQuery).getSingleResult();
            if (teacherCount > 0) {
                return true;
            }

            // Kiểm tra môn học thuộc khoa 
            CriteriaQuery<Long> subjectQuery = builder.createQuery(Long.class);
            Root<Subject> subjectRoot = subjectQuery.from(Subject.class);

            subjectQuery.select(builder.count(subjectRoot));
            subjectQuery.where(builder.equal(subjectRoot.get("departmentID").get("id"), departmentId));

            Long subjectCount = session.createQuery(subjectQuery).getSingleResult();
            return subjectCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Nếu có lỗi, giả định là có dữ liệu liên quan
        }
    }

    @Override
    public long countDepartments() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Department> root = query.from(Department.class);

        query.select(builder.count(root));

        return session.createQuery(query).getSingleResult();
    }

}
