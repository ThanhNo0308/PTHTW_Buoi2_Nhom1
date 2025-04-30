package com.ntn.repository.impl;

import com.ntn.pojo.Forum;
import com.ntn.pojo.Forumcomment;
import com.ntn.pojo.Student;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Teacher;
import com.ntn.repository.ForumRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        // Sửa truy vấn để lấy forum dựa trên teacherId
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
        String hql1 = "SELECT DISTINCT st.subjectTeacherId.id FROM Studentsubjectteacher st WHERE st.studentId.id = :studentId";
        Query q1 = s.createQuery(hql1);
        q1.setParameter("studentId", studentId);
        List<Integer> subjectTeacherIds = q1.getResultList();

        if (subjectTeacherIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Sau đó, lấy các forum thuộc về các subjectTeacher này
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Forum> q = b.createQuery(Forum.class);
        Root<Forum> root = q.from(Forum.class);
        q.select(root);

        q.where(root.get("subjectTeacherId").get("id").in(subjectTeacherIds));
        // Sắp xếp theo thời gian tạo mới nhất
        q.orderBy(b.desc(root.get("createdAt")));

        Query query = s.createQuery(q);
        return query.getResultList();
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
                String hql = "FROM Forumcomment f WHERE f.forumId.id = :forumId";
                Query query = s.createQuery(hql);
                query.setParameter("forumId", forumId);
                List<Forumcomment> comments = query.getResultList();

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
