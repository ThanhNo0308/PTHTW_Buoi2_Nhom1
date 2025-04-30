/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Forumcomment;
import com.ntn.repository.ForumCommentRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Session;
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
public class ForumCommentRepositoryImpl implements ForumCommentRepository {
    
    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Forumcomment> getCommentsByForumId(Integer forumId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Forumcomment> query = builder.createQuery(Forumcomment.class);
        Root<Forumcomment> root = query.from(Forumcomment.class);
        
        query.select(root);
        query.where(builder.equal(root.get("forumId").get("id"), forumId));
        // Sắp xếp theo thời gian tạo
        query.orderBy(builder.asc(root.get("createdAt")));
        
        return session.createQuery(query).getResultList();
    }

    @Override
    public Forumcomment getCommentById(Integer commentId) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Forumcomment.class, commentId);
    }

    @Override
    public boolean addComment(Forumcomment comment) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            session.save(comment);
            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateComment(Forumcomment comment) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            session.update(comment);
            return true;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteComment(Integer commentId) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            Forumcomment comment = session.get(Forumcomment.class, commentId);
            if (comment != null) {
                // Xử lý các comment con trước khi xóa (nếu có)
                List<Forumcomment> childComments = this.getChildComments(commentId);
                for (Forumcomment child : childComments) {
                    session.delete(child);
                }
                
                session.delete(comment);
                return true;
            }
            return false;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private List<Forumcomment> getChildComments(Integer parentCommentId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Forumcomment> query = builder.createQuery(Forumcomment.class);
        Root<Forumcomment> root = query.from(Forumcomment.class);
        
        query.select(root);
        query.where(builder.equal(root.get("parentCommentId").get("id"), parentCommentId));
        
        return session.createQuery(query).getResultList();
    }
}
