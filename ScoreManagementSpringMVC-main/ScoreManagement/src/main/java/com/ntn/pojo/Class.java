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
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Set;

@Entity
@Table(name = "class")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Class.findAll", query = "SELECT c FROM Class c"),
    @NamedQuery(name = "Class.findById", query = "SELECT c FROM Class c WHERE c.id = :id"),
    @NamedQuery(name = "Class.findByClassName", query = "SELECT c FROM Class c WHERE c.className = :className")})
public class Class implements Serializable {

    @Size(max = 255)
    @Column(name = "ClassName")
    private String className;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "classId")
    private Set<Classscoretypes> classscoretypesSet;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;
    @OneToMany(mappedBy = "classId")
    @JsonIgnore
    private List<Student> studentList;
    @JoinColumn(name = "MajorId", referencedColumnName = "Id")
    @ManyToOne
    private Major majorId;
    @JoinColumn(name = "TeacherId", referencedColumnName = "Id")
    @ManyToOne
    private Teacher teacherId;
    @OneToMany(mappedBy = "classId")
    @JsonIgnore
    private List<Subjectteacher> subjectteacherList;

    public Class() {
    }

    public Class(Integer id) {
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
    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    public Major getMajorId() {
        return majorId;
    }

    public void setMajorId(Major majorId) {
        this.majorId = majorId;
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
        if (!(object instanceof Class)) {
            return false;
        }
        Class other = (Class) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return className;
    }

    @XmlTransient
    @JsonIgnore
    public Set<Classscoretypes> getClassscoretypesSet() {
        return classscoretypesSet;
    }

    public void setClassscoretypesSet(Set<Classscoretypes> classscoretypesSet) {
        this.classscoretypesSet = classscoretypesSet;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    
    @XmlTransient
    @JsonIgnore
    public List<Subjectteacher> getSubjectteacherList() {
        return subjectteacherList;
    }

    public void setSubjectteacherList(List<Subjectteacher> subjectteacherList) {
        this.subjectteacherList = subjectteacherList;
    }

}
