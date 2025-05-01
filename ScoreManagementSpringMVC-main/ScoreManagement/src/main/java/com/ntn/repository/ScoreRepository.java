package com.ntn.repository;

import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface ScoreRepository {

    Float getScoreWeight(Score score);

    Float getScoreWeight(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType);

    Map<String, Double> getScoreWeights(Integer subjectTeacherId, Integer schoolYearId);

    List<Score> getScores();

    Score getScoreById(int id);

    List<Score> getScoreByStudentCode(String studentCode);

    List<Score> getScoreByStudentFullName(String firstName, String lastName);

    List<Score> findByStudent(Student student);

    List<Score> getSubjectScoresByStudentCode(String studentCode);

    List<Score> getSubjectScoresByStudentCodeAndSchoolYear(String studentCode, int schoolYearId);

    List<Score> getListScoreBySubjectTeacherIdAndSchoolYearId(int subjectTeacherID, int schoolYearId);

    List<Score> getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(int subjectTeacherID, int schoolYearId, int studentID);

    boolean saveScores(List<Score> scores);

    List<Score> getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(int subjectTeacherId, int classId, int schoolYearId);

    Score getScoreByStudentSubjectSchoolYearAndType(
            int studentId, int subjectTeacherId, int schoolYearId, String scoreType);

    boolean saveScore(Score score);

    boolean updateScoreLockStatus(int scoreId, boolean locked);

    boolean deleteScore(Integer scoreId);

    boolean importScoresFromCsv(MultipartFile file, int subjectTeacherId, int classId, int schoolYearId) throws Exception;

    byte[] exportScoresToCsv(int subjectTeacherId, int classId, int schoolYearId) throws Exception;

    byte[] exportScoresToPdf(int subjectTeacherId, int classId, int schoolYearId) throws Exception;

    boolean lockScores(int subjectTeacherId, int classId, int schoolYearId);

    boolean saveScoresDraft(List<Score> scores);

    boolean addScoreColumn(String columnName, int subjectTeacherId, int schoolYearId);

    boolean saveScoreWeights(Integer subjectTeacherId, Integer schoolYearId, Map<String, Double> weights);

}
