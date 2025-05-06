/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Set;

@Entity
@Table(name = "subjectteacher")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Subjectteacher.findAll", query = "SELECT s FROM Subjectteacher s"),
    @NamedQuery(name = "Subjectteacher.findById", query = "SELECT s FROM Subjectteacher s WHERE s.id = :id")})
public class Subjectteacher implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "subjectTeacherId")
    private Set<Classscoretypes> classscoretypesSet;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;
    @OneToMany(mappedBy = "subjectTeacherId")
    @JsonIgnore
    private List<Forum> forumList;
    @OneToMany(mappedBy = "subjectTeacherID")
    @JsonIgnore
    private List<Score> scoreList;
    @OneToMany(mappedBy = "subjectTeacherId")
    @JsonIgnore
    private List<Studentsubjectteacher> studentsubjectteacherList;
    @JoinColumn(name = "SubjectId", referencedColumnName = "Id")
    @ManyToOne
    private Subject subjectId;
    @JoinColumn(name = "TeacherId", referencedColumnName = "Id")
    @ManyToOne
    private Teacher teacherId;
    @JoinColumn(name = "SchoolYearId", referencedColumnName = "Id")
    @ManyToOne
    private Schoolyear schoolYearId;
    @JoinColumn(name = "ClassId", referencedColumnName = "Id")
    @ManyToOne
    private Class classId;

    public Subjectteacher() {
    }

    public Subjectteacher(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @XmlTransient
    @JsonIgnore
    public List<Forum> getForumList() {
        return forumList;
    }

    public void setForumList(List<Forum> forumList) {
        this.forumList = forumList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Score> getScoreList() {
        return scoreList;
    }

    public void setScoreList(List<Score> scoreList) {
        this.scoreList = scoreList;
    }

    @XmlTransient
    @JsonIgnore
    public List<Studentsubjectteacher> getStudentsubjectteacherList() {
        return studentsubjectteacherList;
    }

    public void setStudentsubjectteacherList(List<Studentsubjectteacher> studentsubjectteacherList) {
        this.studentsubjectteacherList = studentsubjectteacherList;
    }

    public Subject getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Subject subjectId) {
        this.subjectId = subjectId;
    }

    public Teacher getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Teacher teacherId) {
        this.teacherId = teacherId;
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
        if (!(object instanceof Subjectteacher)) {
            return false;
        }
        Subjectteacher other = (Subjectteacher) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return teacherId + " - " + subjectId;
    }

    @XmlTransient
    @JsonIgnore
    public Set<Classscoretypes> getClassscoretypesSet() {
        return classscoretypesSet;
    }

    public void setClassscoretypesSet(Set<Classscoretypes> classscoretypesSet) {
        this.classscoretypesSet = classscoretypesSet;
    }

    public Schoolyear getSchoolYearId() {
        return schoolYearId;
    }

    public void setSchoolYearId(Schoolyear schoolYearId) {
        this.schoolYearId = schoolYearId;
    }
    
     public Class getClassId() {
        return classId;
    }

    public void setClassId(Class classId) {
        this.classId = classId;
    }

}
