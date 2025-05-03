package com.ntn.formatters;

import com.ntn.pojo.Subject;
import com.ntn.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class SubjectFormatter implements Formatter<Subject> {

    @Autowired
    private SubjectService subjectService;

    @Override
    public Subject parse(String text, Locale locale) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(text);
            return subjectService.getSubjectById(id);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid Subject ID: " + text, 0);
        }
    }

    @Override
    public String print(Subject object, Locale locale) {
        if (object == null) {
            return "";
        }
        return object.getId().toString();
    }
}