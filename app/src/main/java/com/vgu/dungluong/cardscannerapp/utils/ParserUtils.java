package com.vgu.dungluong.cardscannerapp.utils;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.ORDINAL_NUMBER_PATTERN;
import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.WEB_ADDRESS_PATTERN;

/**
 * Created by Dung Luong on 16/08/2019
 */
public class ParserUtils {

    private List<String> texts;

    private List<String> emails;

    private List<String> webs;

    private List<Pair<String, String>> phones;

    private List<String> addresses;

    private Map<String, String> zipRegexps;

    private List<String> addressKeywords;

    private String department;

    private String locale;

    public ParserUtils(List<String> texts, String locale) {
        this.texts = texts;
        this.locale = locale;
        emails = new ArrayList<>();
        webs = new ArrayList<>();
        phones = new ArrayList<>();
        addresses = new ArrayList<>();
        department = "";

        zipRegexps = new HashMap<>();
        addressKeywords = new ArrayList<>();
        if(locale.equals("en")) {
            zipRegexps.put("GB", "GIR[ ]?0AA|((AB|AL|B|BA|BB|BD|BH|BL|BN|BR|BS|BT|CA|CB|CF|CH|CM|CO|CR|CT|CV|CW|DA|DD|DE|DG|DH|DL|DN|DT|DY|E|EC|EH|EN|EX|FK|FY|G|GL|GY|GU|HA|HD|HG|HP|HR|HS|HU|HX|IG|IM|IP|IV|JE|KA|KT|KW|KY|L|LA|LD|LE|LL|LN|LS|LU|M|ME|MK|ML|N|NE|NG|NN|NP|NR|NW|OL|OX|PA|PE|PH|PL|PO|PR|RG|RH|RM|S|SA|SE|SG|SK|SL|SM|SN|SO|SP|SR|SS|ST|SW|SY|TA|TD|TF|TN|TQ|TR|TS|TW|UB|W|WA|WC|WD|WF|WN|WR|WS|WV|YO|ZE)(\\d[\\dA-Z]?[ ]?\\d[ABD-HJLN-UW-Z]{2}))|BFPO[ ]?\\d{1,4}");
            zipRegexps.put("JE", "JE\\d[\\dA-Z]?[ ]?\\d[ABD-HJLN-UW-Z]{2}");
            zipRegexps.put("GG", "GY\\d[\\dA-Z]?[ ]?\\d[ABD-HJLN-UW-Z]{2}");
            zipRegexps.put("IM", "IM\\d[\\dA-Z]?[ ]?\\d[ABD-HJLN-UW-Z]{2}");
            zipRegexps.put("US", "\\d{5}([ \\-]\\d{4})?");
            zipRegexps.put("CA", "[ABCEGHJKLMNPRSTVXY]\\d[ABCEGHJ-NPRSTV-Z][ ]?\\d[ABCEGHJ-NPRSTV-Z]\\d");
            zipRegexps.put("DE", "\\d{5}");
            zipRegexps.put("JP", "\\d{3}-\\d{4}");
            zipRegexps.put("FR", "\\d{2}[ ]?\\d{3}");
            zipRegexps.put("AU", "\\d{4}");
            zipRegexps.put("IT", "\\d{5}");
            zipRegexps.put("CH", "\\d{4}");
            zipRegexps.put("AT", "\\d{4}");
            zipRegexps.put("ES", "\\d{5}");
            zipRegexps.put("NL", "\\d{4}[ ]?[A-Z]{2}");
            zipRegexps.put("BE", "\\d{4}");
            zipRegexps.put("DK", "\\d{4}");
            zipRegexps.put("SE", "\\d{3}[ ]?\\d{2}");
            zipRegexps.put("NO", "\\d{4}");
            zipRegexps.put("BR", "\\d{5}[\\-]?\\d{3}");
            zipRegexps.put("PT", "\\d{4}([\\-]\\d{3})?");
            zipRegexps.put("FI", "\\d{5}");
            zipRegexps.put("AX", "22\\d{3}");
            zipRegexps.put("KR", "\\d{3}[\\-]\\d{3}");
            zipRegexps.put("CN", "\\d{6}");
            zipRegexps.put("TW", "\\d{3}(\\d{2})?");
            zipRegexps.put("SG", "\\d{6}");
            zipRegexps.put("DZ", "\\d{5}");
            zipRegexps.put("AD", "AD\\d{3}");
            zipRegexps.put("AR", "([A-HJ-NP-Z])?\\d{4}([A-Z]{3})?");
            zipRegexps.put("AM", "(37)?\\d{4}");
            zipRegexps.put("AZ", "\\d{4}");
            zipRegexps.put("BH", "((1[0-2]|[2-9])\\d{2})?");
            zipRegexps.put("BD", "\\d{4}");
            zipRegexps.put("BB", "(BB\\d{5})?");
            zipRegexps.put("BY", "\\d{6}");
            zipRegexps.put("BM", "[A-Z]{2}[ ]?[A-Z0-9]{2}");
            zipRegexps.put("BA", "\\d{5}");
            zipRegexps.put("BA", "\\d{5}");
            zipRegexps.put("IO", "BBND 1ZZ");
            zipRegexps.put("BN", "[A-Z]{2}[ ]?\\d{4}");
            zipRegexps.put("BG", "\\d{4}");
            zipRegexps.put("KH", "\\d{5}");
            zipRegexps.put("CV", "\\d{4}");
            zipRegexps.put("CL", "\\d{7}");
            zipRegexps.put("CR", "\\d{4,5}|\\d{3}-\\d{4}");
            zipRegexps.put("HR", "\\d{5}");
            zipRegexps.put("CY", "\\d{4}");
            zipRegexps.put("CZ", "\\d{3}[ ]?\\d{2}");
            zipRegexps.put("DO", "\\d{5}");
            zipRegexps.put("EC", "([A-Z]\\d{4}[A-Z]|(?:[A-Z]{2})?\\d{6})?");
            zipRegexps.put("EG", "\\d{5}");
            zipRegexps.put("EE", "\\d{5}");
            zipRegexps.put("FO", "\\d{3}");
            zipRegexps.put("GE", "\\d{4}");
            zipRegexps.put("GR", "\\d{3}[ ]?\\d{2}");
            zipRegexps.put("GL", "39\\d{2}");
            zipRegexps.put("GT", "\\d{5}");
            zipRegexps.put("HT", "\\d{4}");
            zipRegexps.put("HN", "(?:\\d{5})?");
            zipRegexps.put("HU", "\\d{4}");
            zipRegexps.put("IS", "\\d{3}");
            zipRegexps.put("IN", "\\d{6}");
            zipRegexps.put("ID", "\\d{5}");
            zipRegexps.put("IL", "\\d{5}");
            zipRegexps.put("JO", "\\d{5}");
            zipRegexps.put("KZ", "\\d{6}");
            zipRegexps.put("KE", "\\d{5}");
            zipRegexps.put("KW", "\\d{5}");
            zipRegexps.put("LA", "\\d{5}");
            zipRegexps.put("LV", "\\d{4}");
            zipRegexps.put("LB", "(\\d{4}([ ]?\\d{4})?)?");
            zipRegexps.put("LI", "(948[5-9])|(949[0-7])");
            zipRegexps.put("LT", "\\d{5}");
            zipRegexps.put("LU", "\\d{4}");
            zipRegexps.put("MK", "\\d{4}");
            zipRegexps.put("MY", "\\d{5}");
            zipRegexps.put("MV", "\\d{5}");
            zipRegexps.put("MT", "[A-Z]{3}[ ]?\\d{2,4}");
            zipRegexps.put("MU", "(\\d{3}[A-Z]{2}\\d{3})?");
            zipRegexps.put("MX", "\\d{5}");
            zipRegexps.put("MD", "\\d{4}");
            zipRegexps.put("MC", "980\\d{2}");
            zipRegexps.put("MA", "\\d{5}");
            zipRegexps.put("NP", "\\d{5}");
            zipRegexps.put("NZ", "\\d{4}");
            zipRegexps.put("NI", "((\\d{4}-)?\\d{3}-\\d{3}(-\\d{1})?)?");
            zipRegexps.put("NG", "(\\d{6})?");
            zipRegexps.put("OM", "(PC )?\\d{3}");
            zipRegexps.put("PK", "\\d{5}");
            zipRegexps.put("PY", "\\d{4}");
            zipRegexps.put("PH", "\\d{4}");
            zipRegexps.put("PL", "\\d{2}-\\d{3}");
            zipRegexps.put("PR", "00[679]\\d{2}([ \\-]\\d{4})?");
            zipRegexps.put("RO", "\\d{6}");
            zipRegexps.put("RU", "\\d{6}");
            zipRegexps.put("SM", "4789\\d");
            zipRegexps.put("SA", "\\d{5}");
            zipRegexps.put("SN", "\\d{5}");
            zipRegexps.put("SK", "\\d{3}[ ]?\\d{2}");
            zipRegexps.put("SI", "\\d{4}");
            zipRegexps.put("ZA", "\\d{4}");
            zipRegexps.put("LK", "\\d{5}");
            zipRegexps.put("TJ", "\\d{6}");
            zipRegexps.put("TH", "\\d{5}");
            zipRegexps.put("TN", "\\d{4}");
            zipRegexps.put("TR", "\\d{5}");
            zipRegexps.put("TM", "\\d{6}");
            zipRegexps.put("UA", "\\d{5}");
            zipRegexps.put("UY", "\\d{5}");
            zipRegexps.put("UZ", "\\d{6}");
            zipRegexps.put("VA", "00120");
            zipRegexps.put("VE", "\\d{4}");
            zipRegexps.put("ZM", "\\d{5}");
            zipRegexps.put("AS", "96799");
            zipRegexps.put("CC", "6799");
            zipRegexps.put("CK", "\\d{4}");
            zipRegexps.put("RS", "\\d{6}");
            zipRegexps.put("ME", "8\\d{4}");
            zipRegexps.put("CS", "\\d{5}");
            zipRegexps.put("YU", "\\d{5}");
            zipRegexps.put("CX", "6798");
            zipRegexps.put("ET", "\\d{4}");
            zipRegexps.put("FK", "FIQQ 1ZZ");
            zipRegexps.put("NF", "2899");
            zipRegexps.put("FM", "(9694[1-4])([ \\-]\\d{4})?");
            zipRegexps.put("GF", "9[78]3\\d{2}");
            zipRegexps.put("GN", "\\d{3}");
            zipRegexps.put("GP", "9[78][01]\\d{2}");
            zipRegexps.put("GS", "SIQQ 1ZZ");
            zipRegexps.put("GU", "969[123]\\d([ \\-]\\d{4})?");
            zipRegexps.put("GW", "\\d{4}");
            zipRegexps.put("HM", "\\d{4}");
            zipRegexps.put("IQ", "\\d{5}");
            zipRegexps.put("KG", "\\d{6}");
            zipRegexps.put("LR", "\\d{4}");
            zipRegexps.put("LS", "\\d{3}");
            zipRegexps.put("MG", "\\d{3}");
            zipRegexps.put("MH", "969[67]\\d([ \\-]\\d{4})?");
            zipRegexps.put("MN", "\\d{6}");
            zipRegexps.put("MP", "9695[012]([ \\-]\\d{4})?");
            zipRegexps.put("MQ", "9[78]2\\d{2}");
            zipRegexps.put("NC", "988\\d{2}");
            zipRegexps.put("NE", "\\d{4}");
            zipRegexps.put("VI", "008(([0-4]\\d)|(5[01]))([ \\-]\\d{4})?");
            zipRegexps.put("PF", "987\\d{2}");
            zipRegexps.put("PG", "\\d{3}");
            zipRegexps.put("PM", "9[78]5\\d{2}");
            zipRegexps.put("PN", "PCRN 1ZZ");
            zipRegexps.put("PW", "96940");
            zipRegexps.put("RE", "9[78]4\\d{2}");
            zipRegexps.put("SH", "(ASCN|STHL) 1ZZ");
            zipRegexps.put("SJ", "\\d{4}");
            zipRegexps.put("SO", "\\d{5}");
            zipRegexps.put("SZ", "[HLMS]\\d{3}");
            zipRegexps.put("TC", "TKCA 1ZZ");
            zipRegexps.put("WF", "986\\d{2}");
            zipRegexps.put("XK", "\\d{5}");
            zipRegexps.put("YT", "976\\d{2}");

            addressKeywords.add("boulevard");
            addressKeywords.add("blvd");
            addressKeywords.add("street");
            addressKeywords.add("st");
            addressKeywords.add("district");
            addressKeywords.add("province");
            addressKeywords.add("lane");
            addressKeywords.add("ward");
            addressKeywords.add("city");
            addressKeywords.add("apartment");
            addressKeywords.add("room");
            addressKeywords.add("building");
            addressKeywords.add("place");
            addressKeywords.add("station");
            addressKeywords.add("road");
            addressKeywords.add("rd");
            addressKeywords.add("avenue");
            addressKeywords.add("ave.");
            addressKeywords.add("drive");
            addressKeywords.add("court");
            addressKeywords.add("terrace");
            addressKeywords.add("zone");
            addressKeywords.add("ward");
            addressKeywords.add("epz");
            addressKeywords.add("plaza");
            addressKeywords.add("suite");
            addressKeywords.add("industrial");
            addressKeywords.add("square");
            addressKeywords.add("hill");
            addressKeywords.add("floor");
            addressKeywords.add("block");
            addressKeywords.add("island");
            addressKeywords.add("park");
        }else{
            addressKeywords.add("đường");
            addressKeywords.add("quận");
            addressKeywords.add("phường");
            addressKeywords.add("tổ");
            addressKeywords.add("p");
            addressKeywords.add("q");
            addressKeywords.add("lầu");
            addressKeywords.add("lô");
            addressKeywords.add("khu");
            addressKeywords.add("đc");
            addressKeywords.add("tp");
            addressKeywords.add("kcn");
            addressKeywords.add("ngõ");
            addressKeywords.add("tỉnh");
        }
    }

