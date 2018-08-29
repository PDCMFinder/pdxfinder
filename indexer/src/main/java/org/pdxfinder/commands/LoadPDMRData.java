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
import org.pdxfinder.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.ds.Standardizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Load PDMR data
 */
@Component
@Order(value = 0)
public class LoadPDMRData implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadPDMRData.class);

    private final static String DATASOURCE_ABBREVIATION = "PDMR";
    private final static String DATASOURCE_NAME = "National Cancer Institute";
    private final static String DATASOURCE_DESCRIPTION = "NCI Patient-Derived Models Repository ";
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

    private HostStrain nsgBS;
    private Group DS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private DataImportService dataImportService;
    private Session session;

    @Value("${pdmrpdx.file}")
    private String file;

    @Value("${pdmrpdx.url}")
    private String urlStr;

    @Value("${pdmrpdx.variation.url}")
    private String variationURL;

    @Value("${pdmrpdx.histology.url}")
    private String histologyURL;

    @Value("${pdmrpdx.variation.max}")
    private int maxVariations;

    @Value("${pdmrpdx.ref.assembly}")
    private String refAssembly;

    @Value("${pdmrpdx.mutations.file}")
    private String mutationsFile;

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

        if (options.has("loadPDMR") || options.has("loadALL")) {

            log.info("Loading PDMR PDX data.");

            if (file != null) {
                log.info("Loading from file " + file);
                parseJSON(parseFile(file));

                //loadMutationData();


            } /* else if (urlStr != null) {
                log.info("Loading from URL " + urlStr);
                parseJSON(parseURL(urlStr));
            } */else {
                log.error("No pdmrpdx.file or pdmrpdx.url provided in properties");
            }
        }
    }

    //JSON Fields {"Model ID","Gender","Age","Race","Ethnicity","Specimen Site","Primary Site","Initial Diagnosis","Clinical Diagnosis",
    //  "Tumor Type","Grades","Tumor Stage","Markers","Sample Type","Strain","Mouse Sex","Engraftment Site"};
    private void parseJSON(String json) {

        DS = dataImportService.getProviderGroup(DATASOURCE_NAME, DATASOURCE_ABBREVIATION,
                DATASOURCE_DESCRIPTION, PROVIDER_TYPE, ACCESSIBILITY, null, DATASOURCE_CONTACT, SOURCE_URL);

        nsgBS = dataImportService.getHostStrain(NSG_BS_NAME, NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_NAME);

        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("pdxInfo");

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);

                createGraphObjects(j);
            }

        } catch (Exception e) {
            log.error("Error getting JAX PDX models", e);

        }
    }


    @Transactional
    void createGraphObjects(JSONObject j) throws Exception {
        String id = j.getString("Model ID");

        //histologyMap = getHistologyImageMap(id);

        // the preference is for clinical diagnosis but if not available use initial diagnosis
        String diagnosis = j.getString("Clinical Diagnosis");
        if (diagnosis.trim().length() == 0 || "Not specified".equals(diagnosis)) {
            diagnosis = j.getString("Initial Diagnosis");
        }

        String tumorType = Standardizer.getTumorType(j.getString("Tumor Type"));
        String age = Standardizer.getAge(j.getString("Age"));
        String gender = Standardizer.getGender(j.getString("Gender"));
        String patientId = j.getString("Patient ID");
        String race = j.getString("Race");
        String ethnicity = j.getString("Ethnicity");
        String modelId = j.getString("Model ID");
        String primarySite = j.getString("Primary Site");
        String sampleSite = j.getString("Specimen Site");
        String extractionType = j.getString("Sample Type");
        String stage = j.getString("Stage Value");
        String grade = j.getString("Grade Value");



        Patient patient = dataImportService.getPatientWithSnapshots(patientId, DS);

        if(patient == null){

            patient = dataImportService.createPatient(patientId, DS, gender, "", ethnicity);
        }

        PatientSnapshot pSnap = dataImportService.getPatientSnapshot(patient, age, "", "", "");


        //String sourceSampleId, String dataSource,  String typeStr, String diagnosis, String originStr,
        //String sampleSiteStr, String extractionMethod, Boolean normalTissue, String stage, String stageClassification,
        // String grade, String gradeClassification
        Sample humanSample = dataImportService.getSample(id, DS.getAbbreviation(), tumorType, diagnosis, primarySite,
                sampleSite, extractionType, false, stage, "", grade, "");



        List<ExternalUrl> externalUrls = new ArrayList<>();
        externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.CONTACT, DATASOURCE_CONTACT));
        externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.SOURCE, j.getString("Source url")));

        JSONArray validations = j.getJSONArray("Validations");

        List<QualityAssurance> validationList = new ArrayList<>();
        if(validations.length() > 0){

            for(int i=0; i<validations.length(); i++){

                JSONObject validationObj = validations.getJSONObject(i);
                QualityAssurance qa = new QualityAssurance(validationObj.getString("Technique"), validationObj.getString("Description"), validationObj.getString("Passage"));
                validationList.add(qa);
            }
        }

        pSnap.addSample(humanSample);
        dataImportService.savePatientSnapshot(pSnap);

        ModelCreation modelCreation = dataImportService.createModelCreation(id, this.DS.getAbbreviation(), humanSample, validationList, externalUrls);
        modelCreation.addRelatedSample(humanSample);

        //load specimens
        String engraftmentSite = j.getString("Engraftment Site");
        String engraftmentType = j.getString("Engraftment Type");
        String extractionMethod = j.getString("Extraction Method");

        JSONArray samplesArr = j.getJSONArray("Samples");

        if(samplesArr.length() > 0){
            for(int i=0; i<samplesArr.length();i++){

                JSONObject sampleObj = samplesArr.getJSONObject(i);
                String sampleType = sampleObj.getString("Tumor Type");

                if(sampleType.equals("Xenograft Tumor")){

                    String specimenId = sampleObj.getString("Sample ID");
                    String passage = sampleObj.getString("Passage");

                    Specimen specimen = dataImportService.getSpecimen(modelCreation,
                            specimenId, DS.getAbbreviation(), passage);

                    specimen.setHostStrain(nsgBS);

                    EngraftmentSite es = dataImportService.getImplantationSite(engraftmentSite);
                    specimen.setEngraftmentSite(es);

                    EngraftmentType et = dataImportService.getImplantationType(engraftmentType);
                    specimen.setEngraftmentType(et);

                    Sample specSample = new Sample();

                    specSample.setSourceSampleId(specimenId);
                    specSample.setDataSource(DS.getAbbreviation());

                    specimen.setSample(specSample);

                    modelCreation.addSpecimen(specimen);
                    modelCreation.addRelatedSample(specSample);

                }

            }
        }



        //load patient treatment

        TreatmentSummary ts = new TreatmentSummary();

        JSONArray treatmentArr = j.getJSONArray("Treatments");

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
        pSnap.setTreatmentSummary(ts);
        dataImportService.savePatientSnapshot(pSnap);

        //loadVariationData(mc);
        dataImportService.saveModelCreation(modelCreation);
    }



    private void loadMutationData(){

        log.info("Loading mutation data for PDMR samples");
        //load samples file
        int currentLineCounter = 1;
        String currentLine;
        String[] row;
        Sample sample;
        MolecularCharacterization mc;

        Platform pl = dataImportService.getPlatform("Gene Panel", DS);

        Map<String, Sample> sampleMap = new HashMap<>();
        Map<String, MolecularCharacterization> molcharMap = new HashMap<>();

        Set<String> missingPatients = new HashSet<>();
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
                            sample = dataImportService.findHumanSample(modelId, DS.getAbbreviation());
                        }
                        else{

                            //get the xenograft sample
                            sample = dataImportService.findXenograftSample(modelId, DS.getAbbreviation(), sampleId);
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

            JSONObject job = new JSONObject(parseURL(this.variationURL + modelCreation.getSourcePdxId()));
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

                Platform platform = dataImportService.getPlatform(technology, this.DS);
                platform.setUrl(PLATFORM_URL);

                // why would this happen?
                if (platform.getGroup() == null) {
                    platform.setGroup(DS);
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
                    mc.setPlatform(dataImportService.getPlatform(tech, this.DS));
                    mc.setMarkerAssociations(markerMap.get(tech));
                    mcs.add(mc);

                }

                //PdxPassage pdxPassage = new PdxPassage(modelCreation, passage);


                Specimen specimen = dataImportService.getSpecimen(modelCreation, sampleKey, this.DS.getName(), passage);

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
            JSONObject job = new JSONObject(parseURL(this.histologyURL + id));
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
            log.error("Unable to read from URL " + urlStr, e);
        }
        return sb.toString();
    }

    private String parseFile(String path) {

        StringBuilder sb = new StringBuilder();

        try {
            Stream<String> stream = Files.lines(Paths.get(path));

            Iterator itr = stream.iterator();
            while (itr.hasNext()) {
                sb.append(itr.next());
            }
        } catch (Exception e) {
            log.error("Failed to load file " + path, e);
        }
        return sb.toString();
    }

}
