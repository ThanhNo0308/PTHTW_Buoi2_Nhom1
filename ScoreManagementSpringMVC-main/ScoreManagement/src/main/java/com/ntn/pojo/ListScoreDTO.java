package com.ntn.pojo;

import java.util.List;

/**
 * Data Transfer Object cho danh sách điểm của một môn học
 */
public class ListScoreDTO {
    private Integer subjectTeacherId;
    private Integer schoolYearId;
    private String subjectName;
    private String teacherName;
    private String schoolYearName;
    private List<ScoreDTO> scores;
    private boolean locked;

    public ListScoreDTO() {
    }

    public ListScoreDTO(Integer subjectTeacherId, Integer schoolYearId, 
                        String subjectName, String teacherName, String schoolYearName, 
                        List<ScoreDTO> scores, boolean locked) {
        this.subjectTeacherId = subjectTeacherId;
        this.schoolYearId = schoolYearId;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
        this.schoolYearName = schoolYearName;
        this.scores = scores;
        this.locked = locked;
    }

    public Integer getSubjectTeacherId() {
        return subjectTeacherId;
    }

    public void setSubjectTeacherId(Integer subjectTeacherId) {
        this.subjectTeacherId = subjectTeacherId;
    }

    public Integer getSchoolYearId() {
        return schoolYearId;
    }

    public void setSchoolYearId(Integer schoolYearId) {
        this.schoolYearId = schoolYearId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getSchoolYearName() {
        return schoolYearName;
    }

    public void setSchoolYearName(String schoolYearName) {
        this.schoolYearName = schoolYearName;
    }

    public List<ScoreDTO> getScores() {
        return scores;
    }

    public void setScores(List<ScoreDTO> scores) {
        this.scores = scores;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}