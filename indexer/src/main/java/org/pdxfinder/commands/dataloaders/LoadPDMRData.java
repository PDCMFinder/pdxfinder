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
import java.io.FileReader;
import java.util.*;

/**
 * Load PDMR data
 */
@Component
@Order(value = -16)
public class LoadPDMRData extends LoaderBase implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadPDMRData.class);

    private final static String DATASOURCE_ABBREVIATION = "PDMR";
    private final static String DATASOURCE_NAME = "Patient-Derived Models Repository";
    private final static String DATASOURCE_DESCRIPTION = "Patient-Derived Models Repository";
    private final static String DATASOURCE_CONTACT = "https://pdmr.cancer.gov/request/default.htm";

    private final static String PROVIDER_TYPE = "";
    private final static String ACCESSIBILITY = "";

    private final static String NSG_BS_NAME = "NOD scid gamma";
    private final static String NSG_BS_SYMBOL = "NOD.Cg-PrkdcscidIl2rgtm1Wjl/SzJ";
    private final static String NSG_BS_URL = "";
    private final static String HISTOLOGY_NOTE = "";
    private final static String ENGRAFTMENT = "Engraftment";
    private final static String SOURCE_URL = "/source/pdmr/";
    private final static String PLATFORM_URL = "/platform/nci-cancer-gene-panel/";

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private DataImportService dataImportService;
    private Session session;

    @Autowired
    private UtilityService utilityService;


    @Value("${pdmrpdx.variation.max}")
    private int maxVariations;

    @Value("${pdmrpdx.ref.assembly}")
    private String refAssembly;


    @Value("${pdxfinder.data.root.dir}")
    private String dataRootDir;

    HashMap<String, String> passageMap = null;
    HashMap<String, Image> histologyMap = null;

    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadPDMRData(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }



    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadPDMR", "Load PDMR PDX data");
        parser.accepts("loadALL", "Load all, including PDMR PDX data");
        OptionSet options = parser.parse(args);

        initMethod();

        loaderTemplate();

    }


    @Override
    protected void initMethod() {

        log.info("Loading PDMR PDX data.");

        dto = new LoaderDTO();

        jsonFile = dataRootDir+DATASOURCE_ABBREVIATION+"/pdx/models.json";

        dataSource = DATASOURCE_ABBREVIATION;
        dataSourceAbbreviation = DATASOURCE_ABBREVIATION;
        dataSourceContact = DATASOURCE_CONTACT;
    }


    @Override
    protected void step00GetMetaDataFolder() {

    }

    // PDMR uses default implementation Steps step01GetMetaDataJSON


    @Override
    protected void step02CreateProviderGroup() {

        loadProviderGroup(DATASOURCE_NAME, DATASOURCE_ABBREVIATION, DATASOURCE_DESCRIPTION, PROVIDER_TYPE, ACCESSIBILITY, null, DATASOURCE_CONTACT, SOURCE_URL);
    }

    @Override
    protected void step03CreateNSGammaHostStrain() {

        loadNSGammaHostStrain(NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_NAME, NSG_BS_NAME);
    }

    @Override
    protected void step04CreateNSHostStrain() { }


    @Override
    protected void step05CreateProjectGroup() {

    }


    @Override
    protected void step06GetPDXModels() {

        loadPDXModels(metaDataJSON,"pdxInfo");
    }

    // PDMR uses default implementation Steps step07GetMetaData

    @Override
    protected void step08LoadPatientData() {

        if (dataImportService.isExistingModel(dto.getProviderGroup().getAbbreviation(), dto.getModelID())) return;
        super.step08LoadPatientData();
    }


    @Override
    protected void step09LoadExternalURLs() {

        dataImportService.savePatientSnapshot(dto.getPatientSnapshot());

        loadExternalURLs(DATASOURCE_CONTACT, dto.getSourceURL());
    }


    @Override
    protected void step10BLoadBreastMarkers() {

    }



    @Override
    protected void step11CreateModels() throws Exception {

        List<QualityAssurance> validationList = new ArrayList<>();
        if(dto.getValidationsArr().length() > 0){

            for(int k=0; k<dto.getValidationsArr().length(); k++){

                JSONObject validationObj = dto.getValidationsArr().getJSONObject(k);
                QualityAssurance qa = new QualityAssurance(validationObj.getString("Technique"), validationObj.getString("Description"), validationObj.getString("Passage"));
                validationList.add(qa);
            }
        }

        ModelCreation modelCreation = dataImportService.createModelCreation(dto.getModelID(), dto.getProviderGroup().getAbbreviation(), dto.getPatientSample(), validationList, dto.getExternalUrls());
        modelCreation.addRelatedSample(dto.getPatientSample());
        dto.setModelCreation(modelCreation);
    }



    @Override
    protected void step12LoadSpecimens()throws Exception {

        //load specimens
        if(dto.getSamplesArr().length() > 0){
            for(int i=0; i<dto.getSamplesArr().length();i++){

                JSONObject sampleObj = dto.getSamplesArr().getJSONObject(i);
                String sampleType = sampleObj.getString("Tumor Type");

                if(sampleType.equals("Xenograft Tumor")){

                    String specimenId = sampleObj.getString("Sample ID");
                    String passage = sampleObj.getString("Passage");

                    Specimen specimen = dataImportService.getSpecimen(dto.getModelCreation(),
                            specimenId, dto.getProviderGroup().getAbbreviation(), passage);

                    specimen.setHostStrain(dto.getNodScidGamma());

                    EngraftmentSite es = dataImportService.getImplantationSite(dto.getImplantationSiteStr());
                    specimen.setEngraftmentSite(es);

                    EngraftmentType et = dataImportService.getImplantationType(dto.getImplantationtypeStr());
                    specimen.setEngraftmentType(et);

                    Sample specSample = new Sample();

                    specSample.setSourceSampleId(specimenId);
                    specSample.setDataSource(dto.getProviderGroup().getAbbreviation());

                    specimen.setSample(specSample);

                    dto.getModelCreation().addSpecimen(specimen);
                    dto.getModelCreation().addRelatedSample(specSample);

                }

            }
        }
    }




    @Override
    protected void step13CreateCurrentTreatment() throws Exception {

        TreatmentSummary ts;

        //Disable loading treatment temporarily, drug names are not harmonized!
        Boolean loadTreatment = false;
        //don't create two treatmentsummaries for the same snapshot
        if(loadTreatment && dataImportService.findTreatmentSummaryByPatientSnapshot(dto.getPatientSnapshot()) == null){
            ts = new TreatmentSummary();

            JSONArray treatmentArr = dto.getTreatments();

            for(int k=0; k<treatmentArr.length();k++){

                JSONObject treatmentObj = treatmentArr.getJSONObject(k);
                TreatmentProtocol tp;

                String drugString;
                String date;
                String duration = treatmentObj.getString("Duration");
                String response = treatmentObj.getString("Response");
                //this is the current treatment
                if(treatmentObj.has("Current Drug")){

                    drugString = treatmentObj.getString("Current Drug");
                    date = treatmentObj.getString("Starting Date");
                    tp = dataImportService.getTreatmentProtocol(drugString, "", response, true);

                }
                //not current treatment, create default TreatmentProtocol object
                else{

                    drugString = treatmentObj.getString("Prior Drug");
                    date = treatmentObj.getString("Prior Date");
                    tp = dataImportService.getTreatmentProtocol(drugString, "", response, false);
                }

                tp.setTreatmentDate(date);
                tp.addDurationForAllComponents(duration);
                ts.addTreatmentProtocol(tp);
            }

            //save summary on snapshot
            dto.getPatientSnapshot().setTreatmentSummary(ts);
            dataImportService.savePatientSnapshot(dto.getPatientSnapshot());
        }
        //loadVariationData(mc);
        dataImportService.saveModelCreation(dto.getModelCreation());

    }


    @Override
    protected void step14LoadImmunoHistoChemistry() {

    }


    @Override
    protected void step15VariationData() {

    }







    private void loadMutationData(){

        log.info("Loading mutation data for PDMR samples");
        //load samples file
        int currentLineCounter = 1;
        String currentLine;
        String[] row;
        Sample sample;
        MolecularCharacterization mc;

        Platform pl = dataImportService.getPlatform("Gene Panel", dto.getProviderGroup());

        Map<String, Sample> sampleMap = new HashMap<>();
        Map<String, MolecularCharacterization> molcharMap = new HashMap<>();

        Set<String> missingPatients = new HashSet<>();

        String mutationsFile = dataRootDir+DATASOURCE_ABBREVIATION+"/mut/data.csv";
        try {
            BufferedReader buf = new BufferedReader(new FileReader(mutationsFile));

            while (true) {
                currentLine = buf.readLine();
                if (currentLine == null) {
                    break;
                } else if (currentLineCounter < 2) {
                    currentLineCounter++;
                    continue;

                } else {
                    row = currentLine.split("\t");

                    String patientId = row[1];
                    String modelId = row[1] + "-" + row[2];
                    String sampleId = row[3];
                    String markerSymbol = row[8];
                    String aaChange = row[9];

                    String chromosome = row[12];
                    String position = row[13];
                    String refAllele = row[14];
                    String altAllele = row[15];
                    String alleleFreq = row[16];

                    String readDepth = row[17];
                    String consequence = row[18];
                    String rsVariant = row[21];

                    //skip rows where there is no marker
                    if(markerSymbol.equals("None Found")) continue;


                    //check if this is a sample that we already have in the map
                    if(sampleMap.containsKey(modelId+sampleId)){

                        sample = sampleMap.get(modelId+sampleId);
                    }
                    //this is a sample that is not in the map yet, get it from the db
                    else{

                        if(sampleId.equals("ORIGINATOR")){

                            //get a human sample
                            sample = dataImportService.findHumanSample(modelId, dto.getProviderGroup().getAbbreviation());
                        }
                        else{

                            //get the xenograft sample
                            sample = dataImportService.findXenograftSample(modelId, dto.getProviderGroup().getAbbreviation(), sampleId);
                        }

                    }


                    if(sample == null){

                        log.error("Sample "+sampleId + " not found for model "+modelId);
                        missingPatients.add(patientId);
                        continue;
                    }
                    //found the sample
                    else{

                        //add it to the sample map
                        sampleMap.put(modelId+sampleId, sample);

                        //does this sample have molchar obj?
                        if(molcharMap.containsKey(modelId+sampleId)){

                            mc = molcharMap.get(modelId+sampleId);
                        }
                        else{

                            mc = new MolecularCharacterization();
                            mc.setType("mutation");
                            mc.setPlatform(pl);
                        }


                        Marker m = dataImportService.getMarker(markerSymbol);
                        MarkerAssociation ma = new MarkerAssociation();

                        ma.setMarker(m);
                        ma.setAminoAcidChange(aaChange);
                        ma.setChromosome(chromosome);
                        ma.setSeqPosition(position);
                        ma.setRefAllele(refAllele);
                        ma.setAltAllele(altAllele);
                        ma.setAlleleFrequency(alleleFreq);
                        ma.setReadDepth(readDepth);
                        ma.setConsequence(consequence);
                        ma.setRsVariants(rsVariant);


                        mc.addMarkerAssociation(ma);

                        //put the updated mc back into the map
                        molcharMap.put(modelId+sampleId, mc);

                    }

                }
                currentLineCounter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //go through the samples and save the mc object for them
        for (Map.Entry<String, Sample> entry : sampleMap.entrySet()) {
            String key = entry.getKey();
            Sample s = entry.getValue();

            MolecularCharacterization molchar = molcharMap.get(key);

            s.addMolecularCharacterization(molchar);
            dataImportService.saveSample(s);

        }


        log.info("DONE loading mutation data for PDMR.");

        if(missingPatients.size() > 0){

            log.error("Missing patients: " + missingPatients.toString());
        }

    }



    /*
    Use the modelID in the modelCreation object to get model specific variation data
    This is a set of makers with marker association details
    Since we are creating samples here attach any histology images to the sample based on passage #
     */
    private void loadVariationData(ModelCreation modelCreation) {

        if (maxVariations == 0) {
            return;
        }

        try {

            passageMap = new HashMap<>();

            HashMap<String, HashMap<String, List<MarkerAssociation>>> sampleMap = new HashMap<>();
            HashMap<String, List<MarkerAssociation>> markerMap = new HashMap<>();
            String variationURL = "";
            JSONObject job = new JSONObject(utilityService.parseFile(variationURL + modelCreation.getSourcePdxId()));
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

                Platform platform = dataImportService.getPlatform(technology, dto.getProviderGroup());
                platform.setUrl(PLATFORM_URL);

                // why would this happen?
                if (platform.getGroup() == null) {
                    platform.setGroup(dto.getProviderGroup());
                }
                dataImportService.createPlatformAssociation(platform, marker);


                markerMap = sampleMap.get(sample);
                if (markerMap == null) {
                    markerMap = new HashMap<>();
                }

                // make a map of markerAssociation collections keyed to technology
                if (markerMap.containsKey(technology)) {
                    markerMap.get(technology).add(ma);
                }
                else {
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
                    mc.setPlatform(dataImportService.getPlatform(tech, dto.getProviderGroup()));
                    mc.setMarkerAssociations(markerMap.get(tech));
                    mcs.add(mc);

                }

                //PdxPassage pdxPassage = new PdxPassage(modelCreation, passage);


                Specimen specimen = dataImportService.getSpecimen(modelCreation, sampleKey, dto.getProviderGroup().getName(), passage);

                Sample specSample = new Sample();
                specSample.setSourceSampleId(sampleKey);
                specimen.setSample(specSample);
                specSample.setMolecularCharacterizations(mcs);
                //specimen.setPdxPassage(pdxPassage);

                if (histologyMap.containsKey(passage)) {
                    Histology histology = new Histology();
                    Image image = histologyMap.get(passage);
                    histology.addImage(image);
                    specSample.addHistology(histology);

                }

                //pdxPassage.setModelCreation(modelCreation);

                //loaderUtils.savePdxPassage(pdxPassage);
                dataImportService.saveSpecimen(specimen);

                modelCreation.addRelatedSample(specSample);
                dataImportService.saveModelCreation(modelCreation);

                System.out.println("saved passage " + passage + " for model " + modelCreation.getSourcePdxId() + " from sample " + sampleKey);
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
        try {
            String histologyURL = "";
            JSONObject job = new JSONObject(utilityService.parseFile(histologyURL + id));
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

        return map;
    }

}
