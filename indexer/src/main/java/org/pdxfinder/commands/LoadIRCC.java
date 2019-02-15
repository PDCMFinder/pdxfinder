package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.neo4j.ogm.session.Session;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Load data from IRCC.
 */
@Component
@Order(value = -19)
public class LoadIRCC implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadIRCC.class);

    private final static String DATASOURCE_ABBREVIATION = "IRCC-CRC";
    private final static String DATASOURCE_NAME = "Candiolo Cancer Institute - Colorectal";
    private final static String DATASOURCE_DESCRIPTION = "IRCC";
    private final static String DATASOURCE_CONTACT = "andrea.bertotti@ircc.it";

    private final static String PROVIDER_TYPE = "";
    private final static String ACCESSIBILITY = "";

    private final static String NSG_BS_NAME = "NOD scid gamma";
    private final static String NSG_BS_SYMBOL = "NOD.Cg-Prkdc<sup>scid</sup> Il2rg<sup>tm1Wjl</sup>/SzJ"; //yay HTML in name
    private final static String NSG_BS_URL = "http://jax.org/strain/005557";
    
    private final static String TECH = "MUT targeted NGS";

    private final static String DOSING_STUDY_URL = "/platform/ircc-dosing-studies/";
    private final static String TARGETEDNGS_PLATFORM_URL = "/platform/ircc-gene-panel/";
    private final static String SOURCE_URL = "/source/ircc-crc/";

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    private final static String NOT_SPECIFIED = Standardizer.NOT_SPECIFIED;
    public static final String FINGERPRINT_DESCRIPTION = "Model validated against patient germline.";

    private HostStrain nsgBS;
    private Group irccDS;

    private Group projectGroup;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private DataImportService dataImportService;
    private Session session;

    @Autowired
    private UtilityService utilityService;

    // samples -> markerAsssociations
    private HashMap<String, HashSet<MarkerAssociation>> markerAssociations = new HashMap();
    private HashMap<String, HashMap<String, String>> specimenSamples = new HashMap();
    private HashMap<String, HashMap<String, String>> modelSamples = new HashMap();

    private HashSet<Integer> loadedModelHashes = new HashSet<>();

    @Value("${pdxfinder.data.root.dir}")
    private String dataRootDir;

    @Value("${irccpdx.variation.max}")
    private int variationMax;

    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadIRCC(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadIRCC", "Load IRCC PDX data");
        parser.accepts("loadALL", "Load all, including IRCC PDX data");
        OptionSet options = parser.parse(args);


        if (options.has("loadIRCC") || options.has("loadALL")) {

            log.info("Loading IRCC PDX data.");

            String fileStr = dataRootDir+DATASOURCE_ABBREVIATION+"/pdx/models.json";
            File file = new File(fileStr);

            if(file.exists()){

                irccDS = dataImportService.getProviderGroup(DATASOURCE_NAME, DATASOURCE_ABBREVIATION,
                        DATASOURCE_DESCRIPTION, PROVIDER_TYPE, ACCESSIBILITY, "transnational access", DATASOURCE_CONTACT, SOURCE_URL);

                try {
                    nsgBS = dataImportService.getHostStrain(NSG_BS_NAME, NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_NAME);
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                projectGroup = dataImportService.getProjectGroup("EurOPDX");


                parseModels(utilityService.parseFile(fileStr));

                String variationURLStr = dataRootDir+DATASOURCE_ABBREVIATION+"/mut/data.json";
                File varFile = new File(variationURLStr);

                if(varFile.exists()){

                    if (variationURLStr != null && variationMax != 0) {
                        loadVariants(variationURLStr, "TargetedNGS_MUT", "mutation");
                    }
                }


            }
            else{

                log.info("No file found for "+DATASOURCE_ABBREVIATION+", skipping");
            }



        }
    }

    private void parseModels(String json) {

        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("IRCC");

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);

                createGraphObjects(j);
            }

        } catch (Exception e) {
            log.error("Error getting IRCC PDX models", e);

        }
    }

    @Transactional
    void createGraphObjects(JSONObject job) throws Exception {

        if(loadedModelHashes.contains(job.toString().hashCode())) return;
        loadedModelHashes.add(job.toString().hashCode());

        // RETRIEVE THE METADATA:
        LoaderDTO dto = dataImportService.getMetadata(job, DATASOURCE_ABBREVIATION);

        PatientSnapshot pSnap = dto.getPatientSnapshot();
        pSnap.addSample(dto.getPatientSample());

        dto.setNodScidGamma(nsgBS);
        dto.setProjectGroup(projectGroup);
        dto.setProviderGroup(irccDS);

        // CREATE MODEL-CREATION
        dto.setModelCreation(
                dataImportService.createModelCreation(dto.getModelID(), this.irccDS.getAbbreviation(), dto.getPatientSample(), dto.getQualityAssurance(), dto.getExternalUrls())
        );

        dto = dataImportService.loaderSecondStep(dto, pSnap, DATASOURCE_ABBREVIATION);





        //Create Treatment summary without linking TreatmentProtocols to specimens
        TreatmentSummary ts;


        try{
            if(job.has("Treatment")){
                JSONObject treatment = job.optJSONObject("Treatment");
                //if the treatment attribute is not an object = it is an array
                if(treatment == null && job.optJSONArray("Treatment") != null){

                    JSONArray treatments = job.getJSONArray("Treatment");

                    if(treatments.length() > 0){
                        //log.info("Treatments found for model "+mc.getSourcePdxId());
                        ts = new TreatmentSummary();
                        ts.setUrl(DOSING_STUDY_URL);
                        for(int t = 0; t<treatments.length(); t++){
                            JSONObject treatmentObject = treatments.getJSONObject(t);


                            TreatmentProtocol tp = dataImportService.getTreatmentProtocol(treatmentObject.getString("Drug"),
                                    treatmentObject.getString("Dose"), treatmentObject.getString("Response Class"), "");

                            if(tp != null){
                                ts.addTreatmentProtocol(tp);
                            }
                        }

                        ts.setModelCreation(dto.getModelCreation());
                        dto.getModelCreation().setTreatmentSummary(ts);
                    }
                }

            }


        }
        catch(Exception e){

            e.printStackTrace();
        }

        dataImportService.savePatient(dto.getPatient());
        dataImportService.saveModelCreation(dto.getModelCreation());
        
    }

    @Transactional
    public void loadVariants(String variationURLStr, String platformName, String molcharType){

        log.info("Loading variation for platform "+platformName);
        //STEP 1: Save the platform
        Platform platform = dataImportService.getPlatform(platformName, this.irccDS);
        platform.setGroup(irccDS);
        platform.setUrl(TARGETEDNGS_PLATFORM_URL);
        dataImportService.savePlatform(platform);


        //STEP 2: get markers and save them with the platform linked
        try{

            JSONObject job = new JSONObject(utilityService.parseFile(variationURLStr));
            JSONArray jarray = job.getJSONArray("IRCCVariation");
            Set<String> markers = new HashSet<>();
            log.info("Saving Markers to DB");
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject variation = jarray.getJSONObject(i);
                String gene = variation.getString("Gene");
                markers.add(gene);
            }

            for(String m:markers){
                Marker marker = dataImportService.getMarker(m, m);
                //PlatformAssociation pa = loaderUtils.createPlatformAssociation(platform, marker);
                //loaderUtils.savePlatformAssociation(pa);

            }
            log.info("Saved "+markers.size()+" to the DB.");

            //STEP 3: assemble MolecularCharacterization objects for samples

            //sampleId = > molchar
            HashMap<String, MolecularCharacterization> xenoSampleMolCharMap = new HashMap();
            HashMap<String, MolecularCharacterization> humanSampleMolCharMap = new HashMap();


            for (int i = 0; i < jarray.length(); i++) {
                if (i == variationMax) {
                    System.out.println("qutting after loading "+i+" variants");
                    break;
                }

                JSONObject variation = jarray.getJSONObject(i);

                String modelId = variation.getString("Model ID");
                String sample = variation.getString("Sample ID");
                String specimen = variation.getString("Specimen ID");

                String sampleId = variation.getString("Specimen ID");
                String samplePlatformId = sampleId+"____"+platformName;


                String gene = variation.getString("Gene");
                String type = variation.getString("Type");

                Marker marker = dataImportService.getMarker(gene,gene);

                MarkerAssociation ma = new MarkerAssociation();

                ma.setMarker(marker);
                ma.setType(type);
                ma.setCdsChange(variation.getString("CDS"));
                ma.setChromosome(variation.getString("Chrom"));
                ma.setConsequence(variation.getString("Effect"));
                ma.setSeqPosition(variation.getString("Pos"));
                ma.setRefAllele(variation.getString("Ref"));
                ma.setAltAllele(variation.getString("Alt"));
                ma.setAminoAcidChange(variation.getString("Protein"));
                ma.setAlleleFrequency(variation.getString("VAF"));
                ma.setRsVariants(variation.getString("avsnp147"));

                // STEP 4: Determine if sample is human or xenograft
                if(specimen.startsWith(modelId+"H")){

                    if(humanSampleMolCharMap.containsKey(sampleId)){
                        humanSampleMolCharMap.get(sampleId).addMarkerAssociation(ma);
                    }
                    else{
                        MolecularCharacterization mcNew = new MolecularCharacterization();
                        mcNew.setPlatform(platform);
                        mcNew.setType(molcharType);
                        mcNew.addMarkerAssociation(ma);


                        humanSampleMolCharMap.put(modelId,mcNew);

                    }

                }
                else if(specimen.startsWith(modelId+"X")){


                    if(xenoSampleMolCharMap.containsKey(sampleId)){
                        xenoSampleMolCharMap.get(sampleId).addMarkerAssociation(ma);
                    }
                    else{
                        MolecularCharacterization mcNew = new MolecularCharacterization();
                        mcNew.setPlatform(platform);
                        mcNew.setType(molcharType);
                        mcNew.addMarkerAssociation(ma);


                        xenoSampleMolCharMap.put(sampleId,mcNew);

                    }
                }
                else{

                    //something is not right
                    log.error("Cannot determine if sample human or xeno for:"+specimen);
                }



            }


            //STEP 5: loop through xenoSampleMolCharMap and humanSampleMolCharMap to hook mc objects to proper samples then save the graph
            for (Map.Entry<String, MolecularCharacterization> entry : xenoSampleMolCharMap.entrySet()) {
                String sampleId = entry.getKey();
                MolecularCharacterization mc = entry.getValue();
                try{
                    Sample s = dataImportService.findSampleByDataSourceAndSourceSampleId(irccDS.getAbbreviation(), sampleId);

                    if(s == null){
                        log.error("Sample not found: "+sampleId);
                    }
                    else{
                        s.addMolecularCharacterization(mc);
                        dataImportService.saveSample(s);
                        log.info("Saving molchar for sample: "+sampleId);
                    }
                }
                catch(Exception e1){

                    log.error(sampleId);
                    e1.printStackTrace();
                }

            }

            for (Map.Entry<String, MolecularCharacterization> entry : humanSampleMolCharMap.entrySet()) {
                String modelId = entry.getKey();
                MolecularCharacterization mc = entry.getValue();
                try{


                    Sample s = dataImportService.findHumanSample(modelId, irccDS.getAbbreviation());

                    if(s == null){
                        log.error("Human sample not found for model: "+modelId);
                    }
                    else{
                        s.addMolecularCharacterization(mc);
                        dataImportService.saveSample(s);
                        log.info("Saving molchar for human sample: "+modelId);
                    }
                }
                catch(Exception e1){

                    e1.printStackTrace();
                }

            }




        }
        catch (Exception e){

            e.printStackTrace();
        }

    }
    
    
     @Transactional
    public void loadVariantsBySpecimen() {

        try {
            String variationURLStr = dataRootDir+DATASOURCE_ABBREVIATION+"/mut/data.json";
            JSONObject job = new JSONObject(utilityService.parseFile(variationURLStr));
            JSONArray jarray = job.getJSONArray("IRCCVariation");
         //   System.out.println("loading "+jarray.length()+" variant records");

            Platform platform = dataImportService.getPlatform(TECH, this.irccDS, TARGETEDNGS_PLATFORM_URL);
            platform.setGroup(irccDS);
            dataImportService.savePlatform(platform);


            for (int i = 0; i < jarray.length(); i++) {
                if (i == variationMax) {
                    System.out.println("qutting after loading "+i+" variants");
                    break;
                }

                JSONObject variation = jarray.getJSONObject(i);

                String modelId = variation.getString("Model ID");
                String sample = variation.getString("Sample ID");
                String specimen = variation.getString("Specimen ID");
                
               // System.out.println("specimen "+specimen+" has sample "+sample);
                
                if(specimenSamples.containsKey(specimen)){
                    specimenSamples.get(specimen).put(sample, sample);
                }else{
                    HashMap<String,String> samples = new HashMap();
                    samples.put(sample,sample);
                    specimenSamples.put(specimen,samples);
                }
                        
                
                String gene = variation.getString("Gene");
                String type = variation.getString("Type");
                
                Marker marker = dataImportService.getMarker(gene,gene);
                
                MarkerAssociation ma = new MarkerAssociation();
                
                ma.setMarker(marker);
                ma.setType(type);
                ma.setCdsChange(variation.getString("CDS"));
                ma.setChromosome(variation.getString("Chrom"));
                ma.setConsequence(variation.getString("Effect"));
                ma.setSeqPosition(variation.getString("Pos"));
                ma.setRefAllele(variation.getString("Ref"));
                ma.setAltAllele(variation.getString("Alt"));
                ma.setAminoAcidChange(variation.getString("Protein"));
                ma.setAlleleFrequency(variation.getString("VAF"));
                ma.setRsVariants(variation.getString("avsnp147"));

                PlatformAssociation pa = dataImportService.createPlatformAssociation(platform, marker);
                dataImportService.savePlatformAssociation(pa);

                if (markerAssociations.containsKey(sample)) {
                    markerAssociations.get(sample).add(ma);
                } else {
                    HashSet<MarkerAssociation> mas = new HashSet();
                    mas.add(ma);
                    markerAssociations.put(sample, mas);
                }

            }

        } catch (Exception e) {
            log.error("Unable to load variants");
            e.printStackTrace();
        }
       
    }

}
