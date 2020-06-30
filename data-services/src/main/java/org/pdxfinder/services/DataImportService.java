package org.pdxfinder.services;

import org.apache.commons.lang3.StringUtils;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.queryresults.MutatedMarkerData;
import org.pdxfinder.graph.queryresults.TreatmentMappingData;
import org.pdxfinder.graph.repositories.*;
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.NodeSuggestionDTO;
import org.pdxfinder.services.reporting.LogEntity;
import org.pdxfinder.services.reporting.MarkerLogEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pdxfinder.services.reporting.MarkerLogEntity.logNoSingleValidSymbol;
import static org.pdxfinder.services.reporting.MarkerLogEntity.logUpdateFromPreviousSymbol;

@Component
public class DataImportService {

    private TumorTypeRepository tumorTypeRepository;
    private HostStrainRepository hostStrainRepository;
    private EngraftmentTypeRepository engraftmentTypeRepository;
    private EngraftmentSiteRepository engraftmentSiteRepository;
    private EngraftmentMaterialRepository engraftmentMaterialRepository;
    private GroupRepository groupRepository;
    private PatientRepository patientRepository;
    private ModelCreationRepository modelCreationRepository;
    private TissueRepository tissueRepository;
    private PatientSnapshotRepository patientSnapshotRepository;
    private SampleRepository sampleRepository;
    private MarkerRepository markerRepository;
    private MarkerAssociationRepository markerAssociationRepository;
    private MolecularCharacterizationRepository molecularCharacterizationRepository;

    private QualityAssuranceRepository qualityAssuranceRepository;
    private OntologyTermRepository ontologyTermRepository;
    private SpecimenRepository specimenRepository;
    private PlatformRepository platformRepository;
    private PlatformAssociationRepository platformAssociationRepository;
    private DataProjectionRepository dataProjectionRepository;
    private TreatmentSummaryRepository treatmentSummaryRepository;
    private TreatmentProtocolRepository treatmentProtocolRepository;
    private CurrentTreatmentRepository currentTreatmentRepository;
    private ExternalUrlRepository externalUrlRepository;
    private DrugRepository drugRepository;
    private TreatmentRepository treatmentRepository;

    private final static Logger log = LoggerFactory.getLogger(DataImportService.class);

    private HashMap<String, Marker> markersBySymbol = null;
    private HashMap<String, List<Marker>> markersByPrevSymbol = null;
    private HashMap<String, List<Marker>> markersBySynonym = null;

    private boolean markersInitialized = false;

    public DataImportService(TumorTypeRepository tumorTypeRepository,
                             HostStrainRepository hostStrainRepository,
                             EngraftmentTypeRepository engraftmentTypeRepository,
                             EngraftmentSiteRepository engraftmentSiteRepository,
                             EngraftmentMaterialRepository engraftmentMaterialRepository,
                             GroupRepository groupRepository,
                             PatientRepository patientRepository,
                             ModelCreationRepository modelCreationRepository,
                             TissueRepository tissueRepository,
                             PatientSnapshotRepository patientSnapshotRepository,
                             SampleRepository sampleRepository,
                             MarkerRepository markerRepository,
                             MarkerAssociationRepository markerAssociationRepository,
                             MolecularCharacterizationRepository molecularCharacterizationRepository,
                             QualityAssuranceRepository qualityAssuranceRepository,
                             OntologyTermRepository ontologyTermRepository,
                             SpecimenRepository specimenRepository,
                             PlatformRepository platformRepository,
                             PlatformAssociationRepository platformAssociationRepository,
                             DataProjectionRepository dataProjectionRepository,
                             TreatmentSummaryRepository treatmentSummaryRepository,
                             TreatmentProtocolRepository treatmentProtocolRepository,
                             CurrentTreatmentRepository currentTreatmentRepository,
                             ExternalUrlRepository externalUrlRepository,
                             DrugRepository drugRepository,
                             TreatmentRepository treatmentRepository) {

        Assert.notNull(tumorTypeRepository, "tumorTypeRepository cannot be null");
        Assert.notNull(hostStrainRepository, "hostStrainRepository cannot be null");
        Assert.notNull(engraftmentTypeRepository, "implantationTypeRepository cannot be null");
        Assert.notNull(engraftmentSiteRepository, "implantationSiteRepository cannot be null");
        Assert.notNull(engraftmentMaterialRepository, "engraftmentMaterialRepository cannot be null");
        Assert.notNull(groupRepository, "GroupRepository cannot be null");
        Assert.notNull(patientRepository, "patientRepository cannot be null");
        Assert.notNull(modelCreationRepository, "modelCreationRepository cannot be null");
        Assert.notNull(tissueRepository, "tissueRepository cannot be null");
        Assert.notNull(patientSnapshotRepository, "patientSnapshotRepository cannot be null");
        Assert.notNull(sampleRepository, "sampleRepository cannot be null");
        Assert.notNull(markerRepository, "markerRepository cannot be null");
        Assert.notNull(markerAssociationRepository, "markerAssociationRepository cannot be null");
        Assert.notNull(molecularCharacterizationRepository, "molecularCharacterizationRepository cannot be null");
        Assert.notNull(externalUrlRepository, "externalUrlRepository cannot be null");

        this.tumorTypeRepository = tumorTypeRepository;
        this.hostStrainRepository = hostStrainRepository;
        this.engraftmentTypeRepository = engraftmentTypeRepository;
        this.engraftmentSiteRepository = engraftmentSiteRepository;
        this.engraftmentMaterialRepository = engraftmentMaterialRepository;
        this.groupRepository = groupRepository;
        this.patientRepository = patientRepository;
        this.modelCreationRepository = modelCreationRepository;
        this.tissueRepository = tissueRepository;
        this.patientSnapshotRepository = patientSnapshotRepository;
        this.sampleRepository = sampleRepository;
        this.markerRepository = markerRepository;
        this.markerAssociationRepository = markerAssociationRepository;
        this.molecularCharacterizationRepository = molecularCharacterizationRepository;
        this.qualityAssuranceRepository = qualityAssuranceRepository;
        this.ontologyTermRepository = ontologyTermRepository;
        this.specimenRepository = specimenRepository;
        this.platformRepository = platformRepository;
        this.platformAssociationRepository = platformAssociationRepository;
        this.dataProjectionRepository = dataProjectionRepository;
        this.treatmentSummaryRepository = treatmentSummaryRepository;
        this.treatmentProtocolRepository = treatmentProtocolRepository;
        this.currentTreatmentRepository = currentTreatmentRepository;
        this.externalUrlRepository = externalUrlRepository;
        this.drugRepository = drugRepository;
        this.treatmentRepository = treatmentRepository;


        this.markersBySymbol = new HashMap<>();
        this.markersByPrevSymbol = new HashMap<>();
        this.markersBySynonym = new HashMap<>();

    }

