package com.ntn.formatters;

import com.ntn.pojo.Student;
import com.ntn.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class StudentFormatter implements Formatter<Student> {

    @Autowired
    private StudentService studentService;

    @Override
    public Student parse(String text, Locale locale) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(text);
            return studentService.getStudentById(id);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid Student ID: " + text, 0);
        }
    }

    @Override
    public String print(Student object, Locale locale) {
        if (object == null) {
            return "";
        }
        return object.getId().toString();
    }
}