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
@Table(name = "forumcomment")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Forumcomment.findAll", query = "SELECT f FROM Forumcomment f"),
    @NamedQuery(name = "Forumcomment.findById", query = "SELECT f FROM Forumcomment f WHERE f.id = :id"),
    @NamedQuery(name = "Forumcomment.findByTitle", query = "SELECT f FROM Forumcomment f WHERE f.title = :title"),
    @NamedQuery(name = "Forumcomment.findByContent", query = "SELECT f FROM Forumcomment f WHERE f.content = :content"),
    @NamedQuery(name = "Forumcomment.findByCreatedAt", query = "SELECT f FROM Forumcomment f WHERE f.createdAt = :createdAt")})
public class Forumcomment implements Serializable {

    @Size(max = 255)
    @Column(name = "Title")
    private String title;
    @Size(max = 3000)
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
    @JoinColumn(name = "ForumId", referencedColumnName = "Id")
    @ManyToOne
    private Forum forumId;
    @OneToMany(mappedBy = "parentCommentId")
    private List<Forumcomment> forumcommentList;
    @JoinColumn(name = "ParentCommentId", referencedColumnName = "Id")
    @ManyToOne
    private Forumcomment parentCommentId;
    @JoinColumn(name = "UserId", referencedColumnName = "Id")
    @ManyToOne
    private User userId;

    public Forumcomment() {
    }

    public Forumcomment(Integer id) {
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

    public Forum getForumId() {
        return forumId;
    }

    public void setForumId(Forum forumId) {
        this.forumId = forumId;
    }

    @XmlTransient
    @JsonIgnore
    public List<Forumcomment> getForumcommentList() {
        return forumcommentList;
    }

    public void setForumcommentList(List<Forumcomment> forumcommentList) {
        this.forumcommentList = forumcommentList;
    }

    public Forumcomment getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Forumcomment parentCommentId) {
        this.parentCommentId = parentCommentId;
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
        if (!(object instanceof Forumcomment)) {
            return false;
        }
        Forumcomment other = (Forumcomment) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.Forumcomment[ id=" + id + " ]";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}
