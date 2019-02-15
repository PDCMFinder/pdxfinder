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
import org.pdxfinder.services.MolCharService;
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
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Load data from HCI PDXNet.
 */
@Component
@Order(value = -20)
public class LoadHCI implements CommandLineRunner {


    private final static Logger log = LoggerFactory.getLogger(LoadHCI.class);

    private final static String DATASOURCE_ABBREVIATION = "PDXNet-HCI-BCM";
    private final static String DATASOURCE_NAME = "HCI-Baylor College of Medicine";
    private final static String DATASOURCE_DESCRIPTION = "HCI BCM PDX mouse models for PDXNet.";
    private final static String DATASOURCE_CONTACT = "Alana.Welm@hci.utah.edu";
    private final static String PROVIDER_TYPE = "";
    private final static String ACCESSIBILITY = "";

    private final static String NSG_BS_NAME = "NOD scid gamma";
    private final static String NSG_BS_SYMBOL = "NOD.Cg-Prkdc<sup>scid</sup> Il2rg<sup>tm1Wjl</sup>/SzJ"; //yay HTML in name
    private final static String NSG_BS_URL = "http://jax.org/strain/005557";

    private final static String NS_BS_NAME = "NOD scid";
    private final static String NS_BS_SYMBOL = "NOD.CB17-Prkd<sup>cscid</sup>/J"; //yay HTML in name
    private final static String NS_BS_URL = "https://www.jax.org/strain/001303";

    private final static String DOSING_STUDY_URL = "/platform/hci-drug-dosing/";

    private final static String SOURCE_URL = null;

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    private final static String NOT_SPECIFIED = Standardizer.NOT_SPECIFIED;

    private HostStrain nsgBS, nsBS;
    private Group hciDS;
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

    public LoadHCI(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadHCI", "Load HCI PDX data");
        parser.accepts("loadALL", "Load all, including HCI PDX data");
        OptionSet options = parser.parse(args);

        if (options.has("loadHCI") || options.has("loadALL")) {

            log.info("Loading Huntsman PDX data.");

            String modelJson = dataRootDir + DATASOURCE_ABBREVIATION + "/pdx/models.json";

            File file = new File(modelJson);
            if (file.exists()) {

                parseJSON(utilityService.parseFile(modelJson));
            } else {
                log.info("No file found for " + DATASOURCE_ABBREVIATION + ", skipping");
            }


        }
    }

    private void parseJSON(String json) {

        hciDS = dataImportService.getProviderGroup(DATASOURCE_NAME, DATASOURCE_ABBREVIATION,
                DATASOURCE_DESCRIPTION, PROVIDER_TYPE, ACCESSIBILITY, null, DATASOURCE_CONTACT, SOURCE_URL);

        try {
            nsgBS = dataImportService.getHostStrain(NSG_BS_NAME, NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_NAME);
            nsBS = dataImportService.getHostStrain(NS_BS_NAME, NS_BS_SYMBOL, NS_BS_URL, NS_BS_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        projectGroup = dataImportService.getProjectGroup("PDXNet");

        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("HCI");

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);

                createGraphObjects(j);
            }

        } catch (Exception e) {
            log.error("Error getting HCI PDX models", e);

        }

