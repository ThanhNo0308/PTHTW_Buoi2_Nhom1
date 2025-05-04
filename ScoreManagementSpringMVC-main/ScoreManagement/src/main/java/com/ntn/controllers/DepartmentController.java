package com.ntn.controllers;

import com.ntn.pojo.Department;
import com.ntn.service.DepartmentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

// Controller Khoa 
@Controller
@PreAuthorize("hasAuthority('Admin')")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/admin/departments")
    public String getDepartments(Model model) {
        List<Department> departments = departmentService.getDepartments();
        // Thêm đối tượng department mới cho form thêm
        model.addAttribute("department", new Department());
        model.addAttribute("departments", departments);
        return "admin/departments";
    }

    @PostMapping("/admin/departments/add")
    public String departmentAdd(@Valid @ModelAttribute("department") Department department,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.getDepartments());
            return "admin/departments";
        }

        boolean success = departmentService.addOrUpdateDepartment(department);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Thêm khoa thành công");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm khoa");
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/admin/departments/{id}")
    @ResponseBody
    public ResponseEntity<Department> getDepartment(@PathVariable int id) {
        Department department = departmentService.getDepartmentById(id);
        if (department != null) {
            return new ResponseEntity<>(department, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/admin/departments/update")
    public String departmentUpdate(@Valid @ModelAttribute("department") Department department,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.getDepartments());
            return "admin/departments";
        }

        boolean success = departmentService.addOrUpdateDepartment(department);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khoa thành công");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật khoa");
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/admin/departments/delete/{id}")
    public String departmentDelete(@PathVariable("id") int departmentId, RedirectAttributes redirectAttributes) {
        try {
            boolean success = departmentService.deleteDepartment(departmentId);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Xóa khoa thành công");
            } else {
                // Kiểm tra có dữ liệu liên quan
                boolean hasRelatedData = departmentService.hasRelatedData(departmentId);
                if (hasRelatedData) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                            "Không thể xóa khoa vì có dữ liệu liên quan (ngành học, giảng viên, môn học)");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa khoa");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        }

        return "redirect:/admin/departments";
    }
}