    public Group getGroup(String name, String abbrev, String type){

        Group g = groupRepository.findByNameAndType(name, type);

        if(g == null){
            log.info("Group not found. Creating {}", name);

            g = new Group(name, abbrev, type);
            groupRepository.save(g);

        }

        return g;
    }

    public Group getProviderGroup(String name, String abbrev, String description, String providerType, String contact, String url){

        Group g = groupRepository.findByNameAndType(name, "Provider");

        if(g == null){
            log.info("Provider group not found. Creating {}", name);

            g = Group.createProviderGroup(name, abbrev, description, providerType, contact, url);
            groupRepository.save(g);

        }
        return g;
    }

    public Group findProviderGroupByAbbrev(String abbrev){

        return groupRepository.findByAbbrevAndType(abbrev, "Provider");
    }


    public Group getPublicationGroup(String publicationId){

        Group g = groupRepository.findByPubmedIdAndType(publicationId, "Publication");


        if(g == null){
            log.info("Publication group not found. Creating {}", publicationId);

            g = new Group();
            g.setType("Publication");
            g.setPubMedId(publicationId);
            groupRepository.save(g);

        }
        return g;

    }

    public Group getProjectGroup(String groupName){

        Group g = groupRepository.findByNameAndType(groupName, "Project");

        if(g == null){
            log.info("Project group not found. Creating {}", groupName);

            g = new Group();
            g.setType("Project");
            g.setName(groupName);
            groupRepository.save(g);

        }
        return g;

    }

    public Group getAccessibilityGroup(String accessibility, String accessModalities){

        Group g = groupRepository.findAccessGroupByAccessibilityAndAccessModalities(accessibility, accessModalities);

        if(g == null){
            log.info("Access group not found. Creating " + accessibility + " " + accessModalities);

            g = Group.createAccessibilityGroup(accessibility, accessModalities);
            groupRepository.save(g);

        }
        return g;
    }


    public List<Group> getAllProviderGroups(){

        return groupRepository.findAllByType("Provider");
    }

    public void saveGroup(Group g){

        groupRepository.save(g);
    }


    public ExternalUrl getExternalUrl(ExternalUrl.Type type, String url) {
        ExternalUrl externalUrl = externalUrlRepository.findByTypeAndUrl(type.getValue(), url);
        if (externalUrl == null) {
            //log.info("External URL '{}' not found. Creating", type);
            externalUrl = new ExternalUrl(
                    type,
                    url);
            externalUrlRepository.save(externalUrl);
        }

        return externalUrl;

    }


    public ModelCreation createModelCreation(String pdxId, String dataSource,  Sample sample, QualityAssurance qa, List<ExternalUrl> externalUrls) {

        ModelCreation modelCreation = modelCreationRepository.findBySourcePdxIdAndDataSource(pdxId, dataSource);

        if (modelCreation != null) {
            log.info("Deleting existing ModelCreation " + pdxId);
            modelCreationRepository.delete(modelCreation);
        }
        modelCreation = new ModelCreation(pdxId, dataSource, sample, qa, externalUrls);
        modelCreation.addRelatedSample(sample);
        modelCreationRepository.save(modelCreation);
        return modelCreation;
    }

    public ModelCreation createModelCreation(String pdxId, String dataSource,  Sample sample, List<QualityAssurance> qa, List<ExternalUrl> externalUrls) {

        ModelCreation modelCreation = modelCreationRepository.findBySourcePdxIdAndDataSource(pdxId, dataSource);

        if (modelCreation != null) {
            log.info("Deleting existing ModelCreation " + pdxId);
            modelCreationRepository.delete(modelCreation);
        }
        modelCreation = new ModelCreation(pdxId, dataSource, sample, qa, externalUrls);
        modelCreation.addRelatedSample(sample);
        modelCreationRepository.save(modelCreation);
        return modelCreation;
    }

    public boolean isExistingModel(String dataSource, String modelId){

        ModelCreation modelCreation = modelCreationRepository.findBySourcePdxIdAndDataSource(modelId, dataSource);

        return modelCreation != null;
    }


    public ModelCreation findModelBySample(Sample sample){

        return modelCreationRepository.findBySample(sample);
    }


    public Collection<ModelCreation> findAllModelsPlatforms(){

        return modelCreationRepository.findAllModelsPlatforms();
    }

    public int countMarkerAssociationBySourcePdxId(String modelId, String dataSource,  String platformName){

        return modelCreationRepository.countMarkerAssociationBySourcePdxId(modelId, dataSource, platformName);
    }

    public Collection<ModelCreation> findModelsWithPatientData(){

        return modelCreationRepository.findModelsWithPatientData();
    }

    public Collection<ModelCreation> findAllModels(){

        return this.modelCreationRepository.findAllModels();
    }

    public List<ModelCreation> findModelsWithSpecimensAndQAByDS(String ds){

        return this.modelCreationRepository.findModelsWithSpecimensAndQAByDS(ds);
    }

    public List<ModelCreation> findModelXenograftPlatformSampleByDS(String ds){

        return modelCreationRepository.findModelPlatformSampleByDS(ds);
    }

    public ModelCreation findModelWithMolecularDataByDSAndIdAndMolcharType(String dataSource, String modelId, String molcharType){

        return modelCreationRepository.findModelWithMolecularDataByDSAndIdAndMolcharType(dataSource, modelId, molcharType);
    }

    public List<ModelCreation> findModelsWithSharingAndContactByDS(String ds){

        return modelCreationRepository.findModelsWithSharingAndContactByDS(ds);
    }

    public ModelCreation findModelByIdAndDataSource(String modelId, String dataSource){

        return modelCreationRepository.findBySourcePdxIdAndDataSource(modelId, dataSource);
    }

    public ModelCreation findModelByIdAndDataSourceWithSpecimensAndHostStrain(String modelId, String dataSource){

        return modelCreationRepository.findBySourcePdxIdAndDataSourceWithSpecimensAndHostStrain(modelId, dataSource);
    }

