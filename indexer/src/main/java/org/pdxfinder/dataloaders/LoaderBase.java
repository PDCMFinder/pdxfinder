package org.pdxfinder.dataloaders;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ds.Harmonizer;
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.LoaderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public abstract class LoaderBase extends UniversalLoaderOmic implements ApplicationContextAware{

    private static final Logger log = LoggerFactory.getLogger(LoaderBase.class);
    protected String jsonFile;
    protected String dataSource;
    protected String rootDataDirectory;
    protected File[] listOfFiles;
    protected String metaDataJSON = "NOT FOUND";
    protected JSONArray jsonArray;
    protected String filesDirectory;
    protected JSONObject jsonData;
    protected LoaderDTO dto = new LoaderDTO();
    protected static ApplicationContext context;
    protected ReportManager reportManager;
    protected Boolean skipThis = false;

    protected Group providerDS;
    protected Group projectDS;
    protected HostStrain nsgBS;
    protected HostStrain nsBS;

    public LoaderBase(UtilityService utilityService, DataImportService dataImportService) {
        super(utilityService, dataImportService);
    }

    // Derived classes MUST override initMethod()
    abstract void initMethod();

    void step00StartReportManager(){
        reportManager = (ReportManager) context.getBean("ReportManager");
    }

    void step01GetMetaDataFolder(){
        listOfFiles = new File[0];
        File folder = new File(filesDirectory);

        if(folder.exists()){
            listOfFiles = folder.listFiles();
            if(listOfFiles.length == 0){
                log.info("No file found for "+dataSource+", skipping");
                skipThis = true;
            }
        }
        else{ log.info("Directory does not exist, skipping."); }
    }

    void step02GetMetaDataJSON(){
        File file = new File(jsonFile);
        if (file.exists()) {

            log.info("Loading from file " + jsonFile);
            this.metaDataJSON = utilityService.parseFile(jsonFile);
        } else {
            log.info("No file found for " + dataSource + "("+jsonFile+"), skipping");
            skipThis = true;
        }
    }


    void step03CreateProviderGroup(){
        providerDS = dataImportService.getProviderGroup(dataSourceName, dataSourceAbbreviation, dataSourceDescription, providerType, dataSourceContact, sourceURL);

    }

    void step04CreateNSGammaHostStrain(){
        try {

            nsgBS = dataImportService.getHostStrain(nsgBsName, nsgBsSymbol, nsgbsURL, nsgBsName);
        } catch (Exception e) {
            log.error("Failed to create NSG hoststrain", e);
        }
    }

    void step05CreateNSHostStrain(){
        try {
            nsBS = dataImportService.getHostStrain(nsBsName, nsBsSymbol, nsBsURL, nsBsName);
        } catch (Exception e) {
            log.error("Failed to create NS hoststrain", e);
        }
    }

    void step06SetProjectGroup(){
        projectDS = dataImportService.getProjectGroup(projectGroup);
    }

    void step07GetPDXModels(){

        try {
            JSONObject job = new JSONObject(metaDataJSON);
            jsonArray = job.getJSONArray(jsonKey);
        } catch (Exception e) {
            log.error("Error getting "+jsonKey+" PDX models", e);
        }
    }

    void step08GetMetaData() throws Exception {

        dto = new LoaderDTO();

        JSONObject data = this.jsonData;
        String ds = dataSourceAbbreviation;

        dto.setModelID(data.getString("Model ID"));
        dto.setSampleID(Harmonizer.getSampleID(data,ds));
        dto.setDiagnosis(Harmonizer.getDiagnosis(data,ds));
        dto.setPatientId(Standardizer.getValue("Patient ID",data));
        dto.setEthnicity(Harmonizer.getEthnicity(data,ds));
        dto.setStage(Harmonizer.getStage(data,ds));
        dto.setGrade(Harmonizer.getGrade(data,ds));
        dto.setClassification(Harmonizer.getClassification(data,ds));

        dto.setAge(Standardizer.getAge(data.getString("Age")));
        dto.setGender(Standardizer.getGender(data.getString("Gender")));
        dto.setTumorType(Standardizer.getTumorType(data.getString("Tumor Type")));
        dto.setSampleSite(Standardizer.getValue("Sample Site",data));
        dto.setPrimarySite(Standardizer.getValue("Primary Site",data));
        dto.setExtractionMethod(Standardizer.getValue("Sample Type",data));
        dto.setStrain(Standardizer.getValue("Strain",data));
        dto.setModelTag(Standardizer.getValue("Model Tag",data));

        dto.setSourceURL(Standardizer.getValue("Source url",data));

        dto.setMarkerPlatform(Harmonizer.getMarkerPlatform(data,ds));
        dto.setMarkerStr(Harmonizer.getMarkerStr(data,ds));
        dto.setQaPassage(Harmonizer.getQAPassage(data,ds));

        dto.setQualityAssurance(dataImportService.getQualityAssurance(data,ds));
        dto.setImplantationtypeStr(Harmonizer.getImplantationType(data,ds));
        dto.setImplantationSiteStr(Harmonizer.getEngraftmentSite(data,ds));

        dto.setFingerprinting(Harmonizer.getFingerprinting(data, ds));
        dto.setSpecimens(Harmonizer.getSpecimens(data,ds));
        dto.setPatientTreatments(Harmonizer.getPatientTreaments(data, ds));
        dto.setModelDosingStudies(Harmonizer.getModelDosingStudies(data, ds));
        dto.setSamplesArr(Harmonizer.getSamplesArr(data, ds));
        dto.setValidationsArr(Harmonizer.getValidationsArr(data, ds));

        dto.setSkipModel(false);

    }

    void step09LoadPatientData(){

        if(dto.isSkipModel()) return ;

        Patient patient = dataImportService.getPatientWithSnapshots(dto.getPatientId(), providerDS);

        if(patient == null){
            patient = dataImportService.createPatient(dto.getPatientId(), providerDS, dto.getGender(), "", Standardizer.getEthnicity(dto.getEthnicity()));
        }
        dto.setPatient(patient);


        PatientSnapshot pSnap = dataImportService.getPatientSnapshot(patient, dto.getAge(), "", "", "");
        dto.setPatientSnapshot(pSnap);

        Sample patientSample = dataImportService.getSample(dto.getSampleID(), providerDS.getAbbreviation(), dto.getTumorType(), dto.getDiagnosis(), dto.getPrimarySite(),
                dto.getSampleSite(), dto.getExtractionMethod(), false, dto.getStage(), "", dto.getGrade(), "");

        dto.setPatientSample(patientSample);

        dto.getPatientSnapshot().addSample(dto.getPatientSample());

    }

    abstract void step10LoadExternalURLs();
    abstract void step11LoadBreastMarkers();

    void step12CreateModels() throws Exception {
        if(dto.isSkipModel()) return ;
        ModelCreation modelCreation = dataImportService.createModelCreation(dto.getModelID(), providerDS.getAbbreviation(), dto.getPatientSample(), dto.getQualityAssurance(), dto.getExternalUrls());
        dto.setModelCreation(modelCreation);
    }

    abstract void step13LoadSpecimens() throws Exception;
    abstract void step14LoadPatientTreatments() throws Exception;
    abstract void step15LoadImmunoHistoChemistry();
    abstract void step16LoadVariationData();
    abstract void step17LoadModelDosingStudies() throws Exception;
    abstract void step18SetAdditionalGroups();

    /*****************************************************************************************************
     *     SKELETON OF LOADING ALGORITHM STANDARDIZED IN A TEMPLATE METHOD        *
     *******************************************************************************/

    public final void globalLoadingOrder() throws Exception {

        step00StartReportManager();
        step02GetMetaDataJSON();
        if (skipThis) return;
        step03CreateProviderGroup();
        step04CreateNSGammaHostStrain();
        step05CreateNSHostStrain();
        step06SetProjectGroup();
        step07GetPDXModels();

        for (int i = 0; i < jsonArray.length(); i++) {
            dto = new LoaderDTO();
            this.jsonData = jsonArray.getJSONObject(i);

            step08GetMetaData();
            step09LoadPatientData();
            step10LoadExternalURLs();
            step11LoadBreastMarkers();
            step12CreateModels();
            step13LoadSpecimens();
            step14LoadPatientTreatments();
            step17LoadModelDosingStudies();
            step16LoadVariationData();
        }

        step15LoadImmunoHistoChemistry();
    }

    public void loadExternalURLs(String dataSourceContact, String dataSourceURL){
        List<ExternalUrl> externalUrls = new ArrayList<>();
        externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.CONTACT, dataSourceContact));

        if (!dataSourceURL.equals(Standardizer.NOT_SPECIFIED)){
            externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.SOURCE, dataSourceURL));
        }
        dto.setExternalUrls(externalUrls);
    }

    public void loadModelDosingStudies(){
        TreatmentSummary ts;
        try {
            //log.info("Model dosing number for model "+dto.getModelID() + " is "+dto.getModelDosingStudies().length());
            if (dto.getModelDosingStudies().length() > 0) {

                ts = new TreatmentSummary();
                ts.setUrl(dosingStudyURL);

                for (int t = 0; t < dto.getModelDosingStudies().length(); t++) {

                    JSONObject treatmentObject = dto.getModelDosingStudies().getJSONObject(t);
                    //log.info("Treatment: "+treatmentObject.getString("Drug"));

                    TreatmentProtocol treatmentProtocol = dataImportService.getTreatmentProtocol(treatmentObject.getString("Drug"),
                            treatmentObject.getString("Dose"),
                            treatmentObject.getString("Response"), "");

                    if (treatmentProtocol != null) {
                        ts.addTreatmentProtocol(treatmentProtocol);
                    }
                }
                ts.setModelCreation(dto.getModelCreation());
                dto.getModelCreation().setTreatmentSummary(ts);
            }
            dataImportService.saveModelCreation(dto.getModelCreation());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loadSpecimens(String ds)  throws Exception{
        if (ds.equals("mdAnderson") || ds.equals("wustl")) {
            HostStrain bs = dataImportService.getHostStrain("", dto.getStrain(), "", "");
            boolean human = false;
            String markerPlatform = Standardizer.NOT_SPECIFIED;

            try {
                markerPlatform = dto.getMarkerPlatform();
                if ("CMS50".equals(markerPlatform) || "CMS400".equals(dto.getMarkerPlatform())) {
                    human = true;
                }
            } catch (Exception e) { /* this is for the FANG data and we don't really care about markers at this point anyway */ }

            if (human) {
                dto.getPatientSnapshot().addSample(dto.getPatientSample());
            } else {
                String passage = "0";
                try {
                    passage = dto.getQaPassage().replaceAll("P", "");
                } catch (Exception e) {
                    // default is 0
                }
                Specimen specimen = dataImportService.getSpecimen(dto.getModelCreation(), dto.getModelCreation().getSourcePdxId(), providerDS.getAbbreviation(), passage);
                specimen.setHostStrain(bs);

                if (ds.equals("wustl")){
                    Sample mouseSample = new Sample();
                    specimen.setSample(mouseSample);
                    dto.getModelCreation().addRelatedSample(mouseSample);

                    if (dto.getImplantationSiteStr().contains(";")) {
                        String[] parts = dto.getImplantationSiteStr().split(";");
                        dto.setImplantationSiteStr(parts[1].trim());
                        dto.setImplantationtypeStr(parts[0].trim());
                    }
                }

                EngraftmentSite is = dataImportService.getImplantationSite(dto.getImplantationSiteStr());
                specimen.setEngraftmentSite(is);
                EngraftmentType it = dataImportService.getImplantationType(dto.getImplantationtypeStr());
                specimen.setEngraftmentType(it);

                if (ds.equals("wustl")){
                    dto.getModelCreation().addSpecimen(specimen);
                }

                if (ds.equals("mdAnderson")) {
                    specimen.setSample(dto.getPatientSample());
                    dataImportService.saveSpecimen(specimen);
                }

            }
            dataImportService.saveSample(dto.getPatientSample());  // TODO: This was not be implemented for wustl
            dataImportService.saveModelCreation(dto.getModelCreation());
            dataImportService.savePatientSnapshot(dto.getPatientSnapshot());
        }

    }

    static String validateAccessibility(String accessibility) {
        return accessibility == null ? "" : accessibility;
    }

    static String validateModality(String accessModality) {
        if (accessModality == null) accessModality = "";
        accessModality = accessModality.trim();
        ArrayList<String> validOptions = new ArrayList<>(
            Arrays.asList(
                "transnational access",
                "TA",
                "collaboration only"
            ));
        if (validOptions.contains(accessModality)) {
            return accessModality;
        } else {
            log.error(String.format("Invalid access modality passed (%s)", accessModality));
            return accessModality;
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
