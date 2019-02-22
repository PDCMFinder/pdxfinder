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
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Load data from JAX.
 */
@Component
@Order(value = -18)
public class LoadJAXData implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadJAXData.class);

    private final static String DATASOURCE_ABBREVIATION = "JAX";
    private final static String DATASOURCE_NAME = "The Jackson Laboratory";
    private final static String DATASOURCE_DESCRIPTION = "The Jackson Laboratory PDX mouse models.";
    private final static String DATASOURCE_CONTACT = "http://tumor.informatics.jax.org/mtbwi/pdxRequest.do?mice=";
    private final static String DATASOURCE_URL = "http://tumor.informatics.jax.org/mtbwi/pdxDetails.do?modelID=";

    private final static String PROVIDER_TYPE = "";
    private final static String ACCESSIBILITY = "";

    private final static String NSG_BS_NAME = "NOD scid gamma";
    private final static String NSG_BS_SYMBOL = "NOD.Cg-Prkdc<scid> Il2rg<tm1Wjl>/SzJ";
    private final static String NSG_BS_DESC = "";
    private final static String NSG_BS_URL = "http://jax.org/strain/005557";

    private final static String HISTOLOGY_NOTE = "Pathologist assessment of patient tumor and pdx model tumor histology slides.";

    private final static String DOSING_STUDY_URL = "/platform/jax-drug-dosing/";
    private final static String CTP_PLATFORM_URL = "/platform/jax-ctp/";
    private final static String TRUSEQ_PLATFORM_URL = "/platform/jax-truseq/";
    private final static String WHOLE_EXOME_URL = "/platform/jax-whole-exome/";
    private final static String SOURCE_URL = "/source/jax/";

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private DataImportService dataImportService;
    private Session session;

    @Value("${jaxpdx.variation.max}")
    private int maxVariations;

    @Value("${jaxpdx.ref.assembly}")
    private String refAssembly;

    @Value("${pdxfinder.data.root.dir}")
    private String dataRootDir;

    HashMap<String, String> passageMap = null;

    private LoaderDTO dto = new LoaderDTO();

    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    @Autowired
    private UtilityService utilityService;

    public LoadJAXData(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadJAX", "Load JAX PDX data");
        parser.accepts("loadALL", "Load all, including JAX PDX data");
        parser.accepts("loadSlim", "Load slim, then link samples to NCIT terms");
        OptionSet options = parser.parse(args);

        if (options.has("loadJAX") || options.has("loadALL")  || options.has("loadSlim")) {

            log.info("Loading JAX PDX data.");

            String fileStr = dataRootDir+DATASOURCE_ABBREVIATION+"/pdx/models.json";

            String metaDataJSON = dataImportService.stageOneGetMetaDataFile(fileStr, DATASOURCE_ABBREVIATION);

            if (!metaDataJSON.equals("NOT FOUND")){

                parseJSONandCreateGraphObjects(metaDataJSON);
            }

        }
    }


    private void parseJSONandCreateGraphObjects(String json) throws Exception{

        dto = dataImportService.stagetwoCreateProviderGroup(dto, DATASOURCE_NAME, DATASOURCE_ABBREVIATION, DATASOURCE_DESCRIPTION,
                PROVIDER_TYPE, ACCESSIBILITY, null, DATASOURCE_CONTACT, SOURCE_URL);

        dto = dataImportService.stageThreeCreateNSGammaHostStrain(dto, NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_NAME, NSG_BS_NAME);

        // SKIP FOR JAX - dto = dataImportService.stageFiveCreateProjectGroup(dto,"EurOPDX");

        JSONArray jarray = dataImportService.stageSixGetPDXModels(json,"pdxInfo");

        for (int i = 0; i < jarray.length(); i++) {

            JSONObject job = jarray.getJSONObject(i);

            createGraphObjects(job);
        }

    }




    @Transactional
    void createGraphObjects(JSONObject j) throws Exception {


        dto = dataImportService.stageSevenGetMetadata(dto, j, DATASOURCE_ABBREVIATION);

        /* JAX After metadata Uniqueness: */
        dto.setHistologyMap(getHistologyImageMap(dto.getModelID()));
        //Check if model exists in DB, if yes, do not load duplicates
        ModelCreation existingModel = dataImportService.findModelByIdAndDataSource(dto.getModelID(), DATASOURCE_ABBREVIATION);
        if(existingModel != null) {
            log.error("Skipping existing model "+dto.getModelID());
            return;
        }
        // if the diagnosis is still unknown don't load it
        if(dto.getDiagnosis().toLowerCase().contains("unknown") ||
                dto.getDiagnosis().toLowerCase().contains("not specified")){
            System.out.println("Skipping model "+dto.getModelID()+" with diagnosis:"+dto.getDiagnosis());
            return;
        }

        dto = dataImportService.stageEightLoadPatientData(dto, DATASOURCE_CONTACT);

        dto.getPatientSnapshot().addSample(dto.getPatientSample());

        dataImportService.savePatientSnapshot(dto.getPatientSnapshot());

        dto = dataImportService.step09LoadExternalURLs(dto, DATASOURCE_CONTACT+dto.getModelID(), DATASOURCE_URL+dto.getModelID());

        dto = dataImportService.step09BCreateBreastMarkers(dto);


        // JAX - Updates Patient Sample b4 model Creation
        dto.getPatientSample().setExtractionMethod(dto.getExtractionMethod());

        if (dto.getHistologyMap().containsKey("Patient")) {
            Histology histology = new Histology();
            Image image = dto.getHistologyMap().get("Patient");
            histology.addImage(image);
            dto.getPatientSample().addHistology(histology);
        }


        dto = dataImportService.stageNineCreateModels(dto);

        dto = dataImportService.loaderSecondStep(dto, dto.getPatientSnapshot(), DATASOURCE_ABBREVIATION);

        //Create Treatment summary without linking TreatmentProtocols to specimens
        dto = dataImportService.stepThreeCurrentTreatment(dto, DOSING_STUDY_URL,"Response");


        loadVariationData(dto.getModelCreation(), dto.getEngraftmentSite(), dto.getEngraftmentType());

    }


    /*
    Use the modelID in the modelCreation object to get model specific variation data
    This is a set of makers with marker association details
    Since we are creating samples here attach any histology images to the sample based on passage #
     */
    private void loadVariationData(ModelCreation modelCreation, EngraftmentSite engraftmentSite, EngraftmentType engraftmentType) {

        if (maxVariations == 0) {
            return;
        }

        try {

            passageMap = new HashMap<>();

            HashMap<String, HashMap<String, List<MarkerAssociation>>> sampleMap = new HashMap<>();
            HashMap<String, List<MarkerAssociation>> markerMap = new HashMap<>();

            String variationFile = dataRootDir+DATASOURCE_ABBREVIATION+"/mut/" + modelCreation.getSourcePdxId()+".json";
            File file = new File(variationFile);

            if (file.exists()){

                JSONObject job = new JSONObject(utilityService.parseFile(variationFile));
                JSONArray jarray = job.getJSONArray("variation");
                String sample = null;
                String symbol, id, technology, aaChange, chromosome, seqPosition, refAllele, consequence, rsVariants, readDepth, alleleFrequency, altAllele = null;
                log.info(jarray.length() + " gene variants for model " + modelCreation.getSourcePdxId());

                // configure the maximum variations to load in properties file
                // loading them all will take a while (hour?)
                int stop = jarray.length();
                if (maxVariations > 0 && maxVariations < jarray.length()) {
                    stop = maxVariations;
                }
                for (int i = 0; i < stop;) {
                    JSONObject j = jarray.getJSONObject(i);

                    sample = j.getString("sample");
                    symbol = j.getString("gene symbol");
                    id = j.getString("gene id");
                    aaChange = j.getString("amino acid change");
                    technology = j.getString("platform");
                    chromosome = j.getString("chromosome");
                    seqPosition = j.getString("seq position");
                    refAllele = j.getString("ref allele");
                    consequence = j.getString("consequence");
                    rsVariants = j.getString("rs variants");
                    readDepth = j.getString("read depth");
                    alleleFrequency = j.getString("allele frequency");
                    altAllele = j.getString("alt allele");

                    //skip loading fish!
                    if(technology.equals("Other:_FISH")){
                        i++;
                        continue;
                    }

                    passageMap.put(sample, j.getString("passage num"));

                    // since there are 8 fields assume (incorrectly?) all MAs are unique
                    // create a new one rather than look for exisitng one
                    MarkerAssociation ma = new MarkerAssociation();

                    ma.setAminoAcidChange(aaChange);
                    ma.setConsequence(consequence);
                    ma.setAlleleFrequency(alleleFrequency);
                    ma.setChromosome(chromosome);
                    ma.setReadDepth(readDepth);
                    ma.setRefAllele(refAllele);
                    ma.setAltAllele(altAllele);
                    ma.setRefAssembly(refAssembly);
                    ma.setRsVariants(rsVariants);
                    ma.setSeqPosition(seqPosition);
                    ma.setReadDepth(readDepth);

                    Marker marker = dataImportService.getMarker(symbol);
                    marker.setEntrezId(id);

                    ma.setMarker(marker);

                    Platform platform;

                    if(technology.equals("Truseq_JAX")){
                        platform = dataImportService.getPlatform(technology, dto.getProviderGroup(), TRUSEQ_PLATFORM_URL);
                    }
                    else if(technology.equals("Whole_Exome")){
                        platform = dataImportService.getPlatform(technology, dto.getProviderGroup(), WHOLE_EXOME_URL);
                    }
                    else if(technology.equals("CTP")){
                        platform = dataImportService.getPlatform(technology, dto.getProviderGroup(), CTP_PLATFORM_URL);
                    }
                    else{
                        platform = dataImportService.getPlatform(technology, dto.getProviderGroup(), "");
                    }


                    markerMap = sampleMap.get(sample);
                    if (markerMap == null) {
                        markerMap = new HashMap<>();
                    }

                    // make a map of markerAssociation collections keyed to technology
                    if (markerMap.containsKey(technology)) {
                        markerMap.get(technology).add(ma);
                    } else {
                        List<MarkerAssociation> list = new ArrayList<>();
                        list.add(ma);
                        markerMap.put(technology, list);
                    }

                    sampleMap.put(sample, markerMap);
                    i++;
                    if (i % 100 == 0) {
                        System.out.println("loaded " + i + " markers");
                    }
                }
                System.out.println("loaded " + stop + " markers for " + modelCreation.getSourcePdxId());

                for (String sampleKey : sampleMap.keySet()) {

                    String passage = getPassage(sampleKey);
                    markerMap = sampleMap.get(sampleKey);

                    HashSet<MolecularCharacterization> mcs = new HashSet<>();
                    for (String tech : markerMap.keySet()) {
                        MolecularCharacterization mc = new MolecularCharacterization();
                        mc.setType("mutation");

                        Platform platform = null;

                        if(tech.equals("Truseq_JAX")){
                            platform = dataImportService.getPlatform(tech, dto.getProviderGroup(), TRUSEQ_PLATFORM_URL);
                        }
                        else if(tech.equals("Whole_Exome")){
                            platform = dataImportService.getPlatform(tech, dto.getProviderGroup(), WHOLE_EXOME_URL);
                        }
                        else if(tech.equals("CTP")){
                            platform = dataImportService.getPlatform(tech, dto.getProviderGroup(), CTP_PLATFORM_URL);
                        }
                        else{
                            platform = dataImportService.getPlatform(tech, dto.getProviderGroup(), "");
                        }


                        mc.setPlatform(platform);
                        mc.setMarkerAssociations(markerMap.get(tech));
                        mcs.add(mc);

                    }


                    Specimen specimen = dataImportService.getSpecimen(modelCreation, sampleKey, dto.getProviderGroup().getAbbreviation(), passage);

                    Sample specSample = new Sample();
                    specSample.setSourceSampleId(sampleKey);
                    specimen.setSample(specSample);
                    specSample.setMolecularCharacterizations(mcs);


                    if (dto.getHistologyMap().containsKey(passage)) {
                        Histology histology = new Histology();
                        Image image = dto.getHistologyMap().get(passage);
                        histology.addImage(image);
                        specSample.addHistology(histology);

                    }


                    // all JAX mice are NSG, even if not specified in feed
                    specimen.setHostStrain(dto.getNodScidGamma());


                    specimen.setEngraftmentSite(engraftmentSite);
                    specimen.setEngraftmentType(engraftmentType);


                    dataImportService.saveSpecimen(specimen);

                    modelCreation.addSpecimen(specimen);
                    modelCreation.addRelatedSample(specSample);


                    System.out.println("saved passage " + passage + " for model " + modelCreation.getSourcePdxId() + " from sample " + sampleKey);
                }

                dataImportService.saveModelCreation(modelCreation);

            }
            else{
                log.warn("Variation file not found for "+modelCreation.getSourcePdxId());
            }



        } catch (Exception e) {
            log.error("", e);
        }
        

    }

    private String getPassage(String sample) {
        String p = "0";
        try {
            p = passageMap.get(sample).replaceAll("P", "");
        } catch (Exception e) {
            log.info("Unable to determine passage from sample name " + sample + ". Assuming 0");
        }
        return p;

    }

    /*
    For a given model return a map of passage # or "Patient" -> histology image URL
     */
    private HashMap<String, Image> getHistologyImageMap(String id) {
        HashMap<String, Image> map = new HashMap<>();

            String histologyFile = dataRootDir+DATASOURCE_ABBREVIATION+"/hist/"+id;
            File file = new File(histologyFile);

            if(file.exists()){
                try {
                    JSONObject job = new JSONObject(utilityService.parseFile(histologyFile));
                    JSONArray jarray = job.getJSONObject("pdxHistology").getJSONArray("Graphics");
                    String comment = job.getJSONObject("pdxHistology").getString("Comment");

                    for (int i = 0; i < jarray.length(); i++) {
                        job = jarray.getJSONObject(i);
                        String desc = job.getString("Description");

                        // comments apply to all of a models histology but histologies are passage specific
                        // so I guess attach the comment to all image descriptions
                        if (comment != null && comment.trim().length() > 0) {
                            String sep = "";
                            if (desc != null && desc.trim().length() > 0) {
                                sep = " : ";
                            }
                            desc = comment + sep + desc;
                        }

                        String url = job.getString("URL");
                        Image img = new Image();
                        img.setDescription(desc);
                        img.setUrl(url);
                        if (desc.startsWith("Patient") || desc.startsWith("Primary")) {
                            map.put("Patient", img);
                        } else {
                            String[] parts = desc.split(" ");
                            if (parts[0].startsWith("P")) {
                                try {
                                    String passage = new Integer(parts[0].replace("P", "")).toString();
                                    map.put(passage, img);
                                } catch (Exception e) {
                                    log.info("Can't extract passage from description " + desc);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error("Error getting histology for model " + id, e);
                }

            }




        return map;
    }



}
