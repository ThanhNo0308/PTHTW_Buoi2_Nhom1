/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.pojo;

import java.io.Serializable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "score")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Score.findAll", query = "SELECT s FROM Score s"),
    @NamedQuery(name = "Score.findById", query = "SELECT s FROM Score s WHERE s.id = :id"),
    @NamedQuery(name = "Score.findByScoreValue", query = "SELECT s FROM Score s WHERE s.scoreValue = :scoreValue"),
    @NamedQuery(name = "Score.findByIsDraft", query = "SELECT s FROM Score s WHERE s.isDraft = :isDraft"),
    @NamedQuery(name = "Score.findByIsLocked", query = "SELECT s FROM Score s WHERE s.isLocked = :isLocked")})
public class Score implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "ScoreValue")
    private Float scoreValue;
    @Column(name = "IsDraft")
    private Boolean isDraft;
    @Column(name = "IsLocked")
    private Boolean isLocked;
    @JoinColumn(name = "SchoolYearId", referencedColumnName = "Id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Schoolyear schoolYearId;
    @JoinColumn(name = "StudentID", referencedColumnName = "Id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Student studentID;
    @JoinColumn(name = "SubjectTeacherID", referencedColumnName = "Id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Subjectteacher subjectTeacherID;
    @JoinColumn(name = "ScoreType", referencedColumnName = "ScoreType")
    @ManyToOne(fetch = FetchType.EAGER)
    private Typescore scoreType;

    @Transient
    private Float averageScore;

    public Score() {
        this.isDraft = false;
        this.isLocked = false;
    }

    public Score(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(Float scoreValue) {
        this.scoreValue = scoreValue;
    }

    public Boolean getIsDraft() {
        return isDraft;
    }

    public void setIsDraft(Boolean isDraft) {
        this.isDraft = isDraft;
    }

    public Boolean isLocked() {
        return this.isLocked;
    }

    public Boolean getIsLocked() {
        return isLocked != null ? isLocked : false;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    public Schoolyear getSchoolYearId() {
        return schoolYearId;
    }

    public void setSchoolYearId(Schoolyear schoolYearId) {
        this.schoolYearId = schoolYearId;
    }

    public Student getStudentID() {
        return studentID;
    }

    public void setStudentID(Student studentID) {
        this.studentID = studentID;
    }

    public Subjectteacher getSubjectTeacherID() {
        return subjectTeacherID;
    }

    public void setSubjectTeacherID(Subjectteacher subjectTeacherID) {
        this.subjectTeacherID = subjectTeacherID;
    }

    public Typescore getScoreType() {
        return scoreType;
    }

    public void setScoreType(Typescore scoreType) {
        this.scoreType = scoreType;
    }

    public Float getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Float averageScore) {
        this.averageScore = averageScore;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Score)) {
            return false;
        }
        Score other = (Score) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("Score[id=%s, value=%s]",
                (id != null ? id : "null"),
                (scoreValue != null ? scoreValue : "chưa có điểm"));
    }
    
    public Float getWeightFromClassScoreType() {
        if (this.getScoreType() == null || this.getStudentID() == null || 
            this.getSubjectTeacherID() == null || this.getSchoolYearId() == null ||
            this.getStudentID().getClassId() == null)
            return null;
            
        return null;
    }

}
