/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Department;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subject;
import com.ntn.repository.SubjectRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class SubjectRepositoryImpl implements SubjectRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Subject> getSubjects() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Subject> q = b.createQuery(Subject.class);
        Root root = q.from(Subject.class);
        q.select(root);
        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Subject> getListSubjectById(List<Integer> listSubjectID) {
        Session s = this.factory.getObject().getCurrentSession();

        // Xây dựng một danh sách các chuỗi thể hiện danh sách Subject.id
        List<String> parameterStrings = new ArrayList<>();
        for (int i = 0; i < listSubjectID.size(); i++) {
            parameterStrings.add(":subjectId" + i);
        }

        // Tạo chuỗi HQL động cho phần WHERE, sử dụng IN để so sánh danh sách Subject.id
        String parameterHql = "id IN (" + String.join(", ", parameterStrings) + ")";

        // Tạo câu truy vấn HQL hoàn chỉnh
        String queryString = "FROM Subject WHERE " + parameterHql;

        Query q = s.createQuery(queryString);

        // Đặt giá trị cho từng tham số subjectId
        for (int i = 0; i < listSubjectID.size(); i++) {
            q.setParameter("subjectId" + i, listSubjectID.get(i));
        }

        List<Subject> resultList = q.getResultList();
        return resultList;
    }

    @Override
    public List<Subject> getSubjectsByDepartmentId(Integer departmentId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subject> query = builder.createQuery(Subject.class);
        Root<Subject> root = query.from(Subject.class);

        query.select(root);

        if (departmentId != null) {
            query.where(builder.equal(root.get("departmentID").get("id"), departmentId));
        }

        query.orderBy(builder.asc(root.get("subjectName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subject> getSubjectsByDepartmentIdAndKeyword(Integer departmentId, String keyword) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subject> query = builder.createQuery(Subject.class);
        Root<Subject> root = query.from(Subject.class);

        List<Predicate> predicates = new ArrayList<>();

        if (departmentId != null) {
            predicates.add(builder.equal(root.get("departmentID").get("id"), departmentId));
        }

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            // Tìm kiếm theo tên môn học
            Predicate nameLike = builder.like(builder.lower(root.get("subjectName")), pattern);

            // Thêm điều kiện tìm theo mã môn nếu keyword là số
            try {
                Integer codeValue = Integer.parseInt(keyword.trim());
                Predicate codeEqual = builder.equal(root.get("id"), codeValue);

                // Thêm điều kiện OR giữa tìm theo tên và tìm theo mã
                predicates.add(builder.or(nameLike, codeEqual));
            } catch (NumberFormatException e) {
                // Nếu không phải số, chỉ tìm theo tên
                predicates.add(nameLike);
            }
        }

        if (!predicates.isEmpty()) {
            query.where(builder.and(predicates.toArray(new Predicate[0])));
        }

        query.orderBy(builder.asc(root.get("subjectName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Subject> getSubjectsByKeyword(String keyword) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Subject> query = builder.createQuery(Subject.class);
        Root<Subject> root = query.from(Subject.class);

        query.select(root);

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            // Predicate cho tìm kiếm theo tên (sử dụng LIKE)
            Predicate nameLike = builder.like(builder.lower(root.get("subjectName")), pattern);

            // Predicate cho tìm kiếm theo mã môn học (kiểu Integer)
            Predicate codeEqual = null;
            try {
                // Thử chuyển đổi keyword thành Integer để tìm theo mã môn
                Integer codeValue = Integer.parseInt(keyword.trim());
                codeEqual = builder.equal(root.get("id"), codeValue);
            } catch (NumberFormatException e) {
            }

            // Kết hợp các điều kiện tìm kiếm
            if (codeEqual != null) {
                query.where(builder.or(nameLike, codeEqual));
            } else {
                query.where(nameLike);
            }
        }

        query.orderBy(builder.asc(root.get("subjectName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Integer> getSubjectTeacherId(List<Studentsubjectteacher> studentSubjectTeacher) {
        // Sử dụng một Set để lưu trữ các SubjectTeacherId.id duy nhất
        Set<Integer> uniqueSubjectTeacherIds = new HashSet<>();

        // Duyệt qua danh sách studentSubjectTeacher
        for (Studentsubjectteacher studentSubject : studentSubjectTeacher) {
            // Lấy SubjectTeacherId.id từ mỗi phần tử và thêm vào Set
            uniqueSubjectTeacherIds.add(studentSubject.getSubjectTeacherId().getId());
        }

        List<Integer> uniqueSubjectTeacherIdList = new ArrayList<>(uniqueSubjectTeacherIds);

        return uniqueSubjectTeacherIdList;
    }

    @Override
    public List<Integer> getSubjectIdByListSubjectTeacherId(List<Integer> listSubjectTeacherId) {
        Session s = this.factory.getObject().getCurrentSession();

        // Xây dựng một danh sách các chuỗi thể hiện danh sách Subjectteacher.id
        List<String> parameterStrings = new ArrayList<>();
        for (int i = 0; i < listSubjectTeacherId.size(); i++) {
            parameterStrings.add(":teacherId" + i);
        }

        // Tạo chuỗi HQL động cho phần WHERE, sử dụng IN để so sánh danh sách Subjectteacher.id
        String parameterHql = "id IN (" + String.join(", ", parameterStrings) + ")";

        // Tạo câu truy vấn HQL hoàn chỉnh
        String queryString = "SELECT DISTINCT subjectId.id FROM Subjectteacher WHERE " + parameterHql;

        Query q = s.createQuery(queryString);

        // Đặt giá trị cho từng tham số teacherId
        for (int i = 0; i < listSubjectTeacherId.size(); i++) {
            q.setParameter("teacherId" + i, listSubjectTeacherId.get(i));
        }

        List<Integer> resultList = q.getResultList();
        return resultList;
    }

    @Override
    public boolean addOrUpdateSubject(Subject subject) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            // Kiểm tra và cập nhật Department nếu cần
            if (subject.getDepartmentID() != null && subject.getDepartmentID().getId() != null) {
                Department dept = session.get(Department.class, subject.getDepartmentID().getId());
                subject.setDepartmentID(dept);
            }

            // Lưu hoặc cập nhật môn học
            if (subject.getId() == null) {
                // Thêm mới - không cần ID
                session.persist(subject);
            } else {
                // Cập nhật - đã có ID
                session.merge(subject);
            }

            session.flush();

            System.out.println("Saved subject with ID: " + subject.getId());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error saving subject: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteSubject(int subjectId) {
        Session session = this.factory.getObject().getCurrentSession();

        try {
            jakarta.persistence.Query query = session.createNativeQuery("DELETE FROM subject WHERE Id = :id");
            query.setParameter("id", subjectId);

            int result = query.executeUpdate();
            return result > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public Subject getSubjectById(int subjectId) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Subject.class, subjectId);
    }

}
