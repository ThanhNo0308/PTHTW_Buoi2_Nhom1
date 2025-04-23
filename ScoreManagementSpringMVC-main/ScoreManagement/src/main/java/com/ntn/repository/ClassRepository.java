/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Class;
import java.util.List;
/**
 *
 * @author nguye
 */
public interface ClassRepository {
    List<Class> getClasses();
    List<Class> getClassesByMajorId(int majorId);
    List<Class> getClassesByKeyword(String keyword);
    boolean deleteClass(int classId);
    boolean addOrUpdateClass(Class classes);
    
    Class getClassById(int classId);
    boolean updateScoreColumns(int classId, int additionalColumns, 
            String column3Name, String column4Name, String column5Name);
    List<Class> getClassesByTeacher(int teacherId);
    
    int countClasses();
    boolean updateClassConfiguration(int classId, boolean enableAttendance, 
        boolean enableActivityScoring, String gradingPolicy);
    
}
