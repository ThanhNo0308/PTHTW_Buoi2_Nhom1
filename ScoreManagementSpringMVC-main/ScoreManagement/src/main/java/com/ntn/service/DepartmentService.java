/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Department;
import java.util.List;

/**
 *
 * @author nguye
 */
public interface DepartmentService {
    List<Department> getDepartments();
    boolean addOrUpdateDepartment(Department department);
    boolean deleteDepartment(int departmentId);
    Department getDepartmentById(int departmentId);
    boolean hasRelatedData(int departmentId);
    long countDepartments();
}
