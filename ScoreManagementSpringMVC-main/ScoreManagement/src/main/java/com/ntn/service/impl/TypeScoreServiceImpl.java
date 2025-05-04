/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Classscoretypes;
import com.ntn.pojo.Student;
import com.ntn.pojo.Typescore;
import com.ntn.repository.ClassScoreTypeRepository;
import com.ntn.repository.TypeScoreRepository;
import com.ntn.service.TypeScoreService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Admin
 */
@Service
public class TypeScoreServiceImpl implements TypeScoreService {

    @Autowired
    private TypeScoreRepository typeScoreRepository;

    @Autowired
    private ClassScoreTypeRepository classScoreTypeRepository;


    @Override
    public Typescore getScoreTypeByName(String name) {
        return this.typeScoreRepository.getScoreTypeByName(name);
    }

    @Override
    public List<Typescore> getAllScoreTypes() {
        return this.typeScoreRepository.getAllScoreTypes();
    }

    @Override
    public boolean addScoreType(Typescore newType) {
        return this.typeScoreRepository.addScoreType(newType);
    }

    @Override
    public List<String> getScoreTypesByClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId) {
        Set<String> scoreTypes = new HashSet<>();

        // Luôn đảm bảo có các loại điểm mặc định
        scoreTypes.add("Giữa kỳ");
        scoreTypes.add("Cuối kỳ");

        // Thêm các loại điểm tùy chỉnh từ bảng class_score_types
        try {
            List<Classscoretypes> classScoreTypes = classScoreTypeRepository.getScoreTypesByClass(
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

    @Override
    public boolean addScoreTypeToClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType, Float weight) {
        return this.typeScoreRepository.addScoreTypeToClass(classId, subjectTeacherId, schoolYearId, scoreType, weight);
    }

    @Override
    public boolean removeScoreTypeFromClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType) {
       return this.typeScoreRepository.removeScoreTypeFromClass(classId, subjectTeacherId, schoolYearId, scoreType);
    }

    @Override
    public boolean updateScoreTypeWeights(Integer classId, Integer subjectTeacherId, Integer schoolYearId, Map<String, Double> weights) {
       return this.typeScoreRepository.updateScoreTypeWeights(classId, subjectTeacherId, schoolYearId, weights);
    }

    @Override
    public int countScoreTypesBySubjectTeacher(int subjectTeacherId) {
        return this.typeScoreRepository.countScoreTypesBySubjectTeacher(subjectTeacherId);
    }

    @Override
    public Student getStudentByCode(String studentCode) {
        return this.typeScoreRepository.getStudentByCode(studentCode);
    }
}
