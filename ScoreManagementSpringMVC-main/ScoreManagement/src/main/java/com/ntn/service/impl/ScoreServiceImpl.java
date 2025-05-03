package com.ntn.service.impl;

import com.ntn.pojo.Score;
import com.ntn.pojo.Student;
import com.ntn.repository.ClassScoreTypeRepository;
import com.ntn.repository.ScoreRepository;
import com.ntn.service.ScoreService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Triển khai service điểm
 */
@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ScoreRepository scoreRepo;

    @Autowired
    private ClassScoreTypeRepository classScoreTypeRepository;

    @Override
    public Float getScoreWeight(Score score) {
      return this.scoreRepo.getScoreWeight(score);
    }

    @Override
    public Float getScoreWeight(Integer classId, Integer subjectTeacherId, Integer schoolYearId, String scoreType) {
        return classScoreTypeRepository.getWeightForScoreType(classId, subjectTeacherId, schoolYearId, scoreType);
    }

    @Override
    public List<Score> getScores() {
        return this.scoreRepo.getScores();
    }

    @Override
    public Score getScoreById(int id) {
        return this.scoreRepo.getScoreById(id);
    }

    @Override
    public List<Score> getScoreByStudentCode(String studentCode) {
        return this.scoreRepo.getScoreByStudentCode(studentCode);
    }

    @Override
    public List<Score> getScoreByStudentFullName(String firstName, String lastName) {
        return this.scoreRepo.getScoreByStudentFullName(firstName, lastName);
    }

    @Override
    public List<Score> getSubjectScoresByStudentCode(String studentCode) {
        return this.scoreRepo.getSubjectScoresByStudentCode(studentCode);
    }

    @Override
    public List<Score> getSubjectScoresByStudentCodeAndSchoolYear(String studentCode, int schoolYearId) {
        return this.scoreRepo.getSubjectScoresByStudentCodeAndSchoolYear(studentCode, schoolYearId);
    }

    @Override
    public List<Score> getListScoreBySubjectTeacherIdAndSchoolYearId(int subjectTeacherID, int schoolYearId) {
        return this.scoreRepo.getListScoreBySubjectTeacherIdAndSchoolYearId(subjectTeacherID, schoolYearId);
    }

    @Override
    public List<Score> getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(int subjectTeacherID, int schoolYearId, int studentID) {
        return this.scoreRepo.getListScoreBySubjectTeacherIdAndSchoolYearIdAndStudentId(subjectTeacherID, schoolYearId, studentID);
    }

    @Override
    public boolean importScoresFromCsv(MultipartFile file, int subjectTeacherId, int classId, int schoolYearId) throws Exception {
        return this.scoreRepo.importScoresFromCsv(file, subjectTeacherId, classId, schoolYearId);
    }

    @Override
    public byte[] exportScoresToCsv(int subjectTeacherId, int classId, int schoolYearId) throws Exception {
        return this.scoreRepo.exportScoresToCsv(subjectTeacherId, classId, schoolYearId);
    }

    @Override
    public byte[] exportScoresToPdf(int subjectTeacherId, int classId, int schoolYearId) throws Exception {
        return this.scoreRepo.exportScoresToPdf(subjectTeacherId, classId, schoolYearId);
    }

    @Override
    public boolean saveScoresDraft(List<Score> scores) {
        return this.scoreRepo.saveScoresDraft(scores);
    }

    @Override
    public boolean addScoreColumn(String columnName, int subjectTeacherId, int schoolYearId) {
       return this.scoreRepo.addScoreColumn(columnName, subjectTeacherId, schoolYearId);
    }

    @Override
    public List<Score> findByStudent(Student student) {
        return this.scoreRepo.findByStudent(student);
    }

    @Override
    public List<Score> getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(int subjectTeacherId, int classId, int schoolYearId) {
        return this.scoreRepo.getScoresBySubjectTeacherIdAndClassIdAndSchoolYearId(subjectTeacherId, classId, schoolYearId);
    }

    @Override
    public Score getScoreByStudentSubjectSchoolYearAndType(int studentId, int subjectTeacherId, int schoolYearId, String scoreType) {
        return this.scoreRepo.getScoreByStudentSubjectSchoolYearAndType(studentId, subjectTeacherId, schoolYearId, scoreType);
    }

    @Override
    public boolean saveScore(Score score) {
        return this.scoreRepo.saveScore(score);
    }

    @Override
    public boolean saveScores(List<Score> scores) {
        return this.scoreRepo.saveScores(scores);
    }

    @Override
    public List<Score> getScoresByClassAndSubjectAndSchoolYear(int classId, int subjectTeacherId, int schoolYearId) {
        return null;
    }

    @Override
    public boolean saveScoreWeights(Integer subjectTeacherId, Integer schoolYearId, Map<String, Double> weights) {
       return this.scoreRepo.saveScoreWeights(subjectTeacherId, schoolYearId, weights);
    }

    @Override
    public Map<String, Double> getScoreWeights(Integer subjectTeacherId, Integer schoolYearId) {
        return this.scoreRepo.getScoreWeights(subjectTeacherId, schoolYearId);
    }

    @Override
    public boolean deleteScore(Integer scoreId) {
        return this.scoreRepo.deleteScore(scoreId);
    }

}
