package com.ntn.repository.impl;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ntn.pojo.Classscoretypes;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import com.ntn.repository.ClassScoreTypeRepository;
import com.ntn.repository.ScoreRepository;
import com.ntn.repository.SubjectTeacherRepository;
import com.ntn.repository.TypeScoreRepository;
import com.ntn.service.ClassService;
import com.ntn.service.EmailService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.TypeScoreService;
import java.util.List;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Repository
@Transactional
public class ScoreRepositoryImpl implements ScoreRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Autowired
    private ClassScoreTypeRepository classScoreTypeRepository;

    @Autowired
    private SubjectTeacherRepository subjectTeacherRepository;

    @Autowired
    private TypeScoreRepository typeScoreRepository;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private ScoreRepository scoreRepo;

    @Autowired
    private TypeScoreService typeScoreService;

    @Autowired
    private ClassService classService;

    @Autowired
    private EmailService emailService;

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
    public List<Score> getScores() {
        Session session = this.factory.getObject().getCurrentSession();
        Query q = session.createQuery("FROM Score");
        return q.getResultList();
    }

    @Override
    public Score getScoreById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Score.class, id);
    }

    @Override
    public boolean deleteScore(Integer scoreId) {
        try {
            Session session = this.factory.getObject().getCurrentSession();
            Score score = session.get(Score.class, scoreId);
            if (score != null) {
                session.delete(score);
                return true;
            }
            return false;
        } catch (HibernateException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Score> getScoreByStudentCode(String studentCode) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Score> query = builder.createQuery(Score.class);
        Root<Score> root = query.from(Score.class);

        query.where(builder.equal(root.get("studentID").get("studentCode"), studentCode));

        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public List<Score> getScoreByStudentFullName(String firstName, String lastName) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Score> query = builder.createQuery(Score.class);
        Root<Score> root = query.from(Score.class);

        Predicate firstNamePredicate = builder.like(root.get("studentID").get("firstName"), "%" + firstName + "%");
        Predicate lastNamePredicate = builder.like(root.get("studentID").get("lastName"), "%" + lastName + "%");

        query.where(builder.and(firstNamePredicate, lastNamePredicate));

        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public List<Score> findByStudent(Student student) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Score> query = builder.createQuery(Score.class);
        Root<Score> root = query.from(Score.class);

        query.where(builder.equal(root.get("studentID"), student));

        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public List<Score> getSubjectScoresByStudentCode(String studentCode) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Score> query = builder.createQuery(Score.class);
        Root<Score> root = query.from(Score.class);

        query.where(builder.equal(root.get("studentID").get("studentCode"), studentCode));
        query.orderBy(builder.asc(root.get("subjectTeacherID").get("subjectId").get("subjectName")));

        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public List<Score> getSubjectScoresByStudentCodeAndSchoolYear(String studentCode, int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Score> query = builder.createQuery(Score.class);
        Root<Score> root = query.from(Score.class);

        Predicate p1 = builder.equal(root.get("studentID").get("studentCode"), studentCode);
        Predicate p2 = builder.equal(root.get("schoolYearId").get("id"), schoolYearId);

        query.where(builder.and(p1, p2));
        query.orderBy(builder.asc(root.get("subjectTeacherID").get("subjectId").get("subjectName")));

        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public List<Score> getListScoreBySubjectTeacherIdAndSchoolYearId(int subjectTeacherID, int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Score> query = builder.createQuery(Score.class);
        Root<Score> root = query.from(Score.class);

        Predicate p1 = builder.equal(root.get("subjectTeacherID").get("id"), subjectTeacherID);
        Predicate p2 = builder.equal(root.get("schoolYearId").get("id"), schoolYearId);

        query.where(builder.and(p1, p2));

        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public List<Score> getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(
            int subjectTeacherID, int schoolYearId, int studentID) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Score> query = builder.createQuery(Score.class);
        Root<Score> root = query.from(Score.class);

        Predicate p1 = builder.equal(root.get("subjectTeacherID").get("id"), subjectTeacherID);
        Predicate p2 = builder.equal(root.get("schoolYearId").get("id"), schoolYearId);
        Predicate p3 = builder.equal(root.get("studentID").get("id"), studentID);

        query.where(builder.and(p1, p2, p3));

        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public boolean saveScores(List<Score> scores) {
        if (scores == null || scores.isEmpty()) {
            return true;
        }

        try {
            Session session = this.factory.getObject().getCurrentSession();
            for (Score score : scores) {
                // Kiểm tra xem điểm đã tồn tại chưa
                Query query = session.createQuery(
                        "FROM Score WHERE studentID.id = :studentId AND subjectTeacherID.id = :subjectTeacherId "
                        + "AND scoreType.scoreType = :scoreType AND schoolYearId.id = :schoolYearId");

                query.setParameter("studentId", score.getStudentID().getId());
                query.setParameter("subjectTeacherId", score.getSubjectTeacherID().getId());
                query.setParameter("scoreType", score.getScoreType().getScoreType());
                query.setParameter("schoolYearId", score.getSchoolYearId().getId());

                List<Score> existingScores = query.getResultList();

                if (!existingScores.isEmpty()) {
                    // Cập nhật điểm
                    Score existingScore = existingScores.get(0);
                    existingScore.setScoreValue(score.getScoreValue());
                    existingScore.setIsDraft(score.getIsDraft());
                    existingScore.setIsLocked(score.getIsLocked());
                    session.update(existingScore);
                } else {
                    // Tạo mới điểm
                    session.save(score);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // Đồng bộ với ScoreServiceImpl - không ném lại ngoại lệ
            return false;
        }
    }

    @Override
    public List<Score> getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(
            int subjectTeacherId, int classId, int schoolYearId) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Score> query = builder.createQuery(Score.class);
        Root<Score> root = query.from(Score.class);

        // Join với Student để lấy thông tin lớp
        Join<Score, Student> studentJoin = root.join("studentID");

        Predicate p1 = builder.equal(root.get("subjectTeacherID").get("id"), subjectTeacherId);
        Predicate p2 = builder.equal(studentJoin.get("classId").get("id"), classId);
        Predicate p3 = builder.equal(root.get("schoolYearId").get("id"), schoolYearId);

        query.where(builder.and(p1, p2, p3));

        Query q = session.createQuery(query);
        return q.getResultList();
    }

    @Override
    public boolean saveScore(Score score) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            if (score.getId() == null) {
                session.save(score);
            } else {
                session.update(score);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateScoreLockStatus(int scoreId, boolean locked) {
        try {
            Session session = this.factory.getObject().getCurrentSession();
            Score score = session.get(Score.class, scoreId);

            if (score != null) {
                score.setIsLocked(locked);
                session.update(score);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Score getScoreByStudentSubjectSchoolYearAndType(int studentId, int subjectTeacherId, int schoolYearId, String scoreType) {
        Session session = this.factory.getObject().getCurrentSession();

        String hql = "FROM Score s WHERE s.studentID.id = :studentId AND "
                + "s.subjectTeacherID.id = :subjectTeacherId AND "
                + "s.schoolYearId.id = :schoolYearId AND "
                + "s.scoreType.scoreType = :scoreType";

        Query<Score> query = session.createQuery(hql, Score.class);
        query.setParameter("studentId", studentId);
        query.setParameter("subjectTeacherId", subjectTeacherId);
        query.setParameter("schoolYearId", schoolYearId);
        query.setParameter("scoreType", scoreType);

        List<Score> results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        } else {
            // Nếu có nhiều kết quả, trả về kết quả đầu tiên
            return results.get(0);
        }
    }

    @Override
    public boolean importScoresFromCsv(MultipartFile file, int subjectTeacherId, int classId, int schoolYearId) throws Exception {
        try (CSVParser parser = new CSVParser(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<Score> scores = new ArrayList<>();
            Session session = this.factory.getObject().getCurrentSession();

            // Tìm SubjectTeacher chính xác bằng cả 3 thông số
            Subjectteacher subjectTeacher = subjectTeacherRepository.findByIdClassIdAndSchoolYearId(
                    subjectTeacherId, classId, schoolYearId);

            if (subjectTeacher == null) {
                // Nếu không tìm thấy với 3 tiêu chí, thử tìm chỉ với ID
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

                            String hql = "FROM Score s WHERE s.studentID.id = :studentId AND "
                                    + "s.subjectTeacherID.id = :subjectTeacherId AND "
                                    + "s.schoolYearId.id = :schoolYearId AND "
                                    + "s.scoreType.scoreType = :scoreType";

                            Query<Score> query = session.createQuery(hql, Score.class);
                            query.setParameter("studentId", student.getId());
                            query.setParameter("subjectTeacherId", subjectTeacherId);
                            query.setParameter("schoolYearId", schoolYearId);
                            query.setParameter("scoreType", typeScore.getScoreType());

                            // Kiểm tra xem điểm đã tồn tại chưa
                            List<Score> existingScores = query.getResultList();
                            Score existingScore = existingScores.isEmpty() ? null : existingScores.get(0);

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
            return saveScores(scores);
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

        try {
            // Lấy thông tin chi tiết
            Subjectteacher subjectTeacher = subjectTeacherRepository.getSubJectTeacherById(subjectTeacherId);
            com.ntn.pojo.Class classInfo = classService.getClassById(classId);
            Schoolyear schoolYear = schoolYearService.getSchoolYearById(schoolYearId);

            // Lấy danh sách các loại điểm được cấu hình
            List<String> scoreTypes = typeScoreService.getScoreTypesByClass(classId, subjectTeacherId, schoolYearId);

            // Đảm bảo có "Giữa kỳ" và "Cuối kỳ" trong danh sách
            if (!scoreTypes.contains("Giữa kỳ")) {
                scoreTypes.add("Giữa kỳ");
            }
            if (!scoreTypes.contains("Cuối kỳ")) {
                scoreTypes.add("Cuối kỳ");
            }

            // Sắp xếp để "Giữa kỳ" và "Cuối kỳ" luôn ở cuối
            scoreTypes = scoreTypes.stream()
                    .filter(type -> !type.equals("Giữa kỳ") && !type.equals("Cuối kỳ"))
                    .collect(Collectors.toCollection(ArrayList::new));
            scoreTypes.add("Giữa kỳ");
            scoreTypes.add("Cuối kỳ");

            // Format số thập phân
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2); // Tối đa 2 chữ số thập phân
            df.setMinimumFractionDigits(0); // Không cần số 0 thừa
            df.setGroupingUsed(false);

            // Tạo danh sách các tiêu đề
            List<String> headers = new ArrayList<>();
            headers.add("STT");
            headers.add("MSSV");
            headers.add("Họ tên");
            headers.addAll(scoreTypes);
            headers.add("Điểm TB");

            // Tạo CSVPrinter với tiêu đề động
            CSVPrinter printer = new CSVPrinter(
                    new java.io.OutputStreamWriter(out, StandardCharsets.UTF_8),
                    CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0]))
            );

            // Thêm thông tin về lớp, môn học, năm học...
            printer.printComment("Trường: Trường Đại học MilkyWay");

            // Lấy thông tin hệ đào tạo từ Major thông qua Class
            String trainingTypeName = "Chưa xác định";
            if (classInfo != null && classInfo.getMajorId() != null
                    && classInfo.getMajorId().getTrainingTypeId() != null) {
                trainingTypeName = classInfo.getMajorId().getTrainingTypeId().getTrainingTypeName();
            }

            printer.printComment("Hệ đào tạo: " + trainingTypeName);

            if (classInfo != null && classInfo.getMajorId() != null) {
                printer.printComment("Ngành: " + classInfo.getMajorId().getMajorName());
            }

            if (classInfo != null) {
                printer.printComment("Lớp: " + classInfo.getClassName());
            }

            if (subjectTeacher != null && subjectTeacher.getSubjectId() != null) {
                printer.printComment("Môn học: " + subjectTeacher.getSubjectId().getSubjectName());
            }

            if (schoolYear != null) {
                printer.printComment("Năm học/Học kỳ: " + schoolYear.getNameYear() + " " + schoolYear.getSemesterName());
            }

            if (subjectTeacher != null && subjectTeacher.getTeacherId() != null) {
                printer.printComment("Giảng viên: " + subjectTeacher.getTeacherId().getTeacherName());
            }

            // Nhóm điểm theo sinh viên
            Map<Student, List<Score>> scoresByStudent = scores.stream()
                    .collect(Collectors.groupingBy(Score::getStudentID));

            // In dữ liệu sinh viên
            int index = 1;
            for (Map.Entry<Student, List<Score>> entry : scoresByStudent.entrySet()) {
                Student student = entry.getKey();
                List<Score> studentScores = entry.getValue();

                // Tạo danh sách các giá trị cho hàng này
                List<String> rowValues = new ArrayList<>();
                rowValues.add(String.valueOf(index++)); // STT
                rowValues.add(student.getStudentCode()); // MSSV
                rowValues.add(student.getLastName() + " " + student.getFirstName()); // Họ tên

                // Điểm theo từng loại
                Map<String, Float> studentScoresByType = new HashMap<>();
                for (Score score : studentScores) {
                    if (score.getScoreType() != null && score.getScoreValue() != null) {
                        studentScoresByType.put(score.getScoreType().getScoreType(), score.getScoreValue());
                    }
                }

                // Thêm điểm của các loại vào hàng
                double totalWeightedScore = 0;
                double totalWeight = 0;

                for (String scoreType : scoreTypes) {
                    Float scoreValue = studentScoresByType.get(scoreType);
                    String displayValue = scoreValue != null ? df.format(scoreValue) : "-";
                    rowValues.add(displayValue);

                    // Tính điểm trung bình
                    if (scoreValue != null) {
                        Float weight = getScoreWeight(student.getClassId().getId(),
                                subjectTeacherId, schoolYearId, scoreType);
                        if (weight != null) {
                            totalWeightedScore += scoreValue * weight;
                            totalWeight += weight;
                        }
                    }
                }

                // Thêm điểm trung bình vào hàng
                String avgScore = "-";
                if (totalWeight > 0) {
                    avgScore = df.format(totalWeightedScore / totalWeight);
                }
                rowValues.add(avgScore);

                // In hàng vào CSV
                printer.printRecord(rowValues);
            }

            printer.flush();
            printer.close();
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
            // Thiết lập document với kích thước và margin phù hợp
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Cấu hình font hỗ trợ tiếng Việt
            BaseFont unicodeFont = BaseFont.createFont(
                    "c:\\windows\\fonts\\arial.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );

            Font titleFont = new Font(unicodeFont, 14, Font.BOLD);
            Font headerFont = new Font(unicodeFont, 12, Font.BOLD);
            Font normalFont = new Font(unicodeFont, 10, Font.NORMAL);
            Font smallFont = new Font(unicodeFont, 9, Font.NORMAL);

            // Lấy thông tin chi tiết
            Subjectteacher subjectTeacher = subjectTeacherRepository.getSubJectTeacherById(subjectTeacherId);
            com.ntn.pojo.Class classInfo = classService.getClassById(classId);
            Schoolyear schoolYear = schoolYearService.getSchoolYearById(schoolYearId);

            // Lấy danh sách các loại điểm được cấu hình
            List<String> scoreTypes = typeScoreService.getScoreTypesByClass(classId, subjectTeacherId, schoolYearId);

            // Đảm bảo có "Giữa kỳ" và "Cuối kỳ" trong danh sách
            if (!scoreTypes.contains("Giữa kỳ")) {
                scoreTypes.add("Giữa kỳ");
            }
            if (!scoreTypes.contains("Cuối kỳ")) {
                scoreTypes.add("Cuối kỳ");
            }

            // Sắp xếp để "Giữa kỳ" và "Cuối kỳ" luôn ở cuối
            scoreTypes = scoreTypes.stream()
                    .filter(type -> !type.equals("Giữa kỳ") && !type.equals("Cuối kỳ"))
                    .collect(Collectors.toCollection(ArrayList::new));
            scoreTypes.add("Giữa kỳ");
            scoreTypes.add("Cuối kỳ");

            // Thêm tiêu đề
            Paragraph title = new Paragraph("BẢNG ĐIỂM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Thêm thông tin chi tiết
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 3});

            // Lấy thông tin hệ đào tạo từ Major thông qua Class
            String trainingTypeName = "Chưa xác định";
            if (classInfo != null && classInfo.getMajorId() != null
                    && classInfo.getMajorId().getTrainingTypeId() != null) {
                trainingTypeName = classInfo.getMajorId().getTrainingTypeId().getTrainingTypeName();
            }

            String major = classInfo != null && classInfo.getMajorId() != null
                    ? classInfo.getMajorId().getMajorName() : "Chưa có thông tin";

            addInfoRow(infoTable, "Hệ đào tạo:", trainingTypeName, normalFont);
            addInfoRow(infoTable, "Ngành:", major, normalFont);
            addInfoRow(infoTable, "Lớp:", classInfo != null ? classInfo.getClassName() : "N/A", normalFont);
            addInfoRow(infoTable, "Môn học:", subjectTeacher != null && subjectTeacher.getSubjectId() != null
                    ? subjectTeacher.getSubjectId().getSubjectName() : "N/A", normalFont);
            addInfoRow(infoTable, "Năm học/Học kỳ:", schoolYear != null ? schoolYear.getNameYear() + " " + schoolYear.getSemesterName() : "N/A", normalFont);
            addInfoRow(infoTable, "Giảng viên:", subjectTeacher != null && subjectTeacher.getTeacherId() != null
                    ? subjectTeacher.getTeacherId().getTeacherName() : "N/A", normalFont);

            infoTable.setSpacingAfter(15);
            document.add(infoTable);

            // Tạo bảng điểm với số cột động
            // 3 cột cố định + số loại điểm + 1 cột điểm TB
            PdfPTable table = new PdfPTable(3 + scoreTypes.size() + 1);
            table.setWidthPercentage(100);

            // Thiết lập độ rộng cột
            float[] columnWidths = new float[3 + scoreTypes.size() + 1];
            columnWidths[0] = 1.0f; // STT
            columnWidths[1] = 2.5f; // MSSV
            columnWidths[2] = 4.5f; // Họ tên

            for (int i = 0; i < scoreTypes.size(); i++) {
                columnWidths[3 + i] = 2.0f; // Các cột điểm
            }
            columnWidths[columnWidths.length - 1] = 2.0f; // Cột Điểm TB

            table.setWidths(columnWidths);

            // Thêm header
            addTableHeader(table, scoreTypes, headerFont);

            // Thêm dữ liệu - Pass subjectTeacherId and schoolYearId to the method
            addTableData(table, scores, scoreTypes, normalFont, subjectTeacherId, schoolYearId);

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new Exception("Không thể tạo file PDF: " + e.getMessage());
        }

        return out.toByteArray();
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setBorderWidth(0);
        labelCell.setPaddingBottom(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        valueCell.setBorderWidth(0);
        valueCell.setPaddingBottom(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, List<String> scoreTypes, Font font) {
        // Tạo cell mẫu có style chung
        PdfPCell headerCell = new PdfPCell();
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerCell.setPadding(5);
        headerCell.setBackgroundColor(new BaseColor(220, 220, 220));

        // Các cột cơ bản
        headerCell.setPhrase(new Phrase("STT", font));
        table.addCell(headerCell);

        headerCell.setPhrase(new Phrase("MSSV", font));
        table.addCell(headerCell);

        headerCell.setPhrase(new Phrase("Họ tên", font));
        table.addCell(headerCell);

        // Các cột điểm động theo cấu hình
        for (String scoreType : scoreTypes) {
            headerCell.setPhrase(new Phrase(scoreType, font));
            table.addCell(headerCell);
        }

        // Cột điểm trung bình
        headerCell.setPhrase(new Phrase("Điểm TB", font));
        table.addCell(headerCell);
    }

    private String formatScore(Float score) {
        if (score == null) {
            return "-";
        }

        if (score == Math.floor(score)) {
            // Nếu là số nguyên, không hiển thị phần thập phân
            return Integer.toString(score.intValue());
        } else {
            // Sử dụng DecimalFormat để loại bỏ số 0 không cần thiết ở cuối
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(0);
            df.setGroupingUsed(false);
            return df.format(score);
        }
    }

    private void addTableData(PdfPTable table, List<Score> scores, List<String> scoreTypes, Font font,
            Integer subjectTeacherId, Integer schoolYearId) {
        // Nhóm điểm theo sinh viên
        Map<Student, List<Score>> scoresByStudent = scores.stream()
                .collect(Collectors.groupingBy(Score::getStudentID));

        // Format số thập phân
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2); // Tối đa 2 chữ số thập phân
        df.setMinimumFractionDigits(0); // Không cần số 0 thừa
        df.setGroupingUsed(false);

        int index = 1;
        for (Map.Entry<Student, List<Score>> entry : scoresByStudent.entrySet()) {
            Student student = entry.getKey();
            List<Score> studentScores = entry.getValue();

            // Cell STT - căn giữa
            PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(index++), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            // Cell MSSV - căn giữa
            cell = new PdfPCell(new Phrase(student.getStudentCode(), font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            // Cell Họ tên - căn trái
            cell = new PdfPCell(new Phrase(student.getLastName() + " " + student.getFirstName(), font));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            // Điểm theo từng loại
            Map<String, Float> studentScoresByType = new HashMap<>();
            for (Score score : studentScores) {
                if (score.getScoreType() != null && score.getScoreValue() != null) {
                    studentScoresByType.put(score.getScoreType().getScoreType(), score.getScoreValue());
                }
            }

            // Thêm điểm của các loại vào bảng
            double totalWeightedScore = 0;
            double totalWeight = 0;

            for (String scoreType : scoreTypes) {
                Float scoreValue = studentScoresByType.get(scoreType);
                String displayValue = formatScore(scoreValue);

                // Nếu có điểm và có trọng số, tính vào điểm trung bình
                if (scoreValue != null) {
                    // Fix: Use the passed subjectTeacherId and schoolYearId parameters
                    Float weight = getScoreWeight(student.getClassId().getId(),
                            subjectTeacherId, schoolYearId, scoreType);
                    if (weight != null) {
                        totalWeightedScore += scoreValue * weight;
                        totalWeight += weight;
                    }
                }

                // Cell điểm - căn giữa
                cell = new PdfPCell(new Phrase(displayValue, font));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            // Tính và thêm điểm trung bình
            String avgScore = totalWeight > 0 ? formatScore((float) (totalWeightedScore / totalWeight)) : "-";

            // Cell điểm TB - căn giữa
            cell = new PdfPCell(new Phrase(avgScore, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
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

    @Override
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
    public boolean saveScoresDraft(List<Score> scores) {
        // Đặt trạng thái nháp cho tất cả điểm
        for (Score score : scores) {
            score.setIsDraft(true); // Sửa từ setStatus(0) thành setIsDraft(true)
            score.setIsLocked(false); // Đảm bảo không khoá
        }

        return scoreRepo.saveScores(scores);
    }

    @Override
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

            Session session = factory.getObject().getCurrentSession();
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

}
