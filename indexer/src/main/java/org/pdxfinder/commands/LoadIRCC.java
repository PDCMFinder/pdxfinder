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
import org.pdxfinder.dao.*;
import org.pdxfinder.utilities.LoaderUtils;
import org.pdxfinder.utilities.Standardizer;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Load data from IRCC.
 */
@Component
@Order(value = 0)
public class LoadIRCC implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadIRCC.class);

    private final static String IRCC_DATASOURCE_ABBREVIATION = "IRCC";
    private final static String IRCC_DATASOURCE_NAME = "Candiolo Cancer Institute";
    private final static String IRCC_DATASOURCE_DESCRIPTION = "IRCC";

    private final static String NSG_BS_NAME = "NOD scid gamma";
    private final static String NSG_BS_SYMBOL = "NOD.Cg-Prkdc<sup>scid</sup> Il2rg<sup>tm1Wjl</sup>/SzJ"; //yay HTML in name
    private final static String NSG_BS_URL = "http://jax.org/strain/005557";
    
    private final static String TECH = "MUT targeted NGS";

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    private final static String NOT_SPECIFIED = Standardizer.NOT_SPECIFIED;
    public static final String FINGERPRINT_DESCRIPTION = "Model validated against patient germline.";

    private HostStrain nsgBS;
    private ExternalDataSource irccDS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private LoaderUtils loaderUtils;
    private Session session;

    // samples -> markerAsssociations
    private HashMap<String, HashSet<MarkerAssociation>> markerAssociations = new HashMap();
    private HashMap<String, HashMap<String, String>> specimenSamples = new HashMap();
    private HashMap<String, HashMap<String, String>> modelSamples = new HashMap();

    private HashSet<Integer> loadedModelHashes = new HashSet<>();


    @Value("${irccpdx.url}")
    private String urlStr;

    @Value("${irccpdx.variation.url}")
    private String variationURLStr;

    @Value("${irccpdx.variation.max}")
    private int variationMax;

    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadIRCC(LoaderUtils loaderUtils) {
        this.loaderUtils = loaderUtils;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadIRCC", "Load IRCC PDX data");
        parser.accepts("loadALL", "Load all, including IRCC PDX data");
        OptionSet options = parser.parse(args);
        
        irccDS = loaderUtils.getExternalDataSource(IRCC_DATASOURCE_ABBREVIATION, IRCC_DATASOURCE_NAME, IRCC_DATASOURCE_DESCRIPTION);
        nsgBS = loaderUtils.getHostStrain(NSG_BS_NAME, NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_NAME);

        if (options.has("loadIRCC") || options.has("loadALL")) {

            log.info("Loading IRCC PDX data.");


            if (urlStr != null) {
                log.info("Loading from URL " + urlStr);
                parseModels(parseURL(urlStr));
            }
            if (variationURLStr != null && variationMax != 0) {
                loadVariants(variationURLStr, "TargetedNGS_MUT", "mutation");
            }
            else {
                log.error("No irccpdx.url provided in properties");
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

        String id = job.getString("Model ID");

        // the preference is for histology
        String diagnosis = job.getString("Clinical Diagnosis");

        String classification = job.getString("Stage");

        String age = Standardizer.getAge(job.getString("Age"));
        String gender = Standardizer.getGender(job.getString("Gender"));

        PatientSnapshot pSnap = loaderUtils.getPatientSnapshot(job.getString("Patient ID"),
                gender, "", NOT_SPECIFIED, age, irccDS);

        String tumorType = Standardizer.getTumorType(job.getString("Tumor Type"));

        Sample ptSample = loaderUtils.getSample(id, tumorType, diagnosis,
                job.getString("Primary Site"), job.getString("Sample Site"),
                NOT_SPECIFIED, classification, NORMAL_TISSUE_FALSE, irccDS.getAbbreviation());

        pSnap.addSample(ptSample);

        loaderUtils.saveSample(ptSample);
        loaderUtils.savePatientSnapshot(pSnap);

        QualityAssurance qa = new QualityAssurance();

        if ("TRUE".equals(job.getString("Fingerprinting").toUpperCase())) {
            qa.setValidationTechniques(ValidationTechniques.FINGERPRINT);
            qa.setDescription(FINGERPRINT_DESCRIPTION);

            // If the model includes which passages have had QA performed, set the passages on the QA node
            if (job.has("QA Passage") && !job.getString("QA Passage").isEmpty()) {

                List<String> passages = Stream.of(job.getString("QA Passage").split(","))
                        .map(String::trim)
                        .distinct()
                        .collect(Collectors.toList());
                List<Integer> passageInts = new ArrayList<>();

                // NOTE:  IRCC uses passage 0 to mean Patient Tumor, so we need to harmonize according to the other
                // sources.  Subtract 1 from every passage.
                for (String p : passages) {
                    Integer intPassage = Integer.parseInt(p);
                    passageInts.add(intPassage - 1);
                }

                qa.setPassages(StringUtils.join(passages, ", "));

            }

        }

        ModelCreation modelCreation = loaderUtils.createModelCreation(id, this.irccDS.getAbbreviation(), ptSample, qa);

        JSONArray specimens = job.getJSONArray("Specimens");
        for (int i = 0; i < specimens.length(); i++) {
            JSONObject specimenJSON = specimens.getJSONObject(i);

            String specimenId = specimenJSON.getString("Specimen ID");
            
            Specimen specimen = loaderUtils.getSpecimen(modelCreation,
                    specimenId, irccDS.getAbbreviation(), specimenJSON.getString("Passage"));

            specimen.setHostStrain(this.nsgBS);

            ImplantationSite is = new ImplantationSite(specimenJSON.getString("Engraftment Site"));
            specimen.setImplantationSite(is);

            ImplantationType it = new ImplantationType(specimenJSON.getString("Engraftment Type"));
            specimen.setImplantationType(it);

            /*
            
            JSONArray platforms = specimenJSON.getJSONArray("Platforms");
            HashSet<MolecularCharacterization> mcs = new HashSet();

            for (int j = 0; j < platforms.length(); j++) {
                JSONObject platform = platforms.getJSONObject(j);
                MolecularCharacterization mc = new MolecularCharacterization();
                Platform p = loaderUtils.getPlatform(platform.getString("Platform"), this.irccDS);
                loaderUtils.savePlatform(p);

                mc.setPlatform(p);

                mcs.add(mc);
            }
            */

            Sample specSample = new Sample();
            
            
            
            specSample.setSourceSampleId(specimenId);
            specSample.setDataSource(irccDS.getAbbreviation());

            //specSample.setMolecularCharacterizations(mcs);
            specimen.setSample(specSample);

            //    loaderUtils.saveSpecimen(specimen);
            modelCreation.addSpecimen(specimen);
            modelCreation.addRelatedSample(specSample);
            /*
          //  System.out.println("checking for samples for specimen "+specimenId);
            if (specimenSamples.containsKey(specimenId)) {
            for (String sampleID : specimenSamples.get(specimenId).keySet()) {
          //      System.out.println("samples found for specimen");
                Sample variationSample = new Sample();
                variationSample.setSourceSampleId(sampleID);


                MolecularCharacterization mc = new MolecularCharacterization();

                mc.setMarkerAssociations(markerAssociations.get(sampleID));
                Platform platform = loaderUtils.getPlatform(TECH, this.irccDS);
                mc.setPlatform(platform);
               
                mcs = new HashSet();
                mcs.add(mc); 
                variationSample.setMolecularCharacterizations(mcs);


                //modelCreation.addRelatedSample(variationSample);
                //System.out.println("adding "+sampleID+ " to model "+id+ " for specimen "+specimenId);
                
                
            }
           
        }

        */
        }
        
        
        
        loaderUtils.saveModelCreation(modelCreation);
        
    }

    @Transactional
    public void loadVariants(String variationURLStr, String platformName, String molcharType){

        log.info("Loading variation for platform "+platformName);
        //STEP 1: Save the platform
        Platform platform = loaderUtils.getPlatform(platformName, this.irccDS);
        platform.setExternalDataSource(irccDS);
        loaderUtils.savePlatform(platform);


        //STEP 2: get markers and save them with the platform linked
        try{

            JSONObject job = new JSONObject(parseURL(variationURLStr));
            JSONArray jarray = job.getJSONArray("IRCCVariation");
            Set<String> markers = new HashSet<>();
            log.info("Saving Markers to DB");
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject variation = jarray.getJSONObject(i);
                String gene = variation.getString("Gene");
                markers.add(gene);
            }

            for(String m:markers){
                Marker marker = loaderUtils.getMarker(m, m);
                //PlatformAssociation pa = loaderUtils.createPlatformAssociation(platform, marker);
                //loaderUtils.savePlatformAssociation(pa);

            }
            log.info("Saved "+markers.size()+" to the DB.");

            //STEP 3: assemble MolecularCharacterization objects for samples

            //sampleId = > molchar
            HashMap<String, MolecularCharacterization> sampleMolCharMap = new HashMap();

            for (int i = 0; i < jarray.length(); i++) {
                if (i == variationMax) {
                    System.out.println("qutting after loading "+i+" variants");
                    break;
                }

                JSONObject variation = jarray.getJSONObject(i);

                String sample = variation.getString("Sample ID");
                String specimen = variation.getString("Specimen ID");

                String sampleId = variation.getString("Specimen ID");
                String samplePlatformId = sampleId+"____"+platformName;


                String gene = variation.getString("Gene");
                String type = variation.getString("Type");

                Marker marker = loaderUtils.getMarker(gene,gene);

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



                if(sampleMolCharMap.containsKey(sampleId)){
                    sampleMolCharMap.get(sampleId).addMarkerAssociation(ma);
                }
                else{
                    MolecularCharacterization mcNew = new MolecularCharacterization();
                    mcNew.setPlatform(platform);
                    mcNew.setType(molcharType);
                    mcNew.addMarkerAssociation(ma);


                    sampleMolCharMap.put(sampleId,mcNew);

                }

            }


            //STEP 3: loop through sampleMolCharMap to hook mc objects to proper samples then save the graph
            for (Map.Entry<String, MolecularCharacterization> entry : sampleMolCharMap.entrySet()) {
                String sampleId = entry.getKey();
                MolecularCharacterization mc = entry.getValue();
                try{
                    Sample s = loaderUtils.getSampleByDataSourceAndSourceSampleId(irccDS.getAbbreviation(), sampleId);

                    if(s == null){
                        log.error("Sample not found: "+sampleId);
                    }
                    else{
                        s.addMolecularCharacterization(mc);
                        loaderUtils.saveSample(s);
                        log.info("Saving molchar for sample: "+sampleId);
                    }
                }
                catch(Exception e1){

                    log.error(sampleId);
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
            JSONObject job = new JSONObject(parseURL(variationURLStr));
            JSONArray jarray = job.getJSONArray("IRCCVariation");
         //   System.out.println("loading "+jarray.length()+" variant records");

            Platform platform = loaderUtils.getPlatform(TECH, this.irccDS);
            platform.setExternalDataSource(irccDS);
            loaderUtils.savePlatform(platform);


            for (int i = 0; i < jarray.length(); i++) {
                if (i == variationMax) {
                    System.out.println("qutting after loading "+i+" variants");
                    break;
                }

                JSONObject variation = jarray.getJSONObject(i);

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
                
                Marker marker = loaderUtils.getMarker(gene,gene);
                
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
                
                

                PlatformAssociation pa = loaderUtils.createPlatformAssociation(platform, marker);
                loaderUtils.savePlatformAssociation(pa);

            
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
            log.error("Unable to read from IRCC JSON URL " + urlStr, e);
        }
        return sb.toString();
    }

}
