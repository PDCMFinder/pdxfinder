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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Load data from University of Texas MD Anderson PDXNet.
 */
@Component
@Order(value = 0)
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

        String[] urls = {"http://tumor.informatics.jax.org/PDXInfo/MBMDAnderson.json", "http://tumor.informatics.jax.org/PDXInfo/CRCMDAnderson.json", "http://tumor.informatics.jax.org/PDXInfo/MinnaMDAnderson.json", "http://tumor.informatics.jax.org/PDXInfo/FangMDAnderson.json"};
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadMDA", "Load MDAnderson PDX data");

        parser.accepts("loadALL", "Load all, including MDA PDX data");
        OptionSet options = parser.parse(args);

        if (options.has("loadMDA") || options.has("loadALL")) {

            log.info("Loading MDAnderson PDX data.");

            for (String urlStr : urls) {

                log.info("Loading from URL " + urlStr);
                parseJSON(parseURL(urlStr));

            }
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
        String id = j.getString("Model ID");

        // the preference is for histology
        String diagnosis = j.getString("Clinical Diagnosis");
        String histology = j.getString("Histology");
        if (histology.trim().length() > 0) {
            if ("ACA".equals(histology)) {
                diagnosis = "Adenocarcinoma";
            } else {
                diagnosis = histology;
            }
        }

        String classification = j.getString("Stage") + "/" + j.getString("Grades");
        String stage = j.getString("Stage");
        String grade = j.getString("Grades");
        String ethnicity = Standardizer.getValue("Race",j);

        try {
            if (j.getString("Ethnicity").trim().length() > 0) {
                ethnicity = j.getString("Ethnicity");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String age = Standardizer.getAge(j.getString("Age"));
        String gender = Standardizer.getGender(j.getString("Gender"));


        String sampleSite = Standardizer.getValue("Sample Site",j);

        String tumorType = Standardizer.getTumorType(j.getString("Tumor Type"));
        String patientId = j.getString("Patient ID");
        String primarySite = j.getString("Primary Site");
        String extractionMethod = j.getString("Sample Type");


        Patient patient = dataImportService.getPatientWithSnapshots(patientId, mdaDS);

        if(patient == null){

            patient = dataImportService.createPatient(patientId, mdaDS, gender, "", Standardizer.getEthnicity(ethnicity));
        }

        PatientSnapshot pSnap = dataImportService.getPatientSnapshot(patient, age, "", "", "");


        //String sourceSampleId, String dataSource,  String typeStr, String diagnosis, String originStr,
        //String sampleSiteStr, String extractionMethod, Boolean normalTissue, String stage, String stageClassification,
        // String grade, String gradeClassification
        Sample sample = dataImportService.getSample(id, mdaDS.getAbbreviation(), tumorType, diagnosis, primarySite,
                sampleSite, extractionMethod, false, stage, "", grade, "");


        pSnap.addSample(sample);

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

        String engraftmentSite = Standardizer.getValue("Engraftment Site",j);
        
        String tumorPrep = Standardizer.getValue("Tumor Prep",j);

        ModelCreation modelCreation = dataImportService.createModelCreation(id, mdaDS.getAbbreviation(), sample, qa, externalUrls);
        modelCreation.addRelatedSample(sample);
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

        String markerStr = j.getString("Markers");

        String[] markers = markerStr.split(";");
        if (markerStr.trim().length() > 0) {
            Platform pl = dataImportService.getPlatform(markerPlatform, mdaDS);
            MolecularCharacterization molC = new MolecularCharacterization();
            molC.setType("mutation");
            molC.setPlatform(pl);
            List<MarkerAssociation> markerAssocs = new ArrayList<>();

            for (int i = 0; i < markers.length; i++) {
                Marker m = dataImportService.getMarker(markers[i], markers[i]);
                MarkerAssociation ma = new MarkerAssociation();
                ma.setMarker(m);
                markerAssocs.add(ma);
            }
            molC.setMarkerAssociations(markerAssocs);
            Set<MolecularCharacterization> mcs = new HashSet<>();
            mcs.add(molC);
            sample.setMolecularCharacterizations(mcs);

            if (human) {
                pSnap.addSample(sample);

            } else {

                String passage = "0";
                try {
                    passage = j.getString("QA Passage").replaceAll("P", "");
                } catch (Exception e) {
                    // default is 0
                }
                Specimen specimen = dataImportService.getSpecimen(modelCreation,
                        modelCreation.getSourcePdxId(), mdaDS.getAbbreviation(), passage);
                
                specimen.setHostStrain(bs);
                
                EngraftmentSite is = dataImportService.getImplantationSite(engraftmentSite);
                specimen.setEngraftmentSite(is);
                
                EngraftmentType it = dataImportService.getImplantationType(tumorPrep);
                specimen.setEngraftmentType(it);
                  
                specimen.setSample(sample);

                dataImportService.saveSpecimen(specimen);

            }
        }

        dataImportService.saveSample(sample);
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
            log.error("Unable to read from MD Anderson JSON URL " + urlStr, e);
        }
        return sb.toString();
    }

}
