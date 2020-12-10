package org.pdxfinder.dataloaders.updog.tablevalidation;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ValidationRestrictionTests {


    @Test public void UrlSafeRegex_worksAppropriately(){
        String expectedFail = "TEST99";
        String expectedFail2 = " ";
        String expectedFail3 = ".T90-E S_T~";
        String expectedPass = "/T*J+*^|#@";
        String regex = ValueRestrictions.URL_SAFE().getRegex();

        Predicate<String> urlSafeRegex = Pattern.compile(regex)
                .asPredicate()
                .negate();

        Assert.assertFalse(urlSafeRegex.test(expectedFail));
        Assert.assertFalse(urlSafeRegex.test(expectedFail2));
        Assert.assertFalse(urlSafeRegex.test(expectedFail3));
        Assert.assertTrue(urlSafeRegex.test(expectedPass));
    }

    @Test public void FreeTextRegex_worksAppropriately(){
        String expectedFail = "TEST";
        String expectedFail2 = ".', ST";
        String expectedPass = "/T*J+*^|#@";
        String expectedPass2 = "0930934.323";
        String regex = ValueRestrictions.FREE_TEXT().getRegex();

        Predicate<String> FreeTextRegex = Pattern.compile(regex)
                .asPredicate()
                .negate();

        Assert.assertFalse(FreeTextRegex.test(expectedFail));
        Assert.assertFalse(FreeTextRegex.test(expectedFail2));
        Assert.assertTrue(FreeTextRegex.test(expectedPass));
        Assert.assertTrue(FreeTextRegex.test(expectedPass2));
    }

    @Test public void numberRegex_worksAppropriately(){
        String expectedFail = "0930934.323";
        String expectedPass1 = "92TEST";
        String expectedPass2 = "..,9:;_~8-S_T~";
        String expectedPass3 = "/T*J+8*^|#@";
        String number = ValueRestrictions.NUMBER().getRegex();

        Predicate<String> numberRegex = Pattern.compile(number)
                .asPredicate()
                .negate();

        Assert.assertFalse(numberRegex.test(expectedFail));
        Assert.assertTrue(numberRegex.test(expectedPass1));
        Assert.assertTrue(numberRegex.test(expectedPass2));
        Assert.assertTrue(numberRegex.test(expectedPass3));
    }

    @Test public void ListToRegex_givenList_regexHasDiscreteCategories(){
        List<String> categories = Arrays.asList("a", "test", "for", "regex", "categories");
        String expectedfail = "test";
        String expectedfail2 = "regex";
        String expectedpass = "atestfor";
        String expectedpass2 = " regex ";
        String expectedpass3 = "pd'x fi;nder !!?!?!";
        String valueRestrictions = ValueRestrictions.of(categories, "regex test").getRegex();
        Predicate<String> restrictionPredicate = Pattern.compile(valueRestrictions)
                .asPredicate()
                .negate();
        System.out.println(valueRestrictions);
        Assert.assertFalse(restrictionPredicate.test(expectedfail));
        Assert.assertFalse(restrictionPredicate.test(expectedfail2));
        Assert.assertTrue(restrictionPredicate.test(expectedpass));
        Assert.assertTrue(restrictionPredicate.test(expectedpass2));
        Assert.assertTrue(restrictionPredicate.test(expectedpass3));
    }
}
