/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.pojo;

import java.io.Serializable;
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

@Entity
@Table(name = "studentsubjectteacher")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Studentsubjectteacher.findAll", query = "SELECT s FROM Studentsubjectteacher s"),
    @NamedQuery(name = "Studentsubjectteacher.findById", query = "SELECT s FROM Studentsubjectteacher s WHERE s.id = :id")})
public class Studentsubjectteacher implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;
    @JoinColumn(name = "StudentId", referencedColumnName = "Id")
    @ManyToOne
    private Student studentId;
    @JoinColumn(name = "SubjectTeacherId", referencedColumnName = "Id")
    @ManyToOne
    private Subjectteacher subjectTeacherId;

    public Studentsubjectteacher() {
    }

    public Studentsubjectteacher(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Student getStudentId() {
        return studentId;
    }

    public void setStudentId(Student studentId) {
        this.studentId = studentId;
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
        if (!(object instanceof Studentsubjectteacher)) {
            return false;
        }
        Studentsubjectteacher other = (Studentsubjectteacher) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.Studentsubjectteacher[ id=" + id + " ]";
    }
    
}