    public ModelCreation findBySourcePdxIdAndDataSourceWithSamplesAndSpecimensAndHostStrain(String modelId, String dataSource){

        return modelCreationRepository.findBySourcePdxIdAndDataSourceWithSamplesAndSpecimensAndHostStrain(modelId, dataSource);
    }

    public void saveModelCreation(ModelCreation modelCreation){
        this.modelCreationRepository.save(modelCreation);
    }

    public ModelCreation findModelByMolChar(MolecularCharacterization mc){

        return modelCreationRepository.findByMolChar(mc);
    }

    public ModelCreation findModelWithSampleByMolChar(MolecularCharacterization mc){

        return modelCreationRepository.findModelWithSampleByMolChar(mc);
    }

    public int getModelCount(){

        return modelCreationRepository.countAllModels();
    }

    public int getModelCountByDataSource(String dataSource){

        return modelCreationRepository.getModelCountByDataSource(dataSource);
    }


    public Collection<ModelCreation> getModelsWithMolCharBySourceFromTo(String dataSource, int from, int to){

        return modelCreationRepository.getModelsWithMolCharBySourceFromTo(dataSource, from, to);
    }


    public Patient createPatient(String patientId, Group dataSource, String sex, String race, String ethnicity){

        if(patientId == null || patientId.equals("")){
            log.warn("In DataImportService.createPatient() : the patientId is null or blank");
            throw new NullPointerException();
        }

        Patient patient = findPatient(patientId, dataSource);

        if(patient == null){

            patient = new Patient(patientId,sex,race,ethnicity,dataSource);
            patientRepository.save(patient);
        }

        return patient;
    }

    public Patient getPatientWithSnapshots(String patientId, Group group){

        return patientRepository.findByExternalIdAndGroupWithSnapshots(patientId, group);
    }

    public List<Patient> findPatientsByGroup(Group ds){

        return patientRepository.findByGroup(ds);
    }

    public Patient savePatient(Patient patient){

        return patientRepository.save(patient);
    }


    public Patient findPatient(String patientId, Group dataSource){

        return patientRepository.findByExternalIdAndGroupWithSnapshots(patientId, dataSource);

    }


    public List<Patient> findPatientTumorAtCollectionDataByDS(Group ds){

        return patientRepository.findPatientTumorAtCollectionDataByDS(ds);
    }


    public PatientSnapshot getPatientSnapshot(String externalId, String sex, String race, String ethnicity, String age, Group group) {


        if(externalId == null || externalId.equals("")){
            log.warn("In DataImportService.createPatient() : patientId is null or blank");
            throw new NullPointerException();
        }

        Patient patient = patientRepository.findByExternalIdAndGroup(externalId, group);
        PatientSnapshot patientSnapshot;

        if (patient == null) {

            patient = new Patient(externalId,sex,race,ethnicity,group);

            patientSnapshot = new PatientSnapshot(patient, age);
            patientSnapshotRepository.save(patientSnapshot);

        } else {
            patientSnapshot = this.getPatientSnapshot(patient, age);
        }
        return patientSnapshot;
    }

    public PatientSnapshot getPatientSnapshot(Patient patient, String age) {

        PatientSnapshot patientSnapshot = null;

        Set<PatientSnapshot> pSnaps = patientSnapshotRepository.findByPatient(patient.getExternalId());
        for (PatientSnapshot ps : pSnaps) {
            if (ps.getAgeAtCollection().equals(age)) {
                patientSnapshot = ps;
                break;
            }
        }
        if (patientSnapshot == null) {
            //log.info("PatientSnapshot for patient '{}' at age '{}' not found. Creating", patient.getExternalId(), age);
            patientSnapshot = new PatientSnapshot(patient, age);
            patientSnapshotRepository.save(patientSnapshot);
        }

        return patientSnapshot;
    }

    public PatientSnapshot getPatientSnapshot(Patient patient, String ageAtCollection, String collectionDate, String collectionEvent, String ellapsedTime){

        PatientSnapshot ps;

        if(patient.getSnapshots() != null){

            for(PatientSnapshot psnap : patient.getSnapshots()){

                if(psnap.getAgeAtCollection().equals(ageAtCollection) && psnap.getDateAtCollection().equals(collectionDate) &&
                        psnap.getCollectionEvent().equals(collectionEvent) && psnap.getElapsedTime().equals(ellapsedTime)){

                    return psnap;
                }

            }

            //ps = patient.getSnapShotByCollection(ageAtCollection, collectionDate, collectionEvent, ellapsedTime);
        }
        //create new snapshot and save it with the patient
        ps = new PatientSnapshot(patient, ageAtCollection, collectionDate, collectionEvent, ellapsedTime);
        patient.addSnapshot(ps);
        ps.setPatient(patient);
        patientRepository.save(patient);
        patientSnapshotRepository.save(ps);

        return ps;
    }


    public PatientSnapshot getPatientSnapshot(String patientId, String age, String dataSource){

        return patientSnapshotRepository.findByPatientIdAndDataSourceAndAge(patientId, dataSource, age);

    }

    public PatientSnapshot findLastPatientSnapshot(String patientId, Group ds){

        Patient patient = patientRepository.findByExternalIdAndGroupWithSnapshots(patientId, ds);
        PatientSnapshot ps = null;

        if(patient != null){

            ps = patient.getLastSnapshot();
        }
        return ps;
    }



    public Patient getPatient(String externalId, String sex, String race, String ethnicity, Group group) {

        Patient patient = patientRepository.findByExternalIdAndGroup(externalId, group);

        if (patient == null) {
            log.info("Patient '{}' not found. Creating", externalId);

            patient = new Patient(externalId, sex, race, ethnicity, group);

            patientRepository.save(patient);
        }

        return patient;
    }


    public Sample getSample(String sourceSampleId, String typeStr, String diagnosis, String originStr, String sampleSiteStr, String extractionMethod, String classification, Boolean normalTissue, String dataSource) {

        TumorType type = this.getTumorType(typeStr);
        Tissue origin = this.getTissue(originStr);
        Tissue sampleSite = this.getTissue(sampleSiteStr);
        Sample sample = sampleRepository.findBySourceSampleIdAndDataSource(sourceSampleId, dataSource);

        String updatedDiagnosis = diagnosis;

        // Changes Malignant * Neoplasm to * Cancer
        String pattern = "(.*)Malignant(.*)Neoplasm(.*)";

        if (diagnosis.matches(pattern)) {
            updatedDiagnosis = (diagnosis.replaceAll(pattern, "\t$1$2Cancer$3")).trim();
            log.info("Replacing diagnosis '{}' with '{}'", diagnosis, updatedDiagnosis);
        }

        updatedDiagnosis = updatedDiagnosis.replaceAll(",", "");

        if (sample == null) {

            sample = new Sample(sourceSampleId, type, updatedDiagnosis, origin, sampleSite, extractionMethod, classification, normalTissue, dataSource);
            sampleRepository.save(sample);
        }

        return sample;
    }

