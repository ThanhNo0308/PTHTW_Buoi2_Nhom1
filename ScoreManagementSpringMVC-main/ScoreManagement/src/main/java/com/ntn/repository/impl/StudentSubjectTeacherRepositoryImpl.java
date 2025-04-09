/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subject;
import com.ntn.repository.StudentSubjectTeacherRepository;
import java.util.List;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nguye
 */
@Repository
@Transactional
public class StudentSubjectTeacherRepositoryImpl implements StudentSubjectTeacherRepository {
    @Autowired
    private LocalSessionFactoryBean factory;
    @Override
    public List<Studentsubjectteacher> getStudsubjteachs() {
        Session s = this.factory.getObject().getCurrentSession();
       CriteriaBuilder b = s.getCriteriaBuilder();
        CriteriaQuery<Studentsubjectteacher> q = b.createQuery(Studentsubjectteacher.class);
        Root root = q.from(Studentsubjectteacher.class);
        q.select(root); 
        Query query = s.createQuery(q);
        return query.getResultList(); 
        
    }
    
}
