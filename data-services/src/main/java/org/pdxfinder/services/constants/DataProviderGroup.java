package org.pdxfinder.services.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public enum DataProviderGroup {
    All,
    EurOPDX,
    PDXNet,
    UPDOG;


    public static List<DataProvider> getProvidersFrom(DataProviderGroup group) {

        EnumMap<DataProviderGroup, List<DataProvider>> map = new EnumMap<>(DataProviderGroup.class);
        map.put(DataProviderGroup.All, Arrays.asList(DataProvider.values()));
        map.put(DataProviderGroup.EurOPDX, Arrays.asList(
                DataProvider.Curie_BC,
                DataProvider.Curie_LC,
                DataProvider.Curie_OC,
                DataProvider.IRCC_CRC,
                DataProvider.IRCC_GC,
                DataProvider.TRACE,
                DataProvider.UOC_BC,
                DataProvider.UOM_BC,
                DataProvider.VHIO_BC,
                DataProvider.VHIO_CRC,
                DataProvider.NKI,
                DataProvider.UMCG,
                DataProvider.VHIO_PC,
                DataProvider.LIH
        ));

        map.put(DataProviderGroup.UPDOG, Arrays.asList(
                DataProvider.IRCC_GC,
                DataProvider.IRCC_CRC,
                DataProvider.CRL,
                DataProvider.TRACE,
                DataProvider.Curie_BC,
                DataProvider.Curie_LC,
                DataProvider.Curie_OC,
                DataProvider.PMLB,
                DataProvider.UOC_BC,
                DataProvider.UOM_BC,
                DataProvider.VHIO_BC,
                DataProvider.VHIO_CRC,
                DataProvider.DFCI_CPDM,
                DataProvider.NKI,
                DataProvider.PDMR,
                DataProvider.JAX,
                DataProvider.SJCRH,
                DataProvider.UMCG,
                DataProvider.VHIO_PC,
                DataProvider.LIH,
                DataProvider.PPTC,
                DataProvider.WUSTL,
                DataProvider.MDAnderson,
                DataProvider.Wistar_MDAnderson_Penn,
                DataProvider.HCI_BCM
        ));

        map.put(DataProviderGroup.PDXNet, Arrays.asList(
                DataProvider.WUSTL,
                DataProvider.MDAnderson,
                DataProvider.Wistar_MDAnderson_Penn,
                DataProvider.HCI_BCM
        ));

        return map.get(group);
    }

}