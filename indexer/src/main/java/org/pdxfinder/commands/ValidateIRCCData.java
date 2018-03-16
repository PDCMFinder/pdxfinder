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
import org.pdxfinder.utilities.LoaderUtils;
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
@Order(value = 200)
public class ValidateIRCCData implements CommandLineRunner {


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

    private LoaderUtils loaderUtils;
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
        parser.accepts("validateIRCC", "Validate IRCC data");
        parser.accepts("loadALL", "Load all, including validating IRCC data");
        OptionSet options = parser.parse(args);

        if (options.has("validateIRCC")) {

            log.info("Loading IRCC PDX data.");


            loadDataFromFiles();
            //loadDataToNeo4j();

        }
    }

    public ValidateIRCCData(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
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
                }
                else if (lineCounter < 2) {
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

                    IRCCMarkerMutation mm = new IRCCMarkerMutation(rowData[12],rowData[0],rowData[1],rowData[2],rowData[4], rowData[5],
                            rowData[6], rowData[7],rowData[9], rowData[10],rowData[11],
                            rowData[14], rowData[15], rowData[16], rowData[17], rowData[18], rowData[13]);
                    //create row id by combining model id plus marker symbol

                    if(this.markersMutationMap.containsKey(rowData[12]+"_"+rowData[0])){
                        this.markersMutationMap.get(rowData[12]+"_"+rowData[0]).add(mm);
                    }
                    else{
                        List<IRCCMarkerMutation> list = new ArrayList<>();
                        list.add(mm);
                        this.markersMutationMap.put(rowData[12]+"_"+rowData[0], list);
                    }

                    lineCounter++;

                    System.out.println(rowData[12]+"_"+rowData[0]);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        validateData();
        //loadDataToNeo4j();
    }

    private void validateData(){

        List<String> errors = new ArrayList<>();
        List<String> errors2 = new ArrayList<>();

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

                if((kras.equals("WT") || kras.equals("NA")) && this.markersMutationMap.containsKey(modelId+"_KRAS") ){
                    errors.add(modelId+": KRAS is WT or NA but has mutation data.");
                }

                if((braf.equals("WT") || braf.equals("NA")) && this.markersMutationMap.containsKey(modelId+"_BRAF") ){
                    errors.add(modelId+": BRAF is WT or NA but has mutation data.");
                }

                if((nras.equals("WT") || nras.equals("NA")) && this.markersMutationMap.containsKey(modelId+"_NRAS") ){
                    errors.add(modelId+": NRAS is WT or NA but has mutation data.");
                }

                if((pik3ca.equals("WT") || pik3ca.equals("NA")) && this.markersMutationMap.containsKey(modelId+"_PIK3CA") ){
                    errors.add(modelId+": PIK3CA is WT or NA but has mutation data.");
                }



                if(kras.toLowerCase().contains("mut") && !this.markersMutationMap.containsKey(modelId+"_KRAS") ){
                    errors2.add(modelId+": KRAS is mutated but has no mutation data.");
                }

                if(braf.toLowerCase().contains("mut") && !this.markersMutationMap.containsKey(modelId+"_BRAF") ){
                    errors2.add(modelId+": BRAF is mutated but has no mutation data.");
                }

                if(nras.toLowerCase().contains("mut") && !this.markersMutationMap.containsKey(modelId+"_NRAS") ){
                    errors2.add(modelId+": NRAS is mutated but has no mutation data.");
                }

                if(pik3ca.toLowerCase().contains("mut") && !this.markersMutationMap.containsKey(modelId+"_PIK3CA") ){
                    errors2.add(modelId+": PIK3CA is mutated but has no mutation data.");
                }

            }
        }
        Collections.sort(errors);
        Collections.sort(errors2);

        System.out.println("Mutation data found for WT or NA errors: "+errors.size());
        for(String err:errors){
            System.out.println(err);
        }
        System.out.println("Missing mutation data errors: "+errors2.size());
        for(String err:errors2){
            System.out.println(err);
        }


    }



    private void loadDataToNeo4j(){

        //Loading data to Neo4j
        DS = loaderUtils.getExternalDataSource(DATASOURCE_ABBREVIATION, DATASOURCE_NAME, DATASOURCE_DESCRIPTION,DATASOURCE_CONTACT);
        //nsgBS = loaderUtils.getHostStrain(NSG_BS_SYMBOL, NSG_BS_NAME, NSG_BS_NAME, NSG_BS_URL);

        int counter = 0;
        for (Map.Entry<String, List<IRCCSample>> entry : this.samplesMap.entrySet()) {

            String key = entry.getKey();
            System.out.println(key);
            List<IRCCSample> samples = entry.getValue();

            if(counter>15) break;
            for (int i = 0; i < samples.size(); i++) {



                PatientSnapshot pSnap = loaderUtils.getPatientSnapshot(key, patientsMap.get(key).getSex(),
                        "", "", samples.get(i).getAgeAtCollection(), DS);



                Sample sample = loaderUtils.getSample(samples.get(i).getSampleId(), samples.get(i).getTumorType(),
                        samples.get(i).getDiagnosis(), patientsMap.get(key).getPrimarySite(),
                        samples.get(i).getSampleSite(), "", "", NORMAL_TISSUE, DS.getAbbreviation());


                pSnap.addSample(sample);
                loaderUtils.savePatientSnapshot(pSnap);

                QualityAssurance qa = new QualityAssurance("Fingerprint", "Fingerprint", ValidationTechniques.FINGERPRINT, null);
                loaderUtils.saveQualityAssurance(qa);

                ModelCreation mc = loaderUtils.createModelCreation(samples.get(i).getModelId(),DS.getAbbreviation(), sample, qa);

                loadVariationData(mc, samples.get(i));

            }

            counter++;
        }


    }


    private void loadVariationData(ModelCreation modelCreation, IRCCSample sampleRow){

        /*

    String kras = sampleRow.getKrasStatus();
    String braf = sampleRow.getBrafStatus();
    String nras = sampleRow.getNrasStatus();
    String pik3ca = sampleRow.getPik3caStatus();
    String passageNum = "";
    MolecularCharacterization molchar = new MolecularCharacterization();

    //if all four markers are WT, assume its passage 0
    //TODO: check this
    if(kras.equals("WT") && braf.equals("WT") && nras.equals("WT") && pik3ca.equals("WT")){

        MarkerAssociation krasMa = new MarkerAssociation();
        krasMa.setDescription("Wild Type");
        krasMa.setMarker(loaderUtils.getMarker("KRAS"));

        MarkerAssociation brafMa = new MarkerAssociation();
        brafMa.setDescription("Wild Type");
        brafMa.setMarker(loaderUtils.getMarker("BRAF"));

        MarkerAssociation nrasMa = new MarkerAssociation();
        nrasMa.setDescription("Wild Type");
        nrasMa.setMarker(loaderUtils.getMarker("NRAS"));

        MarkerAssociation pik3caMa = new MarkerAssociation();
        pik3caMa.setDescription("Wild Type");
        pik3caMa.setMarker(loaderUtils.getMarker("PIK3CA"));

        HashSet<MarkerAssociation> mas = new HashSet<>();
        mas.add(krasMa);
        mas.add(brafMa);
        mas.add(nrasMa);
        mas.add(pik3caMa);

        molchar.setMarkerAssociations(mas);

        PdxPassage pdxPassage = new PdxPassage(modelCreation, 0);

        Specimen specimen = loaderUtils.getSpecimen(sampleRow.getSampleId());

        HashSet<MolecularCharacterization> mcs = new HashSet<>();
        mcs.add(molchar);
        specimen.setMolecularCharacterizations(mcs);
        specimen.setPdxPassage(pdxPassage);


        pdxPassage.setModelCreation(modelCreation);

        loaderUtils.savePdxPassage(pdxPassage);
        loaderUtils.saveSpecimen(specimen);

    }else{

        int passage = -1;
        String modelId;

        List<IRCCMarkerMutation> mlist;


        //HashSet<MolecularCharacterization> mcs = new HashSet<>();


        if(kras.toLowerCase().contains("mut")){

            modelId = sampleRow.getModelId()+"_KRAS";
            mlist = this.markersMutationMap.get(modelId);


            for(IRCCMarkerMutation mmut:mlist){
                MarkerAssociation krasMa = new MarkerAssociation();
                krasMa.setMarker(loaderUtils.getMarker("KRAS"));
                krasMa.setChromosome(mmut.getChromosome());
                krasMa.setConsequence(mmut.getVariantClassification());
                krasMa.setAminoAcidChange(mmut.getHgvspShort());
                krasMa.setRefAssembly(mmut.getNcbiBuild());
                krasMa.setSeqStartPosition(mmut.getStartPosition());
                krasMa.setSeqEndPosition(mmut.getEndPosition());
                krasMa.setStrand(mmut.getStrand());
                passage = Integer.parseInt(mmut.getXenoPassage());
                molchar.getMarkerAssociations().add(krasMa);

            }



        }
        else{
            krasMa.setDescription("Wild Type");
            krasMa.setMarker(loaderUtils.getMarker("KRAS"));
        }



        if(braf.toLowerCase().contains("mut")){

            modelId = sampleRow.getModelId()+"_BRAF";
            IRCCMarkerMutation mmut = this.markersMutationMap.get(modelId);
            brafMa.setMarker(loaderUtils.getMarker("BRAF"));
            brafMa.setChromosome(mmut.getChromosome());
            brafMa.setConsequence(mmut.getVariantClassification());
            brafMa.setAminoAcidChange(mmut.getHgvspShort());
            brafMa.setRefAssembly(mmut.getNcbiBuild());
            brafMa.setSeqStartPosition(mmut.getStartPosition());
            brafMa.setSeqEndPosition(mmut.getEndPosition());
            brafMa.setStrand(mmut.getStrand());
            passage = Integer.parseInt(mmut.getXenoPassage());

        }
        else{
            brafMa.setDescription("Wild Type");
            brafMa.setMarker(loaderUtils.getMarker("BRAF"));
        }


        if(nras.toLowerCase().contains("mut")){

            modelId = sampleRow.getModelId()+"_NRAS";
            IRCCMarkerMutation mmut = this.markersMutationMap.get(modelId);
            nrasMa.setMarker(loaderUtils.getMarker("NRAS"));
            nrasMa.setChromosome(mmut.getChromosome());
            nrasMa.setConsequence(mmut.getVariantClassification());
            nrasMa.setAminoAcidChange(mmut.getHgvspShort());
            nrasMa.setRefAssembly(mmut.getNcbiBuild());
            nrasMa.setSeqStartPosition(mmut.getStartPosition());
            nrasMa.setSeqEndPosition(mmut.getEndPosition());
            nrasMa.setStrand(mmut.getStrand());
            passage = Integer.parseInt(mmut.getXenoPassage());

        }
        else{
            nrasMa.setDescription("Wild Type");
            nrasMa.setMarker(loaderUtils.getMarker("NRAS"));
        }



        if(pik3ca.toLowerCase().contains("mut")){

            modelId = sampleRow.getModelId()+"_PIK3CA";
            IRCCMarkerMutation mmut = this.markersMutationMap.get(modelId);
            pik3caMa.setMarker(loaderUtils.getMarker("PIK3CA"));
            pik3caMa.setChromosome(mmut.getChromosome());
            pik3caMa.setConsequence(mmut.getVariantClassification());
            pik3caMa.setAminoAcidChange(mmut.getHgvspShort());
            pik3caMa.setRefAssembly(mmut.getNcbiBuild());
            pik3caMa.setSeqStartPosition(mmut.getStartPosition());
            pik3caMa.setSeqEndPosition(mmut.getEndPosition());
            pik3caMa.setStrand(mmut.getStrand());
            passage = Integer.parseInt(mmut.getXenoPassage());

        }
        else{
            pik3caMa.setDescription("Wild Type");
            pik3caMa.setMarker(loaderUtils.getMarker("PIK3CA"));
        }

        //check if passage number has been changed
        if(passage<0) {
            System.out.println("Assuming 0 passage for "+modelCreation.getSourcePdxId());
            passage = 0;
        }
        PdxPassage pdxPassage = new PdxPassage(modelCreation, passage);
        Specimen specimen = loaderUtils.getSpecimen(sampleRow.getSampleId());



        molchar.setPlatform(loaderUtils.getPlatform("Sanger sequencing", DS));
        Set<MarkerAssociation> mas = new HashSet<>();
        mas.add(krasMa);
        mas.add(nrasMa);
        mas.add(brafMa);
        mas.add(pik3caMa);

        molchar.setMarkerAssociations(mas);

        mcs.add(molchar);
        specimen.setMolecularCharacterizations(mcs);
        specimen.setPdxPassage(pdxPassage);

        pdxPassage.setModelCreation(modelCreation);

        loaderUtils.savePdxPassage(pdxPassage);
        loaderUtils.saveSpecimen(specimen);

    }

    */




    }




}