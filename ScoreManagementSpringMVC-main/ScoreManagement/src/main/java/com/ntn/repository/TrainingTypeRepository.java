/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Trainingtype;
import java.util.List;

public interface TrainingTypeRepository {

    List<Trainingtype> getTrainingTypes();

    Trainingtype getTrainingTypeById(int id);

    Trainingtype getTrainingTypeByName(String name);

    boolean addTrainingType(Trainingtype trainingType);

    boolean updateTrainingType(Trainingtype trainingType);

    boolean deleteTrainingType(int id);

    boolean hasRelatedMajors(int trainingTypeId);

    int countTrainingTypes();
}
