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
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Studentsubjectteacher;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import com.ntn.repository.ScoreRepository;
import com.ntn.service.ClassScoreTypeService;
import com.ntn.service.ClassService;
import com.ntn.service.SchoolYearService;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
import com.ntn.service.StudentSubjectTeacherService;
import com.ntn.service.SubjectTeacherService;
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
    private ClassScoreTypeService ClassScoreTypeService;

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Autowired
    private SchoolYearService schoolYearService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private TypeScoreService typeScoreService;

    @Autowired
    private ClassService classService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentSubjectTeacherService studentSubjectTeacherService;

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
        return ClassScoreTypeService.getWeightForScoreType(classId, subjectTeacherId, schoolYearId, scoreType);
    }

    @Override
    public Score getScoreById(int id) {
        Session session = this.factory.getObject().getCurrentSession();
        return session.get(Score.class, id);
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
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<Score> query = builder.createQuery(Score.class);
                Root<Score> root = query.from(Score.class);

                // Tạo các điều kiện
                Predicate studentIdPredicate = builder.equal(root.get("studentID").get("id"), score.getStudentID().getId());
                Predicate subjectTeacherIdPredicate = builder.equal(root.get("subjectTeacherID").get("id"), score.getSubjectTeacherID().getId());
                Predicate scoreTypePredicate = builder.equal(root.get("scoreType").get("scoreType"), score.getScoreType().getScoreType());
                Predicate schoolYearIdPredicate = builder.equal(root.get("schoolYearId").get("id"), score.getSchoolYearId().getId());

                // Kết hợp các điều kiện
                query.where(builder.and(studentIdPredicate, subjectTeacherIdPredicate, scoreTypePredicate, schoolYearIdPredicate));

                List<Score> existingScores = session.createQuery(query).getResultList();

                if (!existingScores.isEmpty()) {
                    // Cập nhật điểm
                    Score existingScore = existingScores.get(0);
                    existingScore.setScoreValue(score.getScoreValue());

                    // Chỉ cập nhật trạng thái nháp/khóa nếu trạng thái mới không mâu thuẫn với trạng thái hiện tại
                    if (existingScore.getIsLocked() && score.getIsLocked() != null && !score.getIsLocked()) {
                        // Giữ nguyên trạng thái đã khóa nếu điểm đang bị khóa và trạng thái mới là mở khóa
                    } else {
                        existingScore.setIsDraft(score.getIsDraft());
                        existingScore.setIsLocked(score.getIsLocked());
                    }

                    session.update(existingScore);

                } else {
                    // Tạo mới điểm
                    session.save(score);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
    public Map<String, Object> importScoresFromCsv(MultipartFile file, int subjectTeacherId, int classId, int schoolYearId) throws Exception {
        // Tạo map kết quả
        Map<String, Object> result = new HashMap<>();

        // Tạo danh sách để lưu các lỗi điểm không hợp lệ
        List<Map<String, String>> invalidScores = new ArrayList<>();

        try (CSVParser parser = new CSVParser(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<Score> scores = new ArrayList<>();
            Set<Integer> processedStudentIds = new HashSet<>(); // Track processed students for enrollment
            Session session = this.factory.getObject().getCurrentSession();

            // Các đoạn code tìm SubjectTeacher và SchoolYear giữ nguyên
            Subjectteacher subjectTeacher = subjectTeacherService.findByIdClassIdAndSchoolYearId(
                    subjectTeacherId, classId, schoolYearId);

            if (subjectTeacher == null) {
                subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
            }

            if (subjectTeacher == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy thông tin phân công giảng dạy với ID=" + subjectTeacherId);
                return result;
            }

            Schoolyear schoolYear = schoolYearService.getSchoolYearById(schoolYearId);
            if (schoolYear == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy thông tin học kỳ với ID=" + schoolYearId);
                return result;
            }

            // Xử lý từng record trong CSV
            int rowNumber = 1; // Đếm số dòng để báo cáo lỗi chính xác
            for (CSVRecord record : parser) {
                rowNumber++; // Tăng số dòng (dòng đầu tiên là header)
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
                Student student = studentService.getStudentByCode(studentCode);
                if (student == null) {
                    System.err.println("Không tìm thấy sinh viên với mã: " + studentCode);
                    continue;
                }

                // Đánh dấu sinh viên đã được xử lý (để tự động đăng ký sau)
                processedStudentIds.add(student.getId());

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
                                // Thu thập thông tin lỗi
                                Map<String, String> error = new HashMap<>();
                                error.put("row", String.valueOf(rowNumber));
                                error.put("studentCode", studentCode);
                                error.put("fullName", student.getLastName() + " " + student.getFirstName());
                                error.put("scoreType", headerName);
                                error.put("value", scoreValueStr);
                                error.put("error", "Điểm phải nằm trong khoảng từ 0 đến 10");
                                invalidScores.add(error);
                                continue;
                            }

                            // Tìm loại điểm theo tên
                            Typescore typeScore = findScoreType(headerName);
                            if (typeScore == null) {
                                System.err.println("Không tìm thấy loại điểm cho header: " + headerName);
                                continue;
                            }

                            // Phần còn lại xử lý và lưu điểm
                            // (code cũ, giữ nguyên)
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
                            // Thu thập thông tin lỗi với giá trị không phải số
                            Map<String, String> error = new HashMap<>();
                            error.put("row", String.valueOf(rowNumber));
                            error.put("studentCode", studentCode);
                            error.put("fullName", student.getLastName() + " " + student.getFirstName());
                            error.put("scoreType", headerName);
                            error.put("value", scoreValueStr);
                            error.put("error", "Giá trị điểm không phải là số hợp lệ");
                            invalidScores.add(error);
                        }
                    }
                }
            }

            // Kiểm tra nếu có lỗi điểm không hợp lệ
            if (!invalidScores.isEmpty()) {
                result.put("success", false);
                result.put("message", "Tìm thấy điểm không hợp lệ trong file CSV");
                result.put("invalidScores", invalidScores);
                return result;
            }

            // Lưu tất cả điểm
            boolean success = saveScores(scores);

            // Thực hiện đăng ký học tập cho tất cả sinh viên đã nhập điểm (nếu chưa đăng ký)
            for (Integer studentId : processedStudentIds) {
                if (!studentSubjectTeacherService.checkDuplicate(studentId, subjectTeacherId)) {
                    Student student = session.get(Student.class, studentId);
                    if (student != null) {
                        addStudentEnrollment(student, subjectTeacher);
                    }
                }
            }

            result.put("success", success);
            result.put("message", success ? "Import điểm thành công" : "Có lỗi khi import điểm");
            return result;

        } catch (Exception e) {
            System.err.println("Lỗi khi import điểm: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Không thể import điểm: " + e.getMessage());
            return result;
        }
    }

    private void addStudentEnrollment(Student student, Subjectteacher subjectTeacher) {
        Session session = this.factory.getObject().getCurrentSession();

        Studentsubjectteacher enrollment = new Studentsubjectteacher();
        enrollment.setStudentId(student);
        enrollment.setSubjectTeacherId(subjectTeacher);

        session.persist(enrollment);
    }

    @Override
    public boolean saveScoresDraft(List<Score> scores) {
        try {
            Session session = this.factory.getObject().getCurrentSession();

            for (Score score : scores) {
                // Kiểm tra xem điểm đã tồn tại chưa
                CriteriaBuilder builder = session.getCriteriaBuilder();
                CriteriaQuery<Score> query = builder.createQuery(Score.class);
                Root<Score> root = query.from(Score.class);

                // Tạo các điều kiện
                Predicate studentIdPredicate = builder.equal(root.get("studentID").get("id"), score.getStudentID().getId());
                Predicate subjectTeacherIdPredicate = builder.equal(root.get("subjectTeacherID").get("id"), score.getSubjectTeacherID().getId());
                Predicate scoreTypePredicate = builder.equal(root.get("scoreType").get("scoreType"), score.getScoreType().getScoreType());
                Predicate schoolYearIdPredicate = builder.equal(root.get("schoolYearId").get("id"), score.getSchoolYearId().getId());

                // Kết hợp các điều kiện
                query.where(builder.and(studentIdPredicate, subjectTeacherIdPredicate, scoreTypePredicate, schoolYearIdPredicate));

                List<Score> existingScores = session.createQuery(query).getResultList();

                if (!existingScores.isEmpty()) {
                    // Cập nhật điểm
                    Score existingScore = existingScores.get(0);

                    // Nếu điểm đã bị khóa, giữ nguyên trạng thái khóa
                    if (existingScore.getIsLocked() != null && existingScore.getIsLocked()) {
                        // Chỉ cập nhật giá trị điểm nếu điểm mới khác điểm cũ
                        if (!existingScore.getScoreValue().equals(score.getScoreValue())) {
                            existingScore.setScoreValue(score.getScoreValue());
                            session.update(existingScore);
                        }
                    } else {
                        // Điểm chưa bị khóa, cập nhật bình thường
                        existingScore.setScoreValue(score.getScoreValue());
                        existingScore.setIsDraft(true); // Luôn là nháp
                        existingScore.setIsLocked(false); // Không khóa
                        session.update(existingScore);
                    }
                } else {
                    // Tạo mới điểm
                    score.setIsDraft(true); // Luôn là nháp
                    score.setIsLocked(false); // Không khóa
                    session.save(score);
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
    public boolean toggleScoreLock(int scoreId, boolean unlock) {
        try {
            Session session = this.factory.getObject().getCurrentSession();
            Score score = session.get(Score.class, scoreId);

            if (score != null) {
                // Cập nhật trạng thái khóa
                score.setIsLocked(!unlock); // true = khóa, false = mở khóa

                score.setIsDraft(unlock);

                session.update(score);
                return true;
            }
            return false;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public byte[] exportScoresToCsv(int subjectTeacherId, int classId, int schoolYearId) throws Exception {
        List<Score> scores = this.scoreService.getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(
                subjectTeacherId, classId, schoolYearId);

        List<Student> allStudentsInClass = studentService.getStudentByClassId(classId);

        // Lấy thêm sinh viên đã đăng ký học từ các lớp khác
        List<Studentsubjectteacher> enrollments = studentSubjectTeacherService.getBySubjectTeacherId(subjectTeacherId);
        Set<Student> uniqueStudents = new HashSet<>(allStudentsInClass);

        // Thêm sinh viên từ danh sách đăng ký
        for (Studentsubjectteacher enrollment : enrollments) {
            uniqueStudents.add(enrollment.getStudentId());
        }

        // Chuyển set thành list để xử lý
        allStudentsInClass = new ArrayList<>(uniqueStudents);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // Lấy thông tin chi tiết
            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
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
            Map<Student, List<Score>> scoresByStudent = new HashMap<>();

            // Nhóm điểm theo sinh viên (nếu có)
            for (Score score : scores) {
                Student student = score.getStudentID();
                if (!scoresByStudent.containsKey(student)) {
                    scoresByStudent.put(student, new ArrayList<>());
                }
                scoresByStudent.get(student).add(score);
            }

            // Đảm bảo tất cả sinh viên đều có trong map, kể cả sinh viên không có điểm nào
            for (Student student : allStudentsInClass) {
                if (!scoresByStudent.containsKey(student)) {
                    scoresByStudent.put(student, new ArrayList<>());
                }
            }

            // Sắp xếp sinh viên theo mã sinh viên để có thứ tự nhất quán
            List<Student> sortedStudents = new ArrayList<>(scoresByStudent.keySet());
            sortedStudents.sort((s1, s2) -> s1.getStudentCode().compareTo(s2.getStudentCode()));

            // In dữ liệu sinh viên
            int index = 1;
            for (Student student : sortedStudents) {
                List<Score> studentScores = scoresByStudent.get(student);

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
        List<Score> scores = this.scoreService.getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(
                subjectTeacherId, classId, schoolYearId);

        List<Student> allStudentsInClass = studentService.getStudentByClassId(classId);

        // Lấy thêm sinh viên đã đăng ký học từ các lớp khác
        List<Studentsubjectteacher> enrollments = studentSubjectTeacherService.getBySubjectTeacherId(subjectTeacherId);
        Set<Student> uniqueStudents = new HashSet<>(allStudentsInClass);

        // Thêm sinh viên từ danh sách đăng ký
        for (Studentsubjectteacher enrollment : enrollments) {
            uniqueStudents.add(enrollment.getStudentId());
        }

        // Chuyển set thành list để xử lý
        allStudentsInClass = new ArrayList<>(uniqueStudents);

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
            Subjectteacher subjectTeacher = subjectTeacherService.getSubjectTeacherById(subjectTeacherId);
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

            Map<Student, List<Score>> scoresByStudent = new HashMap<>();

            // Nhóm điểm theo sinh viên (nếu có)
            for (Score score : scores) {
                Student student = score.getStudentID();
                if (!scoresByStudent.containsKey(student)) {
                    scoresByStudent.put(student, new ArrayList<>());
                }
                scoresByStudent.get(student).add(score);
            }

            // Đảm bảo tất cả sinh viên đều có trong map, kể cả sinh viên không có điểm nào
            for (Student student : allStudentsInClass) {
                if (!scoresByStudent.containsKey(student)) {
                    scoresByStudent.put(student, new ArrayList<>());
                }
            }

            // Thêm dữ liệu - Pass subjectTeacherId and schoolYearId to the method
            addTableData(table, scores, scoreTypes, normalFont, subjectTeacherId, schoolYearId, scoresByStudent, allStudentsInClass);

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new Exception("Không thể tạo file PDF: " + e.getMessage());
        }

        return out.toByteArray();
    }

    // Hàm hỗ trợ tìm loại điểm (không phân biệt hoa thường và khoảng trắng)
    private Typescore findScoreType(String headerName) {
        // Tìm chính xác trước
        Typescore typeScore = typeScoreService.getScoreTypeByName(headerName);
        if (typeScore != null) {
            return typeScore;
        }

        // Nếu không tìm thấy, thử so sánh không phân biệt hoa thường và khoảng trắng
        List<Typescore> allTypes = typeScoreService.getAllScoreTypes();
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
            Integer subjectTeacherId, Integer schoolYearId, Map<Student, List<Score>> scoresByStudent, List<Student> allStudents) {
        // Format số thập phân
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        int index = 1;

        // Sắp xếp sinh viên theo mã sinh viên để có thứ tự nhất quán
        List<Student> sortedStudents = new ArrayList<>(scoresByStudent.keySet());
        sortedStudents.sort((s1, s2) -> s1.getStudentCode().compareTo(s2.getStudentCode()));

        for (Student student : sortedStudents) {
            List<Score> studentScores = scoresByStudent.get(student);

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
                String displayValue = formatScore(scoreValue); // Sẽ trả về "-" nếu điểm là null

                // Nếu có điểm và có trọng số, tính vào điểm trung bình
                if (scoreValue != null) {
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

}
