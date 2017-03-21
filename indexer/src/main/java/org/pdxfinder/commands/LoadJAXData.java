package org.pdxfinder.commands;

import org.apache.commons.cli.*;
import org.neo4j.ogm.session.Session;
import org.pdxfinder.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Stream;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.BackgroundStrain;
<<<<<<< HEAD
import org.pdxfinder.dao.ImplantationSite;
import org.pdxfinder.dao.ImplantationType;
import org.pdxfinder.dao.Patient;
import org.pdxfinder.dao.PdxStrain;
import org.pdxfinder.dao.Tissue;
import org.pdxfinder.dao.Tumor;
import org.pdxfinder.dao.TumorType;
import org.pdxfinder.dao.WrongPlaceWrongName;
import org.pdxfinder.repositories.BackgroundStrainRepository;
import org.pdxfinder.repositories.ImplantationSiteRepository;
import org.pdxfinder.repositories.ImplantationTypeRepository;
import org.pdxfinder.repositories.PatientRepository;
import org.pdxfinder.repositories.PdxStrainRepository;
import org.pdxfinder.repositories.TissueRepository;
import org.pdxfinder.repositories.TumorRepository;
=======
>>>>>>> data-model-v2
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Load data from JAX.
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class LoadJAXData implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadJAXData.class);

    private final static String JAX_DATASOURCE_ABBREVIATION = "JAX";
    private final static String JAX_DATASOURCE_NAME = "The Jackson Laboratory";
    private final static String JAX_DATASOURCE_DESCRIPTION = "The Jackson Laboratory PDX mouse models.";
    private final static String NSG_BS_NAME = "NSG (NOD scid gamma)";
    private final static String NSG_BS_SYMBOL = "NOD.Cg-Prkdc<sup>scid</sup> Il2rg<sup>tm1Wjl</sup>/SzJ"; //yay HTML in name
    private final static String NSG_BS_URL = "http://jax.org/strain/005557";

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE = false;

    // hmm not sure about this
    private final static String MC_TECH = "CTP or Whole Exome";

    private BackgroundStrain nsgBS;
    private ExternalDataSource jaxDS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private WrongPlaceWrongName wpwn;
    private Session session;

    @Value("${jaxpdx.file}")
    private String file;

    @Value("${jaxpdx.url}")
    private String urlStr;

    @PostConstruct
    public void init() {
        options = new Options();
        parser = new DefaultParser();
        formatter = new HelpFormatter();
        log.info("Setting up LoadJAXDataCommand option");
    }

    public LoadJAXData(WrongPlaceWrongName wpwn) {
        this.wpwn = wpwn;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        log.info(args[0]);

        if ("loadJAX".equals(args[0]) || "-loadJAX".equals(args[0])) {

            log.info("Loading JAX PDX data.");
            try {
                cmd = parser.parse(options, args);

            } catch (UnrecognizedOptionException | MissingArgumentException e) {
                formatter.printHelp("load", options);
                System.exit(1);
            }

<<<<<<< HEAD
            // Delete all(?how?) data currently associated to this data source
            ExternalDataSource jaxDS = externalDataSourceRepository.findByAbbreviation(JAX_DATASOURCE_ABBREVIATION);
            if (jaxDS != null) {
                externalDataSourceRepository.delete(jaxDS);
                // delete all associated data....
            }
            
=======
            // Delete all ?how? data currently associated to this data source
            // this wpwn method does noting!
            wpwn.deleteAllByEDSName(JAX_DATASOURCE_NAME);
>>>>>>> data-model-v2

            if (urlStr != null) {
                log.info("Loading from URL " + urlStr);
                parseJSON(parseURL(urlStr));
            } else if (file != null) {
                log.info("Loading from file " + file);
                parseJSON(parseFile(file));
            } else {
                log.error("No jaxpdx.file or jaxpdx.url provided in properties");
            }
        }

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
            log.error("Unable to read from URL " + urlStr, e);
        }
        return sb.toString();
    }

    private String parseFile(String path) {

        StringBuilder sb = new StringBuilder();

        try {
            Stream<String> stream = Files.lines(Paths.get(path));

            Iterator itr = stream.iterator();
            while (itr.hasNext()) {
                sb.append(itr.next());
            }
        } catch (Exception e) {
            log.error("Failed to load file " + path, e);
        }
        return sb.toString();
    }

