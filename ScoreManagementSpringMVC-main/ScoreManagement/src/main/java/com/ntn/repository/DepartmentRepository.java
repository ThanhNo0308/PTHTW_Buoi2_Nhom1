/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Department;
import java.util.List;

public interface DepartmentRepository {

    Department getDepartmentById(int departmentId);

    List<Department> getDepartments();

    long countDepartments();

    boolean addOrUpdateDepartment(Department department);

    boolean deleteDepartment(int departmentId);

    boolean hasRelatedData(int departmentId);

}
