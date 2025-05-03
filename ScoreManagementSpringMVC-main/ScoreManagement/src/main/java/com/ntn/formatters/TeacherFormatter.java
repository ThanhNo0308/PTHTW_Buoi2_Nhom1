package com.ntn.formatters;

import com.ntn.pojo.Teacher;
import com.ntn.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class TeacherFormatter implements Formatter<Teacher> {

    @Autowired
    private TeacherService teacherService;

    @Override
    public Teacher parse(String text, Locale locale) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(text);
            return teacherService.getTeacherById(id);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid Teacher ID: " + text, 0);
        }
    }

    @Override
    public String print(Teacher object, Locale locale) {
        if (object == null) {
            return "";
        }
        return object.getId().toString();
    }
}