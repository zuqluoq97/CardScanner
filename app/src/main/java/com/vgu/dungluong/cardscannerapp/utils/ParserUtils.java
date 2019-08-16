package com.vgu.dungluong.cardscannerapp.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dung Luong on 16/08/2019
 */
public class ParserUtils {

    private ParserUtils() {
    }

    public static List<String> parseEmails(List<String> texts){
        List<String> emails = new ArrayList<>();
        texts.forEach(text -> {
            if(consider(text)){
                List<String> validEmails = extractEmails(text);
                if(validEmails.size() > 0) emails.addAll(extractEmails(text));
                else emails.add(text);
            }
        });
        return emails;
    }

    // Check email address intent
    private static boolean consider(String line) {
        if (AppConstants.INTENT_EMAIL_ADDDRESS_PATTERN.matcher(line).matches()) {
            return true;
        }else{
            return false;
        }
    }

    private static List<String> extractEmails(String text){
        List<String> matches = new ArrayList<String>();
        Matcher m = AppConstants.EMAIL_ADDDRESS_PATTERN.matcher(text);
        while(m.find()) {
            matches.add(m.group(1));
        }
        return matches;
    }

}
