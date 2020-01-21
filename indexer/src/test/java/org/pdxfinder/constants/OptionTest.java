package org.pdxfinder.constants;

import org.junit.Test;
import org.pdxfinder.BaseTest;

import static org.mockito.Matchers.isA;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

public class OptionTest extends BaseTest {


    private final static String ASSERTION_ERROR = "Unknown Option Command Found: ";


    @Test
    public void given_CommandOptionEnumCount_EnsureListIntegrity() {

        final int NUM_OPTIONS = 8;

        assertEquals(NUM_OPTIONS, Option.values().length);
    }


    @Test
    public void given_Option_When_EnumListIsChanged_Then_Error() {

        boolean expected = true;
        String message = "";

        for (Option option : Option.values()) {

            switch (option) {
                case loadMarkers:
                    break;
                case reloadCache:
                    break;
                case loadALL:
                    break;
                case loadEssentials:
                    break;
                case loadNCIT:
                    break;
                case loadNCITPreDef:
                    break;
                case loadNCITDrugs:
                    break;
                case loadSlim:
                    break;
                default:
                    message = String.format("%s %s", ASSERTION_ERROR, option);
                    expected = false;
            }

            assertTrue(message, expected);
        }


    }

}

