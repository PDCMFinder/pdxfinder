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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;

/**
 * Load data from IRCC.
 */
@Component
@Order(value = -19)
@PropertySource("classpath:loader.properties")
@ConfigurationProperties(prefix = "ircc")
public class LoadIRCC extends LoaderBase implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadIRCC.class);

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

            irccAlgorithm();

        }
    }


    public void irccAlgorithm() throws Exception {

        step00StartReportManager();

        step02GetMetaDataJSON();

        step03CreateProviderGroup();

        step04CreateNSGammaHostStrain();

        step05CreateNSHostStrain();

        step06CreateProjectGroup();

        step07GetPDXModels();


        for (int i = 0; i < jsonArray.length(); i++) {

            this.jsonData = jsonArray.getJSONObject(i);

            if (loadedModelHashes.contains(jsonData.toString().hashCode())) continue;
            loadedModelHashes.add(jsonData.toString().hashCode());

            step08GetMetaData();

            step09LoadPatientData();

            step10LoadExternalURLs();

            step11LoadBreastMarkers();

            step12CreateModels();

            step13LoadSpecimens();

            step14LoadCurrentTreatment();

        }

        step15LoadImmunoHistoChemistry();

        step16LoadVariationData();
    }


    @Override
    protected void initMethod() {

        log.info("Loading IRCC PDX data.");
        jsonFile = dataRootDir+dataSourceAbbreviation+"/pdx/models.json";

        dataSource = dataSourceAbbreviation;
        filesDirectory = "";
    }


    @Override
    protected void step01GetMetaDataFolder() { }


    @Override
    protected void step03CreateProviderGroup() {

        loadProviderGroup(dataSourceName, dataSourceAbbreviation, dataSourceDescription, providerType, accessibility, "transnational access", dataSourceContact, sourceURL);
    }

    @Override
    protected void step04CreateNSGammaHostStrain() {

        loadNSGammaHostStrain(nsgBsSymbol, nsgbsURL, nsgBsName, nsgBsName);
    }

    @Override
    protected void step05CreateNSHostStrain() { }


    @Override
    protected void step06CreateProjectGroup() {

        loadProjectGroup("EurOPDX");
    }


    @Override
    protected void step07GetPDXModels() {

        loadPDXModels(metaDataJSON,"IRCC");
    }



    // IRCC uses default implementation of Steps step08GetMetaData, step09LoadPatientData


    @Override
    protected void step10LoadExternalURLs() {

        loadExternalURLs(dataSourceContact,Standardizer.NOT_SPECIFIED);

        dataImportService.saveSample(dto.getPatientSample());
        dataImportService.savePatientSnapshot(dto.getPatientSnapshot());
    }


    @Override
    protected void step11LoadBreastMarkers() {

    }


    // IRCC uses default implementation Steps Step11CreateModels default


    @Override
    protected void step13LoadSpecimens()throws Exception {

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
    protected void step14LoadCurrentTreatment() {

        TreatmentSummary ts;
        try {

            if (dto.getTreatments().length() > 0) {

                ts = new TreatmentSummary();
                ts.setUrl(dosingStudyURL);

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
    protected void step15LoadImmunoHistoChemistry() {
        //no IHC data for IRCC
    }


    @Override
    protected void step16LoadVariationData() {

        String variationURLStr = dataRootDir+dataSourceAbbreviation+"/mut/data.json";
        String platformName = "TargetedNGS_MUT";
        String molcharType = "mutation";

        File varFile = new File(variationURLStr);

        if(varFile.exists()){
            if (variationURLStr != null && variationMax != 0) {

                log.info("Loading variation for platform "+platformName);
                //STEP 1: Save the platform
                Platform platform = dataImportService.getPlatform(platformName, dto.getProviderGroup());
                platform.setGroup(dto.getProviderGroup());
                platform.setUrl(targetedNgsPlatformURL);
                dataImportService.savePlatform(platform);


                //STEP 2: get markers and save them with the platform linked
                try{

                    JSONObject job = new JSONObject(utilityService.parseFile(variationURLStr));
                    JSONArray jarray = job.getJSONArray("IRCCVariation");
                    Set<String> markers = new HashSet<>();


                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject variation = jarray.getJSONObject(i);
                        String gene = variation.getString("Gene");
                        markers.add(gene);
                    }


                    //STEP 3: assemble MolecularCharacterization objects for samples

                    //sampleId = > molchar
                    HashMap<String, MolecularCharacterization> xenoSampleMolCharMap = new HashMap();
                    HashMap<String, MolecularCharacterization> humanSampleMolCharMap = new HashMap();


                    for (int i = 0; i < jarray.length(); i++) {
                        if (i == variationMax) {
                            System.out.println("Qutting after loading "+i+" variants");
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

                        NodeSuggestionDTO nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSource, modelId, gene, "mutation", platformName);

                        Marker marker = null;


                        if(nsdto.getNode() == null){

                            //uh oh, we found an unrecognised marker symbol, abort, abort!!!!
                            reportManager.addMessage(nsdto.getLogEntity());
                            continue;
                        }
                        else{
                            //we have a marker object, yay!

                            marker = (Marker)nsdto.getNode();

                            if(nsdto.getLogEntity() != null){
                                reportManager.addMessage(nsdto.getLogEntity());
                            }


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
                            ma.setRsIdVariants(variation.getString("avsnp147"));

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




                        if(i!=0 && i%500==0) log.info("Loaded "+i+" variants.");
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
            String variationURLStr = dataRootDir+dataSourceAbbreviation+"/mut/data.json";
            JSONObject job = new JSONObject(utilityService.parseFile(variationURLStr));
            JSONArray jarray = job.getJSONArray("IRCCVariation");
            //   System.out.println("loading "+jarray.length()+" variant records");

            Platform platform = dataImportService.getPlatform(tech, dto.getProviderGroup(), targetedNgsPlatformURL);
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
                ma.setRsIdVariants(variation.getString("avsnp147"));

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