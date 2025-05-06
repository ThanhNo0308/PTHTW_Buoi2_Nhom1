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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "schoolyear")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Schoolyear.findAll", query = "SELECT s FROM Schoolyear s"),
    @NamedQuery(name = "Schoolyear.findById", query = "SELECT s FROM Schoolyear s WHERE s.id = :id"),
    @NamedQuery(name = "Schoolyear.findByNameYear", query = "SELECT s FROM Schoolyear s WHERE s.nameYear = :nameYear"),
    @NamedQuery(name = "Schoolyear.findByYearStart", query = "SELECT s FROM Schoolyear s WHERE s.yearStart = :yearStart"),
    @NamedQuery(name = "Schoolyear.findByYearEnd", query = "SELECT s FROM Schoolyear s WHERE s.yearEnd = :yearEnd"),
    @NamedQuery(name = "Schoolyear.findBySemesterName", query = "SELECT s FROM Schoolyear s WHERE s.semesterName = :semesterName")})
public class Schoolyear implements Serializable {

    @Size(max = 50)
    @Column(name = "NameYear")
    private String nameYear;
    @Column(name = "YearStart")
    @Temporal(TemporalType.DATE)
    private Date yearStart;
    @Column(name = "YearEnd")
    @Temporal(TemporalType.DATE)
    private Date yearEnd;
    @Size(max = 55)
    @Column(name = "SemesterName")
    private String semesterName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "schoolYearId")
    private Set<Classscoretypes> classscoretypesSet;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;
    @JsonIgnore
    @OneToMany(mappedBy = "schoolYearId")
    private List<Score> scoreList;

    public Schoolyear() {
    }

    public Schoolyear(Integer id) {
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
    public List<Score> getScoreList() {
        return scoreList;
    }

    public void setScoreList(List<Score> scoreList) {
        this.scoreList = scoreList;
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
        if (!(object instanceof Schoolyear)) {
            return false;
        }
        Schoolyear other = (Schoolyear) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return nameYear + " - " + semesterName;
    }
    @XmlTransient
    @JsonIgnore
    public Set<Classscoretypes> getClassscoretypesSet() {
        return classscoretypesSet;
    }
    public void setClassscoretypesSet(Set<Classscoretypes> classscoretypesSet) {
        this.classscoretypesSet = classscoretypesSet;
    }

    public String getNameYear() {
        return nameYear;
    }

    public void setNameYear(String nameYear) {
        this.nameYear = nameYear;
    }

    public Date getYearStart() {
        return yearStart;
    }

    public void setYearStart(Date yearStart) {
        this.yearStart = yearStart;
    }

    public Date getYearEnd() {
        return yearEnd;
    }

    public void setYearEnd(Date yearEnd) {
        this.yearEnd = yearEnd;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }
    
}
