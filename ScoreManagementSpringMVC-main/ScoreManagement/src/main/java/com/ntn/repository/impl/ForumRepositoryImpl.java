/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Forum;
import com.ntn.repository.ForumRepository;
import java.util.List;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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
        Root root = q.from(Forum.class);
        q.select(root);
        Query query = s.createQuery(q);
        return query.getResultList();
    }

    @Override
    public List<Forum> getForumBySubjectTeacher(int subjectTeacherId) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Forum> q = b.createQuery(Forum.class);
        Root root = q.from(Forum.class);
        q.select(root);
        q.where(b.equal(root.get("subjectTeacherId").get("id"), subjectTeacherId));
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
