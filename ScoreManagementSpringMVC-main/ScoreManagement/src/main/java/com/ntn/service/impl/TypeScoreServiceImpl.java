/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Classscoretypes;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import com.ntn.repository.ClassScoreTypeRepository;
import com.ntn.repository.SchoolYearRepository;
import com.ntn.repository.TypeScoreRepository;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
import com.ntn.service.TypeScoreService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Admin
 */
@Service
public class TypeScoreServiceImpl implements TypeScoreService {

    @Autowired
    private TypeScoreRepository typeScoreRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ClassScoreTypeRepository classScoreTypeRepository;

    @Autowired
    private LocalSessionFactoryBean sessionFactory;

    @Override
    public Typescore getScoreTypeByName(String name) {
        return this.typeScoreRepository.getScoreTypeByName(name);
    }

    @Override
    @Transactional
    public boolean addScoreType(String typeName, int subjectTeacherId) {
        // Kiểm tra số lượng cột điểm hiện tại (không quá 5)
        int currentColumnCount = typeScoreRepository.countScoreTypesBySubjectTeacher(subjectTeacherId);

        if (currentColumnCount >= 5) {
            return false; // Đã đạt giới hạn 5 loại điểm
        }

        // Thêm loại điểm mới
        return typeScoreRepository.addScoreType(typeName, subjectTeacherId);
    }

    @Override
    public List<Typescore> getAllScoreTypes() {
        return typeScoreRepository.getAllScoreTypes();
    }

    @Override
    public boolean addScoreType(Typescore newType) {
        return this.typeScoreRepository.addScoreType(newType);
    }