<<<<<<< HEAD
    
=======
>>>>>>> data-model-v2
    //JSON Fields {"Model ID","Gender","Age","Race","Ethnicity","Specimen Site","Primary Site","Initial Diagnosis","Clinical Diagnosis",
    //  "Tumor Type","Grades","Tumor Stage","Markers","Sample Type","Strain","Mouse Sex","Engraftment Site"};
    private void parseJSON(String json) {
        
        WrongPlaceWrongName wpwn = new WrongPlaceWrongName();
        jaxDS = wpwn.getExternalDataSource(JAX_DATASOURCE_ABBREVIATION, JAX_DATASOURCE_NAME, JAX_DATASOURCE_DESCRIPTION);
        nsgBS = wpwn.getBackgroundStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

<<<<<<< HEAD
        
=======
        jaxDS = wpwn.getExternalDataSource(JAX_DATASOURCE_ABBREVIATION, JAX_DATASOURCE_NAME, JAX_DATASOURCE_DESCRIPTION);
        nsgBS = wpwn.getBackgroundStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

>>>>>>> data-model-v2
        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("pdxInfo");
            String id = "";
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject j = jarray.getJSONObject(i);
<<<<<<< HEAD
                
                        
                Patient p = wpwn.getPatient("JAX"+i, j.getString("Gender"),j.getString("Age"), j.getString("Race"), j.getString("Ethnicity"),jaxDS);
                
                String classification = j.getString("Tumor Stage") + "/" + j.getString("Grades");
                
                Tumor tumor = wpwn.getTumor("JAX " + i, j.getString("Tumor Type"), j.getString("Clinical Diagnosis"), j.getString("Specimen Site"),
                        j.getString("Primary Site"), classification, jaxDS);
                
=======

                PatientSnapshot pSnap = wpwn.getPatientSnapshot("JAX" + i, j.getString("Gender"),
                        j.getString("Race"), j.getString("Ethnicity"), j.getString("Age"), jaxDS);

                String classification = j.getString("Tumor Stage") + "/" + j.getString("Grades");

                Sample sample = wpwn.getSample("JAX " + i, j.getString("Tumor Type"), j.getString("Clinical Diagnosis"),
                        j.getString("Specimen Site"), j.getString("Primary Site"), classification, NORMAL_TISSUE, jaxDS);

                String markerList = j.getString("Markers");
                if (markerList != null && markerList.length() > 0) {
                    HashSet<Marker> markerSet = new HashSet<>();
                    String[] markers = markerList.split(",");
                    for (String symbol : markers) {
                        Marker m = wpwn.getMarker(symbol);
                        markerSet.add(m);
                    }

                    MolecularCharacterization mc = new MolecularCharacterization(MC_TECH);

                    // all positive, being positive about it.
                    mc.setPositiveMarkers(markerSet);

                    // save mc
                    wpwn.saveMolecularCharacterization(mc);
                    HashSet<MolecularCharacterization> mcs = new HashSet<>();
                    mcs.add(mc);
                    sample.setMolecularCharacterizations(mcs);
                }

                pSnap.addSample(sample);
                wpwn.savePatientSnapshot(pSnap);
>>>>>>> data-model-v2
                // models IDs that are numeric should start with 'TM' then the value padded to 5 digits with leading 0s
                try {
                    id = "TM" + String.format("%05d", new Integer(j.getString("Model ID")));
                } catch (Exception e) {
                    // a J#### model
                }
<<<<<<< HEAD
                
                wpwn.createPDXStrain(id, j.getString("Engraftment Site"), j.getString("Sample Type"), tumor, nsgBS, "3");
=======

//                                                                        hope sample type is right value         
                wpwn.createPDXStrain(id, j.getString("Engraftment Site"), j.getString("Sample Type"), sample, nsgBS);

>>>>>>> data-model-v2
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

<<<<<<< HEAD

=======
>>>>>>> data-model-v2
}