    public void run(){
        List<String> clone = new ArrayList<>(texts);
        setEmails(parseEmails(new ArrayList<>(texts)));
        setPhones(parsePhones(new ArrayList<>(texts)));
        setAddresses(parseAddress(new ArrayList<>(texts)));
        setWebs(parseWebs(new ArrayList<>(texts)));

        emails.forEach(email ->{
            clone.forEach(text ->{
                if(text.contains(email))  clone.set(clone.indexOf(text), "");
            });
        });

        phones.forEach(phone ->{
            clone.forEach(text ->{
                if(text.contains(phone.second))  clone.set(clone.indexOf(text), "");
            });
        });

        addresses.forEach(address ->{
            clone.forEach(text ->{
                if(text.contains(address))  clone.set(clone.indexOf(text), "");
            });
        });

        webs.forEach(web ->{
            clone.forEach(text ->{
                if(text.contains(web))  clone.set(clone.indexOf(text), "");
            });
        });

        // Search for department
        clone.forEach(text ->{
            if(text.toLowerCase().contains("department")
                    ||text.toLowerCase().contains("dept")
                    ||text.toLowerCase().contains("dpt")
                    ||text.toLowerCase().contains("division")){
                department = text;
                clone.set(clone.indexOf(text), "");
            }
        });

        // Get last first three lines
        setTexts(clone.stream().filter(text -> !text.isEmpty()).collect(Collectors.toList()));
    }

