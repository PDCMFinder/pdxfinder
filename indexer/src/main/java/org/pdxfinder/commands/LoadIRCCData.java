package org.pdxfinder.commands;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.neo4j.ogm.session.Session;
import org.pdxfinder.dao.*;
import org.pdxfinder.irccdatamodel.IRCCPatient;
import org.pdxfinder.irccdatamodel.IRCCSample;
import org.pdxfinder.utilities.LoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by csaba on 18/04/2017.
 */
@Component
public class LoadIRCCData implements CommandLineRunner {


    private final static Logger log = LoggerFactory.getLogger(LoadJAXData.class);

    private final static String DATASOURCE_ABBREVIATION = "IRCC";
    private final static String DATASOURCE_NAME = "IRCC data";
    private final static String DATASOURCE_DESCRIPTION = "IRCC PDX mouse models.";
    //private final static String NSG_BS_NAME = "NSG (NOD scid gamma)"; //background strain
    //private final static String NSG_BS_SYMBOL = "NOD.Cg-Prkdc<sup>scid</sup> Il2rg<sup>tm1Wjl</sup>/SzJ"; //yay HTML in name
    //private final static String NSG_BS_URL = "http://jax.org/strain/005557";

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE = false;

    // hmm not sure about this
    private final static String MC_TECH = "Gene Panel";

    private BackgroundStrain nsgBS;
    private ExternalDataSource DS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private LoaderUtils loaderUtils;
    private Session session;

    @Value("${irccpatients.file}")
    private String patientsFile;

    @Value("${irccsamples.file}")
    private String samplesFile;


    @Override
    @Transactional
    public void run(String... args) throws Exception {

        log.info(args[0]);

        if ("loadIRCC".equals(args[0]) || "-loadIRCC".equals(args[0])) {

            log.info("Loading IRCC PDX data.");


            loadDataFiles(samplesFile, patientsFile);


        }
    }

    public LoadIRCCData(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
    }

