/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Class;
import com.ntn.repository.ClassRepository;
import com.ntn.service.ClassService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nguye
 */
@Service
public class ClassServiceImpl implements ClassService {
    
    @Autowired
    private ClassRepository classRepo;
    
    @Override
    public List<Class> getClasses() {
        return this.classRepo.getClasses();
    }

    @Override
    public List<Class> getClassesByMajorId(int majorId) {
        return this.classRepo.getClassesByMajorId(majorId);
    }

    @Override
    public boolean deleteClass(int classId) {
        return this.classRepo.deleteClass(classId);
    }

    @Override
    public boolean addOrUpdateClass(Class classes) {
        return this.classRepo.addOrUpdateClass(classes);
    }
    
}