    public Sample getSample(String sourceSampleId, String dataSource,  String typeStr, String diagnosis, String originStr,
                            String sampleSiteStr, String extractionMethod, Boolean normalTissue, String stage, String stageClassification,
                            String grade, String gradeClassification){

        TumorType type = this.getTumorType(typeStr);
        Tissue origin = this.getTissue(originStr);
        Tissue sampleSite = this.getTissue(sampleSiteStr);
        Sample sample = sampleRepository.findHumanSampleBySampleIdAndDataSource(sourceSampleId, dataSource);

        String updatedDiagnosis = diagnosis;

        // Changes Malignant * Neoplasm to * Cancer
        String pattern = "(.*)Malignant(.*)Neoplasm(.*)";

        if (diagnosis.matches(pattern)) {
            updatedDiagnosis = (diagnosis.replaceAll(pattern, "\t$1$2Cancer$3")).trim();
            log.info("Replacing diagnosis '{}' with '{}'", diagnosis, updatedDiagnosis);
        }

        updatedDiagnosis = updatedDiagnosis.replaceAll(",", "");

        if (sample == null) {

            //String sourceSampleId, TumorType type, String diagnosis, Tissue originTissue, Tissue sampleSite, String extractionMethod,
            // String stage, String stageClassification, String grade, String gradeClassification, Boolean normalTissue, String dataSource
            sample = new Sample(sourceSampleId, type, updatedDiagnosis, origin, sampleSite, extractionMethod, stage, stageClassification, grade, gradeClassification, normalTissue, dataSource);
            sampleRepository.save(sample);
        }

        return sample;

    }


    public Sample findSampleByDataSourceAndSourceSampleId(String dataSource, String sampleId){

        return sampleRepository.findBySourceSampleIdAndDataSource(sampleId, dataSource);
    }

    public Collection<Sample> findSamplesWithoutOntologyMapping(){

        return sampleRepository.findSamplesWithoutOntologyMapping();
    }

    public Sample getMouseSample(ModelCreation model, String specimenId, String dataSource, String passage, String sampleId){

        Specimen specimen = this.getSpecimen(model, specimenId, dataSource, passage);
        Sample sample;

        if(specimen.getSample() == null){
            sample = new Sample();
            sample.setSourceSampleId(sampleId);
            sample.setDataSource(dataSource);
            sampleRepository.save(sample);
        }
        else{

            sample = specimen.getSample();
        }

        return sample;
    }

    //public findSampleWithMolcharBySpecimen


    public Sample findMouseSampleWithMolcharByModelIdAndDataSourceAndSampleId(String modelId, String dataSource, String sampleId){

        return sampleRepository.findMouseSampleWithMolcharByModelIdAndDataSourceAndSampleId(modelId, dataSource, sampleId);
    }

    public Sample findHumanSampleWithMolcharByModelIdAndDataSource(String modelId, String dataSource){

        return sampleRepository.findHumanSampleWithMolcharByModelIdAndDataSource(modelId, dataSource);
    }

    public List<MolecularCharacterization> findAllMolcharByDataSource(String dataSource){

        return molecularCharacterizationRepository.findAllByDataSource(dataSource);
    }

    public int findMolcharNumberByDataSource(String ds){
        return molecularCharacterizationRepository.findNumberByDataSource(ds);
    }
    
    public List<MolecularCharacterization> findMolcharByDataSourceSkipLimit(String ds, int skip, int limit){
        return molecularCharacterizationRepository.findByDataSourceSkipLimit(ds, skip, limit);
    }
    
    public Set<MolecularCharacterization> getMolcharsById(Set<Long> ids){

        return molecularCharacterizationRepository.findByIds(ids);
    }

    public Sample getHumanSample(String sampleId, String dataSource){


        return sampleRepository.findHumanSampleBySampleIdAndDataSource(sampleId, dataSource);
    }

    public Sample findHumanSample(String modelId, String dsAbbrev){

        return sampleRepository.findHumanSampleByModelIdAndDS(modelId, dsAbbrev);

    }

    public Sample findXenograftSample(String modelId, String dataSource, String specimenId){

        return sampleRepository.findMouseSampleByModelIdAndDataSourceAndSpecimenId(modelId, dataSource, specimenId);
    }

    public Sample findXenograftSample(String modelId, String dataSource, String passage, String nomenclature){


        return sampleRepository.findMouseSampleByModelIdAndDataSourceAndPassageAndNomenclature(modelId, dataSource, passage, nomenclature);
    }


    public int getHumanSamplesNumber(){

        return sampleRepository.findHumanSamplesNumber();
    }

    public Collection<Sample> findHumanSamplesFromTo(int from, int to){

        return sampleRepository.findHumanSamplesFromTo(from, to);
    }

    public void saveSample(Sample sample){
        sampleRepository.save(sample);
    }

    public EngraftmentSite getImplantationSite(String iSite) {
        EngraftmentSite site = engraftmentSiteRepository.findByName(iSite);
        if (site == null) {
            log.info("Implantation Site '{}' not found. Creating.", iSite);
            site = new EngraftmentSite(iSite);
            engraftmentSiteRepository.save(site);
        }

        return site;
    }

    public EngraftmentType getImplantationType(String iType) {
        EngraftmentType type = engraftmentTypeRepository.findByName(iType);
        if (type == null) {
            log.info("Implantation Type '{}' not found. Creating.", iType);
            type = new EngraftmentType(iType);
            engraftmentTypeRepository.save(type);
        }

        return type;
    }

    public EngraftmentMaterial getEngraftmentMaterial(String eMat){

        EngraftmentMaterial em = engraftmentMaterialRepository.findByName(eMat);

        if(em == null){
            em = new EngraftmentMaterial();
            em.setName(eMat);
            engraftmentMaterialRepository.save(em);
        }

        return em;
    }

