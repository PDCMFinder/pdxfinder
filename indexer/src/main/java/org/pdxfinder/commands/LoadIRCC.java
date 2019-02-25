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
import java.io.File;
import java.util.*;

/**
 * Load data from IRCC.
 */
@Component
@Order(value = -19)
public class LoadIRCC extends LoaderBase implements CommandLineRunner {

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

            initMethod();

            loaderTemplate();

        }
    }


    @Override
    protected void initMethod() {

        log.info("Loading IRCC PDX data.");
        jsonFile = dataRootDir+DATASOURCE_ABBREVIATION+"/pdx/models.json";

        dataSource = DATASOURCE_ABBREVIATION;
        filesDirectory = "";
        dataSourceAbbreviation = DATASOURCE_ABBREVIATION;
        dataSourceContact = DATASOURCE_CONTACT;
        dosingStudyURL = DOSING_STUDY_URL;
    }

    @Override
    protected void step00GetMetaDataFolder() { }


    @Override
    protected void step02CreateProviderGroup() {

        loadProviderGroup(DATASOURCE_NAME, DATASOURCE_ABBREVIATION, DATASOURCE_DESCRIPTION, PROVIDER_TYPE, ACCESSIBILITY, "transnational access", DATASOURCE_CONTACT, SOURCE_URL);
    }

    @Override
    protected void step03CreateNSGammaHostStrain() {

        loadNSGammaHostStrain(NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_NAME, NSG_BS_NAME);
    }

    @Override
    protected void step04CreateNSHostStrain() { }


    @Override
    protected void step05CreateProjectGroup() {

        loadProjectGroup("EurOPDX");
    }


    @Override
    protected void step06GetPDXModels() {

        loadPDXModels(metaDataJSON,"IRCC");
    }

    // IRCC uses default implementation Steps step07GetMetaData,step08LoadPatientData

    @Override
    protected void step09LoadExternalURLs() {

        loadExternalURLs(DATASOURCE_CONTACT,Standardizer.NOT_SPECIFIED);

        dataImportService.saveSample(dto.getPatientSample());
        dataImportService.savePatientSnapshot(dto.getPatientSnapshot());
    }

    // IRCC uses default implementation Steps Step10CreateModels default

    @Override
    protected void step11LoadSpecimens()throws Exception {

        dto.getModelCreation().addGroup(dto.getProjectGroup());

        JSONArray specimens = dto.getSpecimens();

        for (int i = 0; i < specimens.length(); i++) {
            JSONObject specimenJSON = specimens.getJSONObject(i);

            String specimenId = specimenJSON.getString("Specimen ID");

            Specimen specimen = dataImportService.getSpecimen(dto.getModelCreation(),
                    specimenId, dto.getProviderGroup().getAbbreviation(), specimenJSON.getString("Passage"));

            specimen.setHostStrain(dto.getNodScidGamma());

            EngraftmentSite is = dataImportService.getImplantationSite(specimenJSON.getString("Engraftment Site"));
            specimen.setEngraftmentSite(is);

            EngraftmentType it = dataImportService.getImplantationType(specimenJSON.getString("Engraftment Type"));
            specimen.setEngraftmentType(it);

            Sample specSample = new Sample();

            specSample.setSourceSampleId(specimenId);
            specSample.setDataSource(dto.getProviderGroup().getAbbreviation());

            specimen.setSample(specSample);

            dto.getModelCreation().addSpecimen(specimen);
            dto.getModelCreation().addRelatedSample(specSample);

        }
    }



    @Override
    protected void step12CreateCurrentTreatment() {

        TreatmentSummary ts;
        try {

            if (dto.getTreatments().length() > 0) {

                ts = new TreatmentSummary();
                ts.setUrl(DOSING_STUDY_URL);

                for (int t = 0; t < dto.getTreatments().length(); t++) {

                    JSONObject treatmentObject = dto.getTreatments().getJSONObject(t);

                    TreatmentProtocol treatmentProtocol = dataImportService.getTreatmentProtocol(treatmentObject.getString("Drug"),
                            treatmentObject.getString("Dose"),
                            treatmentObject.getString("Response Class"), "");

                    if (treatmentProtocol != null) {
                        ts.addTreatmentProtocol(treatmentProtocol);
                    }
                }
                ts.setModelCreation(dto.getModelCreation());
                dto.getModelCreation().setTreatmentSummary(ts);
            }

            dataImportService.saveModelCreation(dto.getModelCreation());

        } catch (Exception e) { }

        dataImportService.savePatient(dto.getPatient());
        dataImportService.savePatientSnapshot(dto.getPatientSnapshot());
        dataImportService.saveModelCreation(dto.getModelCreation());
    }


    @Override
    protected void step13LoadImmunoHistoChemistry() {

    }


    @Override
    @Transactional
    protected void step14VariationData() {

        log.info("VARIATION DATA LOADING");
        String variationURLStr = dataRootDir+DATASOURCE_ABBREVIATION+"/mut/data.json";
        String platformName = "TargetedNGS_MUT";
        String molcharType = "mutation";

        File varFile = new File(variationURLStr);

        if(varFile.exists()){
            if (variationURLStr != null && variationMax != 0) {

                log.info("Loading variation for platform "+platformName);
                //STEP 1: Save the platform
                Platform platform = dataImportService.getPlatform(platformName, dto.getProviderGroup());
                platform.setGroup(dto.getProviderGroup());
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
                            Sample s = dataImportService.findSampleByDataSourceAndSourceSampleId(dto.getProviderGroup().getAbbreviation(), sampleId);

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

                            Sample s = dataImportService.findHumanSample(modelId, dto.getProviderGroup().getAbbreviation());

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
        }


    }









    @Transactional
    public void loadVariantsBySpecimen() {

        try {
            String variationURLStr = dataRootDir+DATASOURCE_ABBREVIATION+"/mut/data.json";
            JSONObject job = new JSONObject(utilityService.parseFile(variationURLStr));
            JSONArray jarray = job.getJSONArray("IRCCVariation");
            //   System.out.println("loading "+jarray.length()+" variant records");

            Platform platform = dataImportService.getPlatform(TECH, dto.getProviderGroup(), TARGETEDNGS_PLATFORM_URL);
            platform.setGroup(dto.getProviderGroup());
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