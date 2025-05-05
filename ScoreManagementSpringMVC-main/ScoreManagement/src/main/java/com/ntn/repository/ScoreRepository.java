package com.ntn.repository;

import com.ntn.pojo.Score;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ScoreRepository {
    
    Float getScoreWeight(Score score);

    Float getScoreWeight(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType);

    Score getScoreById(int id);

    List<Score> getSubjectScoresByStudentCodeAndSchoolYear(String studentCode, int schoolYearId);

    List<Score> getListScoreBySubjectTeacherIdAndSchoolYearId(int subjectTeacherID, int schoolYearId);

    List<Score> getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(int subjectTeacherID, int schoolYearId, int studentID);

    boolean saveScores(List<Score> scores);

    List<Score> getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(int subjectTeacherId, int classId, int schoolYearId);

    boolean importScoresFromCsv(MultipartFile file, int subjectTeacherId, int classId, int schoolYearId) throws Exception;

    byte[] exportScoresToCsv(int subjectTeacherId, int classId, int schoolYearId) throws Exception;

    byte[] exportScoresToPdf(int subjectTeacherId, int classId, int schoolYearId) throws Exception;

    boolean saveScoresDraft(List<Score> scores);
    
    boolean deleteScore(Integer scoreId);
    
    boolean toggleScoreLock(int scoreId, boolean unlock);
}
