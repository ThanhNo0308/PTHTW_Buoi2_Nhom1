/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Schoolyear;
import java.util.List;

public interface SchoolYearService {

    Schoolyear getSchoolYearById(int schoolYearId);

    List<Schoolyear> getAllSchoolYears();

    List<Schoolyear> getSchoolYearsByNameYear(String nameYear);

    List<Schoolyear> getSchoolYearsBySemester(String semesterName);

    List<Schoolyear> getSchoolYearsByNameYearAndSemester(String nameYear, String semesterName);

    int getCurrentSchoolYearId();

    boolean addOrUpdateSchoolYear(Schoolyear schoolYear);

    boolean deleteSchoolYear(int schoolYearId);

    boolean hasRelatedData(int schoolYearId);
}