    private List<String> parseEmails(List<String> texts){
        List<String> emails = new ArrayList<>();
        texts.forEach(text -> {
            if(consider(text)){
                List<String> validEmails = extractEmails(text);
                if(validEmails.size() > 0){
                    int textIndex = texts.indexOf(text);
                    validEmails.forEach(validEmail -> {
                        String modifiedText = texts.get(textIndex);
                        texts.set(textIndex, modifiedText.replace(validEmail,""));
                    });
                    emails.addAll(validEmails);
                }
                else {
                    emails.add(text);
                }
            }
        });
        setTexts(texts);
        return emails;
    }

    private List<String> parseAddress(List<String> texts){
        List<String> addresses = new ArrayList<>();
        texts.forEach(text -> {
            if(checkLineForAddress(text)){
                int textIndex = texts.indexOf(text);
                texts.set(textIndex, "");
                addresses.add(text);
            }
        });
        setTexts(texts);
        return addresses;
    }

    private List<String> parseWebs(List<String> texts){
        List<String> matches = new ArrayList<String>();
        texts.forEach(text ->{
            Matcher m = WEB_ADDRESS_PATTERN.matcher(text);
            List<String> webs = new ArrayList<>();
            while(m.find()) {
                String result = m.group(0);
                int firstIndex  = text.indexOf(result.charAt(0));
                for(int i = firstIndex - 1; i > -1; i--){
                    char c = text.charAt(i);
                    if(c == 'w' || c == 'W' || c == '.' || Character.isWhitespace(c)){
                        result = c + result;
                    }else{
                        break;
                    }
                }
                //if(result.replaceAll("\\s+","").toLowerCase().contains("www")) {
                    webs.add(result);
                //}
            }
            if(webs.size() > 0) {
                matches.addAll(webs);
                int textIndex = texts.indexOf(text);
                webs.forEach(web -> {
                    String modifiedText = texts.get(textIndex);
                    texts.set(textIndex, modifiedText.replace(web,""));
                });
            }
        });
        setTexts(texts);
        return matches;
    }

