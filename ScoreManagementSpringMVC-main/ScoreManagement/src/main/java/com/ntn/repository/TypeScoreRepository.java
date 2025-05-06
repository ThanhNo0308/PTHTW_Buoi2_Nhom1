/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Typescore;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface TypeScoreRepository {

    Typescore getScoreTypeByName(String name);

    List<Typescore> getAllScoreTypes();
    
    List<String> getScoreTypesByClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId);

    boolean addScoreType(Typescore newType);
    
}
