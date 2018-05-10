package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.neo4j.ogm.session.Session;
import org.pdxfinder.dao.*;
import org.pdxfinder.irccdatamodel.IRCCMarkerMutation;
import org.pdxfinder.irccdatamodel.IRCCPatient;
import org.pdxfinder.irccdatamodel.IRCCSample;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by csaba on 18/04/2017.
 */
@Component
@Order(value = 0)
public class LoadIRCCData implements CommandLineRunner {


    private final static Logger log = LoggerFactory.getLogger(LoadJAXData.class);

    private final static String DATASOURCE_ABBREVIATION = "IRCC";
    private final static String DATASOURCE_NAME = "IRCC data";
    private final static String DATASOURCE_DESCRIPTION = "IRCC PDX mouse models.";
    private final static String DATASOURCE_CONTACT = "andrea.bertotti@ircc.it";

    //private final static String NSG_BS_NAME = "NSG (NOD scid gamma)"; //background strain
    //private final static String NSG_BS_SYMBOL = "NOD.Cg-Prkdc<sup>scid</sup> Il2rg<sup>tm1Wjl</sup>/SzJ"; //yay HTML in name
    //private final static String NSG_BS_URL = "http://jax.org/strain/005557";

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE = false;

    // hmm not sure about this
    private final static String MC_TECH = "Gene Panel";

    private HostStrain nsgBS;
    private ExternalDataSource DS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private DataImportService dataImportService;
    private Session session;


    Map<String, IRCCPatient> patientsMap;
    Map<String, List<IRCCSample>> samplesMap;
    Map<String, List<IRCCMarkerMutation>> markersMutationMap;

    @Value("${irccpatients.file}")
    private String patientsFile;

    @Value("${irccsamples.file}")
    private String samplesFile;

    @Value("${irccmarkermutation.file}")
    private String markerMutationsFile;


    @Override
    @Transactional
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadIRCCOLD", "Load IRCC PDX data");
        parser.accepts("loadALLOLD", "Load all, including JAX PDX data");
        OptionSet options = parser.parse(args);

