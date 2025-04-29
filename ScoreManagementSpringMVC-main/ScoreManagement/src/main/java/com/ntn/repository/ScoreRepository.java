package com.ntn.repository;

import com.ntn.pojo.ListScoreDTO;
import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.pojo.Typescore;
import java.util.List;

public interface ScoreRepository {
    
    List<Score> getScores();
    
    Score getScoreById(int id);
    
    List<Score> getScoreByStudentCode(String studentCode);
    
    List<Score> getScoreByStudentFullName(String firstName, String lastName);
    
    List<Score> findByStudent(Student student);
    
    List<Score> getSubjectScoresByStudentCode(String studentCode);
    
    List<Score> getSubjectScoresByStudentCodeAndSchoolYear(String studentCode, int schoolYearId);
   
    List<Score> getListScoreBySubjectTeacherIdAndSchoolYearId(int subjectTeacherID, int schoolYearId);
    
    List<Score> getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(int subjectTeacherID, int schoolYearId, int studentID);
    
    boolean saveListScoreByListScoreDTO(ListScoreDTO listScoreDTO);
    
    boolean saveScores(List<Score> scores);
    
    List<Score> getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(int subjectTeacherId, int classId, int schoolYearId);
    
     Score getScoreByStudentSubjectSchoolYearAndType(
            int studentId, int subjectTeacherId, int schoolYearId, String scoreType);
    
    boolean saveScore(Score score);
    
    boolean updateScoreLockStatus(int scoreId, boolean locked);
    
    boolean deleteScore(Integer scoreId);
}