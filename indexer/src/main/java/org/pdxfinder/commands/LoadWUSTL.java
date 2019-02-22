package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
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
import java.io.File;

/**
 * Load data from WUSTL PDXNet.
 */
@Component
@Order(value = -14)
public class LoadWUSTL implements CommandLineRunner {

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

            log.info("Loading WUSTL PDX data.");


            String directory = dataRootDir + DATASOURCE_ABBREVIATION + "/pdx/";

            File[] listOfFiles = dataImportService.stageZeroGetMetaDataFolder(directory,DATASOURCE_ABBREVIATION);

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {

                    String fileName = dataRootDir + DATASOURCE_ABBREVIATION + "/pdx/" + listOfFiles[i].getName();
                    String metaDataJSON = dataImportService.stageOneGetMetaDataFile(fileName, DATASOURCE_ABBREVIATION);

                    parseJSONandCreateGraphObjects(metaDataJSON);
                }
            }

        }

    }

    private void parseJSONandCreateGraphObjects(String json) throws Exception {

        LoaderDTO dto = new LoaderDTO();

        dto = dataImportService.stagetwoCreateProviderGroup(dto, DATASOURCE_NAME, DATASOURCE_ABBREVIATION, DATASOURCE_DESCRIPTION,
                PROVIDER_TYPE, ACCESSIBILITY, null, DATASOURCE_CONTACT, SOURCE_URL);

        dto = dataImportService.stageFiveCreateProjectGroup(dto,"PDXNet");

        JSONArray jarray = dataImportService.stageSixGetPDXModels(json,"WUSTL");


        for (int i = 0; i < jarray.length(); i++) {

            JSONObject jsonData = jarray.getJSONObject(i);

            dto = dataImportService.stageSevenGetMetadata(dto, jsonData, DATASOURCE_ABBREVIATION);

            dto = dataImportService.stageEightLoadPatientData(dto, DATASOURCE_CONTACT);

            dto = dataImportService.step09LoadExternalURLs(dto, DATASOURCE_CONTACT);

            PatientSnapshot pSnap = dto.getPatientSnapshot();
            pSnap.addSample(dto.getPatientSample());

            dto = dataImportService.stageNineCreateModels(dto);

            dto = dataImportService.loadSpecimens(dto, pSnap, DATASOURCE_ABBREVIATION);

        }


    }


}
