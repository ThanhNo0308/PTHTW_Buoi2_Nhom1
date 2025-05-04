package com.ntn.repository;

import com.ntn.pojo.Schoolyear;
import java.util.List;

public interface SchoolYearRepository {
    Schoolyear getSchoolYearById(int SchoolYearId);

    int getCurrentSchoolYearId();

    List<Schoolyear> getAllSchoolYears();

    boolean addOrUpdateSchoolYear(Schoolyear schoolYear);

    boolean deleteSchoolYear(int schoolYearId);

    List<Schoolyear> getSchoolYearsByNameYear(String nameYear);

    List<Schoolyear> getSchoolYearsBySemester(String semesterName);

    List<Schoolyear> getSchoolYearsByNameYearAndSemester(String nameYear, String semesterName);

    boolean hasRelatedData(int schoolYearId);
}
