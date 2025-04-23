package com.ntn.repository.impl;

import com.ntn.pojo.ListScoreDTO;
import com.ntn.pojo.ScoreDTO;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Subjectteacher;
import com.ntn.pojo.Typescore;
import com.ntn.repository.ScoreRepository;
import java.util.List;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ScoreRepositoryImpl implements ScoreRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

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
    @Transactional
    public boolean saveListScoreByListScoreDTO(ListScoreDTO listScoreDTO) {
        Session session = this.factory.getObject().getCurrentSession();

        try {
            // Lấy Subjectteacher
            Subjectteacher st = session.get(Subjectteacher.class, listScoreDTO.getSubjectTeacherId());

            // Lấy các điểm hiện có
            List<Score> currentScores = getListScoreBySubjectTeacherIdAndSchoolYearId(
                    listScoreDTO.getSubjectTeacherId(), listScoreDTO.getSchoolYearId());

            // Xóa các điểm hiện có
            for (Score score : currentScores) {
                session.delete(score);
            }

            // Thêm các điểm mới
            for (ScoreDTO scoreDTO : listScoreDTO.getScores()) {
                Score score = new Score();
                score.setSubjectTeacherID(st);

                // Lấy Student
                Student student = session.get(Student.class, scoreDTO.getStudentId());
                score.setStudentID(student);

                // Lấy Typescore
                Typescore type = session.get(Typescore.class, scoreDTO.getTypeScoreId());
                score.setScoreType(type); // Sửa từ setTypeScoreID thành setScoreType

                // Điều chỉnh kiểu dữ liệu từ Double sang Float
                score.setScoreValue(scoreDTO.getScore().floatValue()); // Sửa từ setScore thành setScoreValue

                // Thiết lập trạng thái dựa trên isLocked
                score.setIsDraft(!listScoreDTO.isLocked()); // Sửa từ setStatus thành setIsDraft
                score.setIsLocked(listScoreDTO.isLocked()); // Sửa từ setStatus thành setIsLocked

                session.save(score);
            }

            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean saveScores(List<Score> scores) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            for (Score score : scores) {
                session.saveOrUpdate(score);
                session.flush(); 
            }
            return true;
        } catch (HibernateException ex) {
            ex.printStackTrace();
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
    public Typescore getScoreTypeByName(String name) {
        Session session = this.factory.getObject().getCurrentSession();
        try {
            String hql = "FROM Typescore t WHERE t.scoreType = :name";
            Query<Typescore> query = session.createQuery(hql, Typescore.class);
            query.setParameter("name", name);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    // Triển khai trong ScoreRepositoryImpl
    @Override
    @Transactional
    public boolean updateScoreLockStatus(int scoreId, boolean locked) {
        try {
            System.out.println("Updating score lock status - ID: " + scoreId + ", locked: " + locked);
            Session session = this.factory.getObject().getCurrentSession();
            Score score = session.get(Score.class, scoreId);

            if (score != null) {
                score.setIsLocked(locked);
                session.update(score);
                System.out.println("Score lock status updated successfully");
                return true;
            } else {
                System.out.println("Score not found with ID: " + scoreId);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error updating score lock status: " + e.getMessage());
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

        try {
            return query.getSingleResult();
        } catch (jakarta.persistence.NoResultException ex) {
            return null;
        }
    }
}
