/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.service;

import com.ntn.pojo.Subject;
import java.util.List;

public interface SubjectService {

    Subject getSubjectById(int subjectId);

    List<Subject> getSubjects();

    List<Subject> getSubjectsByKeyword(String keyword);

    List<Subject> getSubjectsByDepartmentId(Integer departmentId);

    List<Subject> getSubjectsByDepartmentIdAndKeyword(Integer departmentId, String keyword);

    boolean addOrUpdateSubject(Subject subject);

    boolean deleteSubject(int subjectId);

}
