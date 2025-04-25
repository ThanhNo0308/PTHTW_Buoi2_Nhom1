/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Forum;
import java.util.List;


public interface ForumService {

    List<Forum> getForums();

    List<Forum> getForumBySubjectTeacher(int subjectTeacherId);

    boolean addForum(Forum forum);
    
    boolean updateForum(Forum forum);
    
     boolean deleteForum(int forumId);
     
     Forum getForumById(int id);
}
