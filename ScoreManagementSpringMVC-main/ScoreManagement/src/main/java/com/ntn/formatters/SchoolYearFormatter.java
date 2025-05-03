package com.ntn.formatters;

import com.ntn.pojo.Schoolyear;
import com.ntn.service.SchoolYearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class SchoolYearFormatter implements Formatter<Schoolyear> {

    @Autowired
    private SchoolYearService schoolYearService;

    @Override
    public Schoolyear parse(String text, Locale locale) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(text);
            return schoolYearService.getSchoolYearById(id);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid SchoolYear ID: " + text, 0);
        }
    }

    @Override
    public String print(Schoolyear object, Locale locale) {
        if (object == null) {
            return "";
        }
        return object.getId().toString();
    }
}