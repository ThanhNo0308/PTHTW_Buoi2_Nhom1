/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.pojo.Class;
import com.ntn.pojo.Department;
import com.ntn.pojo.Trainingtype;
import com.ntn.service.ClassService;
import com.ntn.service.DepartmentService;
import com.ntn.service.TrainingTypeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author nguye
 */
@Controller
public class ClassController {
    @Autowired
    private ClassService classServ;
   
    @GetMapping("/classes")
    public String list(Model model, @RequestParam("majorId") int majorId) {
        List<Class> classes = classServ.getClassesByMajorId(majorId);
        model.addAttribute("classes", classes);
        return "class";
    }
}
