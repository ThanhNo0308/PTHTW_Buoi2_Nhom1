package com.ntn.repository;

import com.ntn.pojo.Schoolyear;
import java.util.List;

public interface SchoolYearRepository {

    Schoolyear getSchoolYearById(int SchoolYearId);

    List<Schoolyear> getAllSchoolYears();

    List<Schoolyear> getSchoolYearsByNameYear(String nameYear);

    List<Schoolyear> getSchoolYearsBySemester(String semesterName);

    List<Schoolyear> getSchoolYearsByNameYearAndSemester(String nameYear, String semesterName);

    int getCurrentSchoolYearId();

    boolean addOrUpdateSchoolYear(Schoolyear schoolYear);

    boolean deleteSchoolYear(int schoolYearId);

    boolean hasRelatedData(int schoolYearId);
}
