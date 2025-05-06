/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Class;
import java.util.List;

public interface ClassRepository {
    
    Class getClassById(int classId);

    List<Class> getClasses();

    List<Class> getClassesByMajorId(int majorId);

    List<Class> getClassesByKeyword(String keyword);
    
    List<Class> getClassesByTeacher(int teacherId);
    
    int countClasses();

    boolean deleteClass(int classId);

    boolean addOrUpdateClass(Class classes);
}
