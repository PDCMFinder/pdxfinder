/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pdxfinder.services;

//import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.repositories.*;
import org.pdxfinder.services.ds.Harmonizer;
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.LoaderDTO;
import org.pdxfinder.services.dto.NodeSuggestionDTO;
import org.pdxfinder.services.reporting.LogEntity;
import org.pdxfinder.services.reporting.LogEntityType;
import org.pdxfinder.services.reporting.MarkerLogEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import org.springframework.cache.annotation.Cacheable;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The hope was to put a lot of reused repository actions into one place ie find
 * or create a node or create a node with that requires a number of 'child'
 * nodes that are terms
 *
 * @author sbn
 */
@Component
public class DataImportService {

    //public static Option loadAll = new Option("LoadAll", false, "Load all PDX Finder data");

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

    private final static Logger log = LoggerFactory.getLogger(DataImportService.class);

    private HashMap<String, Marker> markersBySymbol;
    private HashMap<String, Marker> markersByPrevSymbol;
    private HashMap<String, Marker> markersBySynonym;
    private HashMap<String, NodeSuggestionDTO> notFoundMarkerSymbols;

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
                             ExternalUrlRepository externalUrlRepository) {

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

        this.markersBySymbol = new HashMap<>();
        this.markersByPrevSymbol = new HashMap<>();
        this.markersBySynonym = new HashMap<>();
        this.notFoundMarkerSymbols = new HashMap<>();


    }

    public Group getGroup(String name, String abbrev, String type){

        Group g = groupRepository.findByNameAndType(name, type);

        if(g == null){
            log.info("Group not found. Creating", name);

            g = new Group(name, abbrev, type);
            groupRepository.save(g);

        }

        return g;
    }

    public Group getProviderGroup(String name, String abbrev, String description, String providerType, String contact, String url){

        Group g = groupRepository.findByNameAndType(name, "Provider");

        if(g == null){
            log.info("Provider group not found. Creating", name);

            g = new Group(name, abbrev, description, providerType, contact, url);
            groupRepository.save(g);

        }
        return g;
    }

    public Group getPublicationGroup(String publicationId){

        Group g = groupRepository.findByPubmedIdAndType(publicationId, "Publication");


        if(g == null){
            log.info("Publication group not found. Creating", publicationId);

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
            log.info("Project group not found. Creating", groupName);

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

            g = new Group(accessibility, accessModalities);
            g.setType("Accessibility");
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
            log.info("External URL '{}' not found. Creating", type);
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

        if(modelCreation == null) return false;
        return true;
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

    public ModelCreation findModelByIdAndDataSource(String modelId, String dataSource){

        return modelCreationRepository.findBySourcePdxIdAndDataSource(modelId, dataSource);
    }

    public ModelCreation findModelByIdAndDataSourceWithSpecimensAndHostStrain(String modelId, String dataSource){

        return modelCreationRepository.findBySourcePdxIdAndDataSourceWithSpecimensAndHostStrain(modelId, dataSource);
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

    public Patient createPatient(String patientId, Group dataSource, String sex, String race, String ethnicity){

        Patient patient = findPatient(patientId, dataSource);

        if(patient == null){

            patient = this.getPatient(patientId, sex, race, ethnicity, dataSource);
            patientRepository.save(patient);
        }

        return patient;
    }

    public Patient getPatientWithSnapshots(String patientId, Group group){

        return patientRepository.findByExternalIdAndGroupWithSnapshots(patientId, group);
    }


    public void savePatient(Patient patient){

        patientRepository.save(patient);
    }


    public Patient findPatient(String patientId, Group dataSource){

        return patientRepository.findByExternalIdAndGroupWithSnapshots(patientId, dataSource);

    }



    public PatientSnapshot getPatientSnapshot(String externalId, String sex, String race, String ethnicity, String age, Group group) {

        Patient patient = patientRepository.findByExternalIdAndGroup(externalId, group);
        PatientSnapshot patientSnapshot;

        if (patient == null) {
            log.info("Patient '{}' not found. Creating", externalId);

            patient = this.getPatient(externalId, sex, race, ethnicity, group);

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
        loop:
        for (PatientSnapshot ps : pSnaps) {
            if (ps.getAgeAtCollection().equals(age)) {
                patientSnapshot = ps;
                break loop;
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
        patient.hasSnapshot(ps);
        ps.setPatient(patient);
        patientRepository.save(patient);
        patientSnapshotRepository.save(ps);

        return ps;
    }


    public PatientSnapshot getPatientSnapshot(String patientId, String age, String dataSource){

        PatientSnapshot ps = patientSnapshotRepository.findByPatientIdAndDataSourceAndAge(patientId, dataSource, age);

        return ps;

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
        Sample sample = null;

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
            log.info("Tissue '{}' not found. Creating.", t);
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


    public Set<MarkerAssociation> findMarkerAssocsByMolChar(MolecularCharacterization mc){

        return markerAssociationRepository.findByMolChar(mc);
    }
    public Set<MarkerAssociation> findMutationByMolChar(MolecularCharacterization mc){

        return markerAssociationRepository.findMutationByMolChar(mc);
    }

    public void savePatientSnapshot(PatientSnapshot ps) {
        patientSnapshotRepository.save(ps);
    }

    public void saveMolecularCharacterization(MolecularCharacterization mc) {
        molecularCharacterizationRepository.save(mc);
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

        OntologyTerm ot = ontologyTermRepository.findByUrl(url);
        return ot;
    }

    public OntologyTerm findOntologyTermByLabel(String label){

        OntologyTerm ot = ontologyTermRepository.findByLabel(label);
        return ot;
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


    public void saveOntologyTerm(OntologyTerm ot){

        ontologyTermRepository.save(ot);
    }

    public void deleteOntologyTermsWithoutMapping(){

        ontologyTermRepository.deleteTermsWithZeroMappings();
    }

    public void saveMarker(Marker marker) {
        markerRepository.save(marker);
    }

    public Collection<Marker> getAllMarkers() {
        return markerRepository.findAllMarkers();
    }

    public Collection<Marker> getAllHumanMarkers() {
        return markerRepository.findAllHumanMarkers();
    }

    public Platform getPlatform(String name, Group group) {

        //remove special characters from platform name
        name = name.replaceAll("[^A-Za-z0-9 _-]", "");


        Platform p = platformRepository.findByNameAndDataSource(name, group.getName());
        if (p == null) {
            p = new Platform();
            p.setName(name);
            p.setGroup(group);
            platformRepository.save(p);
        }

        return p;
    }

    public Platform getPlatform(String name, Group group, String platformUrl) {

        //remove special characters from platform name
        name = name.replaceAll("[^A-Za-z0-9 _-]", "");

        Platform p = platformRepository.findByNameAndDataSourceAndUrl(name, group.getName(), platformUrl);

        if (p == null) {
            p = new Platform();
            p.setName(name);
            p.setGroup(group);
            p.setUrl(platformUrl);
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
        if (p.getGroup() == null) {
            System.out.println("P.EDS is null");
        }
        if (m == null) {
            System.out.println("Marker is null");
        }
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

        if(ts != null && ts.getTreatmentProtocols() != null){
            return true;
        }
        return false;
    }

    public boolean isTreatmentSummaryAvailableOnPatient(String dataSource, String modelId){

        TreatmentSummary ts = treatmentSummaryRepository.findPatientTreatmentByDataSourceAndModelId(dataSource, modelId);

        if(ts != null && ts.getTreatmentProtocols() != null){
            return true;
        }

        return false;
    }

    public ModelCreation findModelByTreatmentSummary(TreatmentSummary ts){

        return modelCreationRepository.findByTreatmentSummary(ts);
    }


    public Drug getStandardizedDrug(String drugString){

        Drug d = new Drug();
        d.setName(Standardizer.getDrugName(drugString));

        return d;
    }

    public CurrentTreatment getCurrentTreatment(String name){

        CurrentTreatment ct = currentTreatmentRepository.findByName(name);

        if(ct == null){

            ct = new CurrentTreatment(name);
            currentTreatmentRepository.save(ct);
        }

        return ct;
    }



    /**
     *
     * @param drugString
     * @param doseString
     * @param response
     * @return
     *
     * Creates a (tp:TreatmentProtocol)--(tc:TreatmentComponent)--(d:Drug)
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

                    Drug d = getStandardizedDrug(drugArray[i].trim());
                    TreatmentComponent tc = new TreatmentComponent();
                    tc.setType(Standardizer.getTreatmentComponentType(drugArray[i]));
                    tc.setDose(doseArray[i].trim());
                    //don't load unknown drugs
                    if(!d.getName().equals("Not Specified")){
                        tc.setDrug(d);
                        tp.addTreatmentComponent(tc);
                    }

                }

            }

            else if (drugArray.length > 1 && doseArray.length == 1){

                //use the same dosing for all drugs

                for(int i=0;i<drugArray.length;i++){

                    Drug d = getStandardizedDrug(drugArray[i].trim());
                    TreatmentComponent tc = new TreatmentComponent();
                    tc.setType(Standardizer.getTreatmentComponentType(drugArray[i]));
                    tc.setDose(doseArray[0].trim());
                    //don't load unknown drugs
                    if(!d.getName().equals("Not Specified")){
                        tc.setDrug(d);
                        tp.addTreatmentComponent(tc);
                    }

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

                        Drug d = getStandardizedDrug(drugArray[i].trim());
                        TreatmentComponent tc = new TreatmentComponent();
                        tc.setType(Standardizer.getTreatmentComponentType(drugArray[i]));
                        tc.setDose(doseArray[i].trim());

                        //don't load unknown drugs
                        if(!d.getName().equals("Not Specified")){
                            tc.setDrug(d);
                            tp.addTreatmentComponent(tc);
                        }

                    }

                }



            }
            else{

                String[] drugArray = drugString.split("\\+");

                for(int i=0;i<drugArray.length;i++){

                    Drug d = getStandardizedDrug(drugArray[i].trim());
                    TreatmentComponent tc = new TreatmentComponent();
                    tc.setType(Standardizer.getTreatmentComponentType(drugArray[i]));
                    tc.setDose(doseString.trim());
                    //don't load unknown drugs
                    if(!d.getName().equals("Not Specified")){
                        tc.setDrug(d);
                        tp.addTreatmentComponent(tc);
                    }
                }
            }

        }
        //one drug only
        else{

            Drug d = getStandardizedDrug(drugString.trim());
            TreatmentComponent tc = new TreatmentComponent();
            tc.setType(Standardizer.getTreatmentComponentType(drugString));
            tc.setDrug(d);
            if(doseString != null) {
                tc.setDose(doseString.trim());
            }
            else{
                tc.setDose("");
            }
            //don't load unknown drugs
            if(!d.getName().equals("Not Specified")){
                tc.setDrug(d);
                tp.addTreatmentComponent(tc);
            }
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
                        Integer intPassage = Integer.parseInt(p);
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

    public NodeSuggestionDTO getSuggestedMarker(String reporter, String dataSource, String modelId, String symbol,
                                                String characterizationType, String platform){

        //not found key to avoid looking up not found symbols multiple times
        //key: datasource + modelId + symbol
        if(notFoundMarkerSymbols.containsKey(dataSource+modelId+symbol)) return notFoundMarkerSymbols.get(dataSource+modelId+symbol);

        NodeSuggestionDTO nsdto = new NodeSuggestionDTO();
        LogEntity le;
        Marker m;
        List<Marker> markerSuggestionList = null;
        boolean ready = false;

        //check if marker is cached
        if(markersBySymbol.containsKey(symbol)){
            m = markersBySymbol.get(symbol);
            nsdto.setNode(m);
            ready = true;
        }
        else if(markersByPrevSymbol.containsKey(symbol)){

            m = markersByPrevSymbol.get(symbol);
            le = new MarkerLogEntity(reporter,dataSource, modelId, characterizationType, platform, symbol, m.getHgncSymbol(),"Previous symbol");
            nsdto.setLogEntity(le);
            ready = true;
        }
        else if(markersBySynonym.containsKey(symbol)){

            m = markersBySynonym.get(symbol);
            le = new MarkerLogEntity(reporter,dataSource, modelId, characterizationType, platform, symbol, m.getHgncSymbol(),"Synonym");
            nsdto.setLogEntity(le);
            ready = true;
        }

        if(ready) return nsdto;

        m = getMarkerBySymbol(symbol);

        if(m != null){

            //good, pass the marker
            //no message
            nsdto.setNode(m);
            markersBySymbol.put(symbol, m);
        }
        else{

            markerSuggestionList = getMarkerByPrevSymbol(symbol);

            if(markerSuggestionList != null && markerSuggestionList.size() > 0){

                if(markerSuggestionList.size() == 1){
                    //symbol found in prev symbols
                    m = markerSuggestionList.get(0);
                    le = new MarkerLogEntity(reporter,dataSource, modelId, characterizationType, platform, symbol, m.getHgncSymbol(),"Previous symbol");
                    nsdto.setNode(m);
                    nsdto.setLogEntity(le);
                    markersByPrevSymbol.put(symbol, m);
                }
                else{

                    le = new MarkerLogEntity(reporter,dataSource, modelId, characterizationType, platform, symbol, "","");
                    String prevMarkers = markerSuggestionList.stream().map(Marker::getHgncSymbol).collect(Collectors.joining("; "));

                    le.setMessage("Previous symbol for multiple approved markers: "+prevMarkers);
                    le.setType("ERROR");
                    nsdto.setNode(null);
                    nsdto.setLogEntity(le);
                }

                return nsdto;

            }
            else{

                markerSuggestionList = getMarkerBySynonym(symbol);

                if(markerSuggestionList != null && markerSuggestionList.size() > 0){

                    if(markerSuggestionList.size() == 1){

                        //symbol found in synonym
                        m = markerSuggestionList.get(0);
                        le = new MarkerLogEntity(reporter,dataSource, modelId, characterizationType, platform, symbol, m.getHgncSymbol(),"Synonym");

                        nsdto.setNode(m);
                        nsdto.setLogEntity(le);
                        markersBySynonym.put(symbol, m);
                    }
                    else{
                        le = new MarkerLogEntity(reporter,dataSource, modelId, characterizationType, platform, symbol, "","");
                        String synonymMarkers = markerSuggestionList.stream().map(Marker::getHgncSymbol).collect(Collectors.joining("; "));
                        le.setMessage("Synonym for multiple markers: "+synonymMarkers);
                        le.setType("ERROR");
                        nsdto.setNode(null);
                        nsdto.setLogEntity(le);
                    }

                    return nsdto;
                }
                else{

                    //error, didn't find the symbol anywhere
                    le = new MarkerLogEntity(reporter,dataSource, modelId, characterizationType, platform, symbol, "","");
                    le.setMessage(symbol +" is an unrecognised symbol");
                    le.setType("ERROR");
                    nsdto.setLogEntity(le);
                    notFoundMarkerSymbols.put(dataSource+modelId+symbol, nsdto);
                }
            }

        }



        return nsdto;
    }


}
