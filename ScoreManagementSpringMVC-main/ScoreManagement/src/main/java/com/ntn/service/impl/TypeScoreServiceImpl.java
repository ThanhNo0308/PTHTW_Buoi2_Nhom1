/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Typescore;
import com.ntn.repository.TypeScoreRepository;
import com.ntn.service.TypeScoreService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class TypeScoreServiceImpl implements TypeScoreService {

    @Autowired
    private TypeScoreRepository typeScoreRepository;

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
        return this.typeScoreRepository.getScoreTypesByClass(classId, subjectTeacherId, schoolYearId);
    }

    
}
