/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Schoolyear;
import com.ntn.repository.SchoolYearRepository;
import com.ntn.service.SchoolYearService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchoolYearServiceImp implements SchoolYearService {

    @Autowired
    private SchoolYearRepository schoolYearRepo;

    @Override
    public int getCurrentSchoolYearId() {
        return this.schoolYearRepo.getCurrentSchoolYearId();
    }

    @Override
    public List<Schoolyear> getAllSchoolYears() {
        return this.schoolYearRepo.getAllSchoolYears();
    }

    @Override
    public boolean addOrUpdateSchoolYear(Schoolyear schoolYear) {
        return this.schoolYearRepo.addOrUpdateSchoolYear(schoolYear);
    }

    @Override
    public boolean deleteSchoolYear(int schoolYearId) {
        return this.schoolYearRepo.deleteSchoolYear(schoolYearId);
    }

    @Override
    public Schoolyear getSchoolYearById(int schoolYearId) {
        return this.schoolYearRepo.getSchoolYearById(schoolYearId);
    }

    @Override
    public List<Schoolyear> getSchoolYearsByNameYear(String nameYear) {
        return this.schoolYearRepo.getSchoolYearsByNameYear(nameYear);
    }

    @Override
    public List<Schoolyear> getSchoolYearsBySemester(String semesterName) {
        return this.schoolYearRepo.getSchoolYearsBySemester(semesterName);
    }

    @Override
    public List<Schoolyear> getSchoolYearsByNameYearAndSemester(String nameYear, String semesterName) {
        return this.schoolYearRepo.getSchoolYearsByNameYearAndSemester(nameYear, semesterName);
    }

    @Override
    public boolean hasRelatedData(int schoolYearId) {
        return this.schoolYearRepo.hasRelatedData(schoolYearId);
    }
    
    
}
