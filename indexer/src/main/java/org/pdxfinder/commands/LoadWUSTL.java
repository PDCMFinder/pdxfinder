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
import org.pdxfinder.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.ds.Standardizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Load data from WUSTL PDXNet.
 */
@Component
@Order(value = 0)
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

        String[] urls = {"http://tumor.informatics.jax.org/PDXInfo/WUSTLBreast.json", "http://tumor.informatics.jax.org/PDXInfo/WUSTLSarcoma.json", "http://tumor.informatics.jax.org/PDXInfo/WUSTLPCMNew.json"};
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadWUSTL", "Load WUSTL PDX data");

        parser.accepts("loadALL", "Load all, including WUSTL PDX data");
        OptionSet options = parser.parse(args);

        if (options.has("loadWUSTL") || options.has("loadALL")) {

            log.info("Loading WUSTL PDX data.");

            for (String urlStr : urls) {

                log.info("Loading from URL " + urlStr);
                parseJSON(parseURL(urlStr));

            }
        }

    }

    private void parseJSON(String json) {

        DS = dataImportService.getProviderGroup(DATASOURCE_NAME, DATASOURCE_ABBREVIATION,
                DATASOURCE_DESCRIPTION, PROVIDER_TYPE, ACCESSIBILITY, null, DATASOURCE_CONTACT, SOURCE_URL);

        //      nsgBS = loaderUtils.getHostStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

        projectGroup = dataImportService.getProjectGroup("PDXNet");

        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("WUSTL");

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);

                createGraphObjects(j);
            }

        } catch (Exception e) {
            log.error("Error getting WUSTL PDX models", e);

        }
    }

    @Transactional
    void createGraphObjects(JSONObject j) throws Exception {
        String id = j.getString("Model ID");

        // the preference is for histology
        String diagnosis = j.getString("Clinical Diagnosis");
        String histology = j.getString("Histology");

        if (histology.trim().length() > 0) {
            diagnosis = histology;
        }

        String classification = j.getString("Stage") + "/" + j.getString("Grades");
        String stage = j.getString("Stage");
        String grade = j.getString("Grades");

        String ethnicity = Standardizer.getValue("Race", j);

        try {
            if (j.getString("Ethnicity").trim().length() > 0) {
                ethnicity = j.getString("Ethnicity");
            }
        } catch (Exception e) {
        }

        String age = Standardizer.getAge(j.getString("Age"));
        String gender = Standardizer.getGender(j.getString("Gender"));
        String patientId = j.getString("Patient ID");
        String tumorType = j.getString("Tumor Type");
        String primaryTissue = j.getString("Primary Site");
        String sampleSite = Standardizer.getValue("Sample Site", j);
        String extractionMethod = j.getString("Sample Type");


        Patient patient = dataImportService.getPatientWithSnapshots(patientId, DS);

        if(patient == null){

            patient = dataImportService.createPatient(patientId, DS, gender, "", Standardizer.getEthnicity(ethnicity));
        }

        PatientSnapshot pSnap = dataImportService.getPatientSnapshot(patient, age, "", "", "");


        //String sourceSampleId, String dataSource,  String typeStr, String diagnosis, String originStr,
        //String sampleSiteStr, String extractionMethod, Boolean normalTissue, String stage, String stageClassification,
        // String grade, String gradeClassification
        Sample humanSample = dataImportService.getSample(id, DS.getAbbreviation(), tumorType, diagnosis, primaryTissue,
                sampleSite, extractionMethod, false, stage, "", grade, "");



        pSnap.addSample(humanSample);

        List<ExternalUrl> externalUrls = new ArrayList<>();
        externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.CONTACT, DATASOURCE_CONTACT));

        String qaType = NOT_SPECIFIED;
        try {
            qaType = j.getString("QA") + "on passage " + j.getString("QA Passage");
        } catch (Exception e) {
            // not all groups supplied QA
        }
        String qaPassage = j.has("QA Passage") ? j.getString("QA Passage") : null;

        QualityAssurance qa = new QualityAssurance(qaType,
                NOT_SPECIFIED, qaPassage);
        dataImportService.saveQualityAssurance(qa);
        String strain = j.getString("Strain");
        HostStrain bs = dataImportService.getHostStrain(strain, strain, "", "");

        String engraftmentSite = Standardizer.getValue("Engraftment Site", j);

        String tumorPrep = Standardizer.getValue("Tumor Prep", j);

        ModelCreation modelCreation = dataImportService.createModelCreation(id, DS.getAbbreviation(), humanSample, qa, externalUrls);
        modelCreation.addRelatedSample(humanSample);
        modelCreation.addGroup(projectGroup);

        boolean human = false;
        String markerPlatform = NOT_SPECIFIED;

        try {
            markerPlatform = j.getString("Marker Platform");
            if ("CMS50".equals(markerPlatform) || "CMS400".equals(markerPlatform)) {
                human = true;
            }
        } catch (Exception e) {
            // this is for the FANG data and we don't really care about markers at this point anyway
        }


        if (human) {
            pSnap.addSample(humanSample);

        }
        else{

            Sample mouseSample = new Sample();

            String passage = "0";
            try {
                passage = j.getString("QA Passage").replaceAll("P", "");
            } catch (Exception e) {
                // default is 0
            }
            Specimen specimen = dataImportService.getSpecimen(modelCreation,
                    modelCreation.getSourcePdxId(), DS.getAbbreviation(), passage);

            specimen.setHostStrain(bs);

            specimen.setSample(mouseSample);
            modelCreation.addRelatedSample(mouseSample);

            if (engraftmentSite.contains(";")) {
                String[] parts = engraftmentSite.split(";");
                engraftmentSite = parts[1].trim();
                tumorPrep = parts[0].trim();
            }
            EngraftmentSite is = dataImportService.getImplantationSite(engraftmentSite);
            specimen.setEngraftmentSite(is);

            EngraftmentType it = dataImportService.getImplantationType(tumorPrep);
            specimen.setEngraftmentType(it);

            modelCreation.addSpecimen(specimen);

        }

        dataImportService.saveModelCreation(modelCreation);
        dataImportService.savePatientSnapshot(pSnap);
    }


    private String parseURL(String urlStr) {
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            log.error("Unable to read from WUSTL JSON URL " + urlStr, e);
        }
        return sb.toString();
    }

}
