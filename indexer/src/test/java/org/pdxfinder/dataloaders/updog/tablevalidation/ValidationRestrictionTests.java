package org.pdxfinder.dataloaders.updog.tablevalidation;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ValidationRestrictionTests {


    @Test public void UrlSafeRegex_worksAppropriately(){
        String expectedFail = "TEST";
        String expectedFail2 = ".T-E S_T~";
        String expectedPass = "/T*J+*^|#@";
        String expectedPass2 = "0930934.323";
        String regex = ValueRestrictions.URL_SAFE().getRegex();

        Predicate<String> urlSafeRegex = Pattern.compile(regex)
                .asPredicate()
                .negate();

        Assert.assertFalse(urlSafeRegex.test(expectedFail));
        Assert.assertFalse(urlSafeRegex.test(expectedFail2));
        Assert.assertTrue(urlSafeRegex.test(expectedPass));
        Assert.assertTrue(urlSafeRegex.test(expectedPass2));
    }

    @Test public void FreeTextRegex_worksAppropriately(){
        String expectedFail = "TEST";
        String expectedFail2 = "..,:;-ST";
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



}
