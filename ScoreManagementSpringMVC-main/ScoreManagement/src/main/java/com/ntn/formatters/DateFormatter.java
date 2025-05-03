package com.ntn.formatters;

import org.springframework.format.Formatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class DateFormatter implements Formatter<Date> {
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public DateFormatter() {
        dateFormat.setLenient(false);
    }
    
    @Override
    public Date parse(String text, Locale locale) throws ParseException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return dateFormat.parse(text);
    }

    @Override
    public String print(Date object, Locale locale) {
        if (object == null) {
            return "";
        }
        return dateFormat.format(object);
    }
}