    private void loadDataFiles(String samplesFile, String patientsFile) {

        Map<String, IRCCPatient> patientsMap = new HashMap<>();
        Map<String, List<IRCCSample>> samplesMap = new HashMap<>();


        String currentLine;
        long currentLineCounter = 1;
        String[] rowData;

        //load patients file
        try {
            BufferedReader buf = new BufferedReader(new FileReader(patientsFile));
            currentLine = null;

            while (true) {
                currentLine = buf.readLine();
                if (currentLine == null) {
                    break;
                } else if (currentLineCounter < 6) {
                    currentLineCounter++;
                    continue;

                } else {
                    rowData = currentLine.split("\t");
                    //String externalId, String sex, String ethnicity, String race
                    IRCCPatient p = new IRCCPatient(rowData[0], rowData[7], "", "", rowData[8], rowData[10]);

                    patientsMap.put(rowData[0], p);
                    //log.info("Current patient: "+rowData[0]);

                }
                currentLineCounter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //load samples file
        currentLineCounter = 1;
        try {
            BufferedReader buf = new BufferedReader(new FileReader(samplesFile));

            while (true) {
                currentLine = buf.readLine();
                if (currentLine == null) {
                    break;
                } else if (currentLineCounter < 6) {
                    currentLineCounter++;
                    continue;

                } else {
                    rowData = currentLine.split("\t");

                    //String sampleId, String collectionDate, String ageAtCollection, String diagnosis,
                    //String tumorType, String sampleSite, String msiStatus, String krasStatus, String brafStatus,
                    //String nrasStatus, String pik3caStatus

                    IRCCSample sample = new IRCCSample(rowData[1], rowData[4], rowData[5], "Colorectal Adenocarcinoma",
                            rowData[6], rowData[7], rowData[13], rowData[14], rowData[15],
                            rowData[16], rowData[17], rowData[3], rowData[8], rowData[10], rowData[11], rowData[9]);

                    if (samplesMap.containsKey(rowData[0])) {
                        samplesMap.get(rowData[0]).add(sample);
                    } else {
                        List<IRCCSample> ls = new ArrayList<>();
                        ls.add(sample);
                        samplesMap.put(rowData[0], ls);
                    }

                }
                currentLineCounter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Loading data to Neo4j
        DS = loaderUtils.getExternalDataSource(DATASOURCE_ABBREVIATION, DATASOURCE_NAME, DATASOURCE_DESCRIPTION);
        //nsgBS = loaderUtils.getBackgroundStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);


        for (Map.Entry<String, List<IRCCSample>> entry : samplesMap.entrySet()) {
            String key = entry.getKey();
            List<IRCCSample> samples = entry.getValue();

            for (int i = 0; i < samples.size(); i++) {

                PatientSnapshot pSnap = loaderUtils.getPatientSnapshot(key, patientsMap.get(key).getSex(),
                        "", "", samples.get(i).getAgeAtCollection(), DS);


                Sample sample = loaderUtils.getSample(samples.get(i).getSampleId(), samples.get(i).getTumorType(),
                        samples.get(i).getDiagnosis(), patientsMap.get(key).getPrimarySite(),
                        samples.get(i).getSampleSite(), "", NORMAL_TISSUE, DS);


                HashMap<String, Set<MarkerAssociation>> markerMap = new HashMap<>();

                if (samples.get(i).getMsiStatus() != "NA") {
                    MarkerAssociation msia = loaderUtils.getMarkerAssociation(samples.get(i).getMsiStatus(), "MSI", "MSI");
                    // make a map of markerAssociationCollections keyed to technology
                    if (markerMap.containsKey(MC_TECH)) {
                        markerMap.get(MC_TECH).add(msia);
                    } else {
                        HashSet<MarkerAssociation> set = new HashSet<>();
                        set.add(msia);
                        markerMap.put(MC_TECH, set);
                    }
                }

                if (samples.get(i).getKrasStatus() != "NA") {
                    MarkerAssociation krasa = loaderUtils.getMarkerAssociation(samples.get(i).getKrasStatus(), "KRAS", "KRAS");
                    // make a map of markerAssociationCollections keyed to technology
                    markerMap.get(MC_TECH).add(krasa);
                }

                if (samples.get(i).getBrafStatus() != "NA") {
                    MarkerAssociation brafa = loaderUtils.getMarkerAssociation(samples.get(i).getBrafStatus(), "BRAF", "BRAF");
                    // make a map of markerAssociationCollections keyed to technology
                    markerMap.get(MC_TECH).add(brafa);
                }

                if (samples.get(i).getNrasStatus() != "NA") {
                    MarkerAssociation nrasa = loaderUtils.getMarkerAssociation(samples.get(i).getNrasStatus(), "NRAS", "NRAS");
                    // make a map of markerAssociationCollections keyed to technology
                    markerMap.get(MC_TECH).add(nrasa);
                }
                if (samples.get(i).getPik3caStatus() != "NA") {
                    MarkerAssociation pik3caa = loaderUtils.getMarkerAssociation(samples.get(i).getPik3caStatus(), "PIK3CA", "PIK3CA");
                    // make a map of markerAssociationCollections keyed to technology
                    markerMap.get(MC_TECH).add(pik3caa);
                }


                HashSet<MolecularCharacterization> mcs = new HashSet<>();
                for (String tech : markerMap.keySet()) {
                    MolecularCharacterization mc = new MolecularCharacterization();
                    mc.setTechnology(tech);
                    mc.setMarkerAssociations(markerMap.get(tech));

                    loaderUtils.saveMolecularCharacterization(mc);
                    mcs.add(mc);

                }
                sample.setMolecularCharacterizations(mcs);

                pSnap.addSample(sample);
                loaderUtils.savePatientSnapshot(pSnap);

                QualityAssurance qa = new QualityAssurance("Fingerprint", "Fingerprint", ValidationTechniques.FINGERPRINT);
                loaderUtils.saveQualityAssurance(qa);

                loaderUtils.createModelCreation(samples.get(i).getModelId(), samples.get(i).getImplantSite(), samples.get(i).getImplantType(), sample, nsgBS, qa);


            }


        }


    }


}