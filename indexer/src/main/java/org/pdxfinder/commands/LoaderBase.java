package org.pdxfinder.commands;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ds.Hamonizer;
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
    String metaDataJSON = "NOT FOUND";
    JSONArray jsonArray;

    String dataSourceAbbreviation;
    String dataSourceContact;
    String dosingStudyURL;

    LoaderDTO dto = new LoaderDTO();

    @Autowired
    private UtilityService utilityService;

    @Autowired
    private DataImportService dataImportService;


    /* The Template method */
    public final void loadData() throws Exception  {

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

            step09CreateModels();

            step10CreateEngraftmentsAndSpecimen();

            step11CreateCurrentTreatment();
        }

        step12LoadImmunoHistoChemistry();
    }



    abstract void initMethod();

    abstract void step00GetMetaDataFolder();


    void step01GetMetaDataJSON(){

        File file = new File(jsonFile);

        if (file.exists()) {
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

        String modelID = data.getString("Model ID");
        String sampleID = Hamonizer.getSampleID(data,ds);
        String diagnosis = Hamonizer.getDiagnosis(data,ds);
        String patientId = Standardizer.getValue("Patient ID",data);

        String ethnicity = Hamonizer.getEthnicity(data,ds);

        String stage = Standardizer.getValue("Stage",data);
        String grade = Standardizer.getValue("Grades",data);

        String classification = Hamonizer.getClassification(data,ds);

        String age = Standardizer.getAge(data.getString("Age"));
        String gender = Standardizer.getGender(data.getString("Gender"));
        String tumorType = Standardizer.getTumorType(data.getString("Tumor Type"));
        String sampleSite = Standardizer.getValue("Sample Site",data);
        String primarySite = Standardizer.getValue("Primary Site",data);
        String extractionMethod = Standardizer.getValue("Sample Type",data);
        String strain = Standardizer.getValue("Strain",data);
        String fingerprinting = Hamonizer.getFingerprinting(data, ds);




        String implantationTypeStr = Hamonizer.getImplantationType(data,ds);
        String implantationSiteStr = Hamonizer.getEngraftmentSite(data,ds);
        QualityAssurance qa = Hamonizer.getQualityAssurance(data,ds);

        String markerPlatform = Hamonizer.getMarkerPlatform(data,ds);
        String markerStr = Hamonizer.getMarkerStr(data,ds);
        String passage = Hamonizer.getQAPassage(data,ds);

        JSONArray specimens = Hamonizer.getSpecimens(data,ds);
        JSONArray treatments = Hamonizer.getTreament(data, ds);

        dto.setModelID(modelID);
        dto.setSampleID(sampleID);
        dto.setDiagnosis(diagnosis);
        dto.setPatientId(patientId);
        dto.setEthnicity(ethnicity);
        dto.setStage(stage);
        dto.setGrade(grade);
        dto.setClassification(classification);
        dto.setAge(age);
        dto.setGender(gender);
        dto.setTumorType(tumorType);
        dto.setSampleSite(sampleSite);
        dto.setPrimarySite(primarySite);
        dto.setExtractionMethod(extractionMethod);
        dto.setStrain(strain);
        dto.setMarkerPlatform(markerPlatform);
        dto.setMarkerStr(markerStr);
        dto.setQaPassage(passage);

        dto.setQualityAssurance(qa);
        dto.setImplantationtypeStr(implantationTypeStr);
        dto.setImplantationSiteStr(implantationSiteStr);

        dto.setFingerprinting(fingerprinting);
        dto.setSpecimens(specimens);
        dto.setTreatments(treatments);

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

        List<ExternalUrl> externalUrls = new ArrayList<>();
        externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.CONTACT, dataSourceContact));
        dto.setExternalUrls(externalUrls);

    }


    void step09CreateModels(){

        ModelCreation modelCreation = dataImportService.createModelCreation(dto.getModelID(), dto.getProviderGroup().getAbbreviation(), dto.getPatientSample(), dto.getQualityAssurance(), dto.getExternalUrls());
        dto.setModelCreation(modelCreation);

    }

    abstract void step10CreateEngraftmentsAndSpecimen();

    abstract void step11CreateCurrentTreatment();

    abstract void step12LoadImmunoHistoChemistry();



















    public void loadProviderGroup(String dsName, String dsAbbrev, String dsDesc,
                                                 String providerType, String access,String modalities, String dsContact,String url){

        Group providerDS = dataImportService.getProviderGroup(dsName, dsAbbrev, dsDesc, providerType, access, modalities, dsContact, url);
        dto.setProviderGroup(providerDS);
    }


    public void loadNSGammaHostStrain(String NSG_BS_SYMBOL,String  NSG_BS_URL,String NSG_BS_NAME) {

        try {
            HostStrain nsgBS = dataImportService.getHostStrain(NSG_BS_NAME, NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_NAME);
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



}
