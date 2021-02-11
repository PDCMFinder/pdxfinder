package org.pdxfinder.services.constants;

public enum DataProvider {

    Test_Minimal("Test-Minimal"),
    HCI_BCM("HCI-BCM"),
    IRCC_CRC("IRCC-CRC"),
    JAX("JAX"),
    MDAnderson("MDAnderson"),
    PDMR("PDMR"),
    Wistar_MDAnderson_Penn("Wistar-MDAnderson-Penn"),
    WUSTL("WUSTL"),
    CRL("CRL"),
    Curie_BC("Curie-BC"),
    Curie_LC("Curie-LC"),
    Curie_OC("Curie-OC"),
    IRCC_GC("IRCC-GC"),
    PMLB("PMLB"),
    TRACE("TRACE"),
    UOC_BC("UOC-BC"),
    UOM_BC("UOM-BC"),
    VHIO_BC("VHIO-BC"),
    VHIO_CRC("VHIO-CRC"),
    DFCI_CPDM("DFCI-CPDM"),
    NKI("NKI"),
    SJCRH("SJCRH"),
    UMCG("UMCG"),
    VHIO_PC("VHIO-PC"),
    LIH("LIH"),
    PPTC("PPTC");

    private String name;
    DataProvider(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
