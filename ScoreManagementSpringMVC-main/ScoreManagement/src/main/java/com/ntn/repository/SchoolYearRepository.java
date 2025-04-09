package com.ntn.repository;

import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import java.util.List;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author vhuunghia
 */
public interface SchoolYearRepository {
    List<Schoolyear> getListSchoolYear(String currentYear);
    Schoolyear getSchoolYearById(int SchoolYearId);
    Subjectteacher getSubJectTeacherById(int subJectTeacherId);
    Typescore getScoreTypeByName(String typeScore);
}
