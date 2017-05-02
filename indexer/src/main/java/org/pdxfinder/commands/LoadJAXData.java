package org.pdxfinder.commands;

import org.pdxfinder.utilities.LoaderUtils;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.BackgroundStrain;

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

    private LoaderUtils loaderUtils;
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

    public LoadJAXData(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
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

            // Delete all ?how? data currently associated to this data source
            // this loaderUtils method does noting!
            try{
            loaderUtils.deleteAllByEDSName(JAX_DATASOURCE_NAME);
            }catch(Exception e){
                log.error("to be expected", e);
            }
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

    //JSON Fields {"Model ID","Gender","Age","Race","Ethnicity","Specimen Site","Primary Site","Initial Diagnosis","Clinical Diagnosis",
    //  "Tumor Type","Grades","Tumor Stage","Markers","Sample Type","Strain","Mouse Sex","Engraftment Site"};
    private void parseJSON(String json) {
        
       
        jaxDS = loaderUtils.getExternalDataSource(JAX_DATASOURCE_ABBREVIATION, JAX_DATASOURCE_NAME, JAX_DATASOURCE_DESCRIPTION);
        nsgBS = loaderUtils.getBackgroundStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("pdxInfo");
            String id = "";
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject j = jarray.getJSONObject(i);
                
                String classification = j.getString("Tumor Stage") + "/" + j.getString("Grades");
                
                PatientSnapshot pSnap = loaderUtils.getPatientSnapshot("JAX" + i, j.getString("Gender"),
                        j.getString("Race"), j.getString("Ethnicity"), j.getString("Age"), jaxDS);


                Sample sample = loaderUtils.getSample("JAX " + i, j.getString("Tumor Type"), j.getString("Clinical Diagnosis"),
                        j.getString("Specimen Site"), j.getString("Primary Site"), classification, NORMAL_TISSUE, jaxDS);

                JSONArray markers = j.getJSONArray("Markers");
                HashMap<String,Set<MarkerAssociation>> markerMap = new HashMap<>();
                for(int mIndex = 0; mIndex < markers.length(); mIndex++){
                    JSONObject marker = markers.getJSONObject(mIndex);
                    String symbol = marker.getString("Symbol");
                    String result = marker.getString("Result");
                    String technology = marker.getString("Technology");
                    
                    MarkerAssociation ma = loaderUtils.getMarkerAssociation(result,symbol,symbol);
                    // make a map of markerAssociationCollections keyed to technology
                    if(markerMap.containsKey(technology)){
                        markerMap.get(technology).add(ma);
                    }else{
                        HashSet<MarkerAssociation> set = new HashSet<>();
                        set.add(ma);
                        markerMap.put(technology,set);
                    }
                }
                HashSet<MolecularCharacterization> mcs = new HashSet<>();
                for(String tech : markerMap.keySet()){
                    MolecularCharacterization mc = new MolecularCharacterization();
                    mc.setTechnology(tech);
                    mc.setMarkerAssociations(markerMap.get(tech));
                    
                    loaderUtils.saveMolecularCharacterization(mc);
                    mcs.add(mc);
                    
                }
                sample.setMolecularCharacterizations(mcs);
            
                pSnap.addSample(sample);
                loaderUtils.savePatientSnapshot(pSnap);
                
                // models IDs that are numeric should start with 'TM' then the value padded to 5 digits with leading 0s
                try {
                    id = "TM" + String.format("%05d", new Integer(j.getString("Model ID")));
                } catch (Exception e) {
                    // a J#### model
                }

//                                                                        hope sample type is right value         
                loaderUtils.createPDXStrain(id, j.getString("Engraftment Site"), j.getString("Sample Type"), sample, nsgBS);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
