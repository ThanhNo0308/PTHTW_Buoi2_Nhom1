/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Forum;
import java.util.List;

public interface ForumRepository {

    List<Forum> getForums();

    List<Forum> getForumBySubjectTeacher(int subjectTeacherId);

    boolean addForum(Forum forum);

    boolean deleteForum(int forumId);

    boolean updateForum(Forum forum);

    Forum getForumById(int id);
}
