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
import org.pdxfinder.services.dto.LoaderDTO;
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
import java.util.*;

/**
 * Load PDMR data
 */
@Component
@Order(value = -16)
@PropertySource("classpath:loader.properties")
@ConfigurationProperties(prefix = "pdmr")
public class LoadPDMRData extends LoaderBase implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadPDMRData.class);

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;


    /*private Session session;

    @Value("${pdmrpdx.variation.max}")
    private int maxVariations;

    @Value("${pdmrpdx.ref.assembly}")
    private String refAssembly;

    HashMap<String, String> passageMap = null;
    HashMap<String, Image> histologyMap = null;
    */

    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadPDMRData(UtilityService utilityService, DataImportService dataImportService) {
        super(utilityService, dataImportService);
    }



    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadPDMR", "Load PDMR PDX data");
        parser.accepts("loadALL", "Load all, including PDMR PDX data");
        OptionSet options = parser.parse(args);


        if (options.has("loadPDMR") || options.has("loadALL")) {

            initMethod();

            globalLoadingOrder();
        }



    }




    @Override
    protected void initMethod() {

        log.info("Loading PDMR PDX data.");

        dto = new LoaderDTO();

        jsonFile = finderRootDir +"/data/"+dataSourceAbbreviation+"/pdx/models.json";
        dataSource = dataSourceAbbreviation;
    }


    @Override
    protected void step01GetMetaDataFolder() {

    }

    // PDMR uses default implementation Steps step02GetMetaDataJSON

    @Override
    protected void step05CreateNSHostStrain() {

    }


    @Override
    protected void step06CreateProjectGroup() {

    }

    // PDMR uses default implementation Steps step08GetMetaData

    @Override
    protected void step09LoadPatientData() {

        if (dataImportService.isExistingModel(dto.getProviderGroup().getAbbreviation(), dto.getModelID())) return;
        super.step09LoadPatientData();
    }


    @Override
    protected void step10LoadExternalURLs() {

        dataImportService.savePatientSnapshot(dto.getPatientSnapshot());
        loadExternalURLs(dataSourceContact, dto.getSourceURL());
    }


    @Override
    protected void step11LoadBreastMarkers() {

    }



    @Override
    protected void step12CreateModels() throws Exception {

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
    protected void step13LoadSpecimens()throws Exception {

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
    protected void step14LoadPatientTreatments() throws Exception {

        TreatmentSummary ts;

        //Disable loading treatment temporarily, drug names are not harmonized!
        Boolean loadTreatment = true;
        //don't create two treatmentsummaries for the same snapshot
        if(loadTreatment && dataImportService.findTreatmentSummaryByPatientSnapshot(dto.getPatientSnapshot()) == null){
            ts = new TreatmentSummary();

            JSONArray treatmentArr = dto.getPatientTreatments();

            for(int k=0; k<treatmentArr.length();k++){

                JSONObject treatmentObj = treatmentArr.getJSONObject(k);
                TreatmentProtocol tp = null;

                String drugString = "";
                String date = "";
                String duration = treatmentObj.getString("Duration");
                String response = treatmentObj.getString("Response");
                Boolean currentTreatment = false;

                try {
                    //this is the current treatment
                    if (treatmentObj.has("Current Drug")) {

                        drugString = treatmentObj.getString("Current Drug");
                        date = treatmentObj.getString("Starting Date");
                        currentTreatment = true;

                    }
                    //not current treatment, create default TreatmentProtocol object
                    else {

                        drugString = treatmentObj.getString("Prior Drug");
                        date = treatmentObj.getString("Prior Date");
                        currentTreatment = false;

                    }

                    if (drugString.equals("Released at Trial Closure") || drugString.equals("No Current Therapy") || drugString.toLowerCase().equals("treatment naive")) continue;
                    tp = dataImportService.getTreatmentProtocol(drugString, "", response, currentTreatment);

                }
                catch(Exception e){
                    e.printStackTrace();
                    log.error("Error loading treatment. Model: "+dto.getModelID() +" Drugstring: "+drugString);
                }
                if(tp != null) {
                    tp.setTreatmentDate(date);
                    tp.addDurationForAllComponents(duration);
                    ts.addTreatmentProtocol(tp);
                }
            }

            //save summary on snapshot
            if(ts.getTreatmentProtocols() != null && ts.getTreatmentProtocols().size() > 0){
                dto.getPatientSnapshot().setTreatmentSummary(ts);
                dataImportService.savePatientSnapshot(dto.getPatientSnapshot());
            }

        }
        //loadVariationData(mc);
        dataImportService.saveModelCreation(dto.getModelCreation());

    }


    @Override
    protected void step15LoadImmunoHistoChemistry() {

    }


    @Override
    protected void step16LoadVariationData() {

        log.info("Loading NGS for model " + dto.getModelCreation().getSourcePdxId());

        loadOmicData(dto.getModelCreation(), dto.getProviderGroup(), "mutation",finderRootDir+"/data");
    }


    @Override
    void step17LoadModelDosingStudies() throws Exception {

    }



}
