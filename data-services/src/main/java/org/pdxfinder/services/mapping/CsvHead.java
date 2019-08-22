package org.pdxfinder.services.mapping;

/*
 * Created by abayomi on 22/08/2019.
 */
public enum CsvHead {


    entityId("Data Id"),
    dataSource("Data Source"),
    sampleDiagnosis("Sample Diagnosis"),
    originTissue("Origin Tissue"),
    tumorType("Tumor Type"),

    mappedTerm("Mapped Term"),
    mapType("Type"),
    justification("Justification"),
    yesOrNo("Decision (Yes or No)"),
    validTerm("Valid Term");

    private String value;

    private CsvHead(String val) {
        value = val;
    }

    public String get() {
        return value;
    }


}

