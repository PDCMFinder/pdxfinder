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
    JSONObject jsonData;

    LoaderDTO dto = new LoaderDTO();

    @Autowired
    private UtilityService utilityService;
    @Autowired
    private DataImportService dataImportService;

    /**
     * initMethod
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder method
     */
    abstract void initMethod();


    /**
     * Step 00 GetMetaDataFolder
     *
     * This has Common Implementations: So it is fully implemented in the base class
     * Concrete classes automatically inherits the default implementation or override it
     * Concrete classes can also override and "call back to" this base class method at once using super.step00GetMetaDataFolder()
     */
    void step00GetMetaDataFolder(){

        listOfFiles = new File[0];
        File folder = new File(filesDirectory);

        if(folder.exists()){
            listOfFiles = folder.listFiles();
            if(listOfFiles.length == 0){
                log.info("No file found for "+dataSource+", skipping");
            }
        }
        else{ log.info("Directory does not exist, skipping."); }

    }


    /**
     * Step 01 GetMetaDataJSON
     *
     * This has Common Implementations: So it is fully implemented in the base class
     * Concrete classes automatically inherits the default implementation or override it
     * Concrete classes can also override and "call back to" this base class method at once using super.step01GetMetaDataJSON()
     */
    void step01GetMetaDataJSON(){

        File file = new File(jsonFile);

        if (file.exists()) {

            log.info("Loading from file " + jsonFile);
            this.metaDataJSON = utilityService.parseFile(jsonFile);
        } else {
            log.info("No file found for " + dataSource + ", skipping");
        }
    }


    /**
     * Step 02 CreateProviderGroup
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder method as required
     */
    abstract void step02CreateProviderGroup();


    /**
     * Step 03 CreateNSGammaHostStrain
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder method as required
     */
    abstract void step03CreateNSGammaHostStrain();


    /**
     * Step 04 CreateNSHostStrain
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder method as required
     */
    abstract void step04CreateNSHostStrain();


    /**
     * Step 05 CreateProjectGroup
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder method as required
     */
    abstract void step05CreateProjectGroup();


    /**
     * Step 06 GetPDXModels
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder method as required
     */
    abstract void step06GetPDXModels();


    /**
     * Step 07 GetMetaData
     *
     * Has Common Implementations: So they are fully implemented in the base class
     * Concrete classes automatically inherits these implementations or can override implemented methods
     * Concrete classes can also override and as well "call back to" these base class methods using super.step07GetMetaData() at once
     */
    void step07GetMetaData() throws Exception {

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
        dto.setTreatments(Harmonizer.getTreament(data, ds));
        dto.setSamplesArr(Harmonizer.getSamplesArr(data, ds));
        dto.setValidationsArr(Harmonizer.getValidationsArr(data, ds));

    }



    /**
     * Step 08 LoadPatientData
     *
     * Has Common Implementations: So they are fully implemented in the base class
     * Concrete classes automatically inherits these implementations or can override implemented methods
     * Concrete classes can also override and as well "call back to" this base class method using super.step08LoadPatientData() at once
     */
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

        dto.getPatientSnapshot().addSample(dto.getPatientSample());

    }


    /**
     * Step 06 LoadExternalURLs
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step09LoadExternalURLs();



    /**
     * Step 10 LoadBreastMarkers
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step10LoadBreastMarkers();


    /**
     * Step 11 CreateModels
     *
     * Has Common Implementations: So they are fully implemented in the base class
     * Concrete classes automatically inherits these implementations or can override implemented methods
     * Concrete classes can also override and as well "call back to" these base class methods using super.step11CreateModels() at once
     */
    void step11CreateModels() throws Exception {

        ModelCreation modelCreation = dataImportService.createModelCreation(dto.getModelID(), dto.getProviderGroup().getAbbreviation(), dto.getPatientSample(), dto.getQualityAssurance(), dto.getExternalUrls());
        dto.setModelCreation(modelCreation);

    }

    /**
     * Step 12 LoadSpecimens
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step12LoadSpecimens() throws Exception;


    /**
     * Step 13 LoadCurrentTreatment
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step13LoadCurrentTreatment() throws Exception;

    /**
     * Step 14 LoadImmunoHistoChemistry
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step14LoadImmunoHistoChemistry();

    /**
     * Step 15 LoadVariationData
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step15LoadVariationData();



    /*****************************************************************************************************
     *     SKELETON OF LOADING ALGORITHM STANDARDIZED IN A TEMPLATE METHOD        *
     *******************************************************************************/

    public final void loaderTemplate() throws Exception {

        step01GetMetaDataJSON();

        step02CreateProviderGroup();

        step03CreateNSGammaHostStrain();

        step04CreateNSHostStrain();

        step05CreateProjectGroup();

        step06GetPDXModels();


        for (int i = 0; i < jsonArray.length(); i++) {

            this.jsonData = jsonArray.getJSONObject(i);

            step07GetMetaData();

            step08LoadPatientData();

            step09LoadExternalURLs();

            step10LoadBreastMarkers();

            step11CreateModels();

            step12LoadSpecimens();

            step13LoadCurrentTreatment();

        }
        step14LoadImmunoHistoChemistry();

        step15LoadVariationData();
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



    public void loadCurrentTreatment(){

        TreatmentSummary ts;
        try {

            if (dto.getTreatments().length() > 0) {

                ts = new TreatmentSummary();
                ts.setUrl(dosingStudyURL);

                for (int t = 0; t < dto.getTreatments().length(); t++) {

                    JSONObject treatmentObject = dto.getTreatments().getJSONObject(t);

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

        } catch (Exception e) { }

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

            dataImportService.saveSample(dto.getPatientSample());  // TODO: This was not be implemented for wustl
            dataImportService.saveModelCreation(dto.getModelCreation());
            dataImportService.savePatientSnapshot(dto.getPatientSnapshot());
        }

    }



}
