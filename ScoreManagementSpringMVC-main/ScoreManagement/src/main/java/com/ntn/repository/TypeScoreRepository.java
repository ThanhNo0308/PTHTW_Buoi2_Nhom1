/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Student;
import com.ntn.pojo.Typescore;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Admin
 */
public interface TypeScoreRepository {

    Typescore getScoreTypeByName(String name);

    int countScoreTypesBySubjectTeacher(int subjectTeacherId);

    Student getStudentByCode(String studentCode);

    boolean addScoreTypeToClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType, Float weight);

    boolean removeScoreTypeFromClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType);

    boolean updateScoreTypeWeights(Integer classId, Integer subjectTeacherId, Integer schoolYearId, Map<String, Double> weights);

    List<Typescore> getAllScoreTypes();

    boolean addScoreType(Typescore newType);
}
