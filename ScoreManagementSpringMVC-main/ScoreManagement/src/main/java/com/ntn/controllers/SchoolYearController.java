package com.ntn.controllers;

import com.ntn.pojo.Schoolyear;
import com.ntn.service.SchoolYearService;

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
import java.util.Calendar;

@Controller
@PreAuthorize("hasAuthority('Admin')")
public class SchoolYearController {

    @Autowired
    private SchoolYearService schoolYearService;

    @GetMapping("/admin/school-years")
    public String getSchoolYears(
            @RequestParam(name = "year", required = false) String year,
            @RequestParam(name = "semesterName", required = false) String semesterName,
            Model model) {

        List<Schoolyear> schoolYears;

        if (year != null && !year.isEmpty()) {
            // Nếu có lọc theo năm học (nameYear)
            if (semesterName != null && !semesterName.isEmpty()) {
                // Lọc theo cả năm học và học kỳ
                schoolYears = schoolYearService.getSchoolYearsByNameYearAndSemester(year, semesterName);
            } else {
                // Chỉ lọc theo năm học
                schoolYears = schoolYearService.getSchoolYearsByNameYear(year);
            }
        } else if (semesterName != null && !semesterName.isEmpty()) {
            // Chỉ lọc theo học kỳ
            schoolYears = schoolYearService.getSchoolYearsBySemester(semesterName);
        } else {
            // Không lọc
            schoolYears = schoolYearService.getAllSchoolYears();
        }

        // Thêm năm học hiện tại vào model
        int currentSchoolYearId = schoolYearService.getCurrentSchoolYearId();
        Schoolyear currentSchoolYear = schoolYearService.getSchoolYearById(currentSchoolYearId);

        model.addAttribute("schoolYears", schoolYears);
        model.addAttribute("currentSchoolYear", currentSchoolYear);

        return "admin/school-years";
    }

    @PostMapping("/admin/school-years/add")
    public String schoolYearAdd(
            @ModelAttribute("schoolYear") Schoolyear schoolYear,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin năm học");
                return "redirect:/admin/school-years?error=add-validation";
            }

            // Trích xuất năm từ các đối tượng Date
            Calendar calStart = Calendar.getInstance();
            Calendar calEnd = Calendar.getInstance();

            calStart.setTime(schoolYear.getYearStart());
            calEnd.setTime(schoolYear.getYearEnd());

            int startYear = calStart.get(Calendar.YEAR);
            int endYear = calEnd.get(Calendar.YEAR);

            // Tạo nameYear theo định dạng "2024-2025"
            String nameYear = startYear + "-" + endYear;
            schoolYear.setNameYear(nameYear);

            boolean success = schoolYearService.addOrUpdateSchoolYear(schoolYear);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Thêm năm học thành công");
                return "redirect:/admin/school-years";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm năm học");
                return "redirect:/admin/school-years?error=add-validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/school-years?error=add-validation";
        }
    }

    @PostMapping("/admin/school-years/update")
    public String schoolYearUpdate(
            @ModelAttribute("schoolYear") Schoolyear schoolYear,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin năm học");
                return "redirect:/admin/school-years?error=update-validation";
            }

            // Trích xuất năm từ các đối tượng Date
            Calendar calStart = Calendar.getInstance();
            Calendar calEnd = Calendar.getInstance();

            calStart.setTime(schoolYear.getYearStart());
            calEnd.setTime(schoolYear.getYearEnd());

            int startYear = calStart.get(Calendar.YEAR);
            int endYear = calEnd.get(Calendar.YEAR);

            // Tạo nameYear theo định dạng "2024-2025"
            String nameYear = startYear + "-" + endYear;
            schoolYear.setNameYear(nameYear);

            boolean success = schoolYearService.addOrUpdateSchoolYear(schoolYear);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật năm học thành công");
                return "redirect:/admin/school-years";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật năm học");
                return "redirect:/admin/school-years?error=update-validation";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/school-years?error=update-validation";
        }
    }

    @GetMapping("/admin/school-years/delete/{id}")
    public String schoolYearDelete(@PathVariable("id") int schoolYearId, RedirectAttributes redirectAttributes) {
        try {
            boolean success = schoolYearService.deleteSchoolYear(schoolYearId);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Xóa năm học thành công");
            } else {
                // Kiểm tra xem có dữ liệu liên quan không
                if (schoolYearService.hasRelatedData(schoolYearId)) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Không thể xóa năm học vì đã có dữ liệu điểm số hoặc lịch học liên quan.");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa năm học");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
        }

        return "redirect:/admin/school-years";
    }

    @GetMapping("/admin/school-years/current")
    @ResponseBody
    public ResponseEntity<Schoolyear> getCurrentSchoolYear() {
        int schoolYearId = schoolYearService.getCurrentSchoolYearId();
        Schoolyear schoolYear = schoolYearService.getSchoolYearById(schoolYearId);

        if (schoolYear != null) {
            return new ResponseEntity<>(schoolYear, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
