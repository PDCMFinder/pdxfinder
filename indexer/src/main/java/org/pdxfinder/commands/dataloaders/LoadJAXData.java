package org.pdxfinder.commands.dataloaders;

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
import org.pdxfinder.services.dto.NodeSuggestionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;

/**
 * Load data from JAX.
 */
@Component
@Order(value = -18)
@PropertySource("classpath:loader.properties")
@ConfigurationProperties(prefix = "jax")
public class LoadJAXData extends LoaderBase implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadJAXData.class);

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

    Map<String, Platform> platformMap = new HashMap<>();

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

            initMethod();

            jaxLoadingOrder();

        }
    }




    @Override
    protected void initMethod() {

        log.info("Loading JAX PDX data.");

        dto = new LoaderDTO();

        jsonFile = dataRootDir+dataSourceAbbreviation+"/pdx/models.json";
        dataSource = dataSourceAbbreviation;
    /*  dataSourceAbbreviation = DATASOURCE_ABBREVIATION;
        dataSourceContact = DATASOURCE_CONTACT;
        dosingStudyURL = DOSING_STUDY_URL;  */
    }

    void jaxLoadingOrder() throws Exception {

        step00StartReportManager();

        step02GetMetaDataJSON();

        step03CreateProviderGroup();

        step04CreateNSGammaHostStrain();

        step05CreateNSHostStrain();

        step06CreateProjectGroup();

        step07GetPDXModels();


        for (int i = 0; i < jsonArray.length(); i++) {

            this.jsonData = jsonArray.getJSONObject(i);

            step08GetMetaData();

            //if(!dto.getModelID().equals("TM00016")) continue;

            step09LoadPatientData();

            step10LoadExternalURLs();

            step11LoadBreastMarkers();

            step12CreateModels();

            step13LoadSpecimens();

            step14LoadCurrentTreatment();

            step16LoadVariationData();

        }
        step15LoadImmunoHistoChemistry();


    }


    @Override
    protected void step01GetMetaDataFolder() {

    }



    // JAX uses default implementation Steps step02GetMetaDataJSON



    @Override
    protected void step03CreateProviderGroup() {

        loadProviderGroup(dataSourceName, dataSourceAbbreviation, dataSourceDescription, providerType, accessibility, null, dataSourceContact, sourceURL);
    }



    @Override
    protected void step04CreateNSGammaHostStrain() {

        loadNSGammaHostStrain(nsgBsSymbol, nsgbsURL, nsgBsName, nsgBsName);
    }


    @Override
    protected void step05CreateNSHostStrain() {

    }


    @Override
    protected void step06CreateProjectGroup() {

    }


    @Override
    protected void step07GetPDXModels() {

        loadPDXModels(metaDataJSON,"pdxInfo");
    }



    // JAX uses default implementation Steps step08GetMetaData



    @Override
    protected void step09LoadPatientData() {

        dto.setHistologyMap(getHistologyImageMap(dto.getModelID()));

        //Check if model exists in DB, if yes, do not load duplicates
        ModelCreation existingModel = dataImportService.findModelByIdAndDataSource(dto.getModelID(), dataSourceAbbreviation);
        if(existingModel != null) {
            log.error("Skipping existing model "+dto.getModelID());
            return;
        }
        // if the diagnosis is still unknown don't load it
        if(dto.getDiagnosis().toLowerCase().contains("unknown") ||
                dto.getDiagnosis().toLowerCase().contains("not specified")){
            log.info("Skipping model "+dto.getModelID()+" with diagnosis:"+dto.getDiagnosis());
            return;
        }

        super.step09LoadPatientData();
    }




    @Override
    protected void step10LoadExternalURLs() {

        dataImportService.savePatientSnapshot(dto.getPatientSnapshot());

        loadExternalURLs(dataSourceContact+dto.getModelID(), dataSourceURL+dto.getModelID());
    }




    @Override
    protected void step11LoadBreastMarkers() {


        //create breast cancer markers manually if they are present
        if(!dto.getModelTag().equals(Standardizer.NOT_SPECIFIED)){

            if(dto.getModelTag().equals("Triple Negative Breast Cancer (TNBC)")){
                NodeSuggestionDTO nsdto;

                MolecularCharacterization mc = new MolecularCharacterization();
                mc.setPlatform(dataImportService.getPlatform("Not Specified", dto.getProviderGroup()));
                mc.setType("IHC");

                //we know these markers exist so no need to check for null
                nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSourceAbbreviation, dto.getModelID(), "ERBB2", "IHC", "ImmunoHistoChemistry");
                Marker her2 = (Marker) nsdto.getNode(); // ERBB2  dataImportService.getMarker("HER2", "HER2");

                nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSourceAbbreviation, dto.getModelID(), "EREG", "IHC", "ImmunoHistoChemistry");
                Marker er = (Marker) nsdto.getNode(); //dataImportService.getMarker("ER", "ER");

                nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSourceAbbreviation, dto.getModelID(), "PGR", "IHC", "ImmunoHistoChemistry");
                Marker pr = (Marker) nsdto.getNode(); //dataImportService.getMarker("PR", "PR");

                MarkerAssociation her2a = new MarkerAssociation();
                her2a.setMarker(her2);
                her2a.setImmunoHistoChemistryResult("negative");

                MarkerAssociation era = new MarkerAssociation();
                era.setMarker(er);
                era.setImmunoHistoChemistryResult("negative");

                MarkerAssociation pra = new MarkerAssociation();
                pra.setMarker(pr);
                pra.setImmunoHistoChemistryResult("negative");

                mc.addMarkerAssociation(her2a);
                mc.addMarkerAssociation(era);
                mc.addMarkerAssociation(pra);

                dto.getPatientSample().addMolecularCharacterization(mc);
            }
        }

    }




    @Override
    protected void step12CreateModels() throws Exception  {

        // JAX - Updates Patient Sample b4 model Creation
        dto.getPatientSample().setExtractionMethod(dto.getExtractionMethod());

        if (dto.getHistologyMap().containsKey("Patient")) {
            Histology histology = new Histology();
            Image image = dto.getHistologyMap().get("Patient");
            histology.addImage(image);
            dto.getPatientSample().addHistology(histology);
        }

        super.step12CreateModels();

    }





    @Override
    protected void step13LoadSpecimens() {

        Specimen specimen = dataImportService.getSpecimen(dto.getModelCreation(), dto.getModelID(), dto.getProviderGroup().getAbbreviation(), "");
        specimen.setHostStrain(dto.getNodScidGamma());
        EngraftmentSite engraftmentSite = dataImportService.getImplantationSite(dto.getImplantationSiteStr());
        EngraftmentType engraftmentType = dataImportService.getImplantationType(Standardizer.NOT_SPECIFIED);
        specimen.setEngraftmentSite(engraftmentSite);
        specimen.setEngraftmentType(engraftmentType);

        dto.getModelCreation().addSpecimen(specimen);
        dataImportService.saveSpecimen(specimen);

        dto.setEngraftmentSite(engraftmentSite);
        dto.setEngraftmentType(engraftmentType);

    }




    @Override
    protected void step14LoadCurrentTreatment() {

        loadCurrentTreatment();

    }




    @Override
    protected void step15LoadImmunoHistoChemistry() {

    }


    @Override
    protected void step16LoadVariationData() {
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
            Map<String, MolecularCharacterization> molcharMap = new HashMap<>();

            HashMap<String, HashMap<String, List<MarkerAssociation>>> sampleMap = new HashMap<>();
            HashMap<String, List<MarkerAssociation>> markerMap = new HashMap<>();

            String variationFile = dataRootDir+dataSourceAbbreviation+"/mut/" + modelCreation.getSourcePdxId()+".json";
            File file = new File(variationFile);

            if (file.exists()){

                JSONObject job = new JSONObject(utilityService.parseFile(variationFile));
                JSONArray jarray = job.getJSONArray("variation");
                String sample = null;
                String symbol, id, technology, aaChange, chromosome, seqPosition, refAllele, consequence, rsVariants, readDepth, alleleFrequency, altAllele, passage = null;
                log.info(jarray.length() + " gene variants for model " + modelCreation.getSourcePdxId());

                // configure the maximum variations to load in properties file
                // loading them all will take a while (hour?)
                int stop = jarray.length();
                if (maxVariations > 0 && maxVariations < jarray.length()) {
                    stop = maxVariations;
                }

                //PHASE 1: assemble objects in memory, reducing db interactions as much as possible
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
                    passage = j.getString("passage num");

                    //skip loading fish!
                    if(technology.equals("Other:_FISH")){
                        i++;
                        continue;
                    }

                    //step 1: Get the Platform and cache it like a boss
                    //This is to reduce db interaction
                    Platform platform;
                    if(platformMap.containsKey(technology)){

                        platform = platformMap.get(technology);
                    }
                    else{

                        if(technology.equals("Truseq_JAX")){
                            platform = dataImportService.getPlatform(technology, dto.getProviderGroup(), platformURL.get("Truseq_JAX"));
                        }
                        else if(technology.equals("Whole_Exome")){
                            platform = dataImportService.getPlatform(technology, dto.getProviderGroup(), platformURL.get("Whole_Exome"));
                        }
                        else if(technology.equals("CTP")){
                            platform = dataImportService.getPlatform(technology, dto.getProviderGroup(), platformURL.get("CTP"));
                        }
                        else{
                            platform = dataImportService.getPlatform(technology, dto.getProviderGroup(), "");
                        }

                        platformMap.put(technology, platform);
                    }



                    // step 2: get the cached molchar object or create one if it does not exist in the map
                    //key: sampleid + "__" + passage + "__" + platformtechnology
                    MolecularCharacterization molecularCharacterization;
                    String molcharKey = sample + "__" + passage + "__" + technology;

                    if(molcharMap.containsKey(molcharKey)){

                        molecularCharacterization = molcharMap.get(molcharKey);
                    }
                    else{

                        molecularCharacterization = new MolecularCharacterization();
                        molecularCharacterization.setType("mutation");
                        molecularCharacterization.setPlatform(platform);
                        molcharMap.put(molcharKey, molecularCharacterization);
                    }

                    //step 3: get the marker suggestion from the service
                    NodeSuggestionDTO nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSourceAbbreviation, modelCreation.getSourcePdxId(), symbol, "mutation", technology);

                    Marker marker;

                    if(nsdto.getNode() == null){

                        //uh oh, we found an unrecognised marker symbol, abort, abort!!!!
                        reportManager.addMessage(nsdto.getLogEntity());
                        i++;
                        continue;
                    }
                    else{

                        // step 4: assemble the MarkerAssoc object and add it to molchar
                        marker = (Marker)nsdto.getNode();

                        //if we have any message regarding the suggested marker, ie: prev symbol, synonym, etc, add it to the report
                        if(nsdto.getLogEntity() != null){
                            reportManager.addMessage(nsdto.getLogEntity());
                        }

                        MarkerAssociation ma = new MarkerAssociation();
                        ma.setAminoAcidChange(aaChange);
                        ma.setConsequence(consequence);
                        ma.setAlleleFrequency(alleleFrequency);
                        ma.setChromosome(chromosome);
                        ma.setReadDepth(readDepth);
                        ma.setRefAllele(refAllele);
                        ma.setAltAllele(altAllele);
                        ma.setGenomeAssembly(refAssembly);
                        ma.setRsIdVariants(rsVariants);
                        ma.setSeqPosition(seqPosition);
                        ma.setReadDepth(readDepth);
                        ma.setMarker(marker);

                        molecularCharacterization.addMarkerAssociation(ma);

                    }

                    i++;
                    if (i % 100 == 0) {
                        System.out.println("loaded " + i + " variants");
                    }
                }

                System.out.println("loaded " + stop + " markers for " + modelCreation.getSourcePdxId());

                //PHASE 2: get objects from cache and persist them
                //assembled all mc objects for a particular model, time to link them to the corresponding samples

                for(Map.Entry<String, MolecularCharacterization> mcEntry : molcharMap.entrySet()){
                    //key: sampleid + "__" + passage + "__" + platformtechnology
                    String mcKey = mcEntry.getKey();
                    MolecularCharacterization mc = mcEntry.getValue();


                    String[] mcKeyArr = mcKey.split("__");
                    String sampleId = mcKeyArr[0];
                    String pass = getPassage(mcKeyArr[1]);

                    boolean foundSpecimen = false;

                    if(modelCreation.getSpecimens() != null){

                        for(Specimen specimen : modelCreation.getSpecimens()){

                            if(specimen.getPassage().equals(pass)){

                                if(specimen.getSample() != null && specimen.getSample().getSourceSampleId().equals(sampleId)){

                                    Sample xenograftSample = specimen.getSample();
                                    xenograftSample.addMolecularCharacterization(mc);

                                    foundSpecimen = true;
                                }
                            }
                        }
                    }

                    //this passage is either not present yet or the linked sample has a different ID, create a specimen with sample and link mc
                    if(!foundSpecimen){
                        log.info("Creating new specimen for "+mcKey);

                        Sample xenograftSample = new Sample();
                        xenograftSample.setSourceSampleId(sampleId);
                        xenograftSample.addMolecularCharacterization(mc);

                        Specimen specimen = new Specimen();
                        specimen.setPassage(pass);
                        specimen.setSample(xenograftSample);


                        modelCreation.addRelatedSample(xenograftSample);
                        modelCreation.addSpecimen(specimen);
                    }
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

    private String getPassage(String passageString) {

        if(!passageString.isEmpty() && passageString.toUpperCase().contains("P")){

            passageString = passageString.toUpperCase().replace("P", "");
        }
        //does this string have digits only now?
        if(passageString.matches("\\d+")) return passageString;

        log.warn("Unable to determine passage from sample name " + passageString + ". Assuming 0");
        return "0";

    }

    /*
    For a given model return a map of passage # or "Patient" -> histology image URL
     */
    private HashMap<String, Image> getHistologyImageMap(String id) {
        HashMap<String, Image> map = new HashMap<>();

            String histologyFile = dataRootDir+dataSourceAbbreviation+"/hist/"+id;
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
