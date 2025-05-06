/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Class;
import java.util.List;

public interface ClassService {

    Class getClassById(int classId);

    List<Class> getClasses();

    List<Class> getClassesByMajorId(int majorId);

    List<Class> getClassesByTeacher(int teacherId);

    List<Class> getClassesByKeyword(String keyword);

    int countClasses();

    boolean deleteClass(int classId);

    boolean addOrUpdateClass(Class classes);
}
