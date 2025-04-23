package com.ntn.repository;

import com.ntn.pojo.Classscoretypes;
import java.util.List;
import java.util.Map;

public interface ClassScoreTypeRepository {
    boolean saveScoreType(Classscoretypes classScoreType);
    boolean deleteScoreType(Integer id);
    List<Classscoretypes> getScoreTypesByClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId);
    boolean updateScoreTypeWeights(Integer classId, Integer subjectTeacherId, Integer schoolYearId, Map<String, Float> weights);
    Float getWeightForScoreType(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType);
}