package org.pdxfinder.services.constants;

public enum MolCharTable {

    SAMPLE_ID               ("sampleid", "Sample Id"),
    HGNC_SYMBOL             ("hgncsymbol", "HGNC Symbol"),
    AMINOACID_CHANGE        ("aminoacidchange", "Amino Acid Change"),
    CONSEQUENCE             ("consequence", "Consequence"),
    NUCLEOTIDE_CHANGE       ("nucleotidechange", "Nucleotide Change"),
    READ_DEPTH              ("readdepth", "Read Depth"),
    ALLELE_FREQUENCY        ("allelefrequency", "Allele Frequency"),
    PROBEID_AFFYMETRIX      ("probeidaffymetrix", "Probe Id Affymetrix"),
    CNALOG10_RCNA           ("cnalog10rcna", "Log10 Rcna"),
    CNALOG2_RCNA            ("cnalog2rcna", "Log2 Rcna"),
    CNA_COPYNUMBER_STATUS   ("cnacopynumberstatus", "Copy Number Status"),
    CNA_GISTICVALUES        ("cnagisticvalues", "Gistic Value"),
    CHROMOSOME              ("chromosome", "Chromosome"),
    SEQ_STARTPOSITION       ("seqstartposition", "Seq. Start Position"),
    SEQ_ENDPOSITION         ("seqendposition", "Seq. End Position"),
    REFALLELE               ("refallele", "Ref. Allele"),
    ALTALLELE               ("altallele", "Alt Allele"),
    RS_IDVARIANTS           ("rsidvariants", "Rs Id Variant"),
    ENSEMBL_TRANSCRIPTID    ("ensembltranscriptid", "Ensembl Transcript Id"),
    ENSEMBL_GENEID          ("ensemblgeneid", "Ensembl Gene Id"),
    UCSC_GENEID             ("ucscgeneid", "Ucsc Gene Id"),
    NCBI_GENEID             ("ncbigeneid", "Ncbi Gene Id"),
    ZSCORE                  ("zscore", "Z-Score"),
    GENOME_ASSEMBLY         ("genomeassembly", "Genome Assembly"),
    CYTOGENETICS_RESULT     ("cytogeneticsresult", "Result");

    private final String key;
    private final String col;

    MolCharTable(String key, String col) {
        this.key = key;
        this.col = col;
    }

    public String key() {
        return key;
    }

    public String col() {
        return col;
    }

}
