/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hcmou.controllers;

import com.hcmou.pojo.Class;
import com.hcmou.pojo.Department;
import com.hcmou.pojo.Trainingtype;
import com.hcmou.service.ClassService;
import com.hcmou.service.DepartmentService;
import com.hcmou.service.TrainingTypeService;
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
