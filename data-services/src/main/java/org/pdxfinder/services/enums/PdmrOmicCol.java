package org.pdxfinder.services.enums;

public enum PdmrOmicCol {

    PATIENT_ID("Patient ID"),
    SPECIMEN_ID("Specimen ID"),
    PDM_TYPE("PDM Type"),
    PDM_TYPE_PATENT("Patient/Originator Specimen"),
    Sample_ID("Sample ID"),
    PDM_Type("PDM Type"),
    Gene("Gene"),
    AA_Change("AA Change(canonical transcript)"),
    CodonChange("CodonChange"),
    Impact("Impact"),
    ReadDepth("ReadDepth"),
    AlleleFrequency("AlleleFrequency"),
    Chr("Chr"),
    REF_ALLELE("REF(hg19)"),
    alt_allele("ALT(tumor)"),
    dbSNP_ID("dbSNP ID"),
    Position("Position");


    private String value;

    private PdmrOmicCol(String val) {
        value = val;
    }

    public String get() {
        return value;
    }
}
