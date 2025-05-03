package com.ntn.formatters;

import com.ntn.pojo.Class;
import com.ntn.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class ClassFormatter implements Formatter<Class> {

    @Autowired
    private ClassService classService;

    @Override
    public Class parse(String text, Locale locale) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(text);
            return classService.getClassById(id);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid Class ID: " + text, 0);
        }
    }

    @Override
    public String print(Class object, Locale locale) {
        if (object == null) {
            return "";
        }
        return object.getId().toString();
    }
}