package org.pdxfinder.dataloaders;

import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.LoaderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("LoadWUSTL")
@PropertySource("classpath:loader.properties")
@ConfigurationProperties(prefix = "wustl")
public class LoadWUSTL extends LoaderBase {

    private final static Logger log = LoggerFactory.getLogger(LoadWUSTL.class);
    @Value("${pdxfinder.root.dir}") private String finderRootDir;

    public LoadWUSTL(UtilityService utilityService, DataImportService dataImportService) {
        super(utilityService, dataImportService);
    }

    @PostConstruct public void init() { }

    public void run(String... args) throws Exception {
        initMethod();
        wustlAlgorithm();
    }


    public void wustlAlgorithm() throws Exception {

        step00StartReportManager();
        step01GetMetaDataFolder();

        if (skipThis) return;

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                this.jsonFile = rootDataDirectory + "/data/" + dataSourceAbbreviation + "/pdx/" + listOfFiles[i].getName();
                globalLoadingOrder();
            }
        }
        log.info("Finished loading " + dataSourceAbbreviation + " PDX data.");
    }


    @Override
    protected void initMethod() {
        log.info("Loading WUSTL PDX data.");
        dto = new LoaderDTO();
        rootDataDirectory = finderRootDir;
        dataSource = dataSourceAbbreviation;
        filesDirectory = finderRootDir +"/data/" + dataSourceAbbreviation + "/pdx/";
    }

    @Override protected void step04CreateNSGammaHostStrain() { throw new UnsupportedOperationException(); }
    @Override protected void step05CreateNSHostStrain() { throw new UnsupportedOperationException(); }

    @Override
    protected void step10LoadExternalURLs() {
        loadExternalURLs(dataSourceContact,Standardizer.NOT_SPECIFIED);
    }

    @Override protected void step11LoadBreastMarkers() { throw new UnsupportedOperationException(); }

    @Override
    protected void step13LoadSpecimens()throws Exception {
        loadSpecimens("wustl");
    }

    @Override protected void step14LoadPatientTreatments() { throw new UnsupportedOperationException(); }
    @Override protected void step15LoadImmunoHistoChemistry() { throw new UnsupportedOperationException(); }
    @Override protected void step16LoadVariationData() { throw new UnsupportedOperationException();  }
    @Override void step17LoadModelDosingStudies() { throw new UnsupportedOperationException(); }
    @Override void step18SetAdditionalGroups() { throw new UnsupportedOperationException(); }

}
