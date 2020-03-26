package org.pdxfinder.services.enums;

public enum OmicCSVColumn {


    DATASOURCE("datasource"),                           // Data Source Column
    MODEL_ID("Model_ID"),                               // Model Id Column
    SAMPLE_ID("Sample_ID"),                             // Sample Id Column
    SAMPLE_ORIGIN("sample_origin"),                     // Sample Origin Column
    PASSAGE("Passage"),                                 // Passage Column
    HOST_STRAIN_NAME("host_strain_name"),               // Host Strain name Column
    HGNC_SYMBOL("hgnc_symbol"),                         // HGNC Symbol Column
    AMINO_ACID_CHANGE("amino_acid_change"),             // Amino Acid Change Column
    FUNCTIONAL_PREDICTION("functional_prediction"),     // functional_prediction
    NUCLEOTIDE_CHANGE("nucleotide_change"),             // Nucleotide Change Column
    CODING_SEQUENCE_CHANGE("coding_sequence_change"),   // coding_sequence_change
    CONSEQUENCE("consequence"),                         // Consequence Column
    READ_DEPTH("read_depth"),                           // Read Depth Column
    ALLELE_FREQUENCY("Allele_frequency"),               // Allele Frequency Column
    CHROMOSOME("chromosome"),                           // Chromosome Column
    SEQ_START_POSITION("seq_start_position"),           // Seq Start Position Column
    REF_ALLELE("ref_allele"),                           // Ref Allele Column
    VARIANT_ID("variation_id"),                         // variation_id Column
    ALT_ALLELE("alt_allele"),                           // Alt Allele Column
    UCSC_GENE_ID("ucsc_gene_id"),                       // UCSC Gene Id Column
    NCBI_GENE_ID("ncbi_gene_id"),                       // NCBI Gene Id Column
    ENSEMBL_GENE_ID("ensembl_gene_id"),                 // Ensembl Gene Id Column
    ENSEMBL_TRANSCRIPT_ID("ensembl_transcript_id"),     // Ensembl Transcript ID Column
    RS_ID_VARIANT("rs_id_Variant"),                     // RS ID Variant Column
    GENOME_ASSEMBLY("genome_assembly"),                 // Genome Assembly Column
    PLATFORM("Platform");                               // Platform Column


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
