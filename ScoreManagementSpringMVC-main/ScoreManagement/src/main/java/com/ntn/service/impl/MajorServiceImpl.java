/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Major;
import com.ntn.repository.MajorRepository;
import com.ntn.service.MajorService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MajorServiceImpl implements MajorService {

    @Autowired
    private MajorRepository majorRepo;

    @Override
    public List<Major> getMajorsByDepartmentId(int departmentId) {
        return this.majorRepo.getMajorsByDepartmentId(departmentId);
    }

    @Override
    public List<Major> getMajorsByTrainingTypeId(int trainingtypeId) {
        return this.majorRepo.getMajorsByTrainingTypeId(trainingtypeId);
    }
    
    @Override
    public List<Major> getMajorsByDepartmentAndTrainingType(Integer departmentId, Integer trainingTypeId) {
        return this.majorRepo.getMajorsByDepartmentAndTrainingType(departmentId, trainingTypeId);
    }

    @Override
    public boolean addOrUpdateMajor(Major major) {
        return this.majorRepo.addOrUpdateMajor(major);
    }

    @Override
    public boolean deleteMajor(int majorId) {
        return this.majorRepo.deleteMajor(majorId);
    }

    @Override
    public int countMajors() {
        return this.majorRepo.countMajors();
    }

    @Override
    public List<Major> getMajors() {
        return this.majorRepo.getMajors();
    }

    @Override
    public Major getMajorById(int majorId) {
        return this.majorRepo.getMajorById(majorId);
    }
}
