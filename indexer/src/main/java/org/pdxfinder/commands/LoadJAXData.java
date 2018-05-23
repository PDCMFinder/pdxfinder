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
@Order(value = 0)
public class LoadJAXData implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadJAXData.class);

    private final static String JAX_DATASOURCE_ABBREVIATION = "JAX";
    private final static String JAX_DATASOURCE_NAME = "The Jackson Laboratory";
    private final static String JAX_DATASOURCE_DESCRIPTION = "The Jackson Laboratory PDX mouse models.";
    private final static String DATASOURCE_CONTACT = "http://tumor.informatics.jax.org/mtbwi/pdxRequest.do?mice=";
    private final static String DATASOURCE_URL = "http://tumor.informatics.jax.org/mtbwi/pdxDetails.do?modelID=";

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

    private HostStrain nsgBS;
    private ExternalDataSource jaxDS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private DataImportService dataImportService;
    private Session session;

    @Value("${jaxpdx.file}")
    private String file;

    @Value("${jaxpdx.url}")
    private String urlStr;

    @Value("${jaxpdx.variation.url}")
    private String variationURL;

    @Value("${jaxpdx.histology.url}")
    private String histologyURL;

    @Value("${jaxpdx.variation.max}")
    private int maxVariations;

    @Value("${jaxpdx.ref.assembly}")
    private String refAssembly;


    HashMap<String, String> passageMap = null;
    HashMap<String, Image> histologyMap = null;

    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

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

            if (urlStr != null) {
                log.info("Loading from URL " + urlStr);
                parseJSON(parseURL(urlStr));
            } else if (file != null) {
                log.info("Loading from file " + file);
                parseJSON(parseFile(file));
            } else {
                log.error("No jaxpdx.file or jaxpdx.url provided in properties");
            }
        }
    }

    //JSON Fields {"Model ID","Gender","Age","Race","Ethnicity","Specimen Site","Primary Site","Initial Diagnosis","Clinical Diagnosis",
    //  "Tumor Type","Grades","Tumor Stage","Markers","Sample Type","Strain","Mouse Sex","Engraftment Site"};
    private void parseJSON(String json) {

        jaxDS = dataImportService.getExternalDataSource(JAX_DATASOURCE_ABBREVIATION, JAX_DATASOURCE_NAME, JAX_DATASOURCE_DESCRIPTION,DATASOURCE_CONTACT, SOURCE_URL);

        nsgBS = dataImportService.getHostStrain(NSG_BS_NAME, NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_DESC);

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

        //Check if model exists in DB
        ModelCreation existingModel = dataImportService.findModelByIdAndDataSource(id, JAX_DATASOURCE_ABBREVIATION);
        //Do not load duplicates
        if(existingModel != null) {
            log.error("Skipping existing model "+id);
            return;}

        histologyMap = getHistologyImageMap(id);

        // the preference is for clinical diagnosis but if not available use initial diagnosis
        String diagnosis = j.getString("Clinical Diagnosis");
        if (diagnosis.trim().length() == 0 || "Not specified".equals(diagnosis)) {
            diagnosis = j.getString("Initial Diagnosis");

        }
        
        // if the diagnosis is still unknown don't load it
        if(diagnosis.toLowerCase().contains("unknown") ||
           diagnosis.toLowerCase().contains("not specified")){
            System.out.println("Skipping model "+id+" with diagnosis:"+diagnosis);
            return;
        }

        String classification = j.getString("Tumor Stage") + "/" + j.getString("Grades");
        
        String age = Standardizer.getAge(j.getString("Age"));
        String gender = Standardizer.getGender(j.getString("Gender"));
        
        String race = Standardizer.fixNotString(j.getString("Race"));
        String ethnicity = Standardizer.fixNotString(j.getString("Ethnicity"));

        PatientSnapshot pSnap = dataImportService.getPatientSnapshot(j.getString("Patient ID"), gender,
                race, ethnicity, age, jaxDS);

        String tumorType = Standardizer.getTumorType(j.getString("Tumor Type"));
        Sample sample = dataImportService.getSample(j.getString("Model ID"), tumorType, diagnosis,
                j.getString("Primary Site"), j.getString("Specimen Site"), j.getString("Sample Type"), classification, NORMAL_TISSUE_FALSE, JAX_DATASOURCE_ABBREVIATION);

        List<ExternalUrl> externalUrls = new ArrayList<>();
        externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.CONTACT, DATASOURCE_CONTACT+id));
        externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.SOURCE, DATASOURCE_URL+id));

        String extraction = j.getString("Sample Type");
        sample.setExtractionMethod(extraction);

        if (histologyMap.containsKey("Patient")) {
            Histology histology = new Histology();
            Image image = histologyMap.get("Patient");
            histology.addImage(image);
            sample.addHistology(histology);

        }

        String qaPassages = Standardizer.NOT_SPECIFIED;

        pSnap.addSample(sample);
        dataImportService.savePatientSnapshot(pSnap);
        
         // Pending or Complete
        String qc = j.getString("QC");
        if("Pending".equals(qc)){
            qc = Standardizer.NOT_SPECIFIED;
        }else{
            qc = "QC is "+qc;
        }
        
        // the validation techniques are more than just fingerprint, we don't have a way to capture that
        QualityAssurance qa = new QualityAssurance("Fingerprint", qc, qaPassages);
        dataImportService.saveQualityAssurance(qa);

        ModelCreation mc = dataImportService.createModelCreation(id, jaxDS.getAbbreviation(), sample, qa, externalUrls);
        mc.addRelatedSample(sample);

        String implantationTypeStr = Standardizer.NOT_SPECIFIED;
        String implantationSiteStr = j.getString("Engraftment Site");

        Specimen specimen = dataImportService.getSpecimen(mc, id, jaxDS.getAbbreviation(), "");
        specimen.setHostStrain(nsgBS);
        EngraftmentSite engraftmentSite = dataImportService.getImplantationSite(implantationSiteStr);
        EngraftmentType engraftmentType = dataImportService.getImplantationType(implantationTypeStr);
        specimen.setEngraftmentSite(engraftmentSite);
        specimen.setEngraftmentType(engraftmentType);

        mc.addSpecimen(specimen);

        //Create Treatment summary without linking TreatmentProtocols to specimens
        TreatmentSummary ts;

        try{
            if(j.has("Treatments")){
                JSONArray treatments = j.getJSONArray("Treatments");

                if(treatments.length() > 0){
                    //log.info("Treatments found for model "+mc.getSourcePdxId());
                    ts = new TreatmentSummary();
                    ts.setUrl(DOSING_STUDY_URL);

                    for(int t = 0; t<treatments.length(); t++){
                        JSONObject treatmentObject = treatments.getJSONObject(t);

                        TreatmentProtocol tp = Standardizer.getTreatmentProtocol(treatmentObject.getString("Drug"),
                                treatmentObject.getString("Dose") + " "+treatmentObject.getString("Units"),
                                treatmentObject.getString("Response"));

                        ts.addTreatmentProtocol(tp);
                    }

                    ts.setModelCreation(mc);
                    mc.setTreatmentSummary(ts);
                }
            }


        }
        catch(Exception e){

            e.printStackTrace();
        }

        
        dataImportService.saveSpecimen(specimen);
        //loaderUtils.saveModelCreation(mc);
        loadVariationData(mc, engraftmentSite, engraftmentType);

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

                Platform platform = dataImportService.getPlatform(technology, this.jaxDS);

                if(technology.equals("Truseq_JAX")){
                    platform = dataImportService.getPlatform(technology, this.jaxDS, TRUSEQ_PLATFORM_URL);
                }
                else if(technology.equals("Whole_Exome")){
                    platform = dataImportService.getPlatform(technology, this.jaxDS, WHOLE_EXOME_URL);
                }
                else if(technology.equals("CTP")){
                    platform = dataImportService.getPlatform(technology, this.jaxDS, CTP_PLATFORM_URL);
                }

                platform.setExternalDataSource(jaxDS);

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
                        platform = dataImportService.getPlatform(tech, this.jaxDS, TRUSEQ_PLATFORM_URL);
                    }
                    else if(tech.equals("Whole_Exome")){
                        platform = dataImportService.getPlatform(tech, this.jaxDS, WHOLE_EXOME_URL);
                    }
                    else if(tech.equals("CTP")){
                        platform = dataImportService.getPlatform(tech, this.jaxDS, CTP_PLATFORM_URL);
                    }


                    mc.setPlatform(platform);
                    mc.setMarkerAssociations(markerMap.get(tech));
                    mcs.add(mc);

                }

                
                Specimen specimen = dataImportService.getSpecimen(modelCreation, sampleKey, this.jaxDS.getAbbreviation(), passage);
     
                Sample specSample = new Sample();
                specSample.setSourceSampleId(sampleKey);
                specimen.setSample(specSample);
                specSample.setMolecularCharacterizations(mcs);
                

                if (histologyMap.containsKey(passage)) {
                    Histology histology = new Histology();
                    Image image = histologyMap.get(passage);
                    histology.addImage(image);
                    specSample.addHistology(histology);

                }


                // all JAX mice are NSG, even if not specified in feed
                specimen.setHostStrain(nsgBS);
                

                specimen.setEngraftmentSite(engraftmentSite);
                specimen.setEngraftmentType(engraftmentType);


                dataImportService.saveSpecimen(specimen);

                modelCreation.addSpecimen(specimen);
                modelCreation.addRelatedSample(specSample);


                System.out.println("saved passage " + passage + " for model " + modelCreation.getSourcePdxId() + " from sample " + sampleKey);
            }

            dataImportService.saveModelCreation(modelCreation);

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
