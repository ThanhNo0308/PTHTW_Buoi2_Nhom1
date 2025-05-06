/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.pojo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "classscoretypes")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Classscoretypes.findAll", query = "SELECT c FROM Classscoretypes c"),
    @NamedQuery(name = "Classscoretypes.findById", query = "SELECT c FROM Classscoretypes c WHERE c.id = :id"),
    @NamedQuery(name = "Classscoretypes.findByScoreType", query = "SELECT c FROM Classscoretypes c WHERE c.scoreType = :scoreType"),
    @NamedQuery(name = "Classscoretypes.findByWeight", query = "SELECT c FROM Classscoretypes c WHERE c.weight = :weight")})
public class Classscoretypes implements Serializable {

    @JoinColumn(name = "ScoreType", referencedColumnName = "ScoreType")
    @ManyToOne
    private Typescore scoreType;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Id")
    private Integer id;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "Weight")
    private Float weight;
    @JoinColumn(name = "ClassId", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Class classId;
    @JoinColumn(name = "SchoolYearId", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Schoolyear schoolYearId;
    @JoinColumn(name = "SubjectTeacherId", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private Subjectteacher subjectTeacherId;

    public Classscoretypes() {
    }

    public Classscoretypes(Integer id) {
        this.id = id;
    }

    public Classscoretypes(Integer id, Typescore scoreType) {
        this.id = id;
        this.scoreType = scoreType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Class getClassId() {
        return classId;
    }

    public void setClassId(Class classId) {
        this.classId = classId;
    }

    public Schoolyear getSchoolYearId() {
        return schoolYearId;
    }

    public void setSchoolYearId(Schoolyear schoolYearId) {
        this.schoolYearId = schoolYearId;
    }

    public Subjectteacher getSubjectTeacherId() {
        return subjectTeacherId;
    }

    public void setSubjectTeacherId(Subjectteacher subjectTeacherId) {
        this.subjectTeacherId = subjectTeacherId;
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
        if (!(object instanceof Classscoretypes)) {
            return false;
        }
        Classscoretypes other = (Classscoretypes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.Classscoretypes[ id=" + id + " ]";
    }

    public Typescore getScoreType() {
        return scoreType;
    }

    public void setScoreType(Typescore scoreType) {
        this.scoreType = scoreType;
    }
    
    public String getScoreTypeName() {
        return scoreType != null ? scoreType.getScoreType() : null;
    }
    
}
