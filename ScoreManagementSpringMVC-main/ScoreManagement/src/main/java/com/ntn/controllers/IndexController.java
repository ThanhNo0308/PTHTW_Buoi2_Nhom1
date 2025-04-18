/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.pojo.Department;
import com.ntn.pojo.Trainingtype;
import com.ntn.service.DepartmentService;
import com.ntn.service.TrainingTypeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Kiet
 */
@Controller
@ControllerAdvice
public class IndexController {

    
     @Autowired
    private TrainingTypeService trainService;
    
    @Autowired
    private DepartmentService departService;
    
      @ModelAttribute
    public void commonAttr(Model model) {
        model.addAttribute("departments", this.departService.getDepartments());
         model.addAttribute("trainingTypes", this.trainService.getTrainingType());
    }
    @RequestMapping("/")
    public String index(Model model){
      
        return "index";

    }
}
