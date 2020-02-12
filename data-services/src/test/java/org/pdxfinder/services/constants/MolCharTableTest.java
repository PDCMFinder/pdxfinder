package org.pdxfinder.services.constants;

import org.junit.Test;
import org.pdxfinder.BaseTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MolCharTableTest extends BaseTest {


    private final static String ASSERTION_ERROR = "Unknown Molecular Data Table Column Found: ";


    @Test
    public void given_MolcharTableEnumListCount_EnsureListIntegrity() {

        final int DATA_URL_COUNT = 25;

        assertEquals(DATA_URL_COUNT, MolCharTable.values().length);
    }


    @Test
    public void given_MolcharTable_When_EnumListIsChanged_Then_Error() {

        boolean expected = true;
        String message = "";

        for (MolCharTable option : MolCharTable.values()) {

            switch (option) {
                case SAMPLE_ID:
                    break;
                case HGNC_SYMBOL:
                    break;
                case AMINOACID_CHANGE:
                    break;
                case CONSEQUENCE:
                    break;
                case NUCLEOTIDE_CHANGE:
                    break;
                case READ_DEPTH:
                    break;
                case ALLELE_FREQUENCY:
                    break;
                case PROBEID_AFFYMETRIX:
                    break;
                case CNALOG10_RCNA:
                    break;
                case CNALOG2_RCNA:
                    break;
                case CNA_COPYNUMBER_STATUS:
                    break;
                case CNA_GISTICVALUES:
                    break;
                case CHROMOSOME:
                    break;
                case SEQ_STARTPOSITION:
                    break;
                case SEQ_ENDPOSITION:
                    break;
                case REFALLELE:
                    break;
                case ALTALLELE:
                    break;
                case RS_IDVARIANTS:
                    break;
                case ENSEMBL_TRANSCRIPTID:
                    break;
                case ENSEMBL_GENEID:
                    break;
                case UCSC_GENEID:
                    break;
                case NCBI_GENEID:
                    break;
                case ZSCORE:
                    break;
                case GENOME_ASSEMBLY:
                    break;
                case CYTOGENETICS_RESULT:
                    break;
                default:
                    message = String.format("%s %s", ASSERTION_ERROR, option);
                    expected = false;
            }

            assertTrue(message, expected);
        }

    }

}
