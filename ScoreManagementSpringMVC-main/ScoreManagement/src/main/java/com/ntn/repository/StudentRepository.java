/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.ntn.repository;

import com.ntn.pojo.Student;
import java.util.List;

/**
 *
 * @author nguye
 */
public interface StudentRepository {
    List<Student> getStudentByClassId(int classId);
    boolean addOrUpdateStudent(Student student);
    boolean deleteStudent(int studentId);
}
