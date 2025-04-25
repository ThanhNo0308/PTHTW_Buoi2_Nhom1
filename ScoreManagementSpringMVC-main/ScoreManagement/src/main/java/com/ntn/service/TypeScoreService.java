/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Typescore;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Admin
 */
public interface TypeScoreService {

    Typescore getScoreTypeByName(String name);

    boolean addScoreType(String typeName, int subjectTeacherId);

    boolean addScoreType(Typescore newType);

    List<Typescore> getAllScoreTypes();

    List<String> getScoreTypesByClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId);

    boolean addScoreTypeToClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType, Float weight);

    boolean removeScoreTypeFromClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType);

    boolean updateScoreTypeWeights(Integer classId, Integer subjectTeacherId, Integer schoolYearId, Map<String, Double> weights);
}
