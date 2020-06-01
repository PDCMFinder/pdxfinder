package org.pdxfinder.services.constants;

import org.junit.Test;
import org.pdxfinder.BaseTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataUrlTest extends BaseTest {


    private final static String ASSERTION_ERROR = "Unknown Data Url Found: ";


    @Test
    public void given_DataUrlEnumListCount_EnsureListIntegrity() {

        final int DATA_URL_COUNT = 5;

        assertEquals(DATA_URL_COUNT, DataUrl.values().length);
    }


    @Test
    public void given_DataUrl_When_EnumListIsChanged_Then_Error() {

        boolean expected = true;
        String message = "";

        for (DataUrl option : DataUrl.values()) {

            switch (option) {
                case HUGO_FILE_URL:
                    break;
                case DISEASES_BRANCH_URL:
                    break;
                case ONTOLOGY_URL:
                    break;
                case EUROPE_PMC_URL:
                    break;
                case K8_SERVICE_URL:
                    break;
                default:
                    message = String.format("%s %s", ASSERTION_ERROR, option);
                    expected = false;
            }

            assertTrue(message, expected);
        }

    }
}
