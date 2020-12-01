package org.pdxfinder.dataloaders.updog.tablevalidation;

public class ValueRestrictions {

    //ascii punctuation characters are !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
    //URL restriction is for alphanumeric and _-.¬
    private static final String FREE_TEXT = "[^\\p{Alpha}.,;:?!]+";
    private static final String FREE_TEXT_DESCRIPTION = "US Alphabet and .,:;?!";
    private static final String URL_SAFE = "[^\\p{Alpha]._~- ]";
    private static final String URL_SAFE_DESCRIPTION = "US Alphabet and ._~-";
    private static final String NUMBER = "[^0-9.,-]+";
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

    public String getRegex() { return regex; }

    public String getDescription() { return description; }
}
