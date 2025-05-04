package com.ntn.controllers;

import com.ntn.pojo.Department;
import com.ntn.pojo.Major;
import com.ntn.pojo.Trainingtype;
import com.ntn.service.DepartmentService;
import com.ntn.service.MajorService;
import com.ntn.service.TrainingTypeService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.dao.DataIntegrityViolationException;

// Controller Ngành học
@Controller
@PreAuthorize("hasAuthority('Admin')")
public class MajorController {

    @Autowired
    private MajorService majorService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private TrainingTypeService trainingTypeService;

    @GetMapping("/admin/majors")
    public String showMajors(Model model,
            @RequestParam(name = "trainingTypeId", required = false) Integer trainingTypeId,
            @RequestParam(name = "departmentId", required = false) Integer departmentId) {

        List<Major> majors;
        List<Department> departments = departmentService.getDepartments();

        if (departmentId != null && trainingTypeId != null) {
            // Lọc theo cả khoa và hệ đào tạo
            majors = majorService.getMajorsByDepartmentAndTrainingType(departmentId, trainingTypeId);
            model.addAttribute("selectedDepartment", departmentService.getDepartmentById(departmentId));
            model.addAttribute("selectedTrainingType", trainingTypeService.getTrainingTypeById(trainingTypeId));
        } else if (departmentId != null) {
            // Chỉ lọc theo khoa
            majors = majorService.getMajorsByDepartmentId(departmentId);
            model.addAttribute("selectedDepartment", departmentService.getDepartmentById(departmentId));
        } else if (trainingTypeId != null) {
            // Chỉ lọc theo hệ đào tạo
            majors = majorService.getMajorsByTrainingTypeId(trainingTypeId);
            model.addAttribute("selectedTrainingType", trainingTypeService.getTrainingTypeById(trainingTypeId));
        } else {
            majors = majorService.getMajors();
        }

        model.addAttribute("majors", majors);
        model.addAttribute("trainingTypes", trainingTypeService.getTrainingTypes());
        model.addAttribute("departments", departments);
        return "admin/majors";
    }

    @PostMapping("/admin/major-add")
    public String majorAdd(
            @ModelAttribute("major") Major major,
            BindingResult bindingResult,
            @RequestParam(value = "departmentId.id", required = false) Integer departmentId,
            @RequestParam(value = "trainingTypeId.id", required = false) Integer trainingTypeId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin ngành học");
                return "redirect:/admin/majors?error=add-validation";
            }

            // Xử lý Department và TrainingType thủ công
            if (departmentId != null) {
                Department dept = departmentService.getDepartmentById(departmentId);
                major.setDepartmentId(dept);
            }

            if (trainingTypeId != null) {
                Trainingtype type = trainingTypeService.getTrainingTypeById(trainingTypeId);
                major.setTrainingTypeId(type);
            }

            boolean success = majorService.addOrUpdateMajor(major);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Thêm ngành học thành công");
                return "redirect:/admin/majors";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm ngành học");
                return "redirect:/admin/majors?error=add-validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/majors?error=add-validation";
        }
    }

    @PostMapping("/admin/major-update")
    public String majorUpdate(
            @ModelAttribute("major") Major major,
            BindingResult bindingResult,
            @RequestParam(value = "departmentId.id", required = false) Integer departmentId,
            @RequestParam(value = "trainingTypeId.id", required = false) Integer trainingTypeId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin ngành học");
                return "redirect:/admin/majors?error=update-validation";
            }

            // Xử lý Department và TrainingType thủ công
            if (departmentId != null) {
                Department dept = departmentService.getDepartmentById(departmentId);
                major.setDepartmentId(dept);
            }

            if (trainingTypeId != null) {
                Trainingtype type = trainingTypeService.getTrainingTypeById(trainingTypeId);
                major.setTrainingTypeId(type);
            }

            boolean success = majorService.addOrUpdateMajor(major);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ngành học thành công");
                return "redirect:/admin/majors";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật ngành học");
                return "redirect:/admin/majors?error=update-validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/majors?error=update-validation";
        }
    }

    @GetMapping("/admin/major-delete/{id}")
    public String majorDelete(@PathVariable("id") int majorId, RedirectAttributes redirectAttributes) {
        try {
            majorService.deleteMajor(majorId);
            redirectAttributes.addFlashAttribute("successMessage", "Ngành học đã được xóa thành công.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa ngành học này vì có lớp học hoặc sinh viên thuộc ngành này. "
                    + "Hãy chuyển lớp sang ngành khác trước khi xóa.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa ngành học: " + e.getMessage());
        }

        return "redirect:/admin/majors";
    }
}
