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

import jakarta.validation.Valid;
import java.beans.PropertyEditorSupport;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.WebDataBinder;

@Controller
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

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/admin/major-add")
    public String majorAddForm(Model model) {
        model.addAttribute("major", new Major());
        model.addAttribute("departments", departmentService.getDepartments());
        model.addAttribute("trainingTypes", trainingTypeService.getTrainingTypes());
        return "admin/major-add";
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/admin/major-add")
    public String majorAdd(
            @Valid @ModelAttribute("major") Major major,
            BindingResult bindingResult,
            @RequestParam(value = "departmentId.id", required = false) Integer departmentId,
            @RequestParam(value = "trainingTypeId.id", required = false) Integer trainingTypeId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                System.out.println("VALIDATION ERRORS: " + bindingResult.getAllErrors());
                model.addAttribute("departments", departmentService.getDepartments());
                model.addAttribute("trainingTypes", trainingTypeService.getTrainingTypes());
                return "admin/major-add";
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
                model.addAttribute("errorMessage", "Không thể thêm ngành học");
                model.addAttribute("departments", departmentService.getDepartments());
                model.addAttribute("trainingTypes", trainingTypeService.getTrainingTypes());
                return "admin/major-add";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            model.addAttribute("departments", departmentService.getDepartments());
            model.addAttribute("trainingTypes", trainingTypeService.getTrainingTypes());
            return "admin/major-add";
        }
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/admin/major-update/{id}")
    public String majorUpdateForm(@PathVariable("id") int majorId, Model model) {
        Major major = majorService.getMajorById(majorId);

        if (major == null) {
            return "redirect:/admin/major?error=major-not-found";
        }

        model.addAttribute("major", major);
        model.addAttribute("departments", departmentService.getDepartments());
        model.addAttribute("trainingTypes", trainingTypeService.getTrainingTypes());
        return "admin/major-update";
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/admin/major-update")
    public String majorUpdate(
            @Valid @ModelAttribute("major") Major major,
            BindingResult bindingResult,
            @RequestParam(value = "departmentId.id", required = false) Integer departmentId,
            @RequestParam(value = "trainingTypeId.id", required = false) Integer trainingTypeId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("UPDATE MAJOR: " + major.getMajorName() + ", ID: " + major.getId());
            System.out.println("DEPARTMENT ID: " + departmentId);
            System.out.println("TRAINING TYPE ID: " + trainingTypeId);

            if (bindingResult.hasErrors()) {
                model.addAttribute("departments", departmentService.getDepartments());
                model.addAttribute("trainingTypes", trainingTypeService.getTrainingTypes());
                return "admin/major-update";
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
                model.addAttribute("errorMessage", "Không thể cập nhật ngành học");
                model.addAttribute("departments", departmentService.getDepartments());
                model.addAttribute("trainingTypes", trainingTypeService.getTrainingTypes());
                return "admin/major-update";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            model.addAttribute("departments", departmentService.getDepartments());
            model.addAttribute("trainingTypes", trainingTypeService.getTrainingTypes());
            return "admin/major-update";
        }
    }

    @PreAuthorize("hasAuthority('Admin')")
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
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Đăng ký PropertyEditor tùy chỉnh để chuyển đổi id thành đối tượng Department
        binder.registerCustomEditor(Department.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                    return;
                }
                try {
                    int id = Integer.parseInt(text);
                    Department dept = departmentService.getDepartmentById(id);
                    setValue(dept);
                } catch (NumberFormatException e) {
                    setValue(null);
                }
            }
        });
        
        // Đăng ký PropertyEditor tùy chỉnh để chuyển đổi id thành đối tượng TrainingType
        binder.registerCustomEditor(Trainingtype.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                    return;
                }
                try {
                    int id = Integer.parseInt(text);
                    Trainingtype type = trainingTypeService.getTrainingTypeById(id);
                    setValue(type);
                } catch (NumberFormatException e) {
                    setValue(null);
                }
            }
        });
    }
}
