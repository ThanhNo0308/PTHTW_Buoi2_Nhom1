package com.ntn.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ntn.repository.StatisticsRepository;
import com.ntn.service.StatisticsService;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Override
    public Map<String, Long> getScoreDistributionByClass(Integer classId, Integer schoolYearId, Integer subjectId) {
        Map<String, Long> result = statisticsRepository.getScoreDistributionByClass(classId, schoolYearId, subjectId);

        // Đảm bảo các giá trị không null
        if (result == null) {
            result = new LinkedHashMap<>();
        }

        // Khởi tạo các khoảng điểm với giá trị mặc định là 0
        if (!result.containsKey("Dưới 4")) {
            result.put("Dưới 4", 0L);
        }
        if (!result.containsKey("4 - 5")) {
            result.put("4 - 5", 0L);
        }
        if (!result.containsKey("5 - 6.5")) {
            result.put("5 - 6.5", 0L);
        }
        if (!result.containsKey("6.5 - 8")) {
            result.put("6.5 - 8", 0L);
        }
        if (!result.containsKey("8 - 9")) {
            result.put("8 - 9", 0L);
        }
        if (!result.containsKey("9 - 10")) {
            result.put("9 - 10", 0L);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getAverageScoreBySubject(Integer schoolYearId, Integer teacherId) {
        List<Object[]> data = statisticsRepository.getAverageScoreBySubject(schoolYearId, teacherId);
        List<Map<String, Object>> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();

        for (Object[] row : data) {
            if (row == null || row.length < 4) {
                continue; // Bỏ qua dữ liệu không hợp lệ
            }

            Map<String, Object> item = new HashMap<>();
            item.put("subjectId", row[0]);
            item.put("subjectName", row[1]);

            Double avgScore;
            if (row[2] instanceof BigDecimal) {
                avgScore = ((BigDecimal) row[2]).doubleValue();
            } else {
                avgScore = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            }

            item.put("averageScore", avgScore);
            item.put("studentCount", row[3] != null ? row[3] : 0);

            // Khởi tạo danh sách rỗng để tránh null
            List<Map<String, Object>> scoreTypeDetails = new ArrayList<>();

            // Xử lý thông tin chi tiết các loại điểm nếu có
            if (row.length > 4 && row[4] != null) {
                try {
                    String jsonDetails = row[4].toString();
                    if (jsonDetails != null && !jsonDetails.isEmpty() && !jsonDetails.equals("null")) {
                        scoreTypeDetails = objectMapper.readValue(jsonDetails,
                                new TypeReference<List<Map<String, Object>>>() {
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi xử lý JSON từ SQL: " + e.getMessage());
                }
            }

            // Luôn đặt scoreTypeDetails, không bao giờ để null
            item.put("scoreTypeDetails", scoreTypeDetails);

            result.add(item);
        }

        return result;
    }

    @Override
    public Map<String, Object> getPassFailRateByFilter(Integer classId, Integer majorId, Integer departmentId,
            Integer schoolYearId, Integer subjectId, Integer teacherId) {
        Map<String, Object> result = statisticsRepository.getPassFailRateByFilter(
                classId, majorId, departmentId, schoolYearId, subjectId, teacherId);

        // Đảm bảo các giá trị không null
        if (result == null) {
            result = new HashMap<>();
        }

        if (!result.containsKey("passCount")) {
            result.put("passCount", 0L);
        }
        if (!result.containsKey("failCount")) {
            result.put("failCount", 0L);
        }
        if (!result.containsKey("totalCount")) {
            result.put("totalCount", 0L);
        }
        if (!result.containsKey("passRate")) {
            result.put("passRate", 0.0);
        }
        if (!result.containsKey("failRate")) {
            result.put("failRate", 0.0);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getStudentCountByMajor(Integer departmentId, Integer schoolYearId) {
        List<Object[]> data = statisticsRepository.getStudentCountByMajor(departmentId, schoolYearId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : data) {
            Map<String, Object> item = new HashMap<>();
            item.put("majorId", row[0]);
            item.put("majorName", row[1]);
            item.put("trainingTypeName", row[2]);
            item.put("departmentName", row[3]);
            item.put("studentCount", row[4]);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getAverageScoreByClass(Integer subjectId, Integer schoolYearId) {
        List<Object[]> data = statisticsRepository.getAverageScoreByClass(subjectId, schoolYearId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : data) {
            Map<String, Object> item = new HashMap<>();
            item.put("classId", row[0]);
            item.put("className", row[1]);
            item.put("majorName", row[2]);
            item.put("averageScore", row[3]);
            item.put("studentCount", row[4]);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getAverageScoreByDepartment(Integer schoolYearId) {
        List<Object[]> data = schoolYearId == null
                ? statisticsRepository.getAverageScoreByDepartment()
                : statisticsRepository.getAverageScoreByDepartment(schoolYearId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : data) {
            Map<String, Object> item = new HashMap<>();
            item.put("departmentId", row[0]);
            item.put("departmentName", row[1]);

            // Chuyển đổi BigDecimal sang Double nếu cần
            Double avgScore;
            if (row[2] instanceof BigDecimal) {
                avgScore = ((BigDecimal) row[2]).doubleValue();
            } else {
                avgScore = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            }

            item.put("averageScore", avgScore);
            item.put("studentCount", row[3]);
            result.add(item);
        }

        return result;
    }

    @Override
    public byte[] exportStatisticsToExcel(String statType, Integer classId, Integer majorId, Integer departmentId,
            Integer schoolYearId, Integer subjectId, Integer teacherId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Thống kê");

            // Tạo style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            List<String> headers = new ArrayList<>();
            List<Map<String, Object>> data = new ArrayList<>();

            switch (statType) {
                case "subject":
                    headers = List.of("Mã môn học", "Tên môn học", "Điểm trung bình", "Số sinh viên");
                    data = getAverageScoreBySubject(schoolYearId, departmentId);
                    break;
                case "major":
                    headers = List.of("Mã ngành", "Tên ngành", "Hệ đào tạo", "Khoa", "Số sinh viên");
                    data = getStudentCountByMajor(departmentId, schoolYearId);
                    break;
                case "class":
                    headers = List.of("Mã lớp", "Tên lớp", "Ngành", "Điểm trung bình", "Số sinh viên");
                    data = getAverageScoreByClass(subjectId, schoolYearId);
                    break;
                case "department":
                    headers = List.of("Mã khoa", "Tên khoa", "Điểm trung bình", "Số sinh viên");
                    data = getAverageScoreByDepartment(schoolYearId);
                    break;
                default:
                    headers = List.of("Loại thống kê không hợp lệ");
            }

            // Tạo header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Thêm dữ liệu
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> rowData = data.get(i);

                int cellIndex = 0;
                for (String header : headers) {
                    Cell cell = row.createCell(cellIndex++);

                    switch (header) {
                        case "Mã môn học":
                            cell.setCellValue(rowData.get("subjectId").toString());
                            break;
                        case "Tên môn học":
                            cell.setCellValue((String) rowData.get("subjectName"));
                            break;
                        case "Điểm trung bình":
                            Object avgScore = rowData.get("averageScore");
                            if (avgScore != null) {
                                cell.setCellValue(Double.parseDouble(avgScore.toString()));
                            }
                            break;
                        case "Số sinh viên":
                            cell.setCellValue(Long.parseLong(rowData.get("studentCount").toString()));
                            break;
                        case "Mã ngành":
                            cell.setCellValue(rowData.get("majorId").toString());
                            break;
                        case "Tên ngành":
                            cell.setCellValue((String) rowData.get("majorName"));
                            break;
                        case "Hệ đào tạo":
                            cell.setCellValue((String) rowData.get("trainingTypeName"));
                            break;
                        case "Khoa":
                            cell.setCellValue((String) rowData.get("departmentName"));
                            break;
                        case "Mã lớp":
                            cell.setCellValue(rowData.get("classId").toString());
                            break;
                        case "Tên lớp":
                            cell.setCellValue((String) rowData.get("className"));
                            break;
                        case "Ngành":
                            cell.setCellValue((String) rowData.get("majorName"));
                            break;
                        case "Mã khoa":
                            cell.setCellValue(rowData.get("departmentId").toString());
                            break;
                        case "Tên khoa":
                            cell.setCellValue((String) rowData.get("departmentName"));
                            break;
                    }
                }
            }

            // Auto-size các cột
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public List<Map<String, Object>> getTopStudentsBySubject(Integer subjectId, Integer schoolYearId, Integer limit) {
        List<Object[]> data = statisticsRepository.getTopStudentsBySubject(subjectId, schoolYearId, limit);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : data) {
            Map<String, Object> item = new HashMap<>();
            item.put("studentId", row[0]);
            item.put("studentName", row[1]);
            item.put("studentCode", row[2]);

            Double avgScore;
            if (row[3] instanceof BigDecimal) {
                avgScore = ((BigDecimal) row[3]).doubleValue();
            } else {
                avgScore = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            }

            item.put("averageScore", avgScore);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getScoreTrendsBySubject(Integer subjectId) {
        List<Object[]> data = statisticsRepository.getScoreTrendsBySubject(subjectId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : data) {
            Map<String, Object> item = new HashMap<>();
            item.put("schoolYearId", row[0]);
            item.put("semester", row[1]);

            Double avgScore;
            if (row[2] instanceof BigDecimal) {
                avgScore = ((BigDecimal) row[2]).doubleValue();
            } else {
                avgScore = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            }

            item.put("averageScore", avgScore);
            item.put("studentCount", row[3] != null ? ((Number) row[3]).intValue() : 0);

            result.add(item);
        }

        return result;
    }

    @Override
    public Map<String, Long> getScoreDistributionByMajor(Integer majorId, Integer schoolYearId) {
        Map<String, Long> result = statisticsRepository.getScoreDistributionByMajor(majorId, schoolYearId);

        // Đảm bảo các giá trị không null
        if (result == null) {
            result = new LinkedHashMap<>();
        }

        // Khởi tạo các khoảng điểm với giá trị mặc định là 0
        if (!result.containsKey("0-2")) {
            result.put("0-2", 0L);
        }
        if (!result.containsKey("2-4")) {
            result.put("2-4", 0L);
        }
        if (!result.containsKey("4-6")) {
            result.put("4-6", 0L);
        }
        if (!result.containsKey("6-8")) {
            result.put("6-8", 0L);
        }
        if (!result.containsKey("8-10")) {
            result.put("8-10", 0L);
        }

        return result;
    }
}
