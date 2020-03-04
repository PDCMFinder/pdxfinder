package org.pdxfinder.utils;

import org.pdxfinder.dataloaders.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataProviders {

    /*
    public DataProviders() {}



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
                DataProvider.VHIO_CRC
        ));
        map.put(DataProviderGroup.UPDOG, Arrays.asList(
                DataProvider.IRCC_GC,
                DataProvider.CRL,
                DataProvider.TRACE,
                DataProvider.Curie_BC,
                DataProvider.Curie_LC,
                DataProvider.Curie_OC,
                DataProvider.PMLB,
                DataProvider.UOC_BC,
                DataProvider.UOM_BC,
                DataProvider.VHIO_BC,
                DataProvider.VHIO_CRC
        ));
        return map.get(group);
    }

    public enum DataProvider {
        Test_Minimal,
        PDXNet_HCI_BCM {
            @Override
            public void load() {
                LoadHCI loadHCI = (LoadHCI) applicationContext.getBean("loadHCI");
                try { loadHCI.run(); }
                catch (Exception e) { log.error("{}", e); }
            }},
        IRCC_CRC {
            @Override
            public void load() {
                LoadIRCC loadIRCC = (LoadIRCC) applicationContext.getBean("loadIRCC");
                try { loadIRCC.run(); }
                catch (Exception e) { log.error("{}", e); }
            }},
        JAX {
            @Override
            public void load() {
                LoadJAXData loadJAXData = (LoadJAXData) applicationContext.getBean("loadJAXData");
                try { loadJAXData.run(); }
                catch (Exception e) { log.error("{}", e); }
            }},
        PDXNet_MDAnderson {
            @Override
            public void load() {
                LoadMDAnderson loadMDAnderson = (LoadMDAnderson) applicationContext.getBean("loadMDAnderson");
                try { loadMDAnderson.run(); }
                catch (Exception e) { log.error("{}", e); }
            }},
        PDMR {
            @Override
            public void load() {
                LoadPDMRData loadPDMRData = (LoadPDMRData) applicationContext.getBean("loadPDMRData");
                try { loadPDMRData.run(); }
                catch (Exception e) { log.error("{}", e); }
            }},
        PDXNet_Wistar_MDAnderson_Penn {
            @Override
            public void load() {
                LoadWISTAR loadWISTAR = (LoadWISTAR) applicationContext.getBean("loadWISTAR");
                try { loadWISTAR.run(); }
                catch (Exception e) { log.error("{}", e); }
            }},
        PDXNet_WUSTL {
            @Override
            public void load() {
                c loadWUSTL = (LoadWUSTL) applicationContext.getBean("loadWUSTL");
                try { loadWUSTL.run(); }
                catch (Exception e) { log.error("{}", e); }
            }},
        CRL,
        Curie_BC,
        Curie_LC,
        Curie_OC,
        IRCC_GC,
        PMLB,
        TRACE,
        UOC_BC,
        UOM_BC,
        VHIO_BC,
        VHIO_CRC;

        Logger log = LoggerFactory.getLogger(DataProviders.class);

        DataProvider() {}

        public void load() {
            LoadUniversal loadUniversal = (LoadUniversal) applicationContext.getBean("loadUniversal");
            try {
                loadUniversal.run(this.name());
            } catch (Exception e) {
                log.error("{}", e);
            }
        }

    }*/

}