        if (options.has("loadOLDIRCC")) {

            log.info("NOT Loading OLD! IRCC PDX data.");

         //   loadDataFromFiles();
            //validateData();
         //   loadToNeo4j();


        }
    }

    public LoadIRCCData(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }


    private void loadDataFromFiles() {

        this.patientsMap = new HashMap<>();
        this.samplesMap = new HashMap<>();
        this.markersMutationMap = new HashMap<>();


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
                } else if (currentLineCounter < 5) {
                    currentLineCounter++;
                    continue;

                } else {
                    rowData = currentLine.split("\t");

                    //String sampleId, String collectionDate, String ageAtCollection, String diagnosis,
                    //String tumorType, String sampleSite, String msiStatus, String krasStatus, String brafStatus,
                    //String nrasStatus, String pik3caStatus

                    IRCCSample sample = new IRCCSample(rowData[1], rowData[4], rowData[5], "Colorectal Adenocarcinoma",
                            rowData[6], rowData[7], rowData[13], rowData[14], rowData[15],
                            rowData[16], rowData[17], rowData[3], rowData[8], rowData[10], rowData[11], rowData[9], rowData[27]);

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


        //load marker mutations file
        int lineCounter = 1;
        try {
            BufferedReader buf = new BufferedReader(new FileReader(markerMutationsFile));


            while (true) {
                currentLine = buf.readLine();
                if (currentLine == null) {
                    break;
                } else if (lineCounter < 2) {
                    lineCounter++;
                    continue;

                } else {

                    rowData = currentLine.split("\t");
                    //Hugo_Symbol	Entrez_Gene_Id	NCBI_Build	gene name	exon	chromosome	start	end	Chromosome
                    // Start_Position	End_Position	Strand	Tumor_Sample_Barcode	Variant_Classification	HGVSp_Short
                    // Protein_position	SWISSPROT	Xeno_Passage	Platform

                    //String hugoSymbol, String entrezId, String ncbiBuild, String exon, String chromosome,
                    //String start, String end, String startPosition, String endPosition, String strand,
                    //        String hgvspShort, String proteinPosition, String swissprot, String xenoPassage, String platform

                    IRCCMarkerMutation mm = new IRCCMarkerMutation(rowData[12], rowData[0], rowData[1], rowData[2], rowData[4], rowData[5],
                            rowData[6], rowData[7], rowData[9], rowData[10], rowData[11],
                            rowData[14], rowData[15], rowData[16], rowData[17], rowData[18], rowData[13]);
                    //create row id by combining model id plus marker symbol

                    if (this.markersMutationMap.containsKey(rowData[12])) {
                        this.markersMutationMap.get(rowData[12]).add(mm);
                    } else {
                        List<IRCCMarkerMutation> list = new ArrayList<>();
                        list.add(mm);
                        this.markersMutationMap.put(rowData[12], list);
                    }

                    lineCounter++;

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void validateData() {

        List<String> errors = new ArrayList<>();
        List<String> errors2 = new ArrayList<>();
        List<String> patientsWithoutSamples = new ArrayList<>();

        int counter = 0;
        for (Map.Entry<String, List<IRCCSample>> entry : this.samplesMap.entrySet()) {

            String key = entry.getKey();
            System.out.println(key);
            List<IRCCSample> samples = entry.getValue();

            //if(counter>15) break;
            for (int i = 0; i < samples.size(); i++) {
                String modelId = samples.get(i).getModelId();

                String kras = samples.get(i).getKrasStatus();
                String braf = samples.get(i).getBrafStatus();
                String nras = samples.get(i).getNrasStatus();
                String pik3ca = samples.get(i).getPik3caStatus();

                if ((kras.equals("WT") || kras.equals("NA")) && this.markersMutationMap.containsKey(modelId + "_KRAS")) {
                    errors.add(modelId + ": KRAS is WT or NA but has mutation data.");
                }

                if ((braf.equals("WT") || braf.equals("NA")) && this.markersMutationMap.containsKey(modelId + "_BRAF")) {
                    errors.add(modelId + ": BRAF is WT or NA but has mutation data.");
                }

                if ((nras.equals("WT") || nras.equals("NA")) && this.markersMutationMap.containsKey(modelId + "_NRAS")) {
                    errors.add(modelId + ": NRAS is WT or NA but has mutation data.");
                }

                if ((pik3ca.equals("WT") || pik3ca.equals("NA")) && this.markersMutationMap.containsKey(modelId + "_PIK3CA")) {
                    errors.add(modelId + ": PIK3CA is WT or NA but has mutation data.");
                }


                if (kras.toLowerCase().contains("mut") && !this.markersMutationMap.containsKey(modelId + "_KRAS")) {
                    errors2.add(modelId + ": KRAS is mutated but has no mutation data.");
                }

                if (braf.toLowerCase().contains("mut") && !this.markersMutationMap.containsKey(modelId + "_BRAF")) {
                    errors2.add(modelId + ": BRAF is mutated but has no mutation data.");
                }

                if (nras.toLowerCase().contains("mut") && !this.markersMutationMap.containsKey(modelId + "_NRAS")) {
                    errors2.add(modelId + ": NRAS is mutated but has no mutation data.");
                }

                if (pik3ca.toLowerCase().contains("mut") && !this.markersMutationMap.containsKey(modelId + "_PIK3CA")) {
                    errors2.add(modelId + ": PIK3CA is mutated but has no mutation data.");
                }

            }
        }
        Collections.sort(errors);
        Collections.sort(errors2);

        for (Map.Entry<String, IRCCPatient> entry : this.patientsMap.entrySet()) {

            String key = entry.getKey();

            if (!this.samplesMap.containsKey(key)) {
                patientsWithoutSamples.add(key);
            }

        }

        System.out.println("Patients without samples: " + patientsWithoutSamples.size());
        for (String err : patientsWithoutSamples) {
            System.out.println(err);
        }

        System.out.println("Mutation data found for WT or NA errors: " + errors.size());
        for (String err : errors) {
            System.out.println(err);
        }
        System.out.println("Missing mutation data errors: " + errors2.size());
        for (String err : errors2) {
            System.out.println(err);
        }


    }

    private void loadToNeo4j() {

        DS = dataImportService.getExternalDataSource(DATASOURCE_ABBREVIATION, DATASOURCE_NAME, DATASOURCE_DESCRIPTION,DATASOURCE_CONTACT, null);

        for (Map.Entry<String, List<IRCCSample>> entry : this.samplesMap.entrySet()) {

            String patientId = entry.getKey();
            log.debug("Loading data for patient "+patientId);
            List<IRCCSample> samples = entry.getValue();


            for (int i = 0; i < samples.size(); i++) {
                IRCCSample s = samples.get(i);
                IRCCPatient p = patientsMap.get(patientId);

                String sampleId = s.getSampleId();
                String modelId = s.getModelId();

                PatientSnapshot pSnap = dataImportService.getPatientSnapshot(patientId, p.getSex(),
                        p.getRace(), p.getEthnicity(), s.getAgeAtCollection(), DS);


                Sample humanSample = null;
                Sample mouseSample = null;


                //If the markersMutationMap does not contain the sampleid => human sample
                //If it contains but the xeno_passage is 0 => human sample
                //If its all WT =>  human
                //It's a mouse sample otherwise and the passage is xeno_passage - 1

                //create a human sample
                //getSample(String sourceSampleId, String typeStr, String diagnosis,
                // String originStr, String sampleSiteStr, String extractionMethod, String classification, Boolean normalTissue, ExternalDataSource externalDataSource) {

                humanSample = dataImportService.getSample(sampleId, s.getTumorType(), s.getDiagnosis(),
                        patientsMap.get(patientId).getPrimarySite(), s.getSampleSite(), "Extraction Method", "", NORMAL_TISSUE, DS.getAbbreviation());


                pSnap.addSample(humanSample);
                dataImportService.savePatientSnapshot(pSnap);

                QualityAssurance qa = new QualityAssurance("Fingerprint", "Fingerprint",  "");
                dataImportService.saveQualityAssurance(qa);

                List<ExternalUrl> externalUrls = new ArrayList<>();
                externalUrls.add( dataImportService.getExternalUrl(ExternalUrl.Type.CONTACT, DATASOURCE_CONTACT));

                ModelCreation modelCreation = dataImportService.createModelCreation(modelId, DS.getAbbreviation(), humanSample, qa, externalUrls);

                // determine whether sample is from human or mouse
                if (markersMutationMap.containsKey(sampleId)) {

                    List<IRCCMarkerMutation> mutations = markersMutationMap.get(sampleId);

                    for (IRCCMarkerMutation mutation : mutations) {
                        Boolean isHumanSample = false;

                        if (mutation.getXenoPassage().equals("0")) {
                            isHumanSample = true;
                        }


                        Marker m = dataImportService.getMarker(mutation.getHugoSymbol(), mutation.getHugoSymbol());

                        MarkerAssociation ma = new MarkerAssociation();
                        ma.setChromosome(mutation.getChromosome());
                        ma.setSeqStartPosition(mutation.getStartPosition());
                        ma.setSeqEndPosition(mutation.getEndPosition());
                        ma.setMarker(m);

                        //Set<MarkerAssociation> mas = new HashSet<>();
                        //mas.add(ma);
                        MolecularCharacterization mc = null;

                        if (isHumanSample) {

                            if (humanSample.getMolecularCharacterizations() == null) {

                                mc = new MolecularCharacterization(mutation.getPlatform());
                                List<MarkerAssociation> mas = new ArrayList<>();
                                mas.add(ma);
                                mc.setMarkerAssociations(mas);
                                Set<MolecularCharacterization> mcs = new HashSet<>();
                                mcs.add(mc);
                                humanSample.setMolecularCharacterizations(mcs);
                            } else {

                                for (MolecularCharacterization mc2 : humanSample.getMolecularCharacterizations()) {

                                    if (mc2.getTechnology().equals(mutation.getPlatform())) {
                                        mc = mc2;
                                        break;
                                    }
                                }

                                if (mc == null) {
                                    mc = new MolecularCharacterization(mutation.getPlatform());
                                    mc.setMarkerAssociations(new ArrayList<>());
                                    humanSample.getMolecularCharacterizations().add(mc);
                                }

                                mc.getMarkerAssociations().add(ma);

                            }

                            dataImportService.saveSample(humanSample);

                        } else {
                            //this is a mouse sample, link it to a specimen
                            int passage = Integer.valueOf(mutation.getXenoPassage());
                            passage -= 1; // its an ircc thing, if the passage is 0, it is a human sample, otherwise passage = xenopassage -1;
                            String pass = String.valueOf(passage);
                            Specimen specimen = dataImportService.getSpecimen(modelCreation, modelCreation.getSourcePdxId(), DS.getAbbreviation(), pass);

                            if (specimen.getSample() == null) {

                                mouseSample = new Sample();
                                mouseSample.setSourceSampleId(mutation.getSampleId());

                                mc = new MolecularCharacterization(mutation.getPlatform());
                                List<MarkerAssociation> list = new ArrayList<>();
                                list.add(ma);
                                mc.setMarkerAssociations(list);
                                Set<MolecularCharacterization> mcs = new HashSet<>();
                                mcs.add(mc);
                                mouseSample.setMolecularCharacterizations(mcs);
                                specimen.setSample(mouseSample);

                            } else { // a sample is linked to the specimen already

                                mouseSample = specimen.getSample();
                                if (mouseSample.getMolecularCharacterizations() == null) {
                                    // no molchars on the mouse sample, create one

                                    mc = new MolecularCharacterization(mutation.getPlatform());
                                    List<MarkerAssociation> list = new ArrayList<>();
                                    list.add(ma);
                                    mc.setMarkerAssociations(list);
                                    Set<MolecularCharacterization> mcs = new HashSet<>();
                                    mcs.add(mc);
                                    mouseSample.setMolecularCharacterizations(mcs);
                                } else { // the mouse sample already has molchar(s)

                                    Set<MolecularCharacterization> mcs = specimen.getSample().getMolecularCharacterizations();
                                    // find the molchar with the same technology
                                    for (MolecularCharacterization mc2 : mcs) {

                                        if (mc2.getTechnology().equals(mutation.getPlatform())) {

                                            mc = mc2;
                                            break;
                                        }
                                    }
                                    // if that technology wasn't used before, add it
                                    if (mc == null) {
                                        mc = new MolecularCharacterization(mutation.getPlatform());
                                        mc.setMarkerAssociations(new ArrayList<>());
                                        mouseSample.getMolecularCharacterizations().add(mc);
                                    }

                                    mc.getMarkerAssociations().add(ma);

                                }
                            }

                            dataImportService.saveSample(mouseSample);
                            dataImportService.saveSpecimen(specimen);


                        }


                        //System.out.println();
                    }


                }

            }


        }

    }


}