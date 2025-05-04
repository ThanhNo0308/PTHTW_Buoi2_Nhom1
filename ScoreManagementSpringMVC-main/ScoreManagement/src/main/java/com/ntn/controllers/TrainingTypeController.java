package com.ntn.controllers;

import com.ntn.pojo.Trainingtype;
import com.ntn.service.TrainingTypeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controller loại đào tạo
@Controller
@PreAuthorize("hasAuthority('Admin')")
public class TrainingTypeController {

    @Autowired
    private TrainingTypeService trainingTypeService;

    @GetMapping("/admin/training-types")
    public String getTrainingTypes(Model model) {
        List<Trainingtype> trainingTypes = trainingTypeService.getTrainingTypes();
        model.addAttribute("trainingTypes", trainingTypes);
        return "admin/training-types";
    }

    @PostMapping("/admin/training-types/add")
    public String addTrainingType(@ModelAttribute Trainingtype trainingType, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra trùng tên
            if (trainingTypeService.getTrainingTypeByName(trainingType.getTrainingTypeName()) != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Hệ đào tạo '" + trainingType.getTrainingTypeName() + "' đã tồn tại!");
                return "redirect:/admin/training-types?error=validation";
            }

            boolean success = trainingTypeService.addTrainingType(trainingType);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Thêm hệ đào tạo thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Thêm hệ đào tạo thất bại!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm hệ đào tạo: " + e.getMessage());
        }

        return "redirect:/admin/training-types";
    }

    @PostMapping("/admin/training-types/update")
    public String updateTrainingType(@ModelAttribute Trainingtype trainingType, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra tên trùng với hệ đào tạo khác
            Trainingtype existingType = trainingTypeService.getTrainingTypeByName(trainingType.getTrainingTypeName());
            if (existingType != null && !existingType.getId().equals(trainingType.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Hệ đào tạo '" + trainingType.getTrainingTypeName() + "' đã tồn tại!");
                return "redirect:/admin/training-types";
            }

            boolean success = trainingTypeService.updateTrainingType(trainingType);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hệ đào tạo thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật hệ đào tạo thất bại!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật hệ đào tạo: " + e.getMessage());
        }

        return "redirect:/admin/training-types";
    }

    @GetMapping("/admin/training-types/delete/{id}")
    public String deleteTrainingType(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra xem có ngành nào đang sử dụng hệ đào tạo này không
            if (trainingTypeService.hasRelatedMajors(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa! Hệ đào tạo này đang được sử dụng bởi các ngành.");
                return "redirect:/admin/training-types";
            }

            boolean success = trainingTypeService.deleteTrainingType(id);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Xóa hệ đào tạo thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Xóa hệ đào tạo thất bại!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa hệ đào tạo: " + e.getMessage());
        }

        return "redirect:/admin/training-types";
    }
}
