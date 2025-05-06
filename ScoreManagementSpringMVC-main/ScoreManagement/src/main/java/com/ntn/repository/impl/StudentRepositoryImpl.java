package com.ntn.repository.impl;

import com.ntn.pojo.Student;
import com.ntn.repository.StudentRepository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.Query;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.ArrayList;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class StudentRepositoryImpl implements StudentRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Student> getStudentByClassId(int classId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Student> criteriaQuery = criteriaBuilder.createQuery(Student.class);

        Root<Student> root = criteriaQuery.from(Student.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get("classId").get("id"), classId));
        criteriaQuery.select(root);

        Query query = session.createQuery(criteriaQuery);
        return query.getResultList();
    }

    @Override
    public boolean addOrUpdateStudent(Student student) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            // Xử lý quan hệ với Class 
            Integer classId = null;
            if (student.getClassId() != null) {
                classId = student.getClassId().getId();
            }

            // Lưu sinh viên
            if (student.getId() == null) {
                // Thêm mới
                if (classId != null) {
                    // Lấy lại đối tượng Class từ session hiện tại
                    com.ntn.pojo.Class cls = session.get(com.ntn.pojo.Class.class, classId);
                    student.setClassId(cls);
                }
                session.persist(student);
            } else {
                // Cập nhật
                if (classId != null) {
                    // Lấy lại đối tượng Class từ session hiện tại
                    com.ntn.pojo.Class cls = session.get(com.ntn.pojo.Class.class, classId);
                    student.setClassId(cls);
                }
                session.merge(student);
            }
            session.flush();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error saving student: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public Student getStudentByEmail(String email) {
        Session session = this.factory.getObject().getCurrentSession();

        if (email == null || email.isEmpty()) {
            return null;
        }

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        query.select(root);
        query.where(builder.equal(root.get("email"), email));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            System.err.println("Không tìm thấy sinh viên với email: " + email);
            return null;
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
    public boolean deleteStudent(int studentId) {
        Session s = this.factory.getObject().getCurrentSession();
        Student studentToDelete = s.get(Student.class, studentId);
        if (studentToDelete != null) {
            try {
                s.delete(studentToDelete);
                return true;
            } catch (HibernateException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public Optional<Student> findByStudentCode(String studentCode) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Student> query = builder.createQuery(Student.class);
            Root<Student> root = query.from(Student.class);

            query.where(builder.equal(root.get("studentCode"), studentCode));

            Student student = session.createQuery(query).getSingleResult();
            return Optional.ofNullable(student);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Student getStudentById(int studentId) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Student.class, studentId);
    }

    @Override
    public int countStudents() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Student> root = query.from(Student.class);

        query.select(builder.count(root));

        return session.createQuery(query).getSingleResult().intValue();
    }

    @Override
    public List<Student> getStudents() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = s.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);
        query.select(root);

        return s.createQuery(query).getResultList();
    }

    @Override
    public int countStudentsByClassId(int classId) {
        Session session = this.factory.getObject().getCurrentSession();

        // Sử dụng CriteriaQuery
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Student> root = query.from(Student.class);

        Predicate predicate = builder.equal(root.get("classId").get("id"), classId);
        query.select(builder.count(root)).where(predicate);

        return session.createQuery(query).getSingleResult().intValue();
    }

    @Override
    public List<Student> findStudentsByCode(String code) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        // Tìm chính xác mã sinh viên (không phân biệt hoa thường)
        Predicate codePredicate = builder.like(
                builder.lower(root.get("studentCode")),
                "%" + code.toLowerCase() + "%"
        );

        query.where(codePredicate);
        query.orderBy(builder.asc(root.get("classId").get("className")),
                builder.asc(root.get("lastName")),
                builder.asc(root.get("firstName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Student> getStudentsByKeyword(String keyword) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            // Tìm theo mã sinh viên hoặc tên hoặc email
            Predicate codeLike = builder.like(builder.lower(root.get("studentCode")), pattern);
            Predicate firstNameLike = builder.like(builder.lower(root.get("firstName")), pattern);
            Predicate lastNameLike = builder.like(builder.lower(root.get("lastName")), pattern);
            Predicate emailLike = builder.like(builder.lower(root.get("email")), pattern);

            query.where(builder.or(codeLike, firstNameLike, lastNameLike, emailLike));
        }

        query.orderBy(builder.asc(root.get("lastName")), builder.asc(root.get("firstName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Student> getStudentsByClassIdAndKeyword(Integer classId, String keyword) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        List<Predicate> predicates = new ArrayList<>();

        if (classId != null) {
            predicates.add(builder.equal(root.get("classId").get("id"), classId));
        }

        if (keyword != null && !keyword.isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            // Tìm theo mã sinh viên hoặc tên hoặc email
            Predicate codeLike = builder.like(builder.lower(root.get("studentCode")), pattern);
            Predicate firstNameLike = builder.like(builder.lower(root.get("firstName")), pattern);
            Predicate lastNameLike = builder.like(builder.lower(root.get("lastName")), pattern);
            Predicate emailLike = builder.like(builder.lower(root.get("email")), pattern);

            predicates.add(builder.or(codeLike, firstNameLike, lastNameLike, emailLike));
        }

        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        query.orderBy(builder.asc(root.get("lastName")), builder.asc(root.get("firstName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Student> getStudentbyEmail(String email) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        query.select(root);
        query.where(builder.equal(root.get("email"), email));

        // Lấy danh sách kết quả
        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Student> findStudentsByName(String name) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        // Tìm kiếm bằng cách kết hợp firstName và lastName
        // Phân biệt dấu nhưng không phân biệt chữ hoa thường
        String searchName = "%" + name.toLowerCase() + "%";

        Expression<String> fullName = builder.concat(
                builder.lower(root.get("lastName")),
                builder.concat(" ", builder.lower(root.get("firstName")))
        );

        Predicate fullNamePredicate = builder.like(fullName, searchName);

        query.where(fullNamePredicate);
        query.orderBy(builder.asc(root.get("classId").get("className")),
                builder.asc(root.get("lastName")),
                builder.asc(root.get("firstName")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<Student> findStudentsByClass(String className) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        // Join từ Student đến Class
        Join<Student, java.lang.Class> classJoin = root.join("classId", JoinType.INNER);

        // Điều kiện tìm kiếm - tên lớp chứa chuỗi tìm kiếm
        Predicate classNamePredicate = builder.like(
                builder.lower(classJoin.get("className")),
                "%" + className.toLowerCase() + "%"
        );

        query.where(classNamePredicate);
        query.orderBy(builder.asc(classJoin.get("className")),
                builder.asc(root.get("lastName")),
                builder.asc(root.get("firstName")));

        return session.createQuery(query).getResultList();
    }
}
