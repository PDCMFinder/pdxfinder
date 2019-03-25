package org.pdxfinder.commands.dataloaders;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ds.Harmonizer;
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.LoaderDTO;
import org.pdxfinder.services.dto.NodeSuggestionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.File;
import java.util.*;

public abstract class LoaderBase extends LoaderProperties implements ApplicationContextAware{

    private final static Logger log = LoggerFactory.getLogger(LoaderBase.class);
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
     * Initializes the report manager for the loader
     */
    void step00StartReportManager(){

        reportManager = (ReportManager) context.getBean("ReportManager");
    }


    /**
     *
     * This has Common Implementations: So it is fully implemented in the base class
     * Concrete classes automatically inherits the default implementation or override it
     * Concrete classes can also override and "call back to" this base class method at once using super.step01GetMetaDataFolder()
     */
    void step01GetMetaDataFolder(){


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
     *
     * This has Common Implementations: So it is fully implemented in the base class
     * Concrete classes automatically inherits the default implementation or override it
     * Concrete classes can also override and "call back to" this base class method at once using super.step02GetMetaDataJSON()
     */
    void step02GetMetaDataJSON(){

        File file = new File(jsonFile);

        if (file.exists()) {

            log.info("Loading from file " + jsonFile);
            this.metaDataJSON = utilityService.parseFile(jsonFile);
        } else {
            log.info("No file found for " + dataSource + ", skipping");
        }
    }


    /**
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder method as required
     */
    abstract void step03CreateProviderGroup();


    /**
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder method as required
     */
    abstract void step04CreateNSGammaHostStrain();


    /**
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder method as required
     */
    abstract void step05CreateNSHostStrain();


    /**
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder method as required
     */
    abstract void step06CreateProjectGroup();


    /**
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder method as required
     */
    abstract void step07GetPDXModels();


    /**
     *
     * Has Common Implementations: So they are fully implemented in the base class
     * Concrete classes automatically inherits these implementations or can override implemented methods
     * Concrete classes can also override and as well "call back to" these base class methods using super.step08GetMetaData() at once
     */
    void step08GetMetaData() throws Exception {

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

    }



    /**
     *
     * Has Common Implementations: So they are fully implemented in the base class
     * Concrete classes automatically inherits these implementations or can override implemented methods
     * Concrete classes can also override and as well "call back to" this base class method using super.step09LoadPatientData() at once
     */
    void step09LoadPatientData(){

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
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step10LoadExternalURLs();



    /**
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step11LoadBreastMarkers();


    /**
     *
     * Has Common Implementations: So they are fully implemented in the base class
     * Concrete classes automatically inherits these implementations or can override implemented methods
     * Concrete classes can also override and as well "call back to" these base class methods using super.step12CreateModels() at once
     */
    void step12CreateModels() throws Exception {

        ModelCreation modelCreation = dataImportService.createModelCreation(dto.getModelID(), dto.getProviderGroup().getAbbreviation(), dto.getPatientSample(), dto.getQualityAssurance(), dto.getExternalUrls());
        dto.setModelCreation(modelCreation);

    }

    /**
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step13LoadSpecimens() throws Exception;


    /**
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step14LoadPatientTreatments() throws Exception;

    /**
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step15LoadImmunoHistoChemistry();

    /**
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step16LoadVariationData();

    /**
     *
     * This requires peculiar implementations: So it is implemented as "placeholder" in the base class
     * Concrete / Derived classes MUST override these placeholder methods as required
     */
    abstract void step17LoadModelDosingStudies() throws Exception;



    /*****************************************************************************************************
     *     SKELETON OF LOADING ALGORITHM STANDARDIZED IN A TEMPLATE METHOD        *
     *******************************************************************************/

    public final void globalLoadingOrder() throws Exception {

        step00StartReportManager();

        step02GetMetaDataJSON();

        step03CreateProviderGroup();

        step04CreateNSGammaHostStrain();

        step05CreateNSHostStrain();

        step06CreateProjectGroup();

        step07GetPDXModels();


        for (int i = 0; i < jsonArray.length(); i++) {

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















    public void loadProviderGroup(String dsName, String dsAbbrev, String dsDesc,
                                                 String providerType, String dsContact,String url){

        Group providerDS = dataImportService.getProviderGroup(dsName, dsAbbrev, dsDesc, providerType, dsContact, url);
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








    public void loadOmicData(ModelCreation modelCreation, String dataType) {  // csv or json


        List<Map<String, String>> dataList;

        if (omicDataFilesType.equals("ONE_FILE_PER_MODEL")){

            String modelID = modelCreation.getSourcePdxId();
            dataList = utilityService.serializeDataToMaps(dataRootDirectory+dataSourceAbbreviation+"/mut/"+modelID+"."+omicFileExtension);

        }else {

            String variationURLStr = dataRootDirectory+dataSourceAbbreviation+"/mut/data."+omicFileExtension;

            Map<String, List<Map<String, String>> > fullData = utilityService.serializeMergedData(variationURLStr,omicModelID);

            dataList = fullData.get(modelCreation.getSourcePdxId());
        }

        String modelID = modelCreation.getSourcePdxId();
        Map<String, Platform> platformMap = new HashMap<>();
        Map<String, MolecularCharacterization> molcharMap = new HashMap<>();

        int totalData = 0;
        try {

            totalData = dataList.size();
        }catch (Exception e){

            log.info(" ********* Model ID : {} has no {} data, so skip ********* ", modelID, dataType );
            return;
        }

        log.info(totalData + " gene variants for model " + modelID);

        int count = 0;

        //PHASE 1: ASSEMBLE OBJECTS IN MEMORY, REDUCING DB INTERACTIONS AS MUCH AS POSSIBLE
        for (Map<String, String> data : dataList ) {


            //STEP 1: GET THE PLATFORM AND CACHE IT
            String technology = data.get(omicPlatform);

            //Skip loading fish!
            if(technology.equals("Other:_FISH")){
                count++;
                continue;
            }

            Platform platform;
            if(platformMap.containsKey(technology)){

                platform = platformMap.get(technology);
            }
            else{

                String platformURLKey = technology.replace("\\s","_");

                platform = dataImportService.getPlatform(technology, dto.getProviderGroup(), platformURL.get(platformURLKey));
                platformMap.put(technology, platform);
            }


            // STEP 2: GET THE CACHED MOLCHAR OBJECT OR CREATE ONE IF IT DOESN'T EXIST IN THE MAP, KEY is sampleid__passage__technology
            MolecularCharacterization molecularCharacterization;
            String passage = (data.get(omicPassage) == null) ? "" : data.get(omicPassage);

            String molcharKey = data.get(omicSampleID) + "__" + passage + "__" + data.get(omicPlatform)+ "__" + data.get(omicSampleOrigin);

            if(molcharMap.containsKey(molcharKey)){

                molecularCharacterization = molcharMap.get(molcharKey);
            }
            else{

                molecularCharacterization = new MolecularCharacterization();
                molecularCharacterization.setType(dataType);
                molecularCharacterization.setPlatform(platform);
                molcharMap.put(molcharKey, molecularCharacterization);
            }


            //step 3: get the marker suggestion from the service
            NodeSuggestionDTO nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSourceAbbreviation, modelCreation.getSourcePdxId(), data.get(omicHgncSymbol), dataType, technology);

            Marker marker;

            if(nsdto.getNode() == null){

                // Found an unrecognised marker symbol, abort, abort!!!!
                reportManager.addMessage(nsdto.getLogEntity());
                count++;
                continue;
            }
            else{

                // step 4: assemble the MarkerAssoc object and add it to molchar
                marker = (Marker) nsdto.getNode();

                //if we have any message regarding the suggested marker, ie: prev symbol, synonym, etc, add it to the report
                if(nsdto.getLogEntity() != null){
                    reportManager.addMessage(nsdto.getLogEntity());
                }

                MarkerAssociation ma = new MarkerAssociation();
                switch (dataType){

                    case "cna":
                        setCNAProperties(data, marker);

                    case "mutation":
                        ma = setVariationProperties(data, marker);
                }

                molecularCharacterization.addMarkerAssociation(ma);

            }

            count++;
            if (count % 100 == 0) {
                log.info("loaded {} {} ", count, dataType);
            }
        }
        log.info("loaded " + totalData + " markers for " + modelID);


        //PHASE 2: get objects from cache and persist them
        for(Map.Entry<String, MolecularCharacterization> mcEntry : molcharMap.entrySet()){

            String mcKey = mcEntry.getKey();
            MolecularCharacterization mc = mcEntry.getValue();

            String[] mcKeyArr = mcKey.split("__");
            String sampleId = mcKeyArr[0];
            String pass = getPassage(mcKeyArr[1]);
            String sampleOrigin = mcKeyArr[3];

            boolean foundSpecimen = false;

            if(sampleOrigin.toLowerCase().equals("patient tumor")){

                Sample patientSample = modelCreation.getSample();
                patientSample.addMolecularCharacterization(mc);
                continue;

            }

            if(modelCreation.getSpecimens() != null){

                for(Specimen specimen : modelCreation.getSpecimens()){

                    if(specimen.getPassage().equals(pass)){

                        if(specimen.getSample() != null && specimen.getSample().getSourceSampleId().equals(sampleId)){

                            Sample xenograftSample = specimen.getSample();
                            xenograftSample.addMolecularCharacterization(mc);

                            foundSpecimen = true;

                        }
                    }

                }

            }
            //this passage is either not present yet or the linked sample has a different ID, create a specimen with sample and link mc
            if(!foundSpecimen){
                log.info("Creating new specimen for "+mcKey);

                Sample xenograftSample = new Sample();
                xenograftSample.setSourceSampleId(sampleId);
                xenograftSample.addMolecularCharacterization(mc);

                Specimen specimen = new Specimen();
                specimen.setPassage(pass);
                specimen.setSample(xenograftSample);


                modelCreation.addRelatedSample(xenograftSample);
                modelCreation.addSpecimen(specimen);
            }
        }

        dataImportService.saveModelCreation(modelCreation);

    }



    private MarkerAssociation setVariationProperties(Map<String,String> data, Marker marker){

        MarkerAssociation ma = new MarkerAssociation();
        ma.setAminoAcidChange(data.get(omicAminoAcidChange));
        ma.setConsequence(data.get(omicConsequence));
        ma.setAlleleFrequency(data.get(omicAlleleFrequency));
        ma.setChromosome(data.get(omicChromosome));
        ma.setReadDepth(data.get(omicReadDepth));
        ma.setRefAllele(data.get(omicRefAllele));
        ma.setAltAllele(data.get(omicAltAllele));
        ma.setGenomeAssembly(data.get(omicGenomeAssembly));
        ma.setRsIdVariants(data.get(omicRsIdVariants));
        ma.setSeqStartPosition(data.get(omicSeqStartPosition));

        ma.setEnsemblTranscriptId(data.get(omicEnsemblTranscriptId));
        ma.setNucleotideChange(data.get(omicNucleotideChange));
        ma.setMarker(marker);

        return  ma;
    }



    private MarkerAssociation setCNAProperties(Map<String,String> data, Marker marker){

        MarkerAssociation ma = new MarkerAssociation();

        //setHostStrain Name
        ma.setChromosome(data.get(omicChromosome));
        ma.setSeqStartPosition(data.get(omicSeqStartPosition));
        ma.setSeqEndPosition(data.get(omicSeqEndPosition));
        ma.setCnaLog10RCNA(omicCnaLog10RCNA);
        ma.setCnaLog2RCNA(omicCnaLog2RCNA);
        ma.setCnaCopyNumberStatus(omicCnaCopyNumberStatus);
        ma.setCnaGisticValue(omicCnaGisticvalue);
        ma.setCnaPicnicValue(omicCnaPicnicValue);
        ma.setGenomeAssembly(data.get(omicGenomeAssembly));

        marker.setHgncSymbol(omicHgncSymbol);
        marker.setUcscGeneId(omicUcscGeneId);
        marker.setNcbiGeneId(omicNcbiGeneId);
        marker.setEnsemblGeneId(omicEnsemblGeneId);

        ma.setMarker(marker);
        return  ma;
    }




    public String getPassage(String passageString) {

        if(!passageString.isEmpty() && passageString.toUpperCase().contains("P")){

            passageString = passageString.toUpperCase().replace("P", "");
        }
        //does this string have digits only now?
        if(passageString.matches("\\d+")) return passageString;

        log.warn("Unable to determine passage from sample name " + passageString + ". Assuming 0");
        return "0";

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

            /*
            //Disabling loading markers for MDA

            if (ds.equals("mdAnderson")) {
                String markerStr = dto.getMarkerStr();
                String[] markers = markerStr.split(";");
                if (markerStr.trim().length() > 0 && markers.length>0) {
                    Platform pl = dataImportService.getPlatform(markerPlatform, dto.getProviderGroup());
                    MolecularCharacterization molC = new MolecularCharacterization();
                    molC.setType("mutation");
                    molC.setPlatform(pl);
                    List<MarkerAssociation> markerAssocs = new ArrayList<>();

                    for (int i = 0; i < markers.length; i++) {
                        String markerSymbol = markers[i];


                        NodeSuggestionDTO nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSourceAbbreviation, dto.getModelID(), markerSymbol, "mutation", markerPlatform);

                        Marker m;

                        if(nsdto.getNode() == null){

                            //uh oh, we found an unrecognised marker symbol, abort, abort!!!!
                            reportManager.addMessage(nsdto.getLogEntity());
                            continue;
                        }
                        else{

                            m = (Marker)nsdto.getNode();

                            if(nsdto.getLogEntity() != null){
                                reportManager.addMessage(nsdto.getLogEntity());
                            }



                            MarkerAssociation ma = new MarkerAssociation();
                            ma.setMarker(m);
                            markerAssocs.add(ma);


                        }


                    }
                    molC.setMarkerAssociations(markerAssocs);
                    Set<MolecularCharacterization> mcs = new HashSet<>();
                    mcs.add(molC);

                    //sample.setMolecularCharacterizations(mcs);
                }
            }

            */

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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
