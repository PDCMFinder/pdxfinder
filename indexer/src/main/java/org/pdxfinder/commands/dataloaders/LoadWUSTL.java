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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Load data from WUSTL PDXNet.
 */
@Component
@Order(value = -14)
public class LoadWUSTL extends LoaderBase implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadWUSTL.class);

    private final static String DATASOURCE_ABBREVIATION = "PDXNet-WUSTL";
    private final static String DATASOURCE_NAME = "Washington University in St. Louis";
    private final static String DATASOURCE_DESCRIPTION = "Washington University St. Louis PDX mouse models for PDXNet.";
    private final static String DATASOURCE_CONTACT = "bvantine@wustl.edu,rcfields@wustl.edu,jmudd@wustl.edu,sqli@wustl.edu,tprimeau@wustl.edu";
    private final static String SOURCE_URL = null;


    private final static String PROVIDER_TYPE = "";
    private final static String ACCESSIBILITY = "";

    private final static String NOT_SPECIFIED = Standardizer.NOT_SPECIFIED;

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    //   private HostStrain nsgBS;
    private Group DS;
    private Group projectGroup;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private DataImportService dataImportService;
    private Session session;

    @Autowired
    private UtilityService utilityService;

    @Value("${pdxfinder.data.root.dir}")
    private String dataRootDir;

    public LoadWUSTL(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
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

        for (int i = 0; i < listOfFiles.length; i++) {

            if (listOfFiles[i].isFile()) {

                this.jsonFile = rootDataDirectory + dataSourceAbbreviation + "/pdx/" + listOfFiles[i].getName();
                globalLoadingOrder();
            }
        }
        log.info("Finished loading " + dataSourceAbbreviation + " PDX data.");
    }


    @Override
    protected void initMethod() {

        log.info("Loading WUSTL PDX data.");

        dto = new LoaderDTO();
        rootDataDirectory = dataRootDir;
        dataSource = DATASOURCE_ABBREVIATION;
        filesDirectory = dataRootDir + DATASOURCE_ABBREVIATION + "/pdx/";
        dataSourceAbbreviation = DATASOURCE_ABBREVIATION;
        dataSourceContact = DATASOURCE_CONTACT;
    }

    // WUSTL uses default implementation Steps step01GetMetaDataFolder, step02GetMetaDataJSON

    @Override
    protected void step03CreateProviderGroup() {

        loadProviderGroup(DATASOURCE_NAME, DATASOURCE_ABBREVIATION, DATASOURCE_DESCRIPTION, PROVIDER_TYPE, DATASOURCE_CONTACT, SOURCE_URL);
    }

    @Override
    protected void step04CreateNSGammaHostStrain() {

    }

    @Override
    protected void step05CreateNSHostStrain() {

    }

    @Override
    protected void step06CreateProjectGroup() {

        loadProjectGroup("PDXNet");
    }


    @Override
    protected void step07GetPDXModels() {

        loadPDXModels(metaDataJSON,"WUSTL");
    }

    // WUSTL uses default implementation Steps step08GetMetaData, step09LoadPatientData

    @Override
    protected void step10LoadExternalURLs() {

        loadExternalURLs(DATASOURCE_CONTACT,Standardizer.NOT_SPECIFIED);

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
}
