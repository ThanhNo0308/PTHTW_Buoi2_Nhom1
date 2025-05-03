/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Classscoretypes;
import java.util.List;
import java.util.Map;

public interface ClassScoreTypeService {

    boolean saveScoreType(Classscoretypes classScoreType);

    boolean deleteScoreType(Integer id);

    List<Classscoretypes> getScoreTypesByClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId);

    boolean updateScoreTypeWeights(Integer classId, Integer subjectTeacherId, Integer schoolYearId, Map<String, Float> weights);

    Float getWeightForScoreType(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType);
}
