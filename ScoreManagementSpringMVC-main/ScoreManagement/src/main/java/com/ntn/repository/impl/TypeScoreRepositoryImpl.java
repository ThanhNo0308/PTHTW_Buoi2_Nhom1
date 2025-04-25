/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import com.ntn.repository.TypeScoreRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Admin
 */
@Repository
@Transactional
public class TypeScoreRepositoryImpl implements TypeScoreRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Typescore getScoreTypeByName(String name) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Typescore> query = builder.createQuery(Typescore.class);
        Root<Typescore> root = query.from(Typescore.class);

        query.where(builder.equal(root.get("scoreType"), name));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            // Nếu không tìm thấy, tạo loại điểm mới
            Typescore newType = new Typescore(name);
            session.save(newType);
            return newType;
        }
    }

    @Override
    public int countScoreTypesBySubjectTeacher(int subjectTeacherId) {
        Session session = this.factory.getObject().getCurrentSession();
        String hql = "SELECT COUNT(DISTINCT ts.id) FROM Score s "
                + "JOIN s.typeScoreID ts "
                + "WHERE s.subjectTeacherID.id = :subjectTeacherId";

        Query<Long> q = session.createQuery(hql, Long.class);
        q.setParameter("subjectTeacherId", subjectTeacherId);

        return q.getSingleResult().intValue();
    }

    @Override
    @Transactional
    public boolean addScoreType(String typeName, int subjectTeacherId) {
        Session session = this.factory.getObject().getCurrentSession();

        try {
            // Kiểm tra xem loại điểm đã tồn tại chưa
            String checkHql = "FROM Typescore ts WHERE ts.scoreType = :typeName";
            Query<Typescore> checkQuery = session.createQuery(checkHql, Typescore.class);
            checkQuery.setParameter("typeName", typeName);
            List<Typescore> existingTypes = checkQuery.getResultList();

            Typescore typeScore;
            if (existingTypes.isEmpty()) {
                // Tạo loại điểm mới nếu chưa tồn tại
                typeScore = new Typescore();
                typeScore.setScoreType(typeName);
                session.save(typeScore);
            } else {
                typeScore = existingTypes.get(0);
            }

            // Lấy danh sách sinh viên thuộc môn học của giảng viên
            String studentHql = "SELECT DISTINCT s.studentID FROM Score s "
                    + "WHERE s.subjectTeacherID.id = :subjectTeacherId";
            Query<Student> studentQuery = session.createQuery(studentHql, Student.class);
            studentQuery.setParameter("subjectTeacherId", subjectTeacherId);
            List<Student> students = studentQuery.getResultList();

            // Lấy thông tin Subjectteacher
            Subjectteacher st = session.get(Subjectteacher.class, subjectTeacherId);

            // Tạo điểm mới cho mỗi sinh viên với loại điểm mới
            for (Student student : students) {
                Score newScore = new Score();
                newScore.setStudentID(student);
                newScore.setSubjectTeacherID(st);
                newScore.setScoreType(typeScore); // Sửa từ setTypeScoreID thành setScoreType
                newScore.setScoreValue(0.0f); // Sửa từ setScore thành setScoreValue
                newScore.setIsDraft(true); // Sửa từ setStatus(0) thành setIsDraft(true)
                newScore.setIsLocked(false); // Sửa thêm isLocked = false

                session.save(newScore);
            }

            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public Student getStudentByCode(String studentCode) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        query.where(builder.equal(root.get("studentCode"), studentCode));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (Exception e) {
            return null; // Trả về null nếu không tìm thấy sinh viên
        }
    }

    @Override
    public List<Typescore> getAllScoreTypes() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Typescore> query = builder.createQuery(Typescore.class);
        Root<Typescore> root = query.from(Typescore.class);
        query.select(root);
        return session.createQuery(query).getResultList();
    }

    @Override
    public boolean addScoreType(Typescore newType) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            session.save(newType);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    
}
