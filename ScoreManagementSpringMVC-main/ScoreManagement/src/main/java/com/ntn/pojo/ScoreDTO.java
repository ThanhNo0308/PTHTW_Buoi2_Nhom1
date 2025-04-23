package com.ntn.pojo;

/**
 * Data Transfer Object cho điểm của một sinh viên
 */
public class ScoreDTO {
    private Integer id;
    private Integer studentId;
    private String studentName;
    private String studentCode;
    private Integer typeScoreId;
    private String typeScoreName;
    private Double score;
    private Integer status; // 0: Nháp, 1: Đã khóa

    public ScoreDTO() {
    }

    public ScoreDTO(Integer id, Integer studentId, String studentName, String studentCode, 
                    Integer typeScoreId, String typeScoreName, Double score, Integer status) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentCode = studentCode;
        this.typeScoreId = typeScoreId;
        this.typeScoreName = typeScoreName;
        this.score = score;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public Integer getTypeScoreId() {
        return typeScoreId;
    }

    public void setTypeScoreId(Integer typeScoreId) {
        this.typeScoreId = typeScoreId;
    }

    public String getTypeScoreName() {
        return typeScoreName;
    }

    public void setTypeScoreName(String typeScoreName) {
        this.typeScoreName = typeScoreName;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}