/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import java.util.List;

/**
 *
 * @author vhuunghia
 */
public interface SchoolYearService {
    List<Schoolyear> getListSchoolYear(String currentYear);
    int getCurrentSchoolYearId();
    List<Schoolyear> getAllSchoolYears();
     Subjectteacher getSubJectTeacherById(int id);
     Typescore getScoreTypeByName(String name);
     
    boolean addOrUpdateSchoolYear(Schoolyear schoolYear);
    boolean deleteSchoolYear(int schoolYearId);
    Schoolyear getSchoolYearById(int schoolYearId);
    List<Schoolyear> getSchoolYearsByNameYear(String nameYear);
    List<Schoolyear> getSchoolYearsBySemester(String semesterName);
    List<Schoolyear> getSchoolYearsByNameYearAndSemester(String nameYear, String semesterName);
    boolean hasRelatedData(int schoolYearId);
}
