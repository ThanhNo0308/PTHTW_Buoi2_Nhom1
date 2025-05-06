/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Trainingtype;
import java.util.List;

public interface TrainingTypeService {

    Trainingtype getTrainingTypeById(int id);

    Trainingtype getTrainingTypeByName(String name);

    List<Trainingtype> getTrainingTypes();

    int countTrainingTypes();

    boolean addTrainingType(Trainingtype trainingType);

    boolean updateTrainingType(Trainingtype trainingType);

    boolean deleteTrainingType(int id);

    boolean hasRelatedMajors(int trainingTypeId);

}
