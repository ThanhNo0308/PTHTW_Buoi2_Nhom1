package com.ntn.formatters;

import com.ntn.pojo.User;
import com.ntn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class UserFormatter implements Formatter<User> {

    @Autowired
    private UserService userService;

    @Override
    public User parse(String text, Locale locale) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(text);
            return userService.getUserById(id);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid User ID: " + text, 0);
        }
    }

    @Override
    public String print(User object, Locale locale) {
        if (object == null) {
            return "";
        }
        return object.getId().toString();
    }
}