    @Override
    public List<String> getScoreTypesByClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId) {
        List<String> scoreTypes = new ArrayList<>();

        // Luôn đảm bảo có các loại điểm mặc định
        scoreTypes.add("Giữa kỳ");
        scoreTypes.add("Cuối kỳ");

        // Thêm các loại điểm tùy chỉnh từ bảng class_score_types
        List<Classscoretypes> classScoreTypes = classScoreTypeRepository.getScoreTypesByClass(
                classId, subjectTeacherId, schoolYearId);

        for (Classscoretypes cst : classScoreTypes) {
            String scoreTypeName = cst.getScoreType().getScoreType();
            if (!scoreTypes.contains(scoreTypeName)) {
                scoreTypes.add(scoreTypeName);
            }
        }

        return scoreTypes;
    }

    @Override
    @Transactional
    public boolean addScoreTypeToClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType, Float weight) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();

            // Truy vấn trực tiếp các entity bằng các ID
            String hql = "FROM Classscoretypes c WHERE c.classId.id = :classId AND "
                    + "c.subjectTeacherId.id = :subjectTeacherId AND "
                    + "c.schoolYearId.id = :schoolYearId AND "
                    + "c.scoreType.scoreType = :scoreType";

            Query query = session.createQuery(hql);
            query.setParameter("classId", classId);
            query.setParameter("subjectTeacherId", subjectTeacherId);
            query.setParameter("schoolYearId", schoolYearId);
            query.setParameter("scoreType", scoreType);

            System.out.println("DEBUG: Searching for class score type: " + classId + ", " + subjectTeacherId + ", " + schoolYearId + ", " + scoreType);

            Classscoretypes existingType = null;
            try {
                existingType = (Classscoretypes) query.uniqueResult();
                System.out.println("DEBUG: Found existing type: " + (existingType != null));
            } catch (Exception ex) {
                System.err.println("Error finding class score type: " + ex.getMessage());
                ex.printStackTrace();
            }

            if (existingType != null) {
                // Cập nhật nếu đã tồn tại
                existingType.setWeight(weight);
                session.update(existingType);
                System.out.println("DEBUG: Updated existing class score type");
                return true;
            } else {
                // Tạo mới nếu chưa tồn tại
                Classscoretypes classScoreType = new Classscoretypes();

                // Truy vấn trực tiếp các đối tượng liên quan
                System.out.println("DEBUG: Creating new class score type");
                com.ntn.pojo.Class classEntity = (com.ntn.pojo.Class) session.get(com.ntn.pojo.Class.class, classId);
                Subjectteacher subjectTeacher = (Subjectteacher) session.get(Subjectteacher.class, subjectTeacherId);
                Schoolyear schoolYear = (Schoolyear) session.get(Schoolyear.class, schoolYearId);

                // Lấy hoặc tạo đối tượng Typescore tương ứng
                Typescore typeScoreObj = getScoreTypeByName(scoreType);
                System.out.println("DEBUG: Retrieved typescore: " + (typeScoreObj != null ? typeScoreObj.getScoreType() : "null"));

                if (typeScoreObj == null) {
                    typeScoreObj = new Typescore(scoreType);
                    boolean added = addScoreType(typeScoreObj);
                    System.out.println("DEBUG: Created new typescore, success: " + added);

                    // Refresh để lấy đối tượng đã lưu
                    typeScoreObj = getScoreTypeByName(scoreType);
                }

                if (classEntity == null || subjectTeacher == null || schoolYear == null || typeScoreObj == null) {
                    System.err.println("DEBUG: Missing required entities: "
                            + "class=" + (classEntity == null) + ", "
                            + "subject=" + (subjectTeacher == null) + ", "
                            + "year=" + (schoolYear == null) + ", "
                            + "type=" + (typeScoreObj == null));
                    return false;
                }

                classScoreType.setClassId(classEntity);
                classScoreType.setSubjectTeacherId(subjectTeacher);
                classScoreType.setSchoolYearId(schoolYear);
                classScoreType.setScoreType(typeScoreObj);
                classScoreType.setWeight(weight);

                try {
                    session.save(classScoreType);
                    System.out.println("DEBUG: Saved new class score type successfully");
                    return true;
                } catch (Exception ex) {
                    System.err.println("Error saving class score type: " + ex.getMessage());
                    ex.printStackTrace();
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("Overall error in addScoreTypeToClass: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeScoreTypeFromClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType) {
        try {
            // Lấy TypeScore object từ tên
            Typescore typeScoreObj = typeScoreRepository.getScoreTypeByName(scoreType);
            if (typeScoreObj == null) {
                return false;
            }

            // 1. Xóa các điểm liên quan trong bảng Score
            // Lấy danh sách học sinh trong lớp
            List<Student> students = studentService.getStudentByClassId(classId);
            boolean scoreDeleted = true;

            for (Student student : students) {
                // Xóa điểm của học sinh có loại điểm tương ứng
                List<Score> scores = scoreService.getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(
                        subjectTeacherId, schoolYearId, student.getId());

                for (Score score : scores) {
                    if (score.getScoreType() != null
                            && score.getScoreType().getScoreType().equals(scoreType)) {
                        // Xóa điểm này
                        boolean success = scoreService.deleteScore(score.getId());
                        if (!success) {
                            scoreDeleted = false;
                        }
                    }
                }
            }

            // 2. Xóa loại điểm trong bảng ClassScoreTypes
            List<Classscoretypes> classScoreTypes = classScoreTypeRepository.getScoreTypesByClass(
                    classId, subjectTeacherId, schoolYearId);

            for (Classscoretypes cst : classScoreTypes) {
                if (cst.getScoreType().getScoreType().equals(scoreType)) {
                    return classScoreTypeRepository.deleteScoreType(cst.getId());
                }
            }

            return scoreDeleted;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateScoreTypeWeights(Integer classId, Integer subjectTeacherId, Integer schoolYearId, Map<String, Double> weights) {
        try {
            // Chuyển đổi Map<String, Double> thành Map<String, Float>
            Map<String, Float> floatWeights = new HashMap<>();
            for (Map.Entry<String, Double> entry : weights.entrySet()) {
                floatWeights.put(entry.getKey(), entry.getValue().floatValue());
            }

            return classScoreTypeRepository.updateScoreTypeWeights(classId, subjectTeacherId, schoolYearId, floatWeights);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
