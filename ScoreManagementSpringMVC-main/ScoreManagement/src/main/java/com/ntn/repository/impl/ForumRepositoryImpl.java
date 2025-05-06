package com.ntn.repository.impl;

import com.ntn.pojo.Forum;
import com.ntn.pojo.Forumcomment;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.repository.ForumRepository;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ForumRepositoryImpl implements ForumRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Forum> getForums() {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Forum> q = b.createQuery(Forum.class);
        Root<Forum> root = q.from(Forum.class);
        q.select(root);
        // Sắp xếp theo thời gian tạo mới nhất
        q.orderBy(b.desc(root.get("createdAt")));
        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Forum> getForumBySubjectTeacher(int subjectTeacherId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Forum> q = b.createQuery(Forum.class);
        Root<Forum> root = q.from(Forum.class);
        q.select(root);
        q.where(b.equal(root.get("subjectTeacherId").get("id"), subjectTeacherId));
        // Sắp xếp theo thời gian tạo mới nhất
        q.orderBy(b.desc(root.get("createdAt")));
        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Forum> getForumByTeacher(int teacherId) {
        Session session = this.factory.getObject().getCurrentSession();

        // Lấy forum dựa trên teacherId
        CriteriaBuilder b = session.getCriteriaBuilder();
        CriteriaQuery<Forum> q = b.createQuery(Forum.class);
        Root<Forum> root = q.from(Forum.class);

        // Join với SubjectTeacher
        Join<Forum, Subjectteacher> subjectTeacherJoin = root.join("subjectTeacherId", JoinType.LEFT);
        Join<Subjectteacher, Teacher> teacherJoin = subjectTeacherJoin.join("teacherId", JoinType.LEFT);

        // Lấy forum theo teacherId
        Predicate predicate = b.equal(teacherJoin.get("id"), teacherId);

        q.where(predicate);
        q.orderBy(b.desc(root.get("createdAt")));

        Query query = session.createQuery(q);

        return query.getResultList();
    }

    @Override
    public List<Forum> getForumByStudent(int studentId) {
        Session s = this.factory.getObject().getCurrentSession();

        // Đầu tiên, lấy danh sách các subjectTeacherId mà sinh viên đã đăng ký
        CriteriaBuilder builder = s.getCriteriaBuilder();
        CriteriaQuery<Integer> query = builder.createQuery(Integer.class);
        Root<Studentsubjectteacher> root = query.from(Studentsubjectteacher.class);

        query.select(root.get("subjectTeacherId").get("id")).distinct(true);
        query.where(builder.equal(root.get("studentId").get("id"), studentId));

        List<Integer> subjectTeacherIds = s.createQuery(query).getResultList();

        if (subjectTeacherIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Sau đó, lấy các forum thuộc về các subjectTeacher này
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Forum> q = b.createQuery(Forum.class);
        Root<Forum> forumRoot = q.from(Forum.class);
        q.select(forumRoot);

        q.where(forumRoot.get("subjectTeacherId").get("id").in(subjectTeacherIds));
        q.orderBy(b.desc(forumRoot.get("createdAt")));

        return s.createQuery(q).getResultList();
    }

    @Override
    public boolean addForum(Forum forum) {
        Session s = this.factory.getObject().getCurrentSession();
        try {
            s.save(forum);
            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteForum(int forumId) {
        Session s = this.factory.getObject().getCurrentSession();
        try {
            Forum forumToDelete = s.get(Forum.class, forumId);
            if (forumToDelete != null) {
                // Xóa tất cả comment liên quan
                CriteriaBuilder builder = s.getCriteriaBuilder();
                CriteriaQuery<Forumcomment> query = builder.createQuery(Forumcomment.class);
                Root<Forumcomment> root = query.from(Forumcomment.class);

                query.select(root);
                query.where(builder.equal(root.get("forumId").get("id"), forumId));

                List<Forumcomment> comments = s.createQuery(query).getResultList();

                for (Forumcomment comment : comments) {
                    s.delete(comment);
                }

                s.delete(forumToDelete);
                return true;
            } else {
                return false; // Không tìm thấy bài đăng cần xóa
            }
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateForum(Forum forum) {
        Session s = this.factory.getObject().getCurrentSession();
        try {
            s.update(forum);
            return true;
        } catch (HibernateException ex) {
            System.err.println("== UPDATE FORUM ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public Forum getForumById(int id) {
        Session s = this.factory.getObject().getCurrentSession();
        try {
            return s.get(Forum.class, id);
        } catch (HibernateException ex) {
            System.err.println("== GET FORUM BY ID ERROR: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
}
