package org.pdxfinder.services.enums;

public enum PdmrOmicCol {

    PATIENT_ID("Patient ID"),
    SPECIMEN_ID("Specimen ID"),
    PDM_TYPE("PDM Type"),
    PDM_TYPE_PATENT("Patient/Originator Specimen"),
    SAMPLE_ID("Sample ID"),
    GENE("Gene"),
    AA_CHANGE("AA Change(canonical transcript)"),
    CODON_CHANGE("CodonChange"),
    IMPACT("Impact"),
    READ_DEPTH("ReadDepth"),
    ALLELE_FREQUENCY("AlleleFrequency"),
    CHR("Chr"),
    REF_ALLELE("REF(hg19)"),
    ALT_ALLELE("ALT(tumor)"),
    DB_SNP_ID("dbSNP ID"),
    POSITION("Position");


    private String value;

    private PdmrOmicCol(String val) {
        value = val;
    }

    public String get() {
        return value;
    }

    @Override
    public String toString() {
        return this.get();
    }
}
