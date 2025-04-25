package com.ntn.service;

import com.ntn.pojo.ListScoreDTO;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Typescore;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface ScoreService {

    Float getScoreWeight(Score score);

    Float getScoreWeight(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType);

    List<Score> getScores();

    Score getScoreById(int id);

    List<Score> getScoreByStudentCode(String studentCode);

    List<Score> getScoreByStudentFullName(String firstName, String lastName);

    List<Score> getSubjectScoresByStudentCode(String studentCode);

    List<Score> getSubjectScoresByStudentCodeAndSchoolYear(String studentCode, int schoolYearId);

    List<Score> getListScoreBySubjectTeacherIdAndSchoolYearId(int subjectTeacherID, int schoolYearId);

    List<Score> getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(int subjectTeacherID, int schoolYearId, int studentID);

    boolean saveListScoreByListScoreDTO(ListScoreDTO listScoreDTO);

    boolean importScoresFromCsv(MultipartFile file, int subjectTeacherId, int schoolYearId) throws Exception;

    byte[] exportScoresToCsv(int subjectTeacherId, int classId, int schoolYearId) throws Exception;

    byte[] exportScoresToPdf(int subjectTeacherId, int classId, int schoolYearId) throws Exception;

    boolean lockScores(int subjectTeacherId, int classId, int schoolYearId);

    boolean saveScoresDraft(List<Score> scores);

    boolean addScoreColumn(String columnName, int subjectTeacherId, int schoolYearId);

    List<Score> findByStudent(Student student);

    List<Score> getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(int subjectTeacherId, int classId, int schoolYearId);

    ListScoreDTO createListScoreDTO(List<Score> scores, int subjectTeacherId, int schoolYearId);

    Score getScoreByStudentSubjectSchoolYearAndType(
            int studentId, int subjectTeacherId, int schoolYearId, String scoreType);

    boolean saveScore(Score score);

    boolean updateScoreLockStatus(int scoreId, boolean locked);

    boolean saveScores(List<Score> scores);

    List<Score> getScoresByClassAndSubjectAndSchoolYear(
            int classId, int subjectTeacherId, int schoolYearId);

    boolean saveScoreWeights(Integer subjectTeacherId, Integer schoolYearId, Map<String, Double> weights);

    Map<String, Double> getScoreWeights(Integer subjectTeacherId, Integer schoolYearId);

}
