/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Forumcomment;
import com.ntn.repository.ForumCommentRepository;
import com.ntn.service.ForumCommentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Admin
 */
@Service
public class ForumCommentServiceImpl implements ForumCommentService {
    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Override
    public List<Forumcomment> getCommentsByForumId(Integer forumId) {
        return this.forumCommentRepository.getCommentsByForumId(forumId);
    }

    @Override
    public Forumcomment getCommentById(Integer commentId) {
        return this.forumCommentRepository.getCommentById(commentId);
    }

    @Override
    public boolean addComment(Forumcomment comment) {
        return this.forumCommentRepository.addComment(comment);
    }

    @Override
    public boolean updateComment(Forumcomment comment) {
        return this.forumCommentRepository.updateComment(comment);
    }

    @Override
    public boolean deleteComment(Integer commentId) {
        return this.forumCommentRepository.deleteComment(commentId);
    }
}
