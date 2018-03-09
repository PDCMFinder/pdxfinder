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
import org.pdxfinder.utilities.LoaderUtils;
import org.pdxfinder.utilities.Standardizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Load models from WISTAR
 */
@Component
@Order(value = 0)
public class LoadWISTAR implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadWISTAR.class);

    private final static String WISTAR_DATASOURCE_ABBREVIATION = "PDXNet-Wistar-MDAnderson-Penn";
    private final static String WISTAR_DATASOURCE_NAME = "Melanoma PDX established by the Wistar/MD Anderson/Penn";
    private final static String WISTAR_DATASOURCE_DESCRIPTION = "Wistar-MDAnderson-Penn PDX mouse models for PDXNet.";
    private final static String DATASOURCE_CONTACT = "herlynm@Wistar.org,MDavies@mdanderson.org";

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    private final static String NOT_SPECIFIED = Standardizer.NOT_SPECIFIED;

    //   private HostStrain nsgBS;
    private ExternalDataSource wistarDS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private LoaderUtils loaderUtils;
    private Session session;

    @Value("${wistarpdx.url}")
    private String urlStr;

    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadWISTAR(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadWISTAR", "Load WISTAR PDX data");
        parser.accepts("loadALL", "Load all, including WISTAR PDX data");
        OptionSet options = parser.parse(args);

        if (options.has("loadWISTAR") || options.has("loadALL")) {

            log.info("Loading WISTAR PDX data from URL " + urlStr);
            parseJSON(parseURL(urlStr));

        }

    }

    private void parseJSON(String json) {

        wistarDS = loaderUtils.getExternalDataSource(WISTAR_DATASOURCE_ABBREVIATION, WISTAR_DATASOURCE_NAME, WISTAR_DATASOURCE_DESCRIPTION,DATASOURCE_CONTACT);
        //      nsgBS = loaderUtils.getHostStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

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

        String race = NOT_SPECIFIED;
        try {
            if (j.getString("Race").trim().length() > 0) {
                race = j.getString("Race");
            }
        } catch (Exception e) {
        }

        try {
            if (j.getString("Ethnicity").trim().length() > 0) {
                race = j.getString("Ethnicity");
            }
        } catch (Exception e) {
        }

        String age = Standardizer.getAge(j.getString("Age"));

        String gender = Standardizer.getGender(j.getString("Gender"));


        PatientSnapshot pSnap = loaderUtils.getPatientSnapshot(j.getString("Patient ID"),
                gender, "", race, age, wistarDS);
         String tumorType = Standardizer.getTumorType(j.getString("Tumor Type"));

        
        Sample sample = loaderUtils.getSample(id, j.getString("Tumor Type"), diagnosis,
                NOT_SPECIFIED, NOT_SPECIFIED,
                j.getString("Sample Type"), classification, NORMAL_TISSUE_FALSE, wistarDS.getAbbreviation());

        pSnap.addSample(sample);

        loaderUtils.saveSample(sample);
        loaderUtils.savePatientSnapshot(pSnap);

        String qaType = NOT_SPECIFIED;
        try {
            qaType = j.getString("QA") + "on passage " + j.getString("QA Passage");
        } catch (Exception e) {
            // not all groups supplied QA
        }
        String qaPassage = j.has("QA Passage") ? j.getString("QA Passage") : null;

        QualityAssurance qa = new QualityAssurance(qaType,
                NOT_SPECIFIED, ValidationTechniques.VALIDATION, qaPassage);
        loaderUtils.saveQualityAssurance(qa);

        String strain = j.getString("Strain");
        HostStrain bs = loaderUtils.getHostStrain(strain, strain, "", "");

        String engraftmentSite = Standardizer.getValue("Engraftment Site", j);

        String tumorPrep = Standardizer.getValue("Tumor Prep", j);

        ModelCreation modelCreation = loaderUtils.createModelCreation(id, wistarDS.getAbbreviation(), sample, qa);
        modelCreation.addRelatedSample(sample);

        Specimen specimen = loaderUtils.getSpecimen(modelCreation,
                modelCreation.getSourcePdxId(), wistarDS.getAbbreviation(), NOT_SPECIFIED);

        specimen.setHostStrain(bs);

        ImplantationSite is = new ImplantationSite(engraftmentSite);
        specimen.setImplantationSite(is);

        ImplantationType it = new ImplantationType(tumorPrep);
        specimen.setImplantationType(it);

        specimen.setSample(sample);

        loaderUtils.saveSpecimen(specimen);

        loaderUtils.saveModelCreation(modelCreation);

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
