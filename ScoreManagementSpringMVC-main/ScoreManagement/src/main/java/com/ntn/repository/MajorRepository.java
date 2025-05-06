/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Major;
import java.util.List;

public interface MajorRepository {

    Major getMajorById(int majorId);

    List<Major> getMajors();

    List<Major> getMajorsByDepartmentId(int departmentId);

    List<Major> getMajorsByTrainingTypeId(int trainingtypeId);

    List<Major> getMajorsByDepartmentAndTrainingType(Integer departmentId, Integer trainingTypeId);

    int countMajors();

    boolean addOrUpdateMajor(Major major);

    boolean deleteMajor(int majorId);

}
