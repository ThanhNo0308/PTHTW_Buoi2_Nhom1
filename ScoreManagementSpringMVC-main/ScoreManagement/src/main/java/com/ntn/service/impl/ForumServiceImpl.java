/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.service.impl;

import com.ntn.pojo.Forum;
import com.ntn.repository.ForumRepository;
import com.ntn.service.ForumService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author nguye
 */
@Service
public class ForumServiceImpl implements ForumService {
    
    @Autowired
    private ForumRepository forumRepo;
    @Override
    public List<Forum> getForums() {
        return this.forumRepo.getForums();
    }

    @Override
    public List<Forum> getForumBySubjectTeacher(int subjectTeacherId) {
        return this.forumRepo.getForumBySubjectTeacher(subjectTeacherId);
    }

    @Override
    public boolean addForum(Forum forum) {
        return this.forumRepo.addForum(forum);
    }

    @Override
    public boolean deleteForum(int forumId) {
        return this.forumRepo.deleteForum(forumId);
    }

    
    
    
}