    public EngraftmentMaterial createEngraftmentMaterial(String material, String status){

        EngraftmentMaterial em = new EngraftmentMaterial();
        em.setName(material);
        em.setState(status);
        engraftmentMaterialRepository.save(em);

        return em;

    }

    public Tissue getTissue(String t) {
        Tissue tissue = tissueRepository.findByName(t);
        if (tissue == null) {
            tissue = new Tissue(t);
            tissueRepository.save(tissue);
        }

        return tissue;
    }

    public TumorType getTumorType(String name) {
        TumorType tumorType = tumorTypeRepository.findByName(name);
        if (tumorType == null) {
            log.info("TumorType '{}' not found. Creating.", name);
            tumorType = new TumorType(name);
            tumorTypeRepository.save(tumorType);
        }

        return tumorType;
    }

    public HostStrain getHostStrain(String name, String symbol, String url, String description) throws Exception{

        if(name == null || symbol == null || symbol.isEmpty()) throw new Exception("Symbol or name is null");

        HostStrain hostStrain = hostStrainRepository.findBySymbol(symbol);



        if (hostStrain == null) {
            log.info("Background Strain '{}' not found. Creating", name);
            hostStrain = new HostStrain(name, symbol, description, url);
            hostStrainRepository.save(hostStrain);
        }
        else {
            //if the saved hoststrain's name is empty update the name
            if(!StringUtils.equals(hostStrain.getName(), name) ){

                hostStrain.setName(name);
                hostStrainRepository.save(hostStrain);

            }

        }
        return hostStrain;
    }

    public HostStrain findHostStrain(String symbol){

        return hostStrainRepository.findBySymbol(symbol);
    }


    // is this bad? ... probably..
    public Marker getMarker(String symbol) {
        log.error("MARKER METHOD WAS CALLED!");
        return this.getMarker(symbol, symbol);
    }

    public Marker getMarker(String symbol, String name) {
        log.error("MARKER METHOD WAS CALLED!");
        Marker marker = markerRepository.findByName(name);
        if (marker == null && symbol != null) {
            marker = markerRepository.findBySymbol(symbol);
        }
        if (marker == null) {
            //log.info("Marker '{}' not found. Creating", name);
            marker = new Marker(symbol, name);
            marker = markerRepository.save(marker);
        }
        return marker;
    }

    public List<MutatedMarkerData> getFrequentlyMutatedGenes(){

        return markerRepository.countModelsByMarker();
    }

    public Set<MarkerAssociation> findMarkerAssocsByMolChar(MolecularCharacterization mc){

        return markerAssociationRepository.findByMolChar(mc);
    }
    public Set<MarkerAssociation> findMutationByMolChar(MolecularCharacterization mc){

        return markerAssociationRepository.findMutationByMolChar(mc);
    }

    public void savePatientSnapshot(PatientSnapshot ps) {
        patientSnapshotRepository.save(ps);
    }

    public MolecularCharacterization saveMolecularCharacterization(MolecularCharacterization mc) {
        return molecularCharacterizationRepository.save(mc);
    }

    public void saveQualityAssurance(QualityAssurance qa) {
        if (qa != null) {
            if (null == qualityAssuranceRepository.findFirstByTechnologyAndDescription(qa.getTechnology(), qa.getDescription())) {
                qualityAssuranceRepository.save(qa);
            }
        }
    }

    public Collection<MolecularCharacterization> findMolCharsByType(String type){

        return molecularCharacterizationRepository.findAllByType(type);
    }


    public List<Specimen> findSpecimenByPassage(ModelCreation model, String passage){

        return specimenRepository.findByModelIdAndDataSourceAndAndPassage(model.getSourcePdxId(), model.getDataSource(), passage);
    }

    public Specimen getSpecimen(ModelCreation model, String specimenId, String dataSource, String passage){

        Specimen specimen = specimenRepository.findByModelIdAndDataSourceAndSpecimenIdAndPassage(model.getSourcePdxId(), dataSource, specimenId, passage);

        if(specimen == null){
            specimen = new Specimen();
            specimen.setExternalId(specimenId);
            specimen.setPassage(passage);
            specimenRepository.save(specimen);
        }

        return specimen;

    }


    public Specimen findSpecimenByModelAndPassageAndNomenclature(ModelCreation modelCreation, String passage, String nomenclature){

        return specimenRepository.findByModelAndPassageAndNomenClature(modelCreation, passage, nomenclature);
    }


    public List<Specimen> getAllSpecimenByModel(String modelId, String dataSource){

        return specimenRepository.findByModelIdAndDataSource(modelId, dataSource);
    }


    public Specimen findSpecimenByMolChar(MolecularCharacterization mc){

        return specimenRepository.findByMolChar(mc);
    }

    public void saveSpecimen(Specimen specimen){
        specimenRepository.save(specimen);
    }


    public OntologyTerm getOntologyTerm(String url, String label){

        OntologyTerm ot = ontologyTermRepository.findByUrl(url);

        if(ot == null){
            ot = new OntologyTerm(url, label);
            ontologyTermRepository.save(ot);
        }

        return ot;
    }

    public OntologyTerm getOntologyTerm(String url){
        return ontologyTermRepository.findByUrl(url);
    }

    public OntologyTerm findOntologyTermByLabel(String label){
        return ontologyTermRepository.findByLabel(label);
    }

    public OntologyTerm findOntologyTermByLabelAndType(String label, String type){
        return ontologyTermRepository.findByLabelAndType(label, type);
    }

    public OntologyTerm findOntologyTermByUrl(String url){

        return ontologyTermRepository.findByUrl(url);
    }

    public Collection<OntologyTerm> getAllOntologyTerms() {

        return ontologyTermRepository.findAll();

    }

    public Collection<OntologyTerm> getAllOntologyTermsWithNotZeroDirectMapping(){

        return ontologyTermRepository.findAllWithNotZeroDirectMappingNumber();
    }

    public Collection<OntologyTerm> getAllDirectParents(String termUrl){

        return ontologyTermRepository.findAllDirectParents(termUrl);
    }

    public int getIndirectMappingNumber(String label) {

        return ontologyTermRepository.getIndirectMappingNumber(label);
    }

    public int findDirectMappingNumber(String label) {


        Set<OntologyTerm> otset = ontologyTermRepository.getDistinctSubTreeNodes(label);
        int mapNum = 0;
        for (OntologyTerm ot : otset) {
            mapNum += ot.getDirectMappedSamplesNumber();
        }
        return mapNum;
    }

