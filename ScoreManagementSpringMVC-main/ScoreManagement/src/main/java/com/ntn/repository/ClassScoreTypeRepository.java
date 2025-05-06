package com.ntn.repository;

import com.ntn.pojo.Classscoretypes;
import java.util.List;
import java.util.Map;

public interface ClassScoreTypeRepository {

    List<Classscoretypes> getScoreTypesByClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId);

    Float getWeightForScoreType(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType);

    boolean saveScoreType(Classscoretypes classScoreType);

    boolean deleteScoreType(Integer id);

    boolean updateScoreTypeWeights(Integer classId, Integer subjectTeacherId, Integer schoolYearId, Map<String, Float> weights);

    boolean addScoreTypeToClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType, Float weight);

    boolean removeScoreTypeFromClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType);

}
