package org.pdxfinder.services.constants;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public enum DataProviderGroup {
    All,
    EurOPDX,
    UPDOG,
    CustomLoaders;


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
                DataProvider.LIH,
                DataProvider.PPTC
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
                DataProvider.JAX_XDOG,
                DataProvider.SJCRH,
                DataProvider.UMCG,
                DataProvider.VHIO_PC,
                DataProvider.LIH,
                DataProvider.PPTC
        ));

        map.put(DataProviderGroup.CustomLoaders, Arrays.asList(
                DataProvider.JAX,
                DataProvider.PDXNet_MDAnderson,
                DataProvider.PDXNet_HCI_BCM,
                DataProvider.PDXNet_Wistar_MDAnderson_Penn,
                DataProvider.PDXNet_WUSTL
        ));

        return map.get(group);
    }

}