    public Collection<OntologyTerm> getAllOntologyTermsFromTo(int from, int to) {

        return ontologyTermRepository.findAllFromTo(from, to);

    }

    public Collection<OntologyTerm> getAllOntologyTermsByTypeFromTo(String type, int from, int to) {

        return ontologyTermRepository.findAllByTypeFromTo(type, from, to);

    }

    public int getOntologyTermNumberByType(String type){

        return ontologyTermRepository.getOntologyTermNumberByType(type);
    }

    public int countAllOntologyTerms(){
        return ontologyTermRepository.getOntologyTermNumber();
    }

    public boolean ontologyCacheIsEmpty() {
        return countAllOntologyTerms() == 0;
    }

    public OntologyTerm saveOntologyTerm(OntologyTerm ot){

        return ontologyTermRepository.save(ot);
    }

    public void deleteOntologyTermsWithoutMapping(){

        ontologyTermRepository.deleteTermsWithZeroMappings();
    }

    public Marker saveMarker(Marker marker) {
        return markerRepository.save(marker);
    }

    public void saveAllMarkers(Collection<Marker> markers) {
        markerRepository.saveAll(markers);
    }

    public Collection<Marker> getAllMarkers() {
        return markerRepository.findAllMarkers();
    }

    public Integer countAllMarkers() {
        return markerRepository.countAllMarkers();
    }

    public boolean markerCacheIsEmpty() {
        return countAllMarkers() == 0;
    }

    public Collection<Marker> getAllHumanMarkers() {
        return markerRepository.findAllHumanMarkers();
    }

    public Set<Marker> findAllDistinctMarkersByMolCharId(Long id){

        return markerRepository.findDistinctByMolCharId(id);
    }

    public Platform getPlatform(String name, String type, Group group) {

        //remove special characters from platform name
        name = name.replaceAll("[^A-Za-z0-9 _-]", "");


        Platform p = platformRepository.findByNameAndTypeAndDataSource(name, type, group.getName());
        if (p == null) {
            p = new Platform();
            p.setName(name);
            p.setType(type);
            p.setGroup(group);
            platformRepository.save(p);
        }

        return p;
    }

    public Platform getPlatform(String name, String type, Group group, String platformUrl) {

        //remove special characters from platform name
        name = name.replaceAll("[^A-Za-z0-9 _-]", "");

        Platform p = platformRepository.findByNameAndTypeAndDataSourceAndUrl(name, type, group.getName(), platformUrl);

        if (p == null) {
            p = new Platform();
            p.setName(name);
            p.setGroup(group);
            p.setType(type);
            p.setUrl(platformUrl);
            platformRepository.save(p);
        }

        return p;
    }

    public void savePlatform(Platform p){
        platformRepository.save(p);
    }

    public PlatformAssociation createPlatformAssociation(Platform p, Marker m) {
        if (platformAssociationRepository == null) {
            System.out.println("PAR is null");
        }
        if (p == null) {
            System.out.println("Platform is null");
        }
        assert p != null;
        if (p.getGroup() == null) {
            System.out.println("P.EDS is null");
        }
        if (m == null) {
            System.out.println("Marker is null");
        }
        assert m != null;
        PlatformAssociation pa = platformAssociationRepository.findByPlatformAndMarker(p.getName(), p.getGroup().getName(), m.getHgncSymbol());
        if (pa == null) {
            pa = new PlatformAssociation();
            pa.setPlatform(p);
            pa.setMarker(m);
            //platformAssociationRepository.save(pa);

        }

        return pa;
    }

    public void savePlatformAssociation(PlatformAssociation pa){
        platformAssociationRepository.save(pa);
    }

    public void saveDataProjection(DataProjection dp){

        dataProjectionRepository.save(dp);
    }

    public DataProjection findDataProjectionByLabel(String label){

        return dataProjectionRepository.findByLabel(label);
    }

    public boolean isTreatmentSummaryAvailableOnModel(String dataSource, String modelId){

        TreatmentSummary ts = treatmentSummaryRepository.findModelTreatmentByDataSourceAndModelId(dataSource, modelId);

        return ts != null && ts.getTreatmentProtocols() != null;
    }

    public boolean isTreatmentSummaryAvailableOnPatient(String dataSource, String modelId){

        TreatmentSummary ts = treatmentSummaryRepository.findPatientTreatmentByDataSourceAndModelId(dataSource, modelId);

        return ts != null && ts.getTreatmentProtocols() != null;
    }

    public int findPatientTreatmentNumber(String dataSource){

        if(dataSource == null || dataSource.isEmpty()){

            return treatmentRepository.findPatientTreatmentNumber();
        }
        else{

            return treatmentRepository.findPatientTreatmentNumberByDS(dataSource);
        }



    }

    public int findModelTreatmentNumber(String dataSource){

        if(dataSource == null || dataSource.isEmpty()){

            return treatmentRepository.findModelTreatmentNumber();
        }
        else{

            return treatmentRepository.findModelTreatmentNumberByDS(dataSource);
        }


    }


    public Collection<Treatment> getPatientTreatmentFrom(int from, int batch, String dataSource){

        if(dataSource == null || dataSource.isEmpty()){

            return treatmentRepository.getPatientTreatmentFrom(from, batch);
        }
        else{

            return treatmentRepository.getPatientTreatmentFromByDS(from, batch, dataSource);
        }


    }


    public Collection<Treatment> getModelTreatmentFrom(int from, int batch, String dataSource){

        if(dataSource == null || dataSource.isEmpty()){

            return treatmentRepository.getModelTreatmentFrom(from, batch);
        }
        else{

            return treatmentRepository.getModelTreatmentFromByDS(from, batch, dataSource);
        }

    }


    public TreatmentMappingData getUnmappedPatientTreatments(){

        return treatmentRepository.getUnmappedPatientTreatments();
    }


    public void saveTreatment(Treatment t){

        treatmentRepository.save(t);
    }

    public int findDrugDosingStudyNumberByDataSource(String datasource){

        return treatmentSummaryRepository.findDrugDosingStudyNumberByDataSource(datasource);
    }

    public int findPatientTreatmentNumberByDataSource(String datasource){

        return treatmentSummaryRepository.findPatientTreatmentNumberByDataSource(datasource);
    }

