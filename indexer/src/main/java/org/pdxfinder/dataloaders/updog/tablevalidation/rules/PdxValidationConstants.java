package org.pdxfinder.dataloaders.updog.tablevalidation.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.ValueRestrictions;

public class PdxValidationConstants {

  private static final ValueRestrictions FREE_TEXT_CHARSET = ValueRestrictions.of(
      "^[\\p{Alpha}\\p{Space}\\p{Digit}().',<>%:;_\\/-]+$",
      "have characters not contained in US ASCII Alphanumeric and ().',:;-/"
  );

  private static final ValueRestrictions URL_SAFE_CHARSET = ValueRestrictions.of(
      "^[\\p{Alpha}\\p{Digit}\\p{Space}._~-]+$",
      "have characters not contained in US ASCII Alphanumeric and ._~-"
  );

  private static final ValueRestrictions NUMERICAL_CHARSET = ValueRestrictions.of(
      "^[\\pP{Digit}-.,]+$",
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
      "not of type: PMID: [8 digit id] (comma separated)")
      .canBeEmpty();


  static public Set<ColumnReference> getMetadataColumns(){
  Set<ColumnReference> tableColumns = new HashSet<>();
    Arrays.asList(
        "patient_id",
        "sex",
        "history",
        "ethnicity",
        "ethnicity_assessment_method",
        "initial_diagnosis",
        "age_at_initial_diagnosis"
    ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-patient.tsv", s)));
    Arrays.asList(
        "patient_id",
        "sample_id",
        "collection_date",
        "collection_event",
        "months_since_collection_1",
        "age_in_years_at_collection",
        "diagnosis",
        "tumour_type",
        "primary_site",
        "collection_site",
        "stage",
        "staging_system",
        "grade",
        "grading_system",
        "virology_status",
        "sharable",
        "treatment_naive_at_collection",
        "treated",
        "prior_treatment",
        "model_id"
    ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-sample.tsv", s)));
    Arrays.asList(
        "model_id",
        "host_strain",
        "host_strain_full",
        "engraftment_site",
        "engraftment_type",
        "sample_type",
        "sample_state",
        "passage_number",
        "publications"
    ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-model.tsv", s)));
    Arrays.asList(
        "validation_technique",
        "description",
        "passages_tested",
        "validation_host_strain_full"
    ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-model_validation.tsv", s)));
    Arrays.asList(
        "model_id",
        "provider_type",
        "accessibility",
        "europdx_access_modality",
        "email",
        "name",
        "form_url",
        "database_url",
        "provider_name",
        "provider_abbreviation",
        "project"
    ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-sharing.tsv", s)));
    Arrays.asList(
        "name",
        "abbreviation",
        "internal_url",
        "internal_dosing_url"
    ).forEach(s -> tableColumns.add(ColumnReference.of("metadata-loader.tsv", s)));
    return tableColumns;
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
