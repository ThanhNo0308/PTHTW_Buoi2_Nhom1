/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Classscoretypes;
import com.ntn.repository.ClassScoreTypeRepository;
import com.ntn.service.ClassScoreTypeService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClassScoreTypeServiceImpl implements ClassScoreTypeService {

    @Autowired
    private ClassScoreTypeRepository classScoreTypeRepository;

    @Override
    public boolean saveScoreType(Classscoretypes classScoreType) {
        return this.classScoreTypeRepository.saveScoreType(classScoreType);
    }

    @Override
    public boolean deleteScoreType(Integer id) {
        return this.classScoreTypeRepository.deleteScoreType(id);
    }

    @Override
    public List<Classscoretypes> getScoreTypesByClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId) {
        return this.classScoreTypeRepository.getScoreTypesByClass(classId, subjectTeacherId, schoolYearId);
    }

    @Override
    public boolean updateScoreTypeWeights(Integer classId, Integer subjectTeacherId, Integer schoolYearId, Map<String, Float> weights) {
        return this.classScoreTypeRepository.updateScoreTypeWeights(classId, subjectTeacherId, schoolYearId, weights);
    }

    @Override
    public Float getWeightForScoreType(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType) {
        return this.classScoreTypeRepository.getWeightForScoreType(classId, subjectTeacherId, schoolYearId, scoreType);
    }

    @Override
    public boolean addScoreTypeToClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType, Float weight) {
        return this.classScoreTypeRepository.addScoreTypeToClass(classId, subjectTeacherId, schoolYearId, scoreType, weight);
    }

    @Override
    public boolean removeScoreTypeFromClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType) {
        return this.classScoreTypeRepository.removeScoreTypeFromClass(classId, subjectTeacherId, schoolYearId, scoreType);
    }

}
