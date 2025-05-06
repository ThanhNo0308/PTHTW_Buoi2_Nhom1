/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Classscoretypes;
import com.ntn.pojo.Typescore;
import com.ntn.repository.TypeScoreRepository;
import com.ntn.service.ClassScoreTypeService;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class TypeScoreRepositoryImpl implements TypeScoreRepository {

    @Autowired
    private LocalSessionFactoryBean factory;
    
    @Autowired
    private ClassScoreTypeService classScoreTypeService;

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

    @Override
    public List<String> getScoreTypesByClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId) {
        Set<String> scoreTypes = new HashSet<>();

        // Luôn đảm bảo có các loại điểm mặc định
        scoreTypes.add("Giữa kỳ");
        scoreTypes.add("Cuối kỳ");

        // Thêm các loại điểm tùy chỉnh từ bảng class_score_types
        try {
            List<Classscoretypes> classScoreTypes = classScoreTypeService.getScoreTypesByClass(
                    classId, subjectTeacherId, schoolYearId);

            for (Classscoretypes cst : classScoreTypes) {
                if (cst.getScoreType() != null && cst.getScoreType().getScoreType() != null) {
                    String scoreTypeName = cst.getScoreType().getScoreType();
                    scoreTypes.add(scoreTypeName);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting score types: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>(scoreTypes);
    }
    
    
}
