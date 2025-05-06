/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Set;

@Entity
@Table(name = "typescore")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Typescore.findAll", query = "SELECT t FROM Typescore t"),
    @NamedQuery(name = "Typescore.findByScoreType", query = "SELECT t FROM Typescore t WHERE t.scoreType = :scoreType")})
public class Typescore implements Serializable {

    @OneToMany(mappedBy = "scoreType")
    private Set<Classscoretypes> classscoretypesSet;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Size(min = 1, max = 50)
    @Column(name = "ScoreType")
    private String scoreType;
    @OneToMany(mappedBy = "scoreType")
    @JsonIgnore
    private List<Score> scoreList;

    public Typescore() {
    }

    public Typescore(String scoreType) {
        this.scoreType = scoreType;
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
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
        hash += (scoreType != null ? scoreType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Typescore)) {
            return false;
        }
        Typescore other = (Typescore) object;
        if ((this.scoreType == null && other.scoreType != null) || (this.scoreType != null && !this.scoreType.equals(other.scoreType))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return scoreType;
    }

    @XmlTransient
    @JsonIgnore
    public Set<Classscoretypes> getClassscoretypesSet() {
        return classscoretypesSet;
    }

    public void setClassscoretypesSet(Set<Classscoretypes> classscoretypesSet) {
        this.classscoretypesSet = classscoretypesSet;
    }
    
}