        loadImmunoHistoChemistry();

    }

    @Transactional
    void createGraphObjects(JSONObject j) throws Exception {

        LoaderDTO dto = dataImportService.getMetadata(j, DATASOURCE_ABBREVIATION);

        dto = dataImportService.loaderFirstStep(dto, hciDS, DATASOURCE_CONTACT);

        PatientSnapshot pSnap = dto.getPatientSnapshot();
        pSnap.addSample(dto.getSample());

        dto.setNodScidGamma(nsgBS);
        dto.setNodScid(nsBS);
        dto.setProjectGroup(projectGroup);
        dto.setProviderGroup(hciDS);

        dto.setModelCreation(
                dataImportService.createModelCreation(dto.getModelID(), this.hciDS.getAbbreviation(), dto.getSample(), dto.getQualityAssurance(), dto.getExternalUrls())
        );

        dto = dataImportService.loaderSecondStep(dto, pSnap, DATASOURCE_ABBREVIATION);


        TreatmentSummary ts;
        try {
            if (j.has("Treatments")) {
                JSONObject treatment = j.optJSONObject("Treatments");
                //if the treatment attribute is not an object = it is an array
                if (treatment == null && j.optJSONArray("Treatments") != null) {

                    JSONArray treatments = j.getJSONArray("Treatments");

                    if (treatments.length() > 0) {

                        //log.info("Treatments found for model "+mc.getSourcePdxId());
                        ts = new TreatmentSummary();
                        ts.setUrl(DOSING_STUDY_URL);

                        for (int t = 0; t < treatments.length(); t++) {
                            JSONObject treatmentObject = treatments.getJSONObject(t);


                            TreatmentProtocol tp = dataImportService.getTreatmentProtocol(treatmentObject.getString("Drug"),
                                    treatmentObject.getString("Dose"), treatmentObject.getString("Response"), "");

                            if (tp != null) {
                                ts.addTreatmentProtocol(tp);
                            }
                        }

                        ts.setModelCreation(dto.getModelCreation());
                        dto.getModelCreation().setTreatmentSummary(ts);
                    }
                }

            }

            dataImportService.saveModelCreation(dto.getModelCreation());

        } catch (Exception e) {

            e.printStackTrace();
        }


    }


    private void loadImmunoHistoChemistry() {


        String ihcFileStr = dataRootDir + DATASOURCE_ABBREVIATION + "/ihc/ihc.txt";

        File file = new File(ihcFileStr);

        if (file.exists()) {

            Platform pl = dataImportService.getPlatform("ImmunoHistoChemistry", hciDS);

            String currentLine = "";
            int currentLineCounter = 1;
            String[] row;

            Map<String, MolecularCharacterization> molCharMap = new HashMap<>();

            try {
                BufferedReader buf = new BufferedReader(new FileReader(ihcFileStr));

                while (true) {
                    currentLine = buf.readLine();
                    if (currentLine == null) {
                        break;
                        //skip the first two rows
                    } else if (currentLineCounter < 3) {
                        currentLineCounter++;
                        continue;

                    } else {
                        row = currentLine.split("\t");

                        if (row.length > 0) {

                            String modelId = row[0];
                            String samleId = row[1];
                            String marker = row[2];
                            String result = row[3];
                            //System.out.println(modelId);

                            if (modelId.isEmpty() || samleId.isEmpty() || marker.isEmpty() || result.isEmpty())
                                continue;

                            if (molCharMap.containsKey(modelId + "---" + samleId)) {

                                MolecularCharacterization mc = molCharMap.get(modelId + "---" + samleId);
                                Marker m = dataImportService.getMarker(marker);

                                MarkerAssociation ma = new MarkerAssociation();
                                ma.setImmunoHistoChemistryResult(result);
                                ma.setMarker(m);
                                mc.addMarkerAssociation(ma);
                            } else {

                                MolecularCharacterization mc = new MolecularCharacterization();
                                mc.setType("IHC");
                                mc.setPlatform(pl);


                                Marker m = dataImportService.getMarker(marker);
                                MarkerAssociation ma = new MarkerAssociation();
                                ma.setImmunoHistoChemistryResult(result);
                                ma.setMarker(m);
                                mc.addMarkerAssociation(ma);

                                molCharMap.put(modelId + "---" + samleId, mc);
                            }

                        }


                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(currentLineCounter + " " + currentLine.toString());
            }

            //System.out.println(molCharMap.toString());

            for (Map.Entry<String, MolecularCharacterization> entry : molCharMap.entrySet()) {
                String key = entry.getKey();
                MolecularCharacterization mc = entry.getValue();

                String[] modAndSamp = key.split("---");
                String modelId = modAndSamp[0];
                String sampleId = modAndSamp[1];

                //Sample sample = dataImportService.findMouseSampleWithMolcharByModelIdAndDataSourceAndSampleId(modelId, hciDS.getAbbreviation(), sampleId);
                Sample sample = dataImportService.findHumanSampleWithMolcharByModelIdAndDataSource(modelId, hciDS.getAbbreviation());

                if (sample == null) {
                    log.warn("Missing model or sample: " + modelId + " " + sampleId);
                    continue;
                }

                sample.addMolecularCharacterization(mc);
                dataImportService.saveSample(sample);

            }

        } else {

            log.warn("Skipping loading IHC for HCI");
        }


    }

}
