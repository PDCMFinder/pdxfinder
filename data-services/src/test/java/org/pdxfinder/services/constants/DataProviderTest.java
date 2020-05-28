package org.pdxfinder.services.constants;

import org.junit.Test;
import org.pdxfinder.BaseTest;

import static org.junit.Assert.assertTrue;

public class DataProviderTest extends BaseTest {

    private final static String ASSERTION_ERROR = "Unknown Data Provider Found: ";

    @Test
    public void given_DataProviders_When_EnumListIsChanged_Then_Error() {

        boolean expected = true;
        String message = "";

        for (DataProvider option : DataProvider.values()) {

            switch (option) {
                case Test_Minimal:
                    break;
                case PDXNet_HCI_BCM:
                    break;
                case IRCC_CRC:
                    break;
                case JAX:
                    break;
                case PDXNet_MDAnderson:
                    break;
                case PDMR:
                    break;
                case PDXNet_Wistar_MDAnderson_Penn:
                    break;
                case PDXNet_WUSTL:
                    break;
                case CRL:
                    break;
                case Curie_BC:
                    break;
                case Curie_LC:
                    break;
                case Curie_OC:
                    break;
                case IRCC_GC:
                    break;
                case PMLB:
                    break;
                case TRACE:
                    break;
                case UOC_BC:
                    break;
                case UOM_BC:
                    break;
                case VHIO_BC:
                    break;
                case VHIO_CRC:
                    break;
                case DFCI_CPDM:
                    break;
                case NKI:
                    break;
                case PDMR_XDOG:
                    break;
                case JAX_XDOG:
                    break;
                case SJCRH:
                    break;
                default:
                    message = String.format("%s %s", ASSERTION_ERROR, option);
                    expected = false;
            }

            assertTrue(message, expected);
        }

    }

}
