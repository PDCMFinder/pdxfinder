package org.pdxfinder.commands.dataloaders;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Load data from University of Texas MD Anderson PDXNet.
 */
@Component
@Order(value = -17)
@PropertySource("classpath:loader.properties")
@ConfigurationProperties(prefix = "mda")
public class LoadMDAnderson extends LoaderBase implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadMDAnderson.class);

    //   private HostStrain nsgBS;
    private Group mdaDS;
    private Group projectGroup;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private Session session;


    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    //   @Value("${mdapdx.url}")
    //   private String urlStr;
    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadMDAnderson(UtilityService utilityService, DataImportService dataImportService) {
        super(utilityService, dataImportService);
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadMDA", "Load MDAnderson PDX data");

        parser.accepts("loadALL", "Load all, including MDA PDX data");
        OptionSet options = parser.parse(args);

        if (options.has("loadMDA") || options.has("loadALL")) {

            initMethod();

            mdAndersonAlgorithm();
        }

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

    // MD ANDERSON uses default implementation Steps step01GetMetaDataFolder, step02GetMetaDataJSON

    @Override
    protected void step04CreateNSGammaHostStrain() {

    }

    @Override
    protected void step05CreateNSHostStrain() {

    }


    // MD ANDERSON uses default implementation Steps step08GetMetaData, step09LoadPatientData


    @Override
    protected void step10LoadExternalURLs() {

        loadExternalURLs(dataSourceContact,Standardizer.NOT_SPECIFIED);

    }


    @Override
    protected void step11LoadBreastMarkers() {

    }


    // IRCC uses default implementation Steps Step11CreateModels default


    @Override
    protected void step13LoadSpecimens()throws Exception {

        loadSpecimens("mdAnderson");

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
}
