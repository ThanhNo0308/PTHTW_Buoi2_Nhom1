package com.ntn.formatters;

import com.ntn.pojo.Subjectteacher;
import com.ntn.service.SubjectTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class SubjectTeacherFormatter implements Formatter<Subjectteacher> {

    @Autowired
    private SubjectTeacherService subjectTeacherService;

    @Override
    public Subjectteacher parse(String text, Locale locale) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(text);
            return subjectTeacherService.getSubjectTeacherById(id);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid SubjectTeacher ID: " + text, 0);
        }
    }

    @Override
    public String print(Subjectteacher object, Locale locale) {
        if (object == null) {
            return "";
        }
        return object.getId().toString();
    }
}