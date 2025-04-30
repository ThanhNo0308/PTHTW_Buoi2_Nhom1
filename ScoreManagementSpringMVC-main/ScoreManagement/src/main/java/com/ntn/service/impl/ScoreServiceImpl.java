package com.ntn.service.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.ntn.pojo.ListScoreDTO;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Classscoretypes;
import com.ntn.pojo.ScoreDTO;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import com.ntn.repository.ClassScoreTypeRepository;
import com.ntn.repository.ScoreRepository;
import com.ntn.repository.SchoolYearRepository;
import com.ntn.repository.SubjectTeacherRepository;
import com.ntn.repository.TypeScoreRepository;
import com.ntn.service.ClassService;
import com.ntn.service.EmailService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.ScoreService;
import com.ntn.service.SubjectTeacherService;
import com.ntn.service.TypeScoreService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Triển khai service điểm
 */
@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ScoreRepository scoreRepo;

    @Autowired
    private SchoolYearRepository schoolYearRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Autowired
    private SubjectTeacherRepository subjectTeacherRepository;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private TypeScoreService typeScoreService;

    @Autowired
    private LocalSessionFactoryBean sessionFactory;

    @Autowired
    private ClassScoreTypeRepository classScoreTypeRepository;

    @Autowired
    private TypeScoreRepository typeScoreRepository;

    private final Map<String, Map<String, Double>> weightConfigurations = new HashMap<>();

    @Override
    public Float getScoreWeight(Score score) {
        if (score == null || score.getScoreType() == null || score.getStudentID() == null
                || score.getSubjectTeacherID() == null || score.getSchoolYearId() == null) {
            return null;
        }

        Integer classId = score.getStudentID().getClassId().getId();
        Integer subjectTeacherId = score.getSubjectTeacherID().getId();
        Integer schoolYearId = score.getSchoolYearId().getId();
        String scoreType = score.getScoreType().getScoreType();

        return getScoreWeight(classId, subjectTeacherId, schoolYearId, scoreType);
    }

    @Override
    public Float getScoreWeight(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType) {
        return classScoreTypeRepository.getWeightForScoreType(classId, subjectTeacherId, schoolYearId, scoreType);
    }

    @Override
    @Transactional
    public List<Score> getScores() {
        return this.scoreRepo.getScores();
    }

    @Override
    public Score getScoreById(int id) {
        return this.scoreRepo.getScoreById(id);
    }

    @Override
    public List<Score> getScoreByStudentCode(String studentCode) {
        return this.scoreRepo.getScoreByStudentCode(studentCode);
    }

    @Override
    public List<Score> getScoreByStudentFullName(String firstName, String lastName) {
        return this.scoreRepo.getScoreByStudentFullName(firstName, lastName);
    }

    @Override
    public List<Score> getSubjectScoresByStudentCode(String studentCode) {
        return this.scoreRepo.getSubjectScoresByStudentCode(studentCode);
    }

    @Override
    public List<Score> getSubjectScoresByStudentCodeAndSchoolYear(String studentCode, int schoolYearId) {
        return this.scoreRepo.getSubjectScoresByStudentCodeAndSchoolYear(studentCode, schoolYearId);
    }

    @Override
    public List<Score> getListScoreBySubjectTeacherIdAndSchoolYearId(int subjectTeacherID, int schoolYearId) {
        return this.scoreRepo.getListScoreBySubjectTeacherIdAndSchoolYearId(subjectTeacherID, schoolYearId);
    }

    @Override
    public boolean saveListScoreByListScoreDTO(ListScoreDTO listScoreDTO) {
        return this.scoreRepo.saveListScoreByListScoreDTO(listScoreDTO);
    }

    @Override
    public List<Score> getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(int subjectTeacherID, int schoolYearId, int studentID) {
        return this.scoreRepo.getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(subjectTeacherID, schoolYearId, studentID);
    }

    @Override
    @Transactional
    public boolean importScoresFromCsv(MultipartFile file, int subjectTeacherId, int classId, int schoolYearId) throws Exception {
        try (CSVParser parser = new CSVParser(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<Score> scores = new ArrayList<>();

            // Tìm SubjectTeacher chính xác bằng cả 3 thông số
            Subjectteacher subjectTeacher = subjectTeacherRepository.findByIdClassIdAndSchoolYearId(
                    subjectTeacherId, classId, schoolYearId);

            if (subjectTeacher == null) {
                // Nếu không tìm thấy với 3 tiêu chí, thử tìm chỉ với ID
                System.out.println("SubjectTeacher not found with all 3 params. Trying with ID only.");
                subjectTeacher = subjectTeacherRepository.getSubJectTeacherById(subjectTeacherId);

                // Log cảnh báo nếu classId không khớp
                if (subjectTeacher != null && subjectTeacher.getClassId() != null
                        && subjectTeacher.getClassId().getId() != classId) {
                    System.out.println("WARNING: ClassId mismatch! Selected classId=" + classId
                            + ", but subjectTeacher has classId=" + subjectTeacher.getClassId().getId());
                }
            }

            if (subjectTeacher == null) {
                throw new Exception("Không tìm thấy thông tin phân công giảng dạy với ID=" + subjectTeacherId);
            }

            Schoolyear schoolYear = schoolYearService.getSchoolYearById(schoolYearId);
            if (schoolYear == null) {
                throw new Exception("Không tìm thấy thông tin học kỳ với ID=" + schoolYearId);
            }

            // Xử lý từng record trong CSV
            for (CSVRecord record : parser) {
                String studentCode;
                try {
                    studentCode = record.get("MSSV");
                } catch (IllegalArgumentException e) {
                    try {
                        studentCode = record.get("Mã SV");
                    } catch (IllegalArgumentException e2) {
                        System.err.println("Không tìm thấy cột MSSV hoặc Mã SV trong file CSV");
                        continue;
                    }
                }

                // Tìm thông tin sinh viên
                Student student = typeScoreRepository.getStudentByCode(studentCode);
                if (student == null) {
                    System.err.println("Không tìm thấy sinh viên với mã: " + studentCode);
                    continue;
                }

                // Xử lý từng cột điểm trong file CSV
                for (String headerName : parser.getHeaderNames()) {
                    if (!headerName.equals("MSSV") && !headerName.equals("Họ tên") && !headerName.equals("Mã SV")) {
                        String scoreValueStr = record.get(headerName);

                        // Bỏ qua nếu giá trị điểm trống
                        if (scoreValueStr == null || scoreValueStr.trim().isEmpty()) {
                            continue;
                        }

                        try {
                            float scoreValue = Float.parseFloat(scoreValueStr);

                            // Kiểm tra điểm có hợp lệ không (0-10)
                            if (scoreValue < 0 || scoreValue > 10) {
                                System.err.println("Điểm không hợp lệ (" + scoreValue + ") cho sinh viên " + studentCode);
                                continue;
                            }

                            // Tìm loại điểm theo tên
                            Typescore typeScore = findScoreType(headerName);
                            if (typeScore == null) {
                                System.err.println("Không tìm thấy loại điểm cho header: " + headerName);
                                continue;
                            }

                            // Kiểm tra xem điểm đã tồn tại chưa
                            Score existingScore = scoreRepo.getScoreByStudentSubjectSchoolYearAndType(
                                    student.getId(), subjectTeacherId, schoolYearId, typeScore.getScoreType());

                            if (existingScore != null) {
                                // Nếu điểm đã tồn tại, cập nhật giá trị điểm
                                existingScore.setScoreValue(scoreValue);
                                scores.add(existingScore);
                            } else {
                                // Nếu điểm chưa tồn tại, tạo mới
                                Score newScore = new Score();
                                newScore.setStudentID(student);
                                newScore.setSubjectTeacherID(subjectTeacher);
                                newScore.setScoreType(typeScore);
                                newScore.setScoreValue(scoreValue);
                                newScore.setIsDraft(true);
                                newScore.setIsLocked(false);
                                newScore.setSchoolYearId(schoolYear);
                                scores.add(newScore);
                            }

                        } catch (NumberFormatException e) {
                            System.err.println("Giá trị không phải số (" + scoreValueStr + ") cho sinh viên " + studentCode);
                        }
                    }
                }
            }

            // Lưu tất cả điểm
            return scoreRepo.saveScores(scores);
        } catch (Exception e) {
            System.err.println("Lỗi khi import điểm: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Không thể import điểm: " + e.getMessage());
        }
    }

    // Hàm hỗ trợ tìm loại điểm (không phân biệt hoa thường và khoảng trắng)
    private Typescore findScoreType(String headerName) {
        // Tìm chính xác trước
        Typescore typeScore = typeScoreRepository.getScoreTypeByName(headerName);
        if (typeScore != null) {
            return typeScore;
        }

        // Nếu không tìm thấy, thử so sánh không phân biệt hoa thường và khoảng trắng
        List<Typescore> allTypes = typeScoreRepository.getAllScoreTypes();
        String normalizedHeader = normalizeString(headerName);

        for (Typescore type : allTypes) {
            if (normalizeString(type.getScoreType()).equals(normalizedHeader)) {
                return type;
            }
        }

        return null;
    }

    private String normalizeString(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().toLowerCase();
    }

    @Override
    public byte[] exportScoresToCsv(int subjectTeacherId, int classId, int schoolYearId) throws Exception {
        List<Score> scores = this.scoreRepo.getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(
                subjectTeacherId, classId, schoolYearId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
                new java.io.OutputStreamWriter(out, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withHeader("MSSV", "Họ tên", "Giữa kỳ", "Cuối kỳ", "Tổng kết"))) {

            // Group scores by student
            for (Score score : scores) {
                Student student = score.getStudentID();
                printer.printRecord(
                        student.getStudentCode(),
                        student.getFirstName() + " " + student.getLastName(),
                        getScoreByType(scores, student, "Giữa kỳ"),
                        getScoreByType(scores, student, "Cuối kỳ"),
                        calculateFinalScore(scores, student)
                );
            }
        } catch (IOException e) {
            throw new Exception("Không thể tạo file CSV: " + e.getMessage());
        }

        return out.toByteArray();
    }

    @Override
    public byte[] exportScoresToPdf(int subjectTeacherId, int classId, int schoolYearId) throws Exception {
        List<Score> scores = this.scoreRepo.getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(
                subjectTeacherId, classId, schoolYearId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            // Tiêu đề
            Subjectteacher st = subjectTeacherRepository.getSubJectTeacherById(subjectTeacherId);
            document.add(new Paragraph("BẢNG ĐIỂM"));
            document.add(new Paragraph("Môn học: " + st.getSubjectId().getSubjectName()));
            document.add(new Paragraph("Giảng viên: " + st.getTeacherId().getTeacherName()));
            document.add(new Paragraph("\n"));

            // Tạo bảng điểm
            PdfPTable table = new PdfPTable(5); // 5 cột
            table.setWidthPercentage(100);

            // Thêm header
            addTableHeader(table);

            // Thêm dữ liệu
            addTableData(table, scores);

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new Exception("Không thể tạo file PDF: " + e.getMessage());
        }

        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(5);

        cell.setPhrase(new Paragraph("MSSV"));
        table.addCell(cell);

        cell.setPhrase(new Paragraph("Họ tên"));
        table.addCell(cell);

        cell.setPhrase(new Paragraph("Giữa kỳ"));
        table.addCell(cell);

        cell.setPhrase(new Paragraph("Cuối kỳ"));
        table.addCell(cell);

        cell.setPhrase(new Paragraph("Tổng kết"));
        table.addCell(cell);
    }

    private void addTableData(PdfPTable table, List<Score> scores) {
        // Group scores by student
        scores.stream()
                .map(Score::getStudentID)
                .distinct()
                .forEach(student -> {
                    table.addCell(student.getStudentCode());
                    table.addCell(student.getFirstName() + " " + student.getLastName());
                    table.addCell(String.valueOf(getScoreByType(scores, student, "Giữa kỳ")));
                    table.addCell(String.valueOf(getScoreByType(scores, student, "Cuối kỳ")));
                    table.addCell(String.valueOf(calculateFinalScore(scores, student)));
                });
    }

    private double getScoreByType(List<Score> scores, Student student, String scoreType) {
        return scores.stream()
                .filter(s -> s.getStudentID().getId().equals(student.getId())
                && s.getScoreType() != null
                && scoreType.equals(s.getScoreType().getScoreType()))
                .findFirst()
                .map(s -> s.getScoreValue() != null ? s.getScoreValue().doubleValue() : 0.0)
                .orElse(0.0);
    }

    // Phương thức tính điểm trung bình trong ScoreService
    public Float calculateAverageScore(List<Score> scores) {
        if (scores == null || scores.isEmpty()) {
            return null;
        }

        float totalWeightedScore = 0;
        float totalWeight = 0;

        for (Score score : scores) {
            if (score.getScoreValue() != null && score.getScoreType() != null) {
                // Lấy weight từ classscoretypes thông qua service
                Float weight = getScoreWeight(score);

                if (weight != null) {
                    totalWeightedScore += score.getScoreValue() * weight;
                    totalWeight += weight;
                }
            }
        }

        return totalWeight > 0 ? totalWeightedScore / totalWeight : null;
    }

    private double calculateFinalScore(List<Score> scores, Student student) {
        double midTerm = getScoreByType(scores, student, "Giữa kỳ");
        double finalTerm = getScoreByType(scores, student, "Cuối kỳ");

        // Tính điểm tổng kết (ví dụ: giữa kỳ 30%, cuối kỳ 70%)
        return (midTerm * 0.3) + (finalTerm * 0.7);
    }

    @Override
    @Transactional
    public boolean lockScores(int subjectTeacherId, int classId, int schoolYearId) {
        List<Score> scores = this.scoreRepo.getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(
                subjectTeacherId, classId, schoolYearId);

        // Cập nhật trạng thái khóa cho tất cả điểm
        for (Score score : scores) {
            score.setIsLocked(true); // Sửa từ setStatus(1) thành setIsLocked(true)
            score.setIsDraft(false); // Khi khoá thì không còn là nháp

            // Gửi email thông báo cho sinh viên
            Student student = score.getStudentID();
            String email = student.getEmail();
            String subject = "Thông báo có điểm môn học";

            String message = "Xin chào " + student.getFirstName() + " " + student.getLastName() + ",\n\n"
                    + "Điểm của bạn cho môn " + score.getSubjectTeacherID().getSubjectId().getSubjectName()
                    + " đã được giảng viên cập nhật.\n"
                    + "Vui lòng đăng nhập vào hệ thống để xem chi tiết.\n\n"
                    + "Trân trọng,\nPhòng Đào tạo";

            emailService.sendEmail(email, subject, message);
        }

        return scoreRepo.saveScores(scores);
    }

    @Override
    @Transactional
    public boolean saveScoresDraft(List<Score> scores) {
        // Đặt trạng thái nháp cho tất cả điểm
        for (Score score : scores) {
            score.setIsDraft(true); // Sửa từ setStatus(0) thành setIsDraft(true)
            score.setIsLocked(false); // Đảm bảo không khoá
        }

        return scoreRepo.saveScores(scores);
    }

    @Override
    @Transactional
    public boolean addScoreColumn(String columnName, int subjectTeacherId, int schoolYearId) {
        // Kiểm tra số lượng cột điểm hiện tại (không quá 5)
        int currentColumnCount = typeScoreRepository.countScoreTypesBySubjectTeacher(subjectTeacherId);

        if (currentColumnCount >= 5) {
            return false;
        }

        // Thêm cột điểm mới
        return typeScoreRepository.addScoreType(columnName, subjectTeacherId);
    }

    @Override
    public List<Score> findByStudent(Student student) {
        return scoreRepo.findByStudent(student);
    }

    @Override
    public List<Score> getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(
            int subjectTeacherId, int classId, int schoolYearId) {
        return this.scoreRepo.getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(
                subjectTeacherId, classId, schoolYearId);
    }

    @Override
    public ListScoreDTO createListScoreDTO(List<Score> scores, int subjectTeacherId, int schoolYearId) {
        if (scores == null || scores.isEmpty()) {
            // Lấy thông tin môn học và giáo viên
            Subjectteacher st = subjectTeacherRepository.getSubJectTeacherById(subjectTeacherId);
            Schoolyear schoolYear = schoolYearRepo.getSchoolYearById(schoolYearId);

            if (st != null && schoolYear != null) {
                return new ListScoreDTO(
                        subjectTeacherId,
                        schoolYearId,
                        st.getSubjectId().getSubjectName(),
                        st.getTeacherId().getTeacherName(),
                        schoolYear.getNameYear(),
                        new ArrayList<>(),
                        false
                );
            }
            return null;
        }

        // Lấy thông tin từ điểm đầu tiên
        Score firstScore = scores.get(0);
        Subjectteacher st = firstScore.getSubjectTeacherID();
        String subjectName = st.getSubjectId().getSubjectName();
        String teacherName = st.getTeacherId().getTeacherName();

        // Lấy tên năm học
        String schoolYearName = firstScore.getSchoolYearId().getNameYear();

        // Kiểm tra xem tất cả các điểm có bị khóa không
        boolean isLocked = true;
        for (Score score : scores) {
            if (score.getIsLocked() == null || !score.getIsLocked()) {
                isLocked = false;
                break;
            }
        }

        return new ListScoreDTO(
                subjectTeacherId,
                schoolYearId,
                subjectName,
                teacherName,
                schoolYearName,
                convertToScoreDTOList(scores),
                isLocked
        );
    }

// Phương thức hỗ trợ để chuyển đổi Score sang ScoreDTO
    // Phương thức hỗ trợ để chuyển đổi Score sang ScoreDTO
    private ScoreDTO convertToScoreDTO(Score score) {
        Student student = score.getStudentID();

        // Lấy thông tin loại điểm
        Integer typeScoreId = null;
        String typeScoreName = "Unknown";

        if (score.getScoreType() != null) {
            typeScoreName = score.getScoreType().getScoreType();
            // Vì TypeScore sử dụng String làm ID nên chuyển sang dạng Integer có thể NULL
            try {
                typeScoreId = Integer.valueOf(score.getScoreType().getScoreType().hashCode());
            } catch (NumberFormatException e) {
                typeScoreId = 0;
            }
        }

        // Chuyển đổi trạng thái từ boolean sang integer
        Integer status = 0; // Mặc định là nháp (draft)
        if (score.getIsLocked() != null && score.getIsLocked()) {
            status = 1; // Đã khóa (locked)
        }

        // Chuyển Float sang Double
        Double scoreValue = score.getScoreValue() != null ? score.getScoreValue().doubleValue() : null;

        return new ScoreDTO(
                score.getId(),
                student.getId(),
                student.getFirstName() + " " + student.getLastName(),
                student.getStudentCode(),
                typeScoreId,
                typeScoreName,
                scoreValue,
                status
        );
    }

// Phương thức hỗ trợ để chuyển đổi danh sách Score sang danh sách ScoreDTO
    private List<ScoreDTO> convertToScoreDTOList(List<Score> scores) {
        return scores.stream()
                .map(this::convertToScoreDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Score getScoreByStudentSubjectSchoolYearAndType(
            int studentId, int subjectTeacherId, int schoolYearId, String scoreType) {
        return scoreRepo.getScoreByStudentSubjectSchoolYearAndType(
                studentId, subjectTeacherId, schoolYearId, scoreType);
    }

    @Override
    public boolean saveScore(Score score) {
        return scoreRepo.saveScore(score);
    }

    @Override
    public boolean updateScoreLockStatus(int scoreId, boolean locked) {
        return scoreRepo.updateScoreLockStatus(scoreId, locked);
    }

    @Override
    public boolean saveScores(List<Score> scores) {
        return scoreRepo.saveScores(scores);
    }

    @Override
    public List<Score> getScoresByClassAndSubjectAndSchoolYear(
            int classId, int subjectTeacherId, int schoolYearId) {
        return null;
    }

    @Override
    @Transactional
    public boolean saveScoreWeights(Integer subjectTeacherId, Integer schoolYearId, Map<String, Double> weights) {
        try {
            // Lưu trọng số cho tất cả các lớp sử dụng môn học này
            Set<Integer> classIds = new HashSet<>();

            // Lấy danh sách các lớp từ điểm hiện tại
            List<Score> existingScores = scoreRepo.getListScoreBySubjectTeacherIdAndSchoolYearId(
                    subjectTeacherId, schoolYearId);

            for (Score score : existingScores) {
                if (score.getStudentID() != null && score.getStudentID().getClassId() != null) {
                    classIds.add(score.getStudentID().getClassId().getId());
                }
            }

            // Lưu cấu hình trọng số cho từng lớp
            for (Integer classId : classIds) {
                Map<String, Float> floatWeights = new HashMap<>();
                for (Map.Entry<String, Double> entry : weights.entrySet()) {
                    floatWeights.put(entry.getKey(), entry.getValue().floatValue());
                }

                classScoreTypeRepository.updateScoreTypeWeights(
                        classId, subjectTeacherId, schoolYearId, floatWeights);
            }

            // Không cần cập nhật trọng số trong các điểm hiện có vì đã đánh dấu là @Transient
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, Double> getScoreWeights(Integer subjectTeacherId, Integer schoolYearId) {
        Map<String, Double> weights = new HashMap<>();

        try {
            // Lấy một lớp học bất kỳ cho môn học này
            String hql = "SELECT DISTINCT s.studentID.classId.id FROM Score s WHERE "
                    + "s.subjectTeacherID.id = :subjectTeacherId AND "
                    + "s.schoolYearId.id = :schoolYearId";

            Session session = sessionFactory.getObject().getCurrentSession();
            Query query = session.createQuery(hql);
            query.setParameter("subjectTeacherId", subjectTeacherId);
            query.setParameter("schoolYearId", schoolYearId);
            query.setMaxResults(1);

            Integer classId = (Integer) query.uniqueResult();

            if (classId != null) {
                // Lấy trọng số từ ClassScoreType
                List<Classscoretypes> classScoreTypes = classScoreTypeRepository.getScoreTypesByClass(
                        classId, subjectTeacherId, schoolYearId);

                for (Classscoretypes cst : classScoreTypes) {
                    weights.put(cst.getScoreType().getScoreType(), cst.getWeight().doubleValue());
                }
            }

            // Nếu không có dữ liệu, sử dụng giá trị mặc định
            if (weights.isEmpty()) {
                weights.put("Giữa kỳ", 0.4);
                weights.put("Cuối kỳ", 0.6);
            }

            return weights;
        } catch (Exception e) {
            e.printStackTrace();

            // Giá trị mặc định
            weights.put("Giữa kỳ", 0.4);
            weights.put("Cuối kỳ", 0.6);
            return weights;
        }
    }

    @Override
    public boolean deleteScore(Integer scoreId) {
        return scoreRepo.deleteScore(scoreId);
    }

}
