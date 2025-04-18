/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import com.ntn.pojo.Department;
import com.ntn.pojo.Major;
import com.ntn.pojo.Trainingtype;
import com.ntn.service.DepartmentService;
import com.ntn.service.MajorService;
import com.ntn.service.TrainingTypeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author nguye
 */
@Controller
public class MajorController {
    @Autowired
    private MajorService majorService;
  
      @GetMapping("/major")
    public String showMajorPage(Model model, @RequestParam("departmentId") int departmentId) {
        // Lấy danh sách các Major tương ứng với departmentId và đưa vào model để hiển thị trong trang major.jsp
        List<Major> majors = majorService.getMajorsByDepartmentId(departmentId);
        model.addAttribute("majors", majors);
        return "major";
    }
    
}