    public List<Pair<String, String>> parsePhones(List<String> texts){
        List<Pair<String,String>> matches = new ArrayList<>();
        texts.forEach(text ->{
            int num = 0;
            String phone = "";

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if(isCorrectSpecialCharacter(c)){
                    phone = phone + c;
                    if (Character.isWhitespace(c)){
                        if(i != text.length() - 1){
                            char nextChar = text.charAt(i+1);
                            if(Character.isDigit(nextChar) || isCorrectSpecialCharacter(nextChar) || Character.isWhitespace(nextChar)){
                                continue;
                            }else{
                                if (num > 9) {
                                    String dataType = "";
                                    for(int j = text.indexOf(phone) - 1; j > -1 ; j --){
                                        char c1 = text.charAt(j);
                                        if(Character.isLetter(c1) || c1 == ':')
                                            dataType = c1 + dataType;
                                        else break;
                                    }
                                    matches.add(new Pair<>(dataType.replaceAll(":",""), phone));
                                }
                                num = 0;
                                phone = "";
                            }
                        }
                    }
                }else if (Character.isDigit(c)) {
                    num ++;
                    phone = phone + c;
                } else{
                    if (num > 9) {
                        String dataType = "";
                        for(int j = text.indexOf(phone) - 1; j > -1 ; j --){
                            char c1 = text.charAt(j);
                            if(Character.isLetter(c1) || c1 == ':')
                                dataType = c1 + dataType;
                            else break;
                        }
                        matches.add(new Pair<>(dataType.replaceAll(":",""), phone));
                    }
                    num = 0;
                    phone = "";
                }
                if(i == text.length() -1){
                    if (num > 9) {
                        String dataType = "";
                        for (int j = text.indexOf(phone) - 1; j > -1; j--) {
                            char c1 = text.charAt(j);
                            if (Character.isLetter(c1) || c1 == ':')
                                dataType = c1 + dataType;
                            else break;
                        }
                        matches.add(new Pair<>(dataType.replaceAll(":",""), phone));
                    }
                }
            }
            int textIndex = texts.indexOf(text);
            matches.forEach(p -> {
                String modifiedText = texts.get(textIndex);
                texts.set(textIndex, modifiedText.replace(p.second,""));
            });
        });
        setTexts(texts);
        return matches;
    }

    private boolean isCorrectSpecialCharacter(char c){
        return c == '+'
                || c == '-'
                || c == '#'
                || c == '.'
                || c =='('
                || c == ')'
                || c == '/'
                || c == '*'
                || c == '_'
                || c == ','
                || c == 'N'
                || c == ';'
                ||Character.isWhitespace(c);
    }

    // Check email address intent
    private boolean consider(String line) {
        if (AppConstants.INTENT_EMAIL_ADDDRESS_PATTERN.matcher(line).matches()) {
            return true;
        }else{
            return false;
        }
    }

    private List<String> extractEmails(String text){
        List<String> matches = new ArrayList<String>();
        Matcher m = AppConstants.EMAIL_ADDDRESS_PATTERN.matcher(text);
        while(m.find()) {
            matches.add(m.group(0));
        }
        return matches;
    }

    public boolean checkLineForAddress(String text) {
        float addressProbability = 0;
        if(locale.equals("en")) {
            boolean containsZip = false;
            boolean containsState = false;
            boolean containsKeyword = false;
            boolean containsNumber = false;
            boolean containsComma = false;
            boolean containsCountry = false;
            boolean containsUpperAndDigit = false;


            // Check contain comma
            if (text.contains(",")) {
                containsComma = true;
                addressProbability += 0.1;
            }

            // check building number
            if (text.contains("/")) {
                addressProbability += 0.1;
            }

            if (Pattern.compile("([A-Z]+[0-9]+)+ ").matcher(text).matches()) {
                addressProbability += 0.2;
                containsUpperAndDigit = true;
            }

//        Zero or more whitespaces (\\s*)
//        comma, or whitespace (,|\\s)
//        Zero or more whitespaces (\\s*)
            String[] items = text.split("\\s*(,|\\.|\\s)\\s*");
            for (String item : items) {
                Set<Map.Entry<String, String>> entries = zipRegexps.entrySet();
                containsState = containsState || item.matches("[A-Z]{2}");

                for (Map.Entry<String, String> entry : entries) {
                    containsZip = containsZip || item.matches(entry.getValue());
                    if (containsZip) {
                        break;
                    }
                }

                for (String addressKeyword : addressKeywords) {
                    if (item.toLowerCase().equals(addressKeyword)) {
                        containsKeyword = true;
                        addressProbability += 0.3;
//                    System.out.println(addressKeyword);
                        break;
                    }
                }

                // Check contains country
                String[] countryCodes = Locale.getISOCountries();
                for (String countryCode : countryCodes) {
                    Locale obj = new Locale("", countryCode);
                    if (item.toLowerCase().contains(obj.getDisplayCountry().toLowerCase())) {
                        for (int i = text.indexOf(item) - 1; i > -1; i--) {
                            char c = text.charAt(i);
                            if (Character.isWhitespace(c)) {

                            } else if (c == ',') return true;
                            else break;
                        }
                        containsCountry = true;
                        addressProbability += 0.1;
                        break;
                    }
                }

                // Check is ordinal number
                if (ORDINAL_NUMBER_PATTERN.matcher(item).matches()) {
                    addressProbability += 0.3;
                }

                // Increase 0.03 for each word
                addressProbability += 0.03;
            }

            // Check contains number
            if (Pattern.compile(".*\\d.*").matcher(text).matches()) {
                containsNumber = true;
//            System.out.println("match number");
                addressProbability += 0.1;
            }

            if (containsState && containsZip) return true;
            if (containsState && containsNumber) return true;
            if (containsKeyword && containsNumber) return true;
            if (containsKeyword && containsComma) return true;
            if (containsComma && containsNumber) return true;
            if (containsComma && containsZip) return true;
            if (containsCountry && items.length == 1) return true;
            if (containsCountry && containsNumber) return true;
            if (containsCountry && containsComma) return true;
            if (containsUpperAndDigit && containsComma) return true;
            if (containsState) addressProbability += 0.3;

        }else{
            boolean containsNumber = false;
            boolean containSpecialCharacter = false;

            // Check contain comma
            if(text.contains(",")) {
                containSpecialCharacter = true;
                addressProbability += 0.1;
            }

            // check building number
            if(text.contains("/")) {
                containSpecialCharacter = true;
                addressProbability += 0.1;
            }

            if(text.contains("-")) {
                containSpecialCharacter = true;
                addressProbability += 0.1;
            }

            if(text.contains(".")) {
                containSpecialCharacter = true;
                addressProbability += 0.1;
            }


            String[] items = text.split("\\s*(,|:|-|\\.|\\s)\\s*");
            for (String item : items) {
                for (String addressKeyword : addressKeywords) {
                    if (item.toLowerCase().equals(addressKeyword)) {
                        return true;
                    }
                }
                // Increase 0.03 for each word
                addressProbability += 0.03;

            }

            // Check contains number
            if (Pattern.compile(".*\\d.*").matcher(text).matches()) {
                containsNumber = true;
                addressProbability += 0.1;
            }

            if(containsNumber && containSpecialCharacter) return true;

        }
        AppLogger.i("Address prob: " + addressProbability);
        if (addressProbability > 0.49) return true;
        else return false;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public List<String> getWebs() {
        return webs;
    }

    public void setWebs(List<String> webs) {
        this.webs = webs;
    }

    public List<Pair<String, String>> getPhones() {
        return phones;
    }

    public void setPhones(List<Pair<String, String>> phones) {
        this.phones = phones;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public List<String> getTexts() {
        return texts;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