    public ModelCreation findModelByTreatmentSummary(TreatmentSummary ts){

        return modelCreationRepository.findByTreatmentSummary(ts);
    }

    public Collection<ModelCreation> findModelByPatientTreatmentSummary(TreatmentSummary ts){

        return modelCreationRepository.findByPatientTreatmentSummary(ts);
    }

    public String getDrugDosingUrlByDataSource(String dataSource){

        return treatmentSummaryRepository.findPlatformUrlByDataSource(dataSource);
    }

    private CurrentTreatment getCurrentTreatment(String name){

        CurrentTreatment ct = currentTreatmentRepository.findByName(name);

        if(ct == null){

            ct = new CurrentTreatment(name);
            currentTreatmentRepository.save(ct);
        }

        return ct;
    }


    public void createDrug(Drug d){
        drugRepository.save(d);
    }


    /**
     *
     * @param drugString
     * @param doseString
     * @param response
     * @return
     *
     * Creates a (tp:TreatmentProtocol)--(tc:TreatmentComponent)--(t:Treatment)
     *           (tp)--(r:Response) node
     *
     * If drug names are separated with + it will create multiple components to represent drug combos
     * Doses should be separated with either + or ;
     */
    public TreatmentProtocol getTreatmentProtocol(String drugString, String doseString, String response, String responseClassification){

        TreatmentProtocol tp = new TreatmentProtocol();

        if(doseString == null) doseString = "";

        //combination of drugs?
        if(drugString.contains("+") && doseString.contains(";")){

            String[] drugArray = drugString.split("\\+");
            String[] doseArray = doseString.split(";");

            if(drugArray.length == doseArray.length){

                for(int i=0;i<drugArray.length;i++){


                    Treatment treatment = new Treatment();
                    treatment.setName(drugArray[i].trim());
                    TreatmentComponent tc = new TreatmentComponent();
                    tc.setDose(doseArray[i].trim());


                    tc.setTreatment(treatment);
                    tp.addTreatmentComponent(tc);


                }

            }

            else if (drugArray.length > 1 && doseArray.length == 1){

                //use the same dosing for all drugs

                for (String s : drugArray) {

                    Treatment treatment = new Treatment();
                    treatment.setName(s.trim());

                    TreatmentComponent tc = new TreatmentComponent();
                    tc.setDose(doseArray[0].trim());

                    tc.setTreatment(treatment);
                    tp.addTreatmentComponent(tc);

                }

            }

        }
        else if(drugString.contains("+") && !doseString.contains(";")){

            if(doseString.contains("+")){
                //this data is coming from the universal loader, dose combinations are separated with + instead of ;

                String[] drugArray = drugString.split("\\+");
                String[] doseArray = doseString.split("\\+");

                if(drugArray.length == doseArray.length){

                    for(int i=0;i<drugArray.length;i++){

                        Treatment treatment = new Treatment();
                        treatment.setName(drugArray[i].trim());
                        TreatmentComponent tc = new TreatmentComponent();
                        tc.setDose(doseArray[i].trim());

                        tc.setTreatment(treatment);
                        tp.addTreatmentComponent(tc);

                    }

                }



            }
            else{

                String[] drugArray = drugString.split("\\+");

                for (String s : drugArray) {

                    Treatment treatment = new Treatment();
                    treatment.setName(s.trim());
                    TreatmentComponent tc = new TreatmentComponent();
                    tc.setDose(doseString.trim());
                    tc.setTreatment(treatment);
                    tp.addTreatmentComponent(tc);
                }
            }

        }
        //one drug only
        else{


            Treatment treatment = new Treatment();
            treatment.setName(drugString.trim());
            TreatmentComponent tc = new TreatmentComponent();

            if(doseString != null) {
                tc.setDose(doseString.trim());
            }
            else{
                tc.setDose("");
            }

            tc.setTreatment(treatment);
            tp.addTreatmentComponent(tc);
        }

        Response r = new Response();
        r.setDescription(Standardizer.getDrugResponse(response));
        r.setDescriptionClassification(responseClassification);

        tp.setResponse(r);

        if(tp.getComponents() == null || tp.getComponents().size() == 0) return null;

        return tp;
    }



    public TreatmentProtocol getTreatmentProtocol(String drugString, String doseString, String response, boolean currentTreatment){

        TreatmentProtocol tp = getTreatmentProtocol(drugString, doseString, response, "");

        if(currentTreatment && tp.getCurrentTreatment() == null){

            CurrentTreatment ct = getCurrentTreatment("Current Treatment");
            tp.setCurrentTreatment(ct);
        }

        return tp;

    }





    public TreatmentSummary findTreatmentSummaryByPatientSnapshot(PatientSnapshot ps){

        return treatmentSummaryRepository.findByPatientSnapshot(ps);

    }





    public Set<Object> findUnlinkedNodes(){

        return dataProjectionRepository.findUnlinkedNodes();
    }

    public Set<Object> findPatientsWithMultipleSummaries(){

        return dataProjectionRepository.findPatientsWithMultipleTreatmentSummaries();
    }

    public Set<Object> findPlatformsWithoutUrl(){

        return dataProjectionRepository.findPlatformsWithoutUrl();
    }



