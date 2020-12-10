package org.pdxfinder.dataloaders.updog.tablevalidation;

import java.util.List;

public class ValueRestrictions {

    //ascii punctuation characters are !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
    //URL restriction is for alphanumeric and _-.~
    private static final String FREE_TEXT = "^[\\p{Alpha}\\p{Space}\\p{Digit}().',<>%:;_\\/-]+$";
    private static final String FREE_TEXT_DESCRIPTION = "US ASCII Alphanumeric and ().',:;-/";
    private static final String URL_SAFE = "^[\\p{Alpha}\\p{Digit}\\p{Space}._~-]+$";
    private static final String URL_SAFE_DESCRIPTION = "US ASCII Alphanumeric and ._~-";
    private static final String NUMBER = "^[\\p{Digit}.,-]+$";
    private static final String NUMBER_DESCRIPTION = "0-9 and .,-";

    private String regex;
    private String description;

    private ValueRestrictions(String regex, String description){
        this.regex = regex;
        this.description = description;
    }

    static public ValueRestrictions URL_SAFE(){
        return new ValueRestrictions(URL_SAFE, URL_SAFE_DESCRIPTION);
    }

    static public ValueRestrictions FREE_TEXT(){
        return new ValueRestrictions(FREE_TEXT, FREE_TEXT_DESCRIPTION);
    }

    static public ValueRestrictions NUMBER(){
        return new ValueRestrictions(NUMBER, NUMBER_DESCRIPTION);
    }

    static public ValueRestrictions of(String regexCharset, String charSetDescription){
        return new ValueRestrictions(regexCharset, charSetDescription);
    }

    static public ValueRestrictions of(List<String> categories, String categoryDescription){
        return new ValueRestrictions(listToCaseInsensitiveRegex(categories), categoryDescription);
    }

    static private String listToCaseInsensitiveRegex(List<String> categories){
        String orRegex = String.join(" | ", categories).replaceAll("\\s+", "");
        return anchoredNoGroupingCaseInsensitiveRegex(orRegex);
    }

    static private String anchoredNoGroupingCaseInsensitiveRegex(String orRegex){
        return String.format("(?i)^(?:%s)$", orRegex);
    }

    public String getRegex() { return regex; }

    public String getDescription() { return description; }
}
