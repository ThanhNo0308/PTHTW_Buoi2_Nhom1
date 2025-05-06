/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Department;
import com.ntn.pojo.Teacher;
import com.ntn.pojo.User;
import com.ntn.repository.TeacherRepository;
import jakarta.persistence.NoResultException;
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
public class TeacherRepositoryImp implements TeacherRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public boolean addOrUpdateTeacher(Teacher teacher) {
        Session s = this.factory.getObject().getCurrentSession();
        try {
            if (teacher.getDepartmentId() != null && teacher.getDepartmentId().getId() != null) {
                // Tải đối tượng Department đầy đủ thông tin
                Department dept = s.get(Department.class, teacher.getDepartmentId().getId());
                teacher.setDepartmentId(dept);
            }
            if (teacher.getId() == null) {
                s.save(teacher);
            } else {
                s.update(teacher);
            }
            s.flush();
            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteTeacher(int teacherId) {
        Session s = this.factory.getObject().getCurrentSession();
        Teacher teacherToDelete = s.get(Teacher.class, teacherId);
        if (teacherToDelete != null) {
            try {
                s.delete(teacherToDelete);
                return true; // Trả về true nếu xóa thành công
            } catch (HibernateException ex) {
                ex.printStackTrace();
            }
        }
        return false; // Trả về false nếu không tìm thấy Giáo viên để xóa hoặc có lỗi xảy ra
    }

    @Override
    public Teacher getTeacherByUserId(int userId) {
        Session session = this.factory.getObject().getCurrentSession();

        // Đầu tiên lấy thông tin user
        User user = session.get(User.class, userId);
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            return null;
        }

        // Sau đó tìm teacher dựa trên email
        String email = user.getEmail();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Teacher> query = builder.createQuery(Teacher.class);
        Root<Teacher> root = query.from(Teacher.class);

        query.select(root);
        query.where(builder.equal(root.get("email"), email));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public List<Teacher> getTeachers() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = s.getCriteriaBuilder();
        CriteriaQuery<Teacher> query = builder.createQuery(Teacher.class);
        Root<Teacher> root = query.from(Teacher.class);
        query.select(root);

        return s.createQuery(query).getResultList();
    }

    @Override
    public int countTeachers() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Teacher> root = query.from(Teacher.class);

        query.select(builder.count(root));

        return session.createQuery(query).getSingleResult().intValue();
    }

    @Override
    public Teacher getTeacherById(int teacherId) {
        Session s = this.factory.getObject().getCurrentSession();
        return s.get(Teacher.class, teacherId);
    }

    @Override
    public Teacher getTeacherByEmail(String email) {
        Session session = this.factory.getObject().getCurrentSession();

        if (email == null || email.isEmpty()) {
            return null;
        }

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Teacher> query = builder.createQuery(Teacher.class);
        Root<Teacher> root = query.from(Teacher.class);

        query.select(root);
        query.where(builder.equal(root.get("email"), email));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            System.err.println("Không tìm thấy giảng viên với email: " + email);
            return null;
        }
    }

    @Override
    public List<Teacher> getTeachersByDepartmentId(Integer departmentId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Teacher> q = b.createQuery(Teacher.class);
        Root<Teacher> root = q.from(Teacher.class);
        q.select(root);
        if (departmentId != null) {
            q.where(b.equal(root.get("departmentId").get("id"), departmentId));
        }
        q.orderBy(b.asc(root.get("teacherName")));

        Query query = session.createQuery(q);

        return query.getResultList();
    }

    @Override
    public List<Teacher> getTeachersByKeyword(String keyword) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Teacher> query = builder.createQuery(Teacher.class);
        Root<Teacher> root = query.from(Teacher.class);

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            // Tìm theo tên hoặc email hoặc số điện thoại
            Predicate nameLike = builder.like(builder.lower(root.get("teacherName")), pattern);
            Predicate emailLike = builder.like(builder.lower(root.get("email")), pattern);
            Predicate phoneLike = builder.like(builder.lower(root.get("phoneNumber")), pattern);

            query.where(builder.or(nameLike, emailLike, phoneLike));
        }

        query.orderBy(builder.asc(root.get("teacherName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Teacher> getTeachersByDepartmentIdAndKeyword(Integer departmentId, String keyword) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Teacher> query = builder.createQuery(Teacher.class);
        Root<Teacher> root = query.from(Teacher.class);

        List<Predicate> predicates = new ArrayList<>();

        if (departmentId != null) {
            predicates.add(builder.equal(root.get("departmentId").get("id"), departmentId));
        }

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            // Tìm theo tên hoặc email hoặc số điện thoại
            Predicate nameLike = builder.like(builder.lower(root.get("teacherName")), pattern);
            Predicate emailLike = builder.like(builder.lower(root.get("email")), pattern);
            Predicate phoneLike = builder.like(builder.lower(root.get("phoneNumber")), pattern);

            predicates.add(builder.or(nameLike, emailLike, phoneLike));
        }

        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        query.orderBy(builder.asc(root.get("teacherName")));

        return session.createQuery(query).getResultList();
    }

}
