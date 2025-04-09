/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Schoolyear;
import com.ntn.repository.SchoolYearRepository;
import com.ntn.repository.impl.SchoolYearRepositoryImp;
import com.ntn.service.SchoolYearService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author vhuunghia
 */
@Service
public class SchoolYearServiceImp implements SchoolYearService{
    @Autowired
    private SchoolYearRepository schoolYearRepository;
    
    
    @Override
    public List<Schoolyear> getListSchoolYear(String currentYear){
        
        
        
        return this.schoolYearRepository.getListSchoolYear(currentYear);
    }
}
