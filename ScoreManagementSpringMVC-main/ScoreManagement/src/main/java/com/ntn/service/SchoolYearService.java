/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Schoolyear;
import java.util.List;

/**
 *
 * @author vhuunghia
 */
public interface SchoolYearService {
    List<Schoolyear> getListSchoolYear(String currentYear);
}