    public QualityAssurance getQualityAssurance(JSONObject data, String ds)  throws Exception{

        QualityAssurance qa = new QualityAssurance();
        String qaType = Standardizer.NOT_SPECIFIED;

        if (ds.equals("JAX")){

            String qaPassages = Standardizer.NOT_SPECIFIED;

            // Pending or Complete
            String qc = data.getString("QC");
            if("Pending".equals(qc)){
                qc = Standardizer.NOT_SPECIFIED;
            }else{
                qc = "QC is "+qc;
            }
            // the validation techniques are more than just fingerprint, we don't have a way to capture that
            qa = new QualityAssurance("Fingerprint", qc, qaPassages);
            saveQualityAssurance(qa);
        }


        if (ds.equals("PDXNet-HCI-BCM")){

            // This multiple QA approach only works because Note and Passage are the same for all QAs
            qa = new QualityAssurance(Standardizer.NOT_SPECIFIED,Standardizer.NOT_SPECIFIED,Standardizer.NOT_SPECIFIED);

            StringBuilder technology = new StringBuilder();
            if(data.has("QA")){
                JSONArray qas = data.getJSONArray("QA");
                for (int i = 0; i < qas.length(); i++) {
                    if (qas.getJSONObject(i).getString("Technology").equalsIgnoreCase("histology")) {
                        qa.setTechnology(qas.getJSONObject(i).getString("Technology"));
                        qa.setDescription(qas.getJSONObject(i).getString("Note"));
                        qa.setPassages(qas.getJSONObject(i).getString("Passage"));
                    }
                }
            }

        }



        if (ds.equals("PDXNet-MDAnderson") || ds.equals("PDXNet-WUSTL")) {

            try {
                qaType = data.getString("QA") + " on passage " + data.getString("QA Passage");
            } catch (Exception e) {
                // not all groups supplied QA
            }

            String qaPassage = data.has("QA Passage") ? data.getString("QA Passage") : null;
            qa = new QualityAssurance(qaType, Standardizer.NOT_SPECIFIED, qaPassage);
            saveQualityAssurance(qa);
        }


        if (ds.equals("IRCC-CRC")) {

            String FINGERPRINT_DESCRIPTION = "Model validated against patient germline.";

            if ("TRUE".equals(data.getString("Fingerprinting").toUpperCase())) {
                qa.setTechnology("Fingerprint");
                qa.setDescription(FINGERPRINT_DESCRIPTION);

                // If the model includes which passages have had QA performed, set the passages on the QA node
                if (data.has("QA Passage") && !data.getString("QA Passage").isEmpty()) {

                    List<String> passages = Stream.of(data.getString("QA Passage").split(","))
                            .map(String::trim)
                            .distinct()
                            .collect(Collectors.toList());
                    List<Integer> passageInts = new ArrayList<>();

                    // NOTE:  IRCC uses passage 0 to mean Patient Tumor, so we need to harmonize according to the other
                    // sources.  Subtract 1 from every passage.
                    for (String p : passages) {
                        int intPassage = Integer.parseInt(p);
                        passageInts.add(intPassage - 1);
                    }

                    qa.setPassages(StringUtils.join(passageInts, ", "));
                }
            }

        }


        return qa;
    }

    @Cacheable
    public Marker getMarkerBySymbol(String symbol){

        return markerRepository.findBySymbol(symbol);
    }

    @Cacheable
    public List<Marker> getMarkerByPrevSymbol(String symbol){

        return markerRepository.findByPrevSymbol(symbol);
    }

    @Cacheable
    public List<Marker> getMarkerBySynonym(String symbol){

        return markerRepository.findBySynonym(symbol);
    }

    private void initializeMarkers(){

        int markerCount = markerRepository.getMarkerCount();
        log.info("Initializing {} markers.",markerCount);
        int counter = 0;
        int batchSize = 400;
        while(counter < markerCount){

            Collection<Marker> markerCollection = markerRepository.getAllMarkersSkipLimit(counter, batchSize);

            for(Marker marker: markerCollection){
                addMarkerToMap(marker);
            }

            counter += batchSize;
            Float percent = ((float)counter / markerCount) * 100;
            System.out.print(String.format("%s markers initialized (%.0f%%)...\r", counter, percent));
        }

        markersInitialized = true;
    }

    private void addMarkerToMap(Marker marker){

        markersBySymbol.put(marker.getHgncSymbol(), marker);

        if(marker.getAliasSymbols() != null){
            for(String synonym : marker.getAliasSymbols()){

                addMarkerToMapList(synonym, marker, markersBySynonym);
            }
        }

        if(marker.getPrevSymbols() != null){
            for(String prevSymbol : marker.getPrevSymbols()){

                addMarkerToMapList(prevSymbol, marker, markersByPrevSymbol);
            }
        }

    }

    private void addMarkerToMapList(String key, Marker marker, Map<String, List<Marker>> mapList){

        if(mapList == null) mapList = new HashMap<>();

        if(mapList.containsKey(key)){
            mapList.get(key).add(marker);
        }
        else{
            List<Marker> markerList = new ArrayList<>();
            markerList.add(marker);
            mapList.put(key, markerList);
        }

    }

    public NodeSuggestionDTO getSuggestedMarker(String reporter, String dataSource, String modelId, String symbol,
                                                String characterizationType, String platform){

        if(!markersInitialized) initializeMarkers();

        NodeSuggestionDTO nodeSuggestionDTO = new NodeSuggestionDTO();
        LogEntity logEntity = new LogEntity(reporter, dataSource, modelId);
        Marker marker = null;

        if (markersBySymbol.containsKey(symbol)) {
            marker = markersBySymbol.get(symbol);
            nodeSuggestionDTO.setNode(marker);
        }
        else if(markersByPrevSymbol.containsKey(symbol)){

            List<Marker> markerList = markersByPrevSymbol.get(symbol);
            if (markerList.size() == 1) {
                marker = markerList.get(0);
                logEntity = logUpdateFromPreviousSymbol(reporter,dataSource, modelId, characterizationType, platform,
                    symbol, marker.getHgncSymbol(),"Previous symbol");
                nodeSuggestionDTO.setNode(marker);
            }
            else {
                logEntity = logNoSingleValidSymbol(reporter,dataSource, modelId, characterizationType, platform,
                    symbol, symbol,"");
                String prevMarkers = markerList.stream().map(Marker::getHgncSymbol).collect(Collectors.joining("; "));
                logEntity.setMessage("Previous symbol for multiple approved markers: {} "+prevMarkers);

            }

        }
        else if(markersBySynonym.containsKey(symbol)){

            List<Marker> markerList = markersBySynonym.get(symbol);

            if(markerList.size() == 1) {
                marker = markerList.get(0);
                logEntity = new MarkerLogEntity(reporter,dataSource, modelId, characterizationType, platform, symbol,
                        marker.getHgncSymbol(),"Synonym");
                nodeSuggestionDTO.setNode(marker);
            }
            else{
                logEntity = new MarkerLogEntity(reporter,dataSource, modelId, characterizationType, platform, symbol,
                        symbol,"");
                String synMarkers = markerList.stream().map(Marker::getHgncSymbol).collect(Collectors.joining("; "));
                logEntity.setMessage("Synonym for multiple approved markers: {} "+synMarkers);
            }
        }
        else{
            logEntity.setMessage("Unknown symbol: {} "+symbol);

        }

        nodeSuggestionDTO.setLogEntity(logEntity);

        return nodeSuggestionDTO;
    }


    public Marker getMarkerbyNcbiGeneId(String ncbiGene) {
        return markerRepository.findByNcbiGeneId(ncbiGene);
    }
}
