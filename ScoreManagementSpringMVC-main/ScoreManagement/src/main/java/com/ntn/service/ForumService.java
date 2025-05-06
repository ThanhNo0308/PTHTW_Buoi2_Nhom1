/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Forum;
import java.util.List;

public interface ForumService {

    Forum getForumById(int id);

    List<Forum> getForums();

    List<Forum> getForumBySubjectTeacher(int subjectTeacherId);

    List<Forum> getForumByTeacher(int teacherId);

    List<Forum> getForumByStudent(int studentId);

    boolean addForum(Forum forum);

    boolean updateForum(Forum forum);

    boolean deleteForum(int forumId);
}
