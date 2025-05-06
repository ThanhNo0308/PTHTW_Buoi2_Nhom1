/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Major;
import com.ntn.pojo.Trainingtype;
import com.ntn.repository.TrainingTypeRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.ArrayList;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class TrainingTypeRepositoryImpl implements TrainingTypeRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<Trainingtype> getTrainingTypes() {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Trainingtype> query = builder.createQuery(Trainingtype.class);
            Root<Trainingtype> root = query.from(Trainingtype.class);

            query.select(root);
            query.orderBy(builder.asc(root.get("id")));

            return session.createQuery(query).getResultList();
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Trainingtype getTrainingTypeById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            return session.get(Trainingtype.class, id);
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public Trainingtype getTrainingTypeByName(String name) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Trainingtype> query = builder.createQuery(Trainingtype.class);
            Root<Trainingtype> root = query.from(Trainingtype.class);

            query.select(root);
            query.where(builder.equal(root.get("trainingTypeName"), name));

            List<Trainingtype> result = session.createQuery(query).getResultList();

            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean addTrainingType(Trainingtype trainingType) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            session.save(trainingType);
            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateTrainingType(Trainingtype trainingType) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            Trainingtype existingType = session.get(Trainingtype.class, trainingType.getId());
            if (existingType != null) {
                existingType.setTrainingTypeName(trainingType.getTrainingTypeName());
                session.update(existingType);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteTrainingType(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            Trainingtype trainingType = session.get(Trainingtype.class, id);
            if (trainingType != null) {
                session.delete(trainingType);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean hasRelatedMajors(int trainingTypeId) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<Major> root = query.from(Major.class);

            query.select(builder.count(root));
            query.where(builder.equal(root.get("trainingTypeId").get("id"), trainingTypeId));

            return session.createQuery(query).getSingleResult() > 0;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return true; // Mặc định trả về true để đảm bảo an toàn
        }
    }

    @Override
    public int countTrainingTypes() {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Long> query = builder.createQuery(Long.class);
            Root<Trainingtype> root = query.from(Trainingtype.class);

            query.select(builder.count(root));

            return session.createQuery(query).getSingleResult().intValue();
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

}
