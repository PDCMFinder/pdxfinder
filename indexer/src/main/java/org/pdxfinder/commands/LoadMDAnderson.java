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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Load data from University of Texas MD Anderson PDXNet.
 */
//@Component
//@Order(value = -17)
public class LoadMDAnderson implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadMDAnderson.class);

    private final static String DATASOURCE_ABBREVIATION = "PDXNet-MDAnderson";
    private final static String DATASOURCE_NAME = "MD Anderson Cancer Center";
    private final static String DATASOURCE_DESCRIPTION = "University Texas MD Anderson PDX mouse models for PDXNet.";
    private final static String DATASOURCE_CONTACT = "bfang@mdanderson.org";
    private final static String SOURCE_URL = null;

    private final static String PROVIDER_TYPE = "";
    private final static String ACCESSIBILITY = "";

    private final static String NOT_SPECIFIED = Standardizer.NOT_SPECIFIED;

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    //   private HostStrain nsgBS;
    private Group mdaDS;
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

    //   @Value("${mdapdx.url}")
    //   private String urlStr;
    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadMDAnderson(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadMDA", "Load MDAnderson PDX data");

        parser.accepts("loadALL", "Load all, including MDA PDX data");
        OptionSet options = parser.parse(args);

        if (options.has("loadMDA") || options.has("loadALL")) {

            log.info("Loading MDAnderson PDX data.");

            File folder = new File(dataRootDir + DATASOURCE_ABBREVIATION + "/pdx/");
            if (folder.exists()) {
                File[] listOfFiles = folder.listFiles();

                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        String fileName = dataRootDir + DATASOURCE_ABBREVIATION + "/pdx/" + listOfFiles[i].getName();

                        parseJSON(utilityService.parseFile(fileName));

                    }
                }

            } else {

                log.info("MDA directory not found");
            }


            log.info("Finished loading MDAnderson PDX data.");
        }

    }

    private void parseJSON(String json) {

        mdaDS = dataImportService.getProviderGroup(DATASOURCE_NAME, DATASOURCE_ABBREVIATION,
                DATASOURCE_DESCRIPTION, PROVIDER_TYPE, ACCESSIBILITY, null, DATASOURCE_CONTACT, SOURCE_URL);
        //      nsgBS = loaderUtils.getHostStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

        projectGroup = dataImportService.getProjectGroup("PDXNet");

        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("MDA");

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);

                createGraphObjects(j);
            }

        } catch (Exception e) {
            log.error("Error getting MDA PDX models", e);

        }
    }

    @Transactional
    void createGraphObjects(JSONObject j) throws Exception {

        // RETRIEVE THE METADATA:
        LoaderDTO dto = dataImportService.getMetadata(j, DATASOURCE_ABBREVIATION);

        dto = dataImportService.loaderFirstStep(dto, mdaDS, DATASOURCE_CONTACT);

        PatientSnapshot pSnap = dto.getPatientSnapshot();
        pSnap.addSample(dto.getPatientSample());

        dto.setProjectGroup(projectGroup);
        dto.setProviderGroup(mdaDS);

        // CREATE MODEL-CREATION
        dto.setModelCreation(
                dataImportService.createModelCreation(dto.getModelID(), mdaDS.getAbbreviation(), dto.getPatientSample(), dto.getQualityAssurance(), dto.getExternalUrls())
        );

        dto = dataImportService.loaderSecondStep(dto, pSnap, DATASOURCE_ABBREVIATION);

    }


}
