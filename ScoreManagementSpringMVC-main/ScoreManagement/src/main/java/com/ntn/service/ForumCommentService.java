/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Forumcomment;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface ForumCommentService {
    List<Forumcomment> getCommentsByForumId(Integer forumId);
    Forumcomment getCommentById(Integer commentId);
    boolean addComment(Forumcomment comment);
    boolean updateComment(Forumcomment comment);
    boolean deleteComment(Integer commentId);
}
