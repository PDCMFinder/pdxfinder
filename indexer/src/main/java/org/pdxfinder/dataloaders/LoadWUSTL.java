package org.pdxfinder.dataloaders;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.neo4j.ogm.session.Session;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.LoaderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Load data from WUSTL PDXNet.
 */
@Component
@Order(value = -14)
@PropertySource("classpath:loader.properties")
@ConfigurationProperties(prefix = "wustl")
public class LoadWUSTL extends LoaderBase implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadWUSTL.class);

    //   private HostStrain nsgBS;
    private Group DS;
    private Group projectGroup;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private Session session;


    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    public LoadWUSTL(UtilityService utilityService, DataImportService dataImportService) {
        super(utilityService, dataImportService);
    }

    //   @Value("${mdapdx.url}")
    //   private String urlStr;
    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    @Override
    public void run(String... args) throws Exception {


        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadWUSTL", "Load WUSTL PDX data");

        parser.accepts("loadALL", "Load all, including WUSTL PDX data");
        OptionSet options = parser.parse(args);

        if (options.has("loadWUSTL") || options.has("loadALL")) {

            initMethod();

            wustlAlgorithm();

        }

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

    // WUSTL uses default implementation Steps step01GetMetaDataFolder, step02GetMetaDataJSON

    @Override
    protected void step04CreateNSGammaHostStrain() {

    }

    @Override
    protected void step05CreateNSHostStrain() {

    }

    // WUSTL uses default implementation Steps step08GetMetaData, step09LoadPatientData

    @Override
    protected void step10LoadExternalURLs() {

        loadExternalURLs(dataSourceContact,Standardizer.NOT_SPECIFIED);

    }

    @Override
    protected void step11LoadBreastMarkers() {

    }

    // WUSTL uses default implementation Steps step12CreateModels default

    @Override
    protected void step13LoadSpecimens()throws Exception {

        loadSpecimens("wustl");
    }


    @Override
    protected void step14LoadPatientTreatments() {

    }


    @Override
    protected void step15LoadImmunoHistoChemistry() {

    }


    @Override
    protected void step16LoadVariationData() {

    }

    @Override
    void step17LoadModelDosingStudies() throws Exception {

    }

    @Override
    void step18SetAdditionalGroups() {}

}
