package org.pdxfinder.services.constants;

public enum DataProvider {

    Test_Minimal("Test-Minimal"),
    PDXNet_HCI_BCM("PDXNet-HCI-BCM"),
    IRCC_CRC("IRCC-CRC"),
    JAX("JAX"),
    PDXNet_MDAnderson("PDXNet-MDAnderson"),
    PDMR("PDMR"),
    PDXNet_Wistar_MDAnderson_Penn("PDXNet-Wistar-MDAnderson-Penn"),
    PDXNet_WUSTL("PDXNet-WUSTL"),
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
    JAX_XDOG("JAX-XDOG"),
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
