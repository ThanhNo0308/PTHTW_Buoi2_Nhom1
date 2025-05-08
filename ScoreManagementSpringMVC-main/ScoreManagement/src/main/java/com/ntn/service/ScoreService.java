package com.ntn.service;

import com.ntn.pojo.Score;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface ScoreService {

    Score getScoreById(int id);

    Float getScoreWeight(Score score);

    Float getScoreWeight(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType);

    List<Score> getSubjectScoresByStudentCodeAndSchoolYear(String studentCode, int schoolYearId);

    List<Score> getListScoreBySubjectTeacherIdAndSchoolYearId(int subjectTeacherID, int schoolYearId);

    List<Score> getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(int subjectTeacherId, int classId, int schoolYearId);

    List<Score> getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(int subjectTeacherID, int schoolYearId, int studentID);

    Map<String, Object> importScoresFromCsv(MultipartFile file, int subjectTeacherId, int classId, int schoolYearId) throws Exception;

    boolean saveScoresDraft(List<Score> scores);

    boolean saveScores(List<Score> scores);

    boolean deleteScore(Integer scoreId);

    boolean toggleScoreLock(int scoreId, boolean unlock);

    byte[] exportScoresToCsv(int subjectTeacherId, int classId, int schoolYearId) throws Exception;

    byte[] exportScoresToPdf(int subjectTeacherId, int classId, int schoolYearId) throws Exception;
}
