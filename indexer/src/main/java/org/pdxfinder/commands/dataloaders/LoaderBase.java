package org.pdxfinder.commands.dataloaders;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class LoaderBase {

    private final static Logger log = LoggerFactory.getLogger(LoaderBase.class);
    String jsonFile;
    String dataSource;
    String rootDataDirectory;

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
        dto.setTreatments(Harmonizer.getTreament(data, ds));
        dto.setSamplesArr(Harmonizer.getSamplesArr(data, ds));
        dto.setValidationsArr(Harmonizer.getValidationsArr(data, ds));

    }


    void step08LoadPatientData(){

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


    void step10CreateModels() throws Exception {

        ModelCreation modelCreation = dataImportService.createModelCreation(dto.getModelID(), dto.getProviderGroup().getAbbreviation(), dto.getPatientSample(), dto.getQualityAssurance(), dto.getExternalUrls());
        dto.setModelCreation(modelCreation);

    }

    abstract void step11LoadSpecimens() throws Exception;

    abstract void step12CreateCurrentTreatment() throws Exception;

    abstract void step13LoadImmunoHistoChemistry();

    abstract void step14VariationData();


    /*****************************************************************************************************
     *     SINGLE FILE DATA TEMPLATE METHOD         *
     **********************************************/

    public final void loaderTemplate() throws Exception {

        step01GetMetaDataJSON();

        step02CreateProviderGroup();

        step03CreateNSGammaHostStrain();

        step04CreateNSHostStrain();

        step05CreateProjectGroup();

        step06GetPDXModels();


        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonData = jsonArray.getJSONObject(i);

            step07GetMetaData(jsonData, dataSourceAbbreviation);

            step08LoadPatientData();

            dto.getPatientSnapshot().addSample(dto.getPatientSample());

            step09LoadExternalURLs();

            step10CreateModels();

            step11LoadSpecimens();

            step12CreateCurrentTreatment();

        }
        step13LoadImmunoHistoChemistry();

        step14VariationData();
    }

















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


            if (ds.equals("mdAnderson")) {
                String markerStr = dto.getMarkerStr();
                String[] markers = markerStr.split(";");
                if (markerStr.trim().length() > 0) {
                    Platform pl = dataImportService.getPlatform(markerPlatform, dto.getProviderGroup());
                    MolecularCharacterization molC = new MolecularCharacterization();
                    molC.setType("mutation");
                    molC.setPlatform(pl);
                    List<MarkerAssociation> markerAssocs = new ArrayList<>();

                    for (int i = 0; i < markers.length; i++) {
                        Marker m = dataImportService.getMarker(markers[i], markers[i]);
                        MarkerAssociation ma = new MarkerAssociation();
                        ma.setMarker(m);
                        markerAssocs.add(ma);
                    }
                    molC.setMarkerAssociations(markerAssocs);
                    Set<MolecularCharacterization> mcs = new HashSet<>();
                    mcs.add(molC);

                    //sample.setMolecularCharacterizations(mcs);
                }
            }


            if (human) {
                dto.getPatientSnapshot().addSample(dto.getPatientSample());

            } else {

                String passage = "0";
                try {
                    passage = dto.getQaPassage().replaceAll("P", "");
                } catch (Exception e) {
                    // default is 0
                }
                Specimen specimen = dataImportService.getSpecimen(dto.getModelCreation(), dto.getModelCreation().getSourcePdxId(), dto.getProviderGroup().getAbbreviation(), passage);
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

            dataImportService.saveSample(dto.getPatientSample());  // TODO: This was not be implemented for wustl, find out why
            dataImportService.saveModelCreation(dto.getModelCreation());
            dataImportService.savePatientSnapshot(dto.getPatientSnapshot());
        }

    }



}
