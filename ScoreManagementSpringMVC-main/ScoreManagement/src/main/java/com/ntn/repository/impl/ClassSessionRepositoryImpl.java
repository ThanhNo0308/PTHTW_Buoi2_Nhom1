package com.ntn.repository.impl;

import com.ntn.pojo.ClassSession;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.repository.ClassSessionRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ClassSessionRepositoryImpl implements ClassSessionRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<ClassSession> getAllClassSessions() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ClassSession> query = builder.createQuery(ClassSession.class);
        Root<ClassSession> root = query.from(ClassSession.class);
        query.select(root);
        query.orderBy(builder.asc(root.get("dayOfWeek")), builder.asc(root.get("startTime")));
        return session.createQuery(query).getResultList();
    }

    @Override
    public ClassSession getClassSessionById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(ClassSession.class, id);
    }

    @Override
    public boolean addOrUpdateClassSession(ClassSession classSession) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            if (classSession.getId() == null) {
                session.persist(classSession);
            } else {
                session.merge(classSession);
            }
            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteClassSession(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        ClassSession classSession = session.get(ClassSession.class, id);
        if (classSession != null) {
            session.remove(classSession);
            return true;
        }
        return false;
    }

    @Override
    public List<ClassSession> getClassSessionsByTeacher(int teacherId, Integer schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ClassSession> query = builder.createQuery(ClassSession.class);
        Root<ClassSession> root = query.from(ClassSession.class);
        Join<ClassSession, Subjectteacher> subjectTeacherJoin = root.join("subjectTeacherId");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(subjectTeacherJoin.get("teacherId").get("id"), teacherId));

        if (schoolYearId != null) {
            predicates.add(builder.equal(subjectTeacherJoin.get("schoolYearId").get("id"), schoolYearId));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(builder.asc(root.get("dayOfWeek")), builder.asc(root.get("startTime")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<ClassSession> getClassSessionsByStudent(int studentId, Integer schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();

        StringBuilder hql = new StringBuilder();
        hql.append("SELECT DISTINCT cs FROM ClassSession cs ");
        hql.append("JOIN cs.subjectTeacherId st ");
        hql.append("JOIN Studentsubjectteacher sst ON sst.subjectTeacherId = st ");
        hql.append("WHERE sst.studentId.id = :studentId ");

        if (schoolYearId != null) {
            hql.append("AND st.schoolYearId.id = :schoolYearId ");
        }

        hql.append("ORDER BY cs.dayOfWeek, cs.startTime");

        Query<ClassSession> query = session.createQuery(hql.toString(), ClassSession.class);
        query.setParameter("studentId", studentId);

        if (schoolYearId != null) {
            query.setParameter("schoolYearId", schoolYearId);
        }

        return query.getResultList();
    }

    @Override
    public boolean hasScheduleConflict(ClassSession classSession) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ClassSession> query = builder.createQuery(ClassSession.class);
        Root<ClassSession> root = query.from(ClassSession.class);

        List<Predicate> predicates = new ArrayList<>();

        // Kiểm tra phòng trùng
        predicates.add(builder.equal(root.get("roomId"), classSession.getRoomId()));
        predicates.add(builder.equal(root.get("dayOfWeek"), classSession.getDayOfWeek()));

        // Điều kiện về thời gian
        Predicate timeConflictOlder = builder.and(
                builder.lessThanOrEqualTo(root.get("startTime"), classSession.getStartTime()),
                builder.greaterThan(root.get("endTime"), classSession.getStartTime())
        );

        Predicate timeConflictNewer = builder.and(
                builder.lessThan(root.get("startTime"), classSession.getEndTime()),
                builder.greaterThanOrEqualTo(root.get("endTime"), classSession.getEndTime())
        );

        Predicate timeConflictEnclosing = builder.and(
                builder.greaterThanOrEqualTo(root.get("startTime"), classSession.getStartTime()),
                builder.lessThanOrEqualTo(root.get("endTime"), classSession.getEndTime())
        );

        Predicate timeConflict = builder.or(timeConflictOlder, timeConflictNewer, timeConflictEnclosing);
        predicates.add(timeConflict);

        // Loại trừ session hiện tại khi update
        if (classSession.getId() != null) {
            predicates.add(builder.notEqual(root.get("id"), classSession.getId()));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.select(root);

        // Nếu có kết quả thì có conflict
        return !session.createQuery(query).getResultList().isEmpty();
    }

    @Override
    public List<ClassSession> getClassSessionsBySubjectTeacher(int subjectTeacherId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ClassSession> query = builder.createQuery(ClassSession.class);
        Root<ClassSession> root = query.from(ClassSession.class);

        query.where(builder.equal(root.get("subjectTeacherId").get("id"), subjectTeacherId));
        query.orderBy(builder.asc(root.get("dayOfWeek")), builder.asc(root.get("startTime")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<ClassSession> getClassSessionsByClass(int classId, Integer schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ClassSession> query = builder.createQuery(ClassSession.class);
        Root<ClassSession> root = query.from(ClassSession.class);
        Join<ClassSession, Subjectteacher> subjectTeacherJoin = root.join("subjectTeacherId");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(subjectTeacherJoin.get("classId").get("id"), classId));

        if (schoolYearId != null) {
            predicates.add(builder.equal(subjectTeacherJoin.get("schoolYearId").get("id"), schoolYearId));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(builder.asc(root.get("dayOfWeek")), builder.asc(root.get("startTime")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<ClassSession> getClassSessionsByDepartment(int departmentId, Integer schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ClassSession> query = builder.createQuery(ClassSession.class);
        Root<ClassSession> root = query.from(ClassSession.class);
        Join<ClassSession, Subjectteacher> subjectTeacherJoin = root.join("subjectTeacherId");
        Join subjectJoin = subjectTeacherJoin.join("subjectId");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(subjectJoin.get("departmentID").get("id"), departmentId));

        if (schoolYearId != null) {
            predicates.add(builder.equal(subjectTeacherJoin.get("schoolYearId").get("id"), schoolYearId));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(builder.asc(root.get("dayOfWeek")), builder.asc(root.get("startTime")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<ClassSession> getClassSessionsByDayOfWeek(int dayOfWeek, Integer schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ClassSession> query = builder.createQuery(ClassSession.class);
        Root<ClassSession> root = query.from(ClassSession.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(root.get("dayOfWeek"), dayOfWeek));

        if (schoolYearId != null) {
            Join<ClassSession, Subjectteacher> subjectTeacherJoin = root.join("subjectTeacherId");
            predicates.add(builder.equal(subjectTeacherJoin.get("schoolYearId").get("id"), schoolYearId));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(builder.asc(root.get("startTime")));

        return session.createQuery(query).getResultList();
    }

    @Override
    public List<ClassSession> getClassSessionsBySchoolYear(int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ClassSession> query = builder.createQuery(ClassSession.class);
        Root<ClassSession> root = query.from(ClassSession.class);
        Join<ClassSession, Subjectteacher> subjectTeacherJoin = root.join("subjectTeacherId");

        query.where(builder.equal(subjectTeacherJoin.get("schoolYearId").get("id"), schoolYearId));
        query.orderBy(builder.asc(root.get("dayOfWeek")), builder.asc(root.get("startTime")));

        return session.createQuery(query).getResultList();
    }
}
