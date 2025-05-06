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


@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Override
    public List<Trainingtype> getTrainingTypes() {
        return this.trainingTypeRepository.getTrainingTypes();
    }

    @Override
    public Trainingtype getTrainingTypeById(int id) {
        return this.trainingTypeRepository.getTrainingTypeById(id);
    }

    @Override
    public Trainingtype getTrainingTypeByName(String name) {
        return this.trainingTypeRepository.getTrainingTypeByName(name);
    }

    @Override
    public boolean addTrainingType(Trainingtype trainingType) {
        return this.trainingTypeRepository.addTrainingType(trainingType);
    }

    @Override
    public boolean updateTrainingType(Trainingtype trainingType) {
        return this.trainingTypeRepository.updateTrainingType(trainingType);
    }

    @Override
    public boolean deleteTrainingType(int id) {
        return this.trainingTypeRepository.deleteTrainingType(id);
    }

    @Override
    public boolean hasRelatedMajors(int trainingTypeId) {
        return this.trainingTypeRepository.hasRelatedMajors(trainingTypeId);
    }

    @Override
    public int countTrainingTypes() {
        return this.trainingTypeRepository.countTrainingTypes();
    }

}
