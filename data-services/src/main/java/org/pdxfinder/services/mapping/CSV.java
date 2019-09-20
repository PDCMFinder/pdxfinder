package org.pdxfinder.services.mapping;

/*
 * Created by abayomi on 22/08/2019.
 */
public enum CSV {

    // Diagnosis csv Header Strings
    entityId("Data Id"),
    dataSource("Data Source"),
    sampleDiagnosis("Sample Diagnosis"),
    originTissue("Origin Tissue"),
    tumorType("Tumor Type"),

    // Treatment csv Header Strings
    treatmentName("Treatment Name"),

    // CSV Header Strings common to all entity types
    mappedTerm("Mapped Term"),
    mappedTermUrl("Mapped Term URL"),
    mapType("Type"),
    justification("Justification"),
    decision("Decision (Yes or No)"),
    validTerm("Valid Term"),


    yes("yes"),
    no("no");



    private String value;

    private CSV(String val) {
        value = val;
    }

    public String get() {
        return value;
    }


}

