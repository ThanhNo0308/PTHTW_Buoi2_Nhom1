package com.ntn.formatters;

import com.ntn.pojo.Department;
import com.ntn.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class DepartmentFormatter implements Formatter<Department> {

    @Autowired
    private DepartmentService departmentService;

    @Override
    public Department parse(String text, Locale locale) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(text);
            return departmentService.getDepartmentById(id);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid Department ID: " + text, 0);
        }
    }

    @Override
    public String print(Department object, Locale locale) {
        if (object == null) {
            return "";
        }
        return object.getId().toString();
    }
}