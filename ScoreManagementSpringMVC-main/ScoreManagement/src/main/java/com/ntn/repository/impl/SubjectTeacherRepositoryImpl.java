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
    public List<Subjectteacher> getSubjectTeachers() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Subjectteacher> q = b.createQuery(Subjectteacher.class);
        Root root = q.from(Subjectteacher.class);
        q.select(root);
        Query query = s.createQuery(q);
        return query.getResultList();
    }

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
    public Subjectteacher getSubJectTeacherById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Subjectteacher.class, id);
    }

    @Override
    public List<Subjectteacher> getSubjectTeacherByTeacherID(int TeacherID) {
        Session s = this.factory.getObject().getCurrentSession();
        Query q = s.createQuery("FROM Subjectteacher WHERE teacherId.id = :TeacherID");
        q.setParameter("TeacherID", TeacherID);
        List<Subjectteacher> subjectTeacher = q.getResultList();
        return subjectTeacher;
    }

    @Override
    public List<Subjectteacher> getSubjectTeacherByListSubjectTeacherId(List<Studentsubjectteacher> listStudentSubjectTeacher) {
        Session s = this.factory.getObject().getCurrentSession();

        List<Integer> subjectTeacherIds = new ArrayList<>();

        // Lặp qua danh sách và lấy ra subjectTeacherId của từng phần tử
        for (Studentsubjectteacher studentSubjectTeacher : listStudentSubjectTeacher) {
            subjectTeacherIds.add(studentSubjectTeacher.getSubjectTeacherId().getId());
        }

        // Sử dụng việc ghép chuỗi HQL
        String queryString = "FROM Subjectteacher WHERE id IN (";
        for (int i = 0; i < subjectTeacherIds.size(); i++) {
            queryString += ":id" + i;
            if (i < subjectTeacherIds.size() - 1) {
                queryString += ", ";
            }
        }
        queryString += ")";

        Query q = s.createQuery(queryString);

        // Đặt các tham số
        for (int i = 0; i < subjectTeacherIds.size(); i++) {
            q.setParameter("id" + i, subjectTeacherIds.get(i));
        }

        List<Subjectteacher> resultList = q.getResultList();
        return resultList;
    }

}
