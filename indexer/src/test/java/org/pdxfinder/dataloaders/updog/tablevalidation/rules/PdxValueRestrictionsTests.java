package org.pdxfinder.dataloaders.updog.tablevalidation.rules;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.junit.Assert;
import org.junit.Test;
import org.pdxfinder.dataloaders.updog.tablevalidation.ValueRestrictions;

public class PdxValueRestrictionsTests {

  @Test
  public void UrlSafeRegex_worksAppropriately(){
    String expectedFail = "TEST99";
    String expectedFail2 = " ";
    String expectedFail3 = ".T90-E S_T~";
    String expectedPass = "/T*J+*^|#@";

    Predicate<String> urlSafeRegex = PdxValueRestrictions.getUrlSafeCharset()
        .getInvalidValuePredicate();

    Assert.assertFalse(urlSafeRegex.test(expectedFail));
    Assert.assertFalse(urlSafeRegex.test(expectedFail2));
    Assert.assertFalse(urlSafeRegex.test(expectedFail3));
    Assert.assertTrue(urlSafeRegex.test(expectedPass));
  }

  @Test
  public void given_BlankString_Then_haveDefinedBehavior(){
    String blank = "";

    Predicate<String> urlSafeRegex = PdxValueRestrictions.getUrlSafeCharset()
        .getInvalidValuePredicate();

    Predicate<String> FreeTextRegex = PdxValueRestrictions.getFreeTextCharset()
        .getInvalidValuePredicate();

    Assert.assertTrue(urlSafeRegex.test(blank));
    Assert.assertTrue(FreeTextRegex.test(blank));
  }

  @Test public void FreeTextRegex_worksAppropriately(){
    String expectedFail = "TEST";
    String expectedFail2 = ".', ST";
    String expectedPass = "/T*J+*^|#@";

    Predicate<String> FreeTextRegex = PdxValueRestrictions.getFreeTextCharset()
        .getInvalidValuePredicate();

    Assert.assertFalse(FreeTextRegex.test(expectedFail));
    Assert.assertFalse(FreeTextRegex.test(expectedFail2));
    Assert.assertTrue(FreeTextRegex.test(expectedPass));
  }

  @Test public void numberRegex_worksAppropriately(){
    String expectedFail = "0930934.323";
    String expectedFail2 = "10-20";
    String expectedFail3 = "10,20,30,40";
    String expectedPass1 = "92TEST";
    String expectedPass2 = "..,9:;_~8-S_T~";
    String expectedPass3 = "/T*J+8*^|#@";

    Predicate<String> numberRegex = PdxValueRestrictions.getNumericalCharset()
        .getInvalidValuePredicate();

    Assert.assertFalse(numberRegex.test(expectedFail));
    Assert.assertFalse(numberRegex.test(expectedFail2));
    Assert.assertFalse(numberRegex.test(expectedFail3));
    Assert.assertTrue(numberRegex.test(expectedPass1));
    Assert.assertTrue(numberRegex.test(expectedPass2));
    Assert.assertTrue(numberRegex.test(expectedPass3));
  }


  @Test public void ListToRegex_givenList_regexHasDiscreteCategories(){
    List<String> categories = Arrays
        .asList("a", "test", "for", "regex", "categories", "value with space");
    String expectedfail = "test";
    String expectedfail2 = "value with space";
    String expectedpass = "atestfor";
    String expectedpass2 = " regex ";
    String expectedpass3 = "pd'x fi;nder !!?!?!";
    Predicate<String> categoriesPredicate = ValueRestrictions
        .of(categories)
        .getInvalidValuePredicate();

    Assert.assertFalse(categoriesPredicate.test(expectedfail));
    Assert.assertFalse(categoriesPredicate.test(expectedfail2));
    Assert.assertTrue(categoriesPredicate.test(expectedpass));
    Assert.assertTrue(categoriesPredicate.test(expectedpass2));
    Assert.assertTrue(categoriesPredicate.test(expectedpass3));
  }
}

