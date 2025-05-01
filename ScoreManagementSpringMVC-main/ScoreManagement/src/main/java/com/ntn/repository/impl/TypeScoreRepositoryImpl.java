/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.repository.impl;

import com.ntn.pojo.Classscoretypes;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import com.ntn.repository.ClassScoreTypeRepository;
import com.ntn.repository.TypeScoreRepository;
import com.ntn.service.ScoreService;
import com.ntn.service.StudentService;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Admin
 */
@Repository
@Transactional
public class TypeScoreRepositoryImpl implements TypeScoreRepository {

    @Autowired
    private LocalSessionFactoryBean factory;
    
     @Autowired
    private StudentService studentService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private ClassScoreTypeRepository classScoreTypeRepository;
    
    @Autowired
    private TypeScoreRepository typeScoreRepository;

    @Override
    public Typescore getScoreTypeByName(String name) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Typescore> query = builder.createQuery(Typescore.class);
        Root<Typescore> root = query.from(Typescore.class);

        query.where(builder.equal(root.get("scoreType"), name));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            // Nếu không tìm thấy, tạo loại điểm mới
            Typescore newType = new Typescore(name);
            session.save(newType);
            return newType;
        }
    }

    @Override
    public int countScoreTypesBySubjectTeacher(int subjectTeacherId) {
        Session session = this.factory.getObject().getCurrentSession();
        String hql = "SELECT COUNT(DISTINCT ts.id) FROM Score s "
                + "JOIN s.typeScoreID ts "
                + "WHERE s.subjectTeacherID.id = :subjectTeacherId";

        Query<Long> q = session.createQuery(hql, Long.class);
        q.setParameter("subjectTeacherId", subjectTeacherId);

        return q.getSingleResult().intValue();
    }

    @Override
    @Transactional
    public boolean addScoreType(String typeName, int subjectTeacherId) {
        Session session = this.factory.getObject().getCurrentSession();

        try {
            // Kiểm tra xem loại điểm đã tồn tại chưa
            String checkHql = "FROM Typescore ts WHERE ts.scoreType = :typeName";
            Query<Typescore> checkQuery = session.createQuery(checkHql, Typescore.class);
            checkQuery.setParameter("typeName", typeName);
            List<Typescore> existingTypes = checkQuery.getResultList();

            Typescore typeScore;
            if (existingTypes.isEmpty()) {
                // Tạo loại điểm mới nếu chưa tồn tại
                typeScore = new Typescore();
                typeScore.setScoreType(typeName);
                session.save(typeScore);
            } else {
                typeScore = existingTypes.get(0);
            }

            // Lấy danh sách sinh viên thuộc môn học của giảng viên
            String studentHql = "SELECT DISTINCT s.studentID FROM Score s "
                    + "WHERE s.subjectTeacherID.id = :subjectTeacherId";
            Query<Student> studentQuery = session.createQuery(studentHql, Student.class);
            studentQuery.setParameter("subjectTeacherId", subjectTeacherId);
            List<Student> students = studentQuery.getResultList();

            // Lấy thông tin Subjectteacher
            Subjectteacher st = session.get(Subjectteacher.class, subjectTeacherId);

            // Tạo điểm mới cho mỗi sinh viên với loại điểm mới
            for (Student student : students) {
                Score newScore = new Score();
                newScore.setStudentID(student);
                newScore.setSubjectTeacherID(st);
                newScore.setScoreType(typeScore); // Sửa từ setTypeScoreID thành setScoreType
                newScore.setScoreValue(0.0f); // Sửa từ setScore thành setScoreValue
                newScore.setIsDraft(true); // Sửa từ setStatus(0) thành setIsDraft(true)
                newScore.setIsLocked(false); // Sửa thêm isLocked = false

                session.save(newScore);
            }

            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public Student getStudentByCode(String studentCode) {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Student> query = builder.createQuery(Student.class);
        Root<Student> root = query.from(Student.class);

        query.where(builder.equal(root.get("studentCode"), studentCode));

        try {
            return session.createQuery(query).getSingleResult();
        } catch (Exception e) {
            return null; // Trả về null nếu không tìm thấy sinh viên
        }
    }

    @Override
    public List<Typescore> getAllScoreTypes() {
        Session session = this.factory.getObject().getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Typescore> query = builder.createQuery(Typescore.class);
        Root<Typescore> root = query.from(Typescore.class);
        query.select(root);
        return session.createQuery(query).getResultList();
    }

    @Override
    public boolean addScoreType(Typescore newType) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            session.save(newType);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean addScoreTypeToClass(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType, Float weight) {
        try {
            Session session = factory.getObject().getCurrentSession();

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
