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
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.pdxfinder.utilities.Standardizer;

/**
 * Load data from WUSTL PDXNet.
 */
@Component
@Order(value = 0)
public class LoadWUSTL implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadWUSTL.class);

    private final static String WUSTL_DATASOURCE_ABBREVIATION = "PDXNet-WUSTL";
    private final static String WUSTL_DATASOURCE_NAME = "Washington University St. Louis";
    private final static String WUSTL_DATASOURCE_DESCRIPTION = "Washington University St. Louis PDX mouse models for PDXNet.";

    private final static String NOT_SPECIFIED = Standardizer.NOT_SPECIFIED;

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    //   private BackgroundStrain nsgBS;
    private ExternalDataSource mdaDS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private LoaderUtils loaderUtils;
    private Session session;

    //   @Value("${mdapdx.url}")
    //   private String urlStr;
    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadWUSTL(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
    }

    @Override
    public void run(String... args) throws Exception {

        String[] urls = {"http://tumor.informatics.jax.org/PDXInfo/WUSTLBreast.json", "http://tumor.informatics.jax.org/PDXInfo/WUSTLSarcoma.json", "http://tumor.informatics.jax.org/PDXInfo/WUSTLPCM.json"};
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

        mdaDS = loaderUtils.getExternalDataSource(WUSTL_DATASOURCE_ABBREVIATION, WUSTL_DATASOURCE_NAME, WUSTL_DATASOURCE_DESCRIPTION);
        //      nsgBS = loaderUtils.getBackgroundStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

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

        String race = Standardizer.getValue("Race", j);

        try {
            if (j.getString("Ethnicity").trim().length() > 0) {
                race = j.getString("Ethnicity");
            }
        } catch (Exception e) {
        }
        
          String age = Standardizer.getAge(j.getString("Age"));
          String gender = Standardizer.getGender(j.getString("Gender"));

        PatientSnapshot pSnap = loaderUtils.getPatientSnapshot(j.getString("Patient ID"),
                gender, "", race, age, mdaDS);

        Sample sample = loaderUtils.getSample(id, j.getString("Tumor Type"), diagnosis,
                NOT_SPECIFIED, NOT_SPECIFIED,
                j.getString("Sample Type"), classification, NORMAL_TISSUE_FALSE, mdaDS.getAbbreviation());

        pSnap.addSample(sample);

        String qaType = NOT_SPECIFIED;
        try {
            qaType = j.getString("QA") + "on passage " + j.getString("QA Passage");
        } catch (Exception e) {
            // not all groups supplied QA
        }
        QualityAssurance qa = new QualityAssurance(qaType,
                NOT_SPECIFIED, ValidationTechniques.VALIDATION);
        loaderUtils.saveQualityAssurance(qa);
        String strain = j.getString("Strain");
        BackgroundStrain bs = loaderUtils.getBackgroundStrain(strain, strain, "", "");

        String engraftmentSite = Standardizer.getValue("Engraftment Site", j);

        String tumorPrep = Standardizer.getValue("Tumor Prep", j);

        ModelCreation modelCreation = loaderUtils.createModelCreation(id, mdaDS.getAbbreviation(), sample, qa);
        modelCreation.addRelatedSample(sample);

        boolean human = false;
        String markerPlatform = NOT_SPECIFIED;;
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

                String passage = "0";
                try {
                    passage = j.getString("QA Passage").replaceAll("P", "");
                } catch (Exception e) {
                    // default is 0
                }
                Specimen specimen = loaderUtils.getSpecimen(modelCreation,
                        modelCreation.getSourcePdxId(), mdaDS.getAbbreviation(), passage);
                specimen.setSample(sample);

                specimen.setBackgroundStrain(bs);

                ImplantationSite is = new ImplantationSite(engraftmentSite);
                specimen.setImplantationSite(is);

                ImplantationType it = new ImplantationType(tumorPrep);
                specimen.setImplantationType(it);

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
            log.error("Unable to read from WUSTL JSON URL " + urlStr, e);
        }
        return sb.toString();
    }

}
