package org.pdxfinder.dataloaders.updog.tablevalidation.rules;

import java.util.Arrays;
import org.pdxfinder.dataloaders.updog.tablevalidation.ValueRestrictions;

public class PdxValueRestrictions {

  static final String NOTCOLLECTED = "not collected";
  static final String NOTPROVIDED = "not provided";

  private PdxValueRestrictions(){}

  private static final ValueRestrictions FREE_TEXT_CHARSET = ValueRestrictions.of(
      "^[\\p{Alpha}\\p{Space}\\p{Digit}().',<>%:;_\\/-]+$",
      "have characters not contained in US ASCII Alphanumeric and ().',:;-/"
  );

  private static final ValueRestrictions URL_SAFE_CHARSET = ValueRestrictions.of(
      "^[\\p{Alpha}\\p{Digit}\\p{Space}._~-]+$",
      "have characters not contained in US ASCII Alphanumeric and ._~-"
  );

  private static final ValueRestrictions NUMERICAL_CHARSET = ValueRestrictions.of(
      "^[\\p{Digit}\\p{Space}pP\\.,-]+$",
      "have characters not contains in US ASCII numbers and pP-.,"
  );

  private static final ValueRestrictions COLLECTION_EVENT_FORMAT = ValueRestrictions.of(
      "(?i)^collection event [0-9]{1,3}",
      "Not of type: collection event [0-9]"
  );

  private static final ValueRestrictions COLLECTION_DATE_FORMAT = ValueRestrictions.of(
          "[A-Za-z]{3} [0-9]{4}",
          "not of type: [MMM YYYY] three letter month and 4 digit year"
  );

  private static final ValueRestrictions PMID_FORMAT = ValueRestrictions.of(
      "(?i)^(?:pmid:\\s?[0-9]{8},?\\s?)*$",
      "not of type: PMID: [8 digit id] (comma separated)"
  );


  private static final ValueRestrictions SEX_CATEGORIES = ValueRestrictions.of(
      Arrays.asList(
      "male",
      "female",
      "other",
      getNOTCOLLECTED(),
      getNOTPROVIDED()
  ));

  private static final ValueRestrictions ETHNICITY_ASSESSMENT_CATEGORIES = ValueRestrictions.of(
      "self-assessed",
"genetic"
  );

  private static final ValueRestrictions TUMOUR_TYPE_CATEGORIES = ValueRestrictions.of(
      Arrays.asList(
          "primary",
          "metastatic",
          "recurrent",
          "refactory",
          getNOTCOLLECTED(),
          getNOTPROVIDED()
      ));

  private static final ValueRestrictions SHARE_CATEGORIES = ValueRestrictions.of(
      Arrays.asList(
      "yes",
      "no",
      getNOTPROVIDED()
  ));

  private static final ValueRestrictions TREATMENT_NAIVE_AT_COLLECTION_CATEGORIES = ValueRestrictions.of(
      Arrays.asList(
      "treatment naive" ,
      "not treatment naive",
      getNOTCOLLECTED(),
      getNOTPROVIDED()
  ));

  private static final ValueRestrictions priorTreatmentCategories = ValueRestrictions.of(
      Arrays.asList(
          "yes",
          "no",
          getNOTPROVIDED(),
          getNOTCOLLECTED()
      ));

  private static final ValueRestrictions providerTypeCategories =  ValueRestrictions.of(
      Arrays.asList(
          "academia",
          "industry",
          "academia and industry",
          "CRO",
          "pharma"
      ));

  public static String getNOTCOLLECTED() {
    return NOTCOLLECTED;
  }

  public static String getNOTPROVIDED() {
    return NOTPROVIDED;
  }

  public static ValueRestrictions getSexCategories() {
    return SEX_CATEGORIES;
  }
  public static ValueRestrictions getEthnicityAssessmentCategories() {
    return ETHNICITY_ASSESSMENT_CATEGORIES;
  }

  public static ValueRestrictions getTumourTypeCategories() {
    return TUMOUR_TYPE_CATEGORIES;
  }

  public static ValueRestrictions getShareCategories() {
    return SHARE_CATEGORIES;
  }

  public static ValueRestrictions getTreatmentNaiveAtCollectionCategories() {
    return TREATMENT_NAIVE_AT_COLLECTION_CATEGORIES;
  }

  public static ValueRestrictions getPriorTreatmentCategories() {
    return priorTreatmentCategories;
  }

  public static ValueRestrictions getProviderTypeCategories() {
    return providerTypeCategories;
  }

  public static ValueRestrictions getFreeTextCharset() {
    return FREE_TEXT_CHARSET;
  }

  public static ValueRestrictions getUrlSafeCharset() {
    return URL_SAFE_CHARSET;
  }

  public static ValueRestrictions getCollectionEventFormat() {
    return COLLECTION_EVENT_FORMAT;
  }

  public static ValueRestrictions getCollectionDateFormat() {
    return COLLECTION_DATE_FORMAT;
  }

  public static ValueRestrictions getPmidFormat() {
    return PMID_FORMAT;
  }

  public static ValueRestrictions getNumericalCharset() {
    return NUMERICAL_CHARSET;
  }
}
