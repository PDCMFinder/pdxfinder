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

@Component("LoadMDAnderson")
@PropertySource("classpath:loader.properties")
@ConfigurationProperties(prefix = "mda")
public class LoadMDAnderson extends LoaderBase {

    private final static Logger log = LoggerFactory.getLogger(LoadMDAnderson.class);

    @Value("${pdxfinder.root.dir}") private String finderRootDir;
    @PostConstruct public void init() { }

    public LoadMDAnderson(UtilityService utilityService, DataImportService dataImportService) {
        super(utilityService, dataImportService);
    }

    public void run(String... args) throws Exception {
        initMethod();
        mdAndersonAlgorithm();
    }

    public void mdAndersonAlgorithm() throws Exception {
        step00StartReportManager();
        step01GetMetaDataFolder();

        if (skipThis) return;

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                this.jsonFile = rootDataDirectory +"/" + dataSourceAbbreviation + "/pdx/" + listOfFiles[i].getName();
                globalLoadingOrder();
            }
        }
        log.info("Finished loading " + dataSourceAbbreviation + " PDX data.");
    }



    @Override
    protected void initMethod() {

        log.info("Loading MDAnderson PDX data.");

        dto = new LoaderDTO();
        rootDataDirectory = finderRootDir + "/data";
        dataSource = dataSourceAbbreviation;
        filesDirectory = finderRootDir + "/data/" + dataSourceAbbreviation + "/pdx/";
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
        loadSpecimens("mdAnderson");
    }

    @Override protected void step14LoadPatientTreatments() { throw new UnsupportedOperationException(); }
    @Override protected void step15LoadImmunoHistoChemistry() { throw new UnsupportedOperationException(); }
    @Override protected void step16LoadVariationData() { throw new UnsupportedOperationException(); }
    @Override void step17LoadModelDosingStudies() { throw new UnsupportedOperationException(); }
    @Override void step18SetAdditionalGroups() { throw new UnsupportedOperationException(); }

}
