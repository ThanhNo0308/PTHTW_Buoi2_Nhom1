/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Typescore;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface TypeScoreRepository {
    Typescore getScoreTypeByName(String name);
    int countScoreTypesBySubjectTeacher(int subjectTeacherId);
    
    boolean addScoreType(String typeName, int subjectTeacherId);
    
    Student getStudentByCode(String studentCode);
    
   
    
    List<Typescore> getAllScoreTypes();
    
    boolean addScoreType(Typescore newType);
}
