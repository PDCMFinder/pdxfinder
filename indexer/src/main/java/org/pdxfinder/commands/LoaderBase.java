package org.pdxfinder.commands;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ds.Harmonizer;
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.LoaderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class LoaderBase {

    private final static Logger log = LoggerFactory.getLogger(LoaderBase.class);
    String jsonFile;
    String dataSource;

    File[] listOfFiles;

    String metaDataJSON = "NOT FOUND";
    JSONArray jsonArray;

    String filesDirectory;
    String dataSourceAbbreviation;
    String dataSourceContact;
    String dosingStudyURL;

    LoaderDTO dto = new LoaderDTO();

    @Autowired
    private UtilityService utilityService;

    @Autowired
    private DataImportService dataImportService;


    /*****************************************************************************************************
     *     SINGLE FILE DATA TEMPLATE METHOD         *
     **********************************************/

    public final void loaderTemplate() throws Exception  {

        initMethod();

        step00GetMetaDataFolder();

        step01GetMetaDataJSON();

        step02CreateProviderGroup();

        step03CreateNSGammaHostStrain();

        step04CreateNSHostStrain();

        step05CreateProjectGroup();

        step06GetPDXModels();


        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonData = jsonArray.getJSONObject(i);

            step07GetMetaData(jsonData,dataSourceAbbreviation);

            step08LoadPatientData(dataSourceContact);

            dto.getPatientSnapshot().addSample(dto.getPatientSample());

            step09LoadExternalURLs();

            step10CreateModels();

            step11LoadSpecimens();

            step12CreateCurrentTreatment();
        }

        step13LoadImmunoHistoChemistry();
    }







    abstract void initMethod();


    void step00GetMetaDataFolder(){

        listOfFiles = new File[0];
        File folder = new File(filesDirectory);

        if(folder.exists()){
            listOfFiles = folder.listFiles();
            if(listOfFiles.length == 0){
                log.info("No file found for "+dataSource+", skipping");
            }
        }
        else{ log.info("Directory does not exist, skipping."); } // "+filesDirectory+"

    }


    void step01GetMetaDataJSON(){

        File file = new File(jsonFile);

        if (file.exists()) {

            log.info("Loading from file " + jsonFile);
            this.metaDataJSON = utilityService.parseFile(jsonFile);
        } else {
            log.info("No file found for " + dataSource + ", skipping");
        }

    }


    abstract void step02CreateProviderGroup();

    abstract void step03CreateNSGammaHostStrain();

    abstract void step04CreateNSHostStrain();

    abstract void step05CreateProjectGroup();

    abstract void step06GetPDXModels();


    void step07GetMetaData(JSONObject data, String ds) throws Exception {

        dto.setModelID(data.getString("Model ID"));
        dto.setSampleID(Harmonizer.getSampleID(data,ds));
        dto.setDiagnosis(Harmonizer.getDiagnosis(data,ds));
        dto.setPatientId(Standardizer.getValue("Patient ID",data));
        dto.setEthnicity(Harmonizer.getEthnicity(data,ds));
        dto.setStage(Standardizer.getValue("Stage",data));
        dto.setGrade(Standardizer.getValue("Grades",data));
        dto.setClassification(Harmonizer.getClassification(data,ds));
        dto.setAge(Standardizer.getAge(data.getString("Age")));
        dto.setGender(Standardizer.getGender(data.getString("Gender")));
        dto.setTumorType(Standardizer.getTumorType(data.getString("Tumor Type")));
        dto.setSampleSite(Standardizer.getValue("Sample Site",data));
        dto.setPrimarySite(Standardizer.getValue("Primary Site",data));
        dto.setExtractionMethod(Standardizer.getValue("Sample Type",data));
        dto.setStrain(Standardizer.getValue("Strain",data));
        dto.setMarkerPlatform(Harmonizer.getMarkerPlatform(data,ds));
        dto.setMarkerStr(Harmonizer.getMarkerStr(data,ds));
        dto.setQaPassage(Harmonizer.getQAPassage(data,ds));

        dto.setQualityAssurance(dataImportService.getQualityAssurance(data,ds));
        dto.setImplantationtypeStr(Harmonizer.getImplantationType(data,ds));
        dto.setImplantationSiteStr(Harmonizer.getEngraftmentSite(data,ds));

        dto.setFingerprinting(Harmonizer.getFingerprinting(data, ds));
        dto.setSpecimens(Harmonizer.getSpecimens(data,ds));
        dto.setTreatments(Harmonizer.getTreament(data, ds));

    }


    void step08LoadPatientData(String dataSourceContact){

        Group dataSource = dto.getProviderGroup();
        Patient patient = dataImportService.getPatientWithSnapshots(dto.getPatientId(), dataSource);

        if(patient == null){
            patient = dataImportService.createPatient(dto.getPatientId(), dataSource, dto.getGender(), "", Standardizer.getEthnicity(dto.getEthnicity()));
        }
        dto.setPatient(patient);


        PatientSnapshot pSnap = dataImportService.getPatientSnapshot(patient, dto.getAge(), "", "", "");
        dto.setPatientSnapshot(pSnap);

        Sample patientSample = dataImportService.getSample(dto.getSampleID(), dataSource.getAbbreviation(), dto.getTumorType(), dto.getDiagnosis(), dto.getPrimarySite(),
                dto.getSampleSite(), dto.getExtractionMethod(), false, dto.getStage(), "", dto.getGrade(), "");

        dto.setPatientSample(patientSample);

    }


    abstract void step09LoadExternalURLs();


    void step10CreateModels(){

        ModelCreation modelCreation = dataImportService.createModelCreation(dto.getModelID(), dto.getProviderGroup().getAbbreviation(), dto.getPatientSample(), dto.getQualityAssurance(), dto.getExternalUrls());
        dto.setModelCreation(modelCreation);

    }

    abstract void step11LoadSpecimens();

    abstract void step12CreateCurrentTreatment();

    abstract void step13LoadImmunoHistoChemistry();



















    public void loadProviderGroup(String dsName, String dsAbbrev, String dsDesc,
                                                 String providerType, String access,String modalities, String dsContact,String url){

        Group providerDS = dataImportService.getProviderGroup(dsName, dsAbbrev, dsDesc, providerType, access, modalities, dsContact, url);
        dto.setProviderGroup(providerDS);
    }


    public void loadNSGammaHostStrain(String NSG_BS_SYMBOL,String  NSG_BS_URL,String NSG_BS_NAME, String NSG_BS_DESC) {

        try {
            HostStrain nsgBS = dataImportService.getHostStrain(NSG_BS_NAME, NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_DESC);
            dto.setNodScidGamma(nsgBS);
        } catch (Exception e) {}
    }


    public void loadNSHostStrain(String NS_BS_SYMBOL,String  NS_BS_URL,String NS_BS_NAME) {

        try {
            HostStrain nsBS = dataImportService.getHostStrain(NS_BS_NAME, NS_BS_SYMBOL, NS_BS_URL, NS_BS_NAME);
            dto.setNodScid(nsBS);
        } catch (Exception e) {}
    }


    public void loadProjectGroup(String projectName) {

        Group projectGroup = dataImportService.getProjectGroup(projectName);
        dto.setProjectGroup(projectGroup);
    }


    public void loadPDXModels(String jsonString, String key){

        try {
            JSONObject job = new JSONObject(jsonString);
            jsonArray = job.getJSONArray(key);
        } catch (Exception e) {
            log.error("Error getting "+key+" PDX models", e);
        }
    }


    public void loadExternalURLs(String dataSourceContact, String dataSourceURL){

        List<ExternalUrl> externalUrls = new ArrayList<>();
        externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.CONTACT, dataSourceContact));

        if (!dataSourceURL.equals(Standardizer.NOT_SPECIFIED)){
            externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.SOURCE, dataSourceURL));
        }
        dto.setExternalUrls(externalUrls);

    }



}
