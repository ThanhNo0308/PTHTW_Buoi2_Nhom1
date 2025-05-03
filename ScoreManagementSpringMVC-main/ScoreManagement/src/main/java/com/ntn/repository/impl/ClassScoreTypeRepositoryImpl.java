package com.ntn.repository.impl;

import com.ntn.pojo.Classscoretypes;
import com.ntn.pojo.Schoolyear;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import com.ntn.repository.ClassScoreTypeRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class ClassScoreTypeRepositoryImpl implements ClassScoreTypeRepository {

    @Autowired
    private LocalSessionFactoryBean sessionFactory;

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
            String hql = "FROM Classscoretypes c WHERE "
                    + "c.classId.id = :classId AND "
                    + "c.subjectTeacherId.id = :subjectTeacherId AND "
                    + "c.schoolYearId.id = :schoolYearId";

            Query query = session.createQuery(hql);
            query.setParameter("classId", classId);
            query.setParameter("subjectTeacherId", subjectTeacherId);
            query.setParameter("schoolYearId", schoolYearId);

            List<Classscoretypes> result = query.getResultList();
            System.out.println("Found " + result.size() + " score type configurations");

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

        String hql = "FROM Classscoretypes c WHERE c.classId.id = :classId AND "
                + "c.subjectTeacherId.id = :subjectTeacherId AND "
                + "c.schoolYearId.id = :schoolYearId AND "
                + "c.scoreType.scoreType = :scoreType";

        Query query = session.createQuery(hql);
        query.setParameter("classId", classId);
        query.setParameter("subjectTeacherId", subjectTeacherId);
        query.setParameter("schoolYearId", schoolYearId);
        query.setParameter("scoreType", scoreType);

        try {
            Classscoretypes classScoreType = (Classscoretypes) query.getSingleResult();
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
}
