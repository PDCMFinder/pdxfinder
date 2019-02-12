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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Load models from WISTAR
 */
@Component
@Order(value = -15)
public class LoadWISTAR implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadWISTAR.class);

    private final static String DATASOURCE_ABBREVIATION = "PDXNet-Wistar-MDAnderson-Penn";
    private final static String DATASOURCE_NAME = "Wistar/MD Anderson/Penn";
    private final static String DATASOURCE_DESCRIPTION = "Wistar-MDAnderson-Penn PDX mouse models for PDXNet.";
    private final static String DATASOURCE_CONTACT = "herlynm@Wistar.org,MDavies@mdanderson.org";
    private final static String SOURCE_URL = null;

    private final static String PROVIDER_TYPE = "";
    private final static String ACCESSIBILITY = "";

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    private final static String NOT_SPECIFIED = Standardizer.NOT_SPECIFIED;

    //   private HostStrain nsgBS;
    private Group wistarDS;
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

    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadWISTAR(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadWISTAR", "Load WISTAR PDX data");
        parser.accepts("loadALL", "Load all, including WISTAR PDX data");
        OptionSet options = parser.parse(args);

        if (options.has("loadWISTAR") || options.has("loadALL")) {


            String urlStr = dataRootDir+DATASOURCE_ABBREVIATION+"/pdx/models.json";
            File file = new File(urlStr);
            if(file.exists()){

                log.info("Loading WISTAR PDX data from URL " + urlStr);
                parseJSON(utilityService.parseFile(urlStr));
            }
            else{

                log.info("No file found for "+DATASOURCE_ABBREVIATION+", skipping");
            }



        }

    }

    private void parseJSON(String json) {

        wistarDS = dataImportService.getProviderGroup(DATASOURCE_NAME, DATASOURCE_ABBREVIATION,
                DATASOURCE_DESCRIPTION, PROVIDER_TYPE, ACCESSIBILITY, null, DATASOURCE_CONTACT, SOURCE_URL);

        //      nsgBS = loaderUtils.getHostStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

        projectGroup = dataImportService.getProjectGroup("PDXNet");

        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("WISTAR");

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);

                createGraphObjects(j);
            }

        } catch (Exception e) {
            log.error("Error getting WISTAR PDX models", e);

        }
    }

    @Transactional
    void createGraphObjects(JSONObject j) throws Exception {
        String id = j.getString("Model ID").trim();

        // the preference is for histology
        String diagnosis = j.getString("Clinical Diagnosis");
        String histology = j.getString("Histology");

        if (histology.trim().length() > 0) {
            diagnosis = histology;
        }

        if (diagnosis.trim().length() == 0) {
            // no histology no diagnosis -> no model
            return;
        }

        String classification = j.getString("Stage") + "/" + j.getString("Grades");
        String stage = j.getString("Stage");
        String grade = j.getString("Grades");

        String ethnicity = NOT_SPECIFIED;
        try {
            if (j.getString("Race").trim().length() > 0) {
                ethnicity = j.getString("Race");
            }
        } catch (Exception e) {
        }

        try {
            if (j.getString("Ethnicity").trim().length() > 0) {
                ethnicity = j.getString("Ethnicity");
            }
        } catch (Exception e) {
        }

        String age = Standardizer.getAge(j.getString("Age"));

        String gender = Standardizer.getGender(j.getString("Gender"));
        String patientId = j.getString("Patient ID");

        String tumorType = Standardizer.getTumorType(j.getString("Tumor Type"));
        String extractionMethod = j.getString("Sample Type");

        Patient patient = dataImportService.getPatientWithSnapshots(patientId, wistarDS);

        if(patient == null){

            patient = dataImportService.createPatient(patientId, wistarDS, gender, "", Standardizer.getEthnicity(ethnicity));
        }

        PatientSnapshot pSnap = dataImportService.getPatientSnapshot(patient, age, "", "", "");


        //String sourceSampleId, String dataSource,  String typeStr, String diagnosis, String originStr,
        //String sampleSiteStr, String extractionMethod, Boolean normalTissue, String stage, String stageClassification,
        // String grade, String gradeClassification
        Sample sample = dataImportService.getSample(id, wistarDS.getAbbreviation(), tumorType, diagnosis, NOT_SPECIFIED,
                NOT_SPECIFIED, extractionMethod, false, stage, "", grade, "");




        pSnap.addSample(sample);

        dataImportService.saveSample(sample);
        dataImportService.savePatientSnapshot(pSnap);

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
        HostStrain bs = dataImportService.getHostStrain("", strain, "", "");

        String engraftmentSite = Standardizer.getValue("Engraftment Site", j);

        String tumorPrep = Standardizer.getValue("Tumor Prep", j);

        ModelCreation modelCreation = dataImportService.createModelCreation(id, wistarDS.getAbbreviation(), sample, qa, externalUrls);
        modelCreation.addRelatedSample(sample);
        modelCreation.addGroup(projectGroup);

        Specimen specimen = dataImportService.getSpecimen(modelCreation,
                modelCreation.getSourcePdxId(), wistarDS.getAbbreviation(), NOT_SPECIFIED);

        specimen.setHostStrain(bs);

        EngraftmentSite is = dataImportService.getImplantationSite(engraftmentSite);
        specimen.setEngraftmentSite(is);

        EngraftmentType it = dataImportService.getImplantationType(tumorPrep);
        specimen.setEngraftmentType(it);

        specimen.setSample(sample);

        dataImportService.saveSpecimen(specimen);

        dataImportService.saveModelCreation(modelCreation);

    }


}
