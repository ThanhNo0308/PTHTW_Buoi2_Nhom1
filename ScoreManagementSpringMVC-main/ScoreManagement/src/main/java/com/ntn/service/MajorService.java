/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Major;
import java.util.List;

/**
 *
 * @author nguye
 */
public interface MajorService {
     List<Major> getMajorsByDepartmentId(int departmentId);
     List<Major> getMajorsByTrainingTypeId(int trainingtypeId);
     boolean addOrUpdateMajor(Major major);
    boolean deleteMajor(int majorId);
}
