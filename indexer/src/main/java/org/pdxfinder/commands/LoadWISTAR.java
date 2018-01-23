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
    private final static String WISTAR_DATASOURCE_NAME = "Wistar-MDAnderson-Penn";
    private final static String WISTAR_DATASOURCE_DESCRIPTION = "Wistar-MDAnderson-Penn PDX mouse models for PDXNet.";

  

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;
    
     private final static String NOT_SPECIFIED = "Not Specified";

    //   private BackgroundStrain nsgBS;
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

        wistarDS = loaderUtils.getExternalDataSource(WISTAR_DATASOURCE_ABBREVIATION, WISTAR_DATASOURCE_NAME, WISTAR_DATASOURCE_DESCRIPTION);
        //      nsgBS = loaderUtils.getBackgroundStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

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
        String id = j.getString("Model ID");

        // the preference is for histology
        String diagnosis = j.getString("Clinical Diagnosis");
        String histology = j.getString("Histology");
        if (histology.trim().length() > 0) {
                diagnosis = histology;
            }
        

        String classification = j.getString("Stage") + "/" + j.getString("Grades");
        
        String race =  NOT_SPECIFIED;
        try{
            if(j.getString("Race").trim().length()>0){
                race = j.getString("Race");
            }
        }catch(Exception e){}
        
        try{
            if(j.getString("Ethnicity").trim().length()>0){
                race = j.getString("Ethnicity");
            }
        }catch(Exception e){}


        PatientSnapshot pSnap = loaderUtils.getPatientSnapshot(j.getString("Patient ID"),
                j.getString("Gender"), "", race, j.getString("Age"), wistarDS);

        
        Sample sample = loaderUtils.getSample(id, j.getString("Tumor Type"), diagnosis,
                NOT_SPECIFIED, NOT_SPECIFIED,
                j.getString("Sample Type"), classification, NORMAL_TISSUE_FALSE, wistarDS);

        pSnap.addSample(sample);

        loaderUtils.saveSample(sample);
        loaderUtils.savePatientSnapshot(pSnap);
        
        String qaType = NOT_SPECIFIED;
        try{
            qaType = j.getString("QA") + "on passage " + j.getString("QA Passage");
        }catch(Exception e){
            // not all groups supplied QA
        }
        QualityAssurance qa = new QualityAssurance(qaType,
                NOT_SPECIFIED, ValidationTechniques.VALIDATION);
        loaderUtils.saveQualityAssurance(qa);
        
        String strain = j.getString("Strain");
        BackgroundStrain bs = loaderUtils.getBackgroundStrain(strain, strain, "", "");
        
        String engraftmentSite = NOT_SPECIFIED;
        try{
            engraftmentSite = j.getString("Engraftment Site");
        }catch(Exception e){
            // uggh
        }
        
        String tumorPrep = NOT_SPECIFIED;
        
        try{
            tumorPrep = j.getString("Tumor Prep");
        }catch(Exception e){
            // uggh again
        }

        ModelCreation modelCreation = loaderUtils.createModelCreation(id, engraftmentSite,
                tumorPrep, sample, bs, qa);
        modelCreation.addRelatedSample(sample);

        
        
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
