package com.ntn.repository.impl;

import com.ntn.pojo.Classscoretypes;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import com.ntn.repository.ClassScoreTypeRepository;
import com.ntn.service.ClassScoreTypeService;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
import com.ntn.service.TypeScoreService;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class ClassScoreTypeRepositoryImpl implements ClassScoreTypeRepository {

    @Autowired
    private LocalSessionFactoryBean sessionFactory;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ClassScoreTypeService classScoreTypeService;

    @Autowired
    private TypeScoreService typeScoreService;

    @Override
    public boolean saveScoreType(Classscoretypes classScoreType) {
        try {
            Session session = this.sessionFactory.getObject().getCurrentSession();
            session.saveOrUpdate(classScoreType);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteScoreType(Integer id) {
        try {
            Session session = this.sessionFactory.getObject().getCurrentSession();
            Classscoretypes classScoreType = session.get(Classscoretypes.class, id);
            session.delete(classScoreType);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Classscoretypes> getScoreTypesByClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId) {
        Session session = this.sessionFactory.getObject().getCurrentSession();

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Classscoretypes> query = builder.createQuery(Classscoretypes.class);
            Root<Classscoretypes> root = query.from(Classscoretypes.class);

            // Tạo các điều kiện
            Predicate classIdPredicate = builder.equal(root.get("classId").get("id"), classId);
            Predicate subjectTeacherIdPredicate = builder.equal(root.get("subjectTeacherId").get("id"), subjectTeacherId);
            Predicate schoolYearIdPredicate = builder.equal(root.get("schoolYearId").get("id"), schoolYearId);

            // Kết hợp các điều kiện
            query.where(builder.and(classIdPredicate, subjectTeacherIdPredicate, schoolYearIdPredicate));

            List<Classscoretypes> result = session.createQuery(query).getResultList();

            return result;
        } catch (Exception e) {
            System.err.println("Error querying class score types: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updateScoreTypeWeights(Integer classId, Integer subjectTeacherId, Integer schoolYearId, Map<String, Float> weights) {
        try {
            Session session = this.sessionFactory.getObject().getCurrentSession();

            // 1. Lấy các đối tượng entity từ ID
            com.ntn.pojo.Class classEntity = (com.ntn.pojo.Class) session.get(com.ntn.pojo.Class.class, classId);
            Subjectteacher subjectTeacher = (Subjectteacher) session.get(Subjectteacher.class, subjectTeacherId);
            Schoolyear schoolYear = (Schoolyear) session.get(Schoolyear.class, schoolYearId);

            if (classEntity == null || subjectTeacher == null || schoolYear == null) {
                return false;
            }

            // 2. Lấy tất cả loại điểm hiện có cho lớp/môn học này
            List<Classscoretypes> existingTypes = getScoreTypesByClass(classId, subjectTeacherId, schoolYearId);
            Map<String, Classscoretypes> typeMap = new HashMap<>();

            // Tạo map để tra cứu nhanh
            for (Classscoretypes type : existingTypes) {
                typeMap.put(type.getScoreType().getScoreType(), type); // Sửa từ getScoreTypeName() thành getScoreType().getScoreType()
            }

            // 3. Cập nhật các loại điểm hiện có hoặc tạo mới loại điểm
            for (Map.Entry<String, Float> entry : weights.entrySet()) {
                String scoreTypeStr = entry.getKey();
                Float weight = entry.getValue();

                if (typeMap.containsKey(scoreTypeStr)) {
                    // Cập nhật loại điểm đã tồn tại
                    Classscoretypes existingType = typeMap.get(scoreTypeStr);
                    existingType.setWeight(weight);
                    session.update(existingType);
                } else {
                    // Tạo mới loại điểm chưa tồn tại
                    Classscoretypes newType = new Classscoretypes();
                    newType.setClassId(classEntity);
                    newType.setSubjectTeacherId(subjectTeacher);
                    newType.setSchoolYearId(schoolYear);

                    // Lấy hoặc tạo đối tượng Typescore tương ứng
                    Typescore typeScore = (Typescore) session.createQuery("FROM Typescore WHERE scoreType = :type")
                            .setParameter("type", scoreTypeStr)
                            .uniqueResult();

                    if (typeScore == null) {
                        typeScore = new Typescore(scoreTypeStr);
                        session.save(typeScore);
                    }

                    newType.setScoreType(typeScore); // Sửa: Đặt đối tượng Typescore thay vì String
                    newType.setWeight(weight);
                    session.save(newType);
                }
            }

            // 4. Xóa các loại điểm không còn trong cấu hình mới
            for (Classscoretypes type : existingTypes) {
                if (!weights.containsKey(type.getScoreType().getScoreType())) { // Sửa từ getScoreType() thành getScoreType().getScoreType()
                    session.delete(type);
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Float getWeightForScoreType(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType) {
        Session session = this.sessionFactory.getObject().getCurrentSession();

        try {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Classscoretypes> query = builder.createQuery(Classscoretypes.class);
            Root<Classscoretypes> root = query.from(Classscoretypes.class);

            // Tạo các điều kiện
            Predicate classIdPredicate = builder.equal(root.get("classId").get("id"), classId);
            Predicate subjectTeacherIdPredicate = builder.equal(root.get("subjectTeacherId").get("id"), subjectTeacherId);
            Predicate schoolYearIdPredicate = builder.equal(root.get("schoolYearId").get("id"), schoolYearId);
            Predicate scoreTypePredicate = builder.equal(root.get("scoreType").get("scoreType"), scoreType);

            // Kết hợp các điều kiện
            query.where(builder.and(classIdPredicate, subjectTeacherIdPredicate, schoolYearIdPredicate, scoreTypePredicate));

            Classscoretypes classScoreType = session.createQuery(query).getSingleResult();
            return classScoreType.getWeight();
        } catch (jakarta.persistence.NoResultException e) {
            // Trả về giá trị mặc định nếu không có cấu hình
            if ("Giữa kỳ".equals(scoreType)) {
                return 0.4f;
            } else if ("Cuối kỳ".equals(scoreType)) {
                return 0.6f;
            } else {
                return 0.1f;
            }
        }
    }

    @Override
    public boolean addScoreTypeToClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType, Float weight) {
        try {
            Session session = sessionFactory.getObject().getCurrentSession();

            // Sử dụng CriteriaBuilder để kiểm tra tồn tại
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Classscoretypes> query = builder.createQuery(Classscoretypes.class);
            Root<Classscoretypes> root = query.from(Classscoretypes.class);

            // Tạo các điều kiện
            Predicate classIdPredicate = builder.equal(root.get("classId").get("id"), classId);
            Predicate subjectTeacherIdPredicate = builder.equal(root.get("subjectTeacherId").get("id"), subjectTeacherId);
            Predicate schoolYearIdPredicate = builder.equal(root.get("schoolYearId").get("id"), schoolYearId);
            Predicate scoreTypePredicate = builder.equal(root.get("scoreType").get("scoreType"), scoreType);

            // Kết hợp các điều kiện
            query.where(builder.and(classIdPredicate, subjectTeacherIdPredicate, schoolYearIdPredicate, scoreTypePredicate));

            Classscoretypes existingType = null;
            try {
                existingType = session.createQuery(query).uniqueResult();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (existingType != null) {
                // Cập nhật nếu đã tồn tại
                existingType.setWeight(weight);
                session.update(existingType);
                return true;
            } else {
                // Tạo mới nếu chưa tồn tại
                Classscoretypes classScoreType = new Classscoretypes();

                // Truy vấn trực tiếp các đối tượng liên quan
                com.ntn.pojo.Class classEntity = (com.ntn.pojo.Class) session.get(com.ntn.pojo.Class.class, classId);
                Subjectteacher subjectTeacher = (Subjectteacher) session.get(Subjectteacher.class, subjectTeacherId);
                Schoolyear schoolYear = (Schoolyear) session.get(Schoolyear.class, schoolYearId);

                // Lấy hoặc tạo đối tượng Typescore tương ứng
                Typescore typeScoreObj = typeScoreService.getScoreTypeByName(scoreType);

                if (typeScoreObj == null) {
                    typeScoreObj = new Typescore(scoreType);
                    boolean added = typeScoreService.addScoreType(typeScoreObj);

                    // Refresh để lấy đối tượng đã lưu
                    typeScoreObj = typeScoreService.getScoreTypeByName(scoreType);
                }

                if (classEntity == null || subjectTeacher == null || schoolYear == null || typeScoreObj == null) {
                    return false;
                }

                classScoreType.setClassId(classEntity);
                classScoreType.setSubjectTeacherId(subjectTeacher);
                classScoreType.setSchoolYearId(schoolYear);
                classScoreType.setScoreType(typeScoreObj);
                classScoreType.setWeight(weight);

                try {
                    session.save(classScoreType);
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
            Typescore typeScoreObj = typeScoreService.getScoreTypeByName(scoreType);
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
            List<Classscoretypes> classScoreTypes = classScoreTypeService.getScoreTypesByClass(
                    classId, subjectTeacherId, schoolYearId);

            for (Classscoretypes cst : classScoreTypes) {
                if (cst.getScoreType().getScoreType().equals(scoreType)) {
                    return classScoreTypeService.deleteScoreType(cst.getId());
                }
            }

            return scoreDeleted;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
