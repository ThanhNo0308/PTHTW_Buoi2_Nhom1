/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Trainingtype;
import com.ntn.repository.TrainingTypeRepository;
import com.ntn.service.TrainingTypeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nguye
 */
@Service
public class TrainingServiceImpl implements TrainingTypeService{
    
    @Autowired
    private TrainingTypeRepository trainRepo;
    
    @Override
    public List<Trainingtype> getTrainingType() {
        return this.trainRepo.getTrainingType();
    }
    
}
