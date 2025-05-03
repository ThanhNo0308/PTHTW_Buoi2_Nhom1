/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Department;
import com.ntn.repository.DepartmentRepository;
import com.ntn.service.DepartmentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl implements DepartmentService{
    
    @Autowired
    private DepartmentRepository departRepo;

    @Override
    public List<Department> getDepartments() {
        return this.departRepo.getDepartments();
    }   

    @Override
    public boolean addOrUpdateDepartment(Department department) {
        return this.departRepo.addOrUpdateDepartment(department);
    }

    @Override
    public boolean deleteDepartment(int departmentId) {
        return this.departRepo.deleteDepartment(departmentId);
    }
    
    @Override
    public Department getDepartmentById(int departmentId) {
        return this.departRepo.getDepartmentById(departmentId);
    }
    
    @Override
    public boolean hasRelatedData(int departmentId) {
        return this.departRepo.hasRelatedData(departmentId);
    }
    
    @Override
    public long countDepartments() {
        return this.departRepo.countDepartments();
    }
}
