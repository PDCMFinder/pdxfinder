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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Load data from University of Texas MD Anderson PDXNet.
 */
@Component
@Order(value = 0)
public class LoadMDAnderson implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadMDAnderson.class);

    private final static String MDA_DATASOURCE_ABBREVIATION = "ut-md-anderson";
    private final static String MDA_DATASOURCE_NAME = "UT MD Anderson (PDXNet)";
    private final static String MDA_DATASOURCE_DESCRIPTION = "University Texas MD Anderson PDX mouse models for PDXNet.";

    private final static String NSG_BS_NAME = "NSG (NOD scid gamma)";
    private final static String NSG_BS_SYMBOL = "NOD.Cg-Prkdc<sup>scid</sup> Il2rg<sup>tm1Wjl</sup>/SzJ"; //yay HTML in name
    private final static String NSG_BS_URL = "http://jax.org/strain/005557";

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    private BackgroundStrain nsgBS;
    private ExternalDataSource mdaDS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private LoaderUtils loaderUtils;
    private Session session;

    @Value("${mdapdx.url}")
    private String urlStr;

   

    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadMDAnderson(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
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

            if (urlStr != null) {
                log.info("Loading from URL " + urlStr);
                parseJSON(parseURL(urlStr));
            } else {
                log.error("No mdapdx.url provided in properties");
            }
        }
    }

    private void parseJSON(String json) {

        mdaDS = loaderUtils.getExternalDataSource(MDA_DATASOURCE_ABBREVIATION, MDA_DATASOURCE_NAME, MDA_DATASOURCE_DESCRIPTION);
        nsgBS = loaderUtils.getBackgroundStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("MDAnderson");

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
        if(histology.trim().length()>0){
            if("ACA".equals(histology)){
                diagnosis= "Adenocarcinoma";
            }else{
                diagnosis = histology;
            }
        }

        String classification = j.getString("Classification") + "/" + j.getString("Grades");

        PatientSnapshot pSnap = loaderUtils.getPatientSnapshot(j.getString("Patient ID"),
                j.getString("Gender"),"", j.getString("Ethnicity"), j.getString("Age"), mdaDS);

        // asssume specimen site is primary site?
        Sample sample = loaderUtils.getSample(id, j.getString("Tumor Type"), diagnosis,
                j.getString("Primary Site"), j.getString("Primary Site"), 
                j.getString("Sample Type"), classification, NORMAL_TISSUE_FALSE, mdaDS);
        
        pSnap.addSample(sample);

        QualityAssurance qa = new QualityAssurance(j.getString("QA") + "on passage " + j.getString("QA Passage"),
                "HISTOLOGY_NOTE?", ValidationTechniques.VALIDATION);
        loaderUtils.saveQualityAssurance(qa);

        ModelCreation modelCreation = loaderUtils.createModelCreation(id, j.getString("Engraftment Site"),
                "Tumor Prep", sample, nsgBS, qa);
        modelCreation.addRelatedSample(sample);

        String markerPlatform = j.getString("Marker Platform");
        boolean human = false;
        if ("CMS50".equals(markerPlatform) || "CMS400".equals(markerPlatform)) {
            human = true;
        }
        
        String markerStr = j.getString("Markers");
        
        String[] markers = markerStr.split(";");
        if(markerStr.trim().length()>0){
            
            MolecularCharacterization molC = new MolecularCharacterization(markerPlatform);
            Set<MarkerAssociation> markerAssocs = new HashSet();
            
            for (int i = 0; i < markers.length; i++) {
                Marker m = loaderUtils.getMarker(markers[i], markers[i]);
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

                int passage = 0;
                try {
                    passage = new Integer(j.getString("QA Passage").replaceAll("P", "")).intValue();
                } catch (NumberFormatException e) {
                    // default is 0
                     }
                Specimen specimen = loaderUtils.getSpecimen(modelCreation, 
                        modelCreation.getSourcePdxId(), mdaDS.getAbbreviation(), passage);
                specimen.setSample(sample);

                loaderUtils.saveSpecimen(specimen);

            }
        }
        
        loaderUtils.saveSample(sample);
        loaderUtils.savePatientSnapshot(pSnap);
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
