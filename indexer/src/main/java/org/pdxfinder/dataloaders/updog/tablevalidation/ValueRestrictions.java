package org.pdxfinder.dataloaders.updog.tablevalidation;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ValueRestrictions {

    private Predicate<String> predicate;
    private String errorDescription;
    private boolean canBeEmpty = false;

    private ValueRestrictions(String regex, String description){
        this.predicate = regexToPredicate(regex);
        this.errorDescription = description;
    }

    private ValueRestrictions(Predicate<String> predicate, String errorDescription){
        this.predicate = predicate;
        this.errorDescription = errorDescription;
    }

    static public ValueRestrictions of(String regexCharset, String charSetDescription){
        return new ValueRestrictions(regexCharset, charSetDescription);
    }

    static public ValueRestrictions of(List<String> categories){
        String errorDescription = String.format("not in a required category. "
                + "Required Categories: [%s] Value found", String.join(",", categories));
        return new ValueRestrictions(listToCaseInsensitivePredicate(categories), errorDescription);
    }

    static private Predicate<String> listToCaseInsensitivePredicate(List<String> categories){
       String orRegex = String.join("|", categories);
       String builtRegex = anchoredNoGroupingCaseInsensitiveRegex(orRegex);
       return regexToPredicate(builtRegex);
    }

    static private String anchoredNoGroupingCaseInsensitiveRegex(String orRegex){
        return String.format("(?i)^(?:%s)$", orRegex);
    }

    static private Predicate<String> regexToPredicate(String regex){
        return Pattern.compile(regex)
            .asPredicate();
    }

    public ValueRestrictions canBeEmpty() {
        canBeEmpty = true;
        return this;
    }

    public Predicate<String> getEmptyFilter(){
        Predicate<String> emptyFilter = String::isEmpty;
        return (canBeEmpty) ? emptyFilter.negate() : emptyFilter;
    }

    public Predicate<String> getInvalidValuePredicate() { return predicate.negate(); }

    public String getErrorDescription() { return errorDescription; }
}
