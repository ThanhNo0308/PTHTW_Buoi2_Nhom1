/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "forum")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Forum.findAll", query = "SELECT f FROM Forum f"),
    @NamedQuery(name = "Forum.findById", query = "SELECT f FROM Forum f WHERE f.id = :id"),
    @NamedQuery(name = "Forum.findByTitle", query = "SELECT f FROM Forum f WHERE f.title = :title"),
    @NamedQuery(name = "Forum.findByDescription", query = "SELECT f FROM Forum f WHERE f.description = :description"),
    @NamedQuery(name = "Forum.findByContent", query = "SELECT f FROM Forum f WHERE f.content = :content"),
    @NamedQuery(name = "Forum.findByCreatedAt", query = "SELECT f FROM Forum f WHERE f.createdAt = :createdAt")})
public class Forum implements Serializable {

    @Size(max = 255)
    @Column(name = "Title")
    private String title;
    @Size(max = 255)
    @Column(name = "Description")
    private String description;
    @Size(max = 4000)
    @Column(name = "Content")
    private String content;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;
    @Column(name = "CreatedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @OneToMany(mappedBy = "forumId")
    private List<Forumcomment> forumcommentList;
    @JoinColumn(name = "SubjectTeacherId", referencedColumnName = "Id")
    @ManyToOne
    private Subjectteacher subjectTeacherId;
    @JoinColumn(name = "UserId", referencedColumnName = "Id")
    @ManyToOne
    private User userId;

    public Forum() {
    }

    public Forum(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @XmlTransient
    @JsonIgnore
    public List<Forumcomment> getForumcommentList() {
        return forumcommentList;
    }

    public void setForumcommentList(List<Forumcomment> forumcommentList) {
        this.forumcommentList = forumcommentList;
    }

    public Subjectteacher getSubjectTeacherId() {
        return subjectTeacherId;
    }

    public void setSubjectTeacherId(Subjectteacher subjectTeacherId) {
        this.subjectTeacherId = subjectTeacherId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
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
        if (!(object instanceof Forum)) {
            return false;
        }
        Forum other = (Forum) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.Forum[ id=" + id + " ]";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}
