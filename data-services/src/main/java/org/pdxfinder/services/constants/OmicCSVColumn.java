package org.pdxfinder.services.constants;

public enum OmicCSVColumn {

    DATASOURCE("datasource"),
    MODEL_ID("Model_ID"),
    SAMPLE_ID("Sample_ID"),
    SAMPLE_ORIGIN("sample_origin"),
    PASSAGE("Passage"),
    HOST_STRAIN_NAME("host_strain_name"),
    HGNC_SYMBOL("hgnc_symbol"),
    AMINO_ACID_CHANGE("amino_acid_change"),
    FUNCTIONAL_PREDICTION("functional_prediction"),
    NUCLEOTIDE_CHANGE("nucleotide_change"),
    CODING_SEQUENCE_CHANGE("coding_sequence_change"),
    CONSEQUENCE("consequence"),
    READ_DEPTH("read_depth"),
    ALLELE_FREQUENCY("Allele_frequency"),
    CHROMOSOME("chromosome"),
    SEQ_START_POSITION("seq_start_position"),
    REF_ALLELE("ref_allele"),
    VARIANT_ID("variation_id"),
    ALT_ALLELE("alt_allele"),
    UCSC_GENE_ID("ucsc_gene_id"),
    NCBI_GENE_ID("ncbi_gene_id"),
    ENSEMBL_GENE_ID("ensembl_gene_id"),
    ENSEMBL_TRANSCRIPT_ID("ensembl_transcript_id"),
    RS_ID_VARIANT("rs_id_Variant"),
    GENOME_ASSEMBLY("genome_assembly"),
    PLATFORM("Platform");


    private String value;

    private OmicCSVColumn(String val) {
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
