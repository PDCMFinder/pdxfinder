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
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.LoaderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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

    public Group getProviderGroup(String name, String abbrev, String description, String providerType, String accessibility,
                                  String accessModalities, String contact, String url){

        Group g = groupRepository.findByNameAndType(name, "Provider");

        if(g == null){
            log.info("Provider group not found. Creating", name);

            g = new Group(name, abbrev, description, providerType, accessibility, accessModalities, contact, url);
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
        return this.getMarker(symbol, symbol);
    }

    public Marker getMarker(String symbol, String name) {

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

    public MarkerAssociation getMarkerAssociation(String type, String markerSymbol, String markerName) {
        Marker m = this.getMarker(markerSymbol, markerName);
        MarkerAssociation ma = markerAssociationRepository.findByTypeAndMarkerName(type, m.getName());

        if (ma == null && m.getSymbol() != null) {
            ma = markerAssociationRepository.findByTypeAndMarkerSymbol(type, m.getSymbol());
        }

        if (ma == null) {
            ma = new MarkerAssociation(type, m);
            markerAssociationRepository.save(ma);
        }

        return ma;
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
        PlatformAssociation pa = platformAssociationRepository.findByPlatformAndMarker(p.getName(), p.getGroup().getName(), m.getSymbol());
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

            else{
                //TODO: deal with the case when there are more drugs than doses or vice versa
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
            tc.setDose(doseString.trim());
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


    @Autowired
    private UtilityService utilityService;
    String hci = "PDXNet-HCI-BCM";
    String mdAnderson = "PDXNet-MDAnderson";
    String irccCrc = "IRCC-CRC";
    String wustl = "PDXNet-WUSTL";


    public File[] stageZeroGetMetaDataFolder(String directory, String dataSource){

        File[] listOfFiles = new File[0];
        File folder = new File(directory);

        if(folder.exists()){
            listOfFiles = folder.listFiles();

            if(listOfFiles.length == 0){
                log.info("No file found for "+dataSource+", skipping");
            }
        }
        else{ log.info("Directory "+directory+" does not exist, skipping."); }

        return listOfFiles;
    }


    public String stageOneGetMetaDataFile(String modelJson, String dataSource){

        String metaDataString = "NOT FOUND";

        File file = new File(modelJson);

        if (file.exists()) {
            metaDataString = utilityService.parseFile(modelJson);
        } else {
            log.info("No file found for " + dataSource + ", skipping");
        }

        return metaDataString;
    }


    public LoaderDTO stagetwoCreateProviderGroup(LoaderDTO dto, String dsName, String dsAbbrev, String dsDesc,
                                                 String providerType, String access,String modalities, String dsContact,String url){

        Group providerDS = getProviderGroup(dsName, dsAbbrev, dsDesc, providerType, access, modalities, dsContact, url);
        dto.setProviderGroup(providerDS);

        return dto;
    }


    public LoaderDTO stageThreeCreateNSGammaHostStrain(LoaderDTO dto, String NSG_BS_SYMBOL,String  NSG_BS_URL,String NSG_BS_NAME) {

        try {
            HostStrain nsgBS = getHostStrain(NSG_BS_NAME, NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_NAME);
            dto.setNodScidGamma(nsgBS);
        } catch (Exception e) {}

        return dto;
    }


    public LoaderDTO stageFourCreateNSHostStrain(LoaderDTO dto, String NS_BS_SYMBOL,String  NS_BS_URL,String NS_BS_NAME) {

        try {
            HostStrain nsBS = getHostStrain(NS_BS_NAME, NS_BS_SYMBOL, NS_BS_URL, NS_BS_NAME);
            dto.setNodScid(nsBS);
        } catch (Exception e) {}

        return dto;
    }


    public LoaderDTO stageFiveCreateProjectGroup(LoaderDTO dto, String projectName) {

        Group projectGroup = getProjectGroup(projectName);
        dto.setProjectGroup(projectGroup);

        return dto;
    }


    public JSONArray stageSixGetPDXModels(String jsonString,String key){

        JSONArray jsonArray = new JSONArray();

        try {
            JSONObject job = new JSONObject(jsonString);
            jsonArray = job.getJSONArray(key);
        } catch (Exception e) {
            log.error("Error getting "+key+" PDX models", e);
        }

        return jsonArray;
    }



    public LoaderDTO stageSevenGetMetadata(LoaderDTO dto ,JSONObject data, String ds) throws Exception {

        String modelID = data.getString("Model ID");
        String sampleID = getSampleID(data,ds);
        String diagnosis = getDiagnosis(data,ds);
        String patientId = Standardizer.getValue("Patient ID",data);

        String ethnicity = getEthnicity(data,ds);

        String stage = Standardizer.getValue("Stage",data);
        String grade = Standardizer.getValue("Grades",data);

        String classification = getClassification(data,ds);

        String age = Standardizer.getAge(data.getString("Age"));
        String gender = Standardizer.getGender(data.getString("Gender"));
        String tumorType = Standardizer.getTumorType(data.getString("Tumor Type"));
        String sampleSite = Standardizer.getValue("Sample Site",data);
        String primarySite = Standardizer.getValue("Primary Site",data);
        String extractionMethod = Standardizer.getValue("Sample Type",data);
        String strain = Standardizer.getValue("Strain",data);
        String fingerprinting = getFingerprinting(data, ds);




        String implantationTypeStr = getImplantationType(data,ds);
        String implantationSiteStr = getEngraftmentSite(data,ds);
        QualityAssurance qa = getQualityAssurance(data,ds);

        String markerPlatform = getMarkerPlatform(data,ds);
        String markerStr = getMarkerStr(data,ds);
        String passage = getQAPassage(data,ds);

        JSONArray specimens = getSpecimens(data,ds);
        JSONArray treatments = getTreament(data, ds);


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


        return dto;

    }



    /*************************************************************************************************************
     *     CREATE PATIENT, PATIENT-SNAPSHOT, PATIENT SAMPLE, & EXTERNAL-URL        *
     *********************************************************************/

    public LoaderDTO stageEightLoadPatientData(LoaderDTO dto, String dataSourceContact){

        Group dataSource = dto.getProviderGroup();
        Patient patient = getPatientWithSnapshots(dto.getPatientId(), dataSource);

        if(patient == null){
            patient = createPatient(dto.getPatientId(), dataSource, dto.getGender(), "", Standardizer.getEthnicity(dto.getEthnicity()));
        }
        dto.setPatient(patient);


        PatientSnapshot pSnap = getPatientSnapshot(patient, dto.getAge(), "", "", "");
        dto.setPatientSnapshot(pSnap);

        Sample patientSample = getSample(dto.getSampleID(), dataSource.getAbbreviation(), dto.getTumorType(), dto.getDiagnosis(), dto.getPrimarySite(),
                dto.getSampleSite(), dto.getExtractionMethod(), false, dto.getStage(), "", dto.getGrade(), "");

        dto.setPatientSample(patientSample);

        List<ExternalUrl> externalUrls = new ArrayList<>();
        externalUrls.add(getExternalUrl(ExternalUrl.Type.CONTACT, dataSourceContact));
        dto.setExternalUrls(externalUrls);

        return dto;

    }


    public LoaderDTO stageNineCreateModels(LoaderDTO dto){

        ModelCreation modelCreation = createModelCreation(dto.getModelID(), dto.getProviderGroup().getAbbreviation(), dto.getPatientSample(), dto.getQualityAssurance(), dto.getExternalUrls());
        dto.setModelCreation(modelCreation);

        return dto;
    }




    /*************************************************************************************************************
     *     CREATE IMPLANTATION-SITE, IMPLANTATION-TYPE, SPECIMEN, UPDATE MODEL-CREATION         *
     ******************************************************************************************/

    public LoaderDTO loaderSecondStep(LoaderDTO dto, PatientSnapshot pSnap, String ds)  throws Exception{


        if (ds.equals(hci)){

            dto.getModelCreation().addRelatedSample(dto.getPatientSample());
            dto.getModelCreation().addGroup(dto.getProjectGroup());

            saveSample(dto.getPatientSample());
            savePatientSnapshot(dto.getPatientSnapshot());

            EngraftmentSite engraftmentSite = getImplantationSite(dto.getImplantationSiteStr());
            EngraftmentType engraftmentType = getImplantationType(dto.getImplantationtypeStr());

            // uggh parse strains
            ArrayList<HostStrain> strainList= new ArrayList();
            String strains = dto.getStrain();
            if(strains.contains(" and ")){
                strainList.add(dto.getNodScidGamma());
                strainList.add(dto.getNodScid());
            }else if(strains.contains("gamma")){
                strainList.add(dto.getNodScid());
            }else{
                strainList.add(dto.getNodScid());
            }

            int count = 0;
            for(HostStrain strain : strainList){
                count++;
                Specimen specimen = new Specimen();
                specimen.setExternalId(dto.getModelID()+"-"+count);
                specimen.setEngraftmentSite(engraftmentSite);
                specimen.setEngraftmentType(engraftmentType);
                specimen.setHostStrain(strain);

                Sample specSample = new Sample();
                specSample.setSourceSampleId(dto.getModelID()+"-"+count);
                specimen.setSample(specSample);

                dto.getModelCreation().addSpecimen(specimen);
                dto.getModelCreation().addRelatedSample(specSample);
                saveSpecimen(specimen);
            }
            saveModelCreation(dto.getModelCreation());

        }


        if (ds.equals(mdAnderson) || ds.equals(wustl)) {

            HostStrain bs = getHostStrain("", dto.getStrain(), "", "");
            boolean human = false;
            String markerPlatform = Standardizer.NOT_SPECIFIED;

            try {
                markerPlatform = dto.getMarkerPlatform();
                if ("CMS50".equals(markerPlatform) || "CMS400".equals(dto.getMarkerPlatform())) {
                    human = true;
                }
            } catch (Exception e) { /* this is for the FANG data and we don't really care about markers at this point anyway */ }


            if (ds.equals(mdAnderson)) {
                String markerStr = dto.getMarkerStr();
                String[] markers = markerStr.split(";");
                if (markerStr.trim().length() > 0) {
                    Platform pl = getPlatform(markerPlatform, dto.getProviderGroup());
                    MolecularCharacterization molC = new MolecularCharacterization();
                    molC.setType("mutation");
                    molC.setPlatform(pl);
                    List<MarkerAssociation> markerAssocs = new ArrayList<>();

                    for (int i = 0; i < markers.length; i++) {
                        Marker m = getMarker(markers[i], markers[i]);
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
                pSnap.addSample(dto.getPatientSample());

            } else {

                String passage = "0";
                try {
                    passage = dto.getQaPassage().replaceAll("P", "");
                } catch (Exception e) {
                    // default is 0
                }
                Specimen specimen = getSpecimen(dto.getModelCreation(), dto.getModelCreation().getSourcePdxId(), dto.getProviderGroup().getAbbreviation(), passage);
                specimen.setHostStrain(bs);

                if (ds.equals(wustl)){
                    Sample mouseSample = new Sample();
                    specimen.setSample(mouseSample);
                    dto.getModelCreation().addRelatedSample(mouseSample);

                    if (dto.getImplantationSiteStr().contains(";")) {
                        String[] parts = dto.getImplantationSiteStr().split(";");
                        dto.setImplantationSiteStr(parts[1].trim());
                        dto.setImplantationtypeStr(parts[0].trim());
                    }

                }

                EngraftmentSite is = getImplantationSite(dto.getImplantationSiteStr());
                specimen.setEngraftmentSite(is);

                EngraftmentType it = getImplantationType(dto.getImplantationtypeStr());
                specimen.setEngraftmentType(it);


                if (ds.equals(wustl)){
                    dto.getModelCreation().addSpecimen(specimen);
                }

                if (ds.equals(mdAnderson)) {
                    specimen.setSample(dto.getPatientSample());
                    saveSpecimen(specimen);
                }

            }

            saveSample(dto.getPatientSample());  // TODO: This was not be implemented for wustl, find out why
            saveModelCreation(dto.getModelCreation());
            savePatientSnapshot(pSnap);
        }


        if (ds.equals(irccCrc)){

            dto.getModelCreation().addGroup(dto.getProjectGroup());

            JSONArray specimens = dto.getSpecimens();

            for (int i = 0; i < specimens.length(); i++) {
                JSONObject specimenJSON = specimens.getJSONObject(i);

                String specimenId = specimenJSON.getString("Specimen ID");

                Specimen specimen = getSpecimen(dto.getModelCreation(),
                        specimenId, dto.getProviderGroup().getAbbreviation(), specimenJSON.getString("Passage"));

                specimen.setHostStrain(dto.getNodScidGamma());

                EngraftmentSite is = getImplantationSite(specimenJSON.getString("Engraftment Site"));
                specimen.setEngraftmentSite(is);

                EngraftmentType it = getImplantationType(specimenJSON.getString("Engraftment Type"));
                specimen.setEngraftmentType(it);

                Sample specSample = new Sample();

                specSample.setSourceSampleId(specimenId);
                specSample.setDataSource(dto.getProviderGroup().getAbbreviation());

                specimen.setSample(specSample);

                dto.getModelCreation().addSpecimen(specimen);
                dto.getModelCreation().addRelatedSample(specSample);

            }

        }

        return dto;

    }





    public LoaderDTO stepThreeCurrentTreatment(LoaderDTO dto, String DOSING_STUDY_URL, String responsekey){

        TreatmentSummary ts;
        try {

            if (dto.getTreatments().length() > 0) {

                ts = new TreatmentSummary();
                ts.setUrl(DOSING_STUDY_URL);

                for (int t = 0; t < dto.getTreatments().length(); t++) {

                    JSONObject treatmentObject = dto.getTreatments().getJSONObject(t);

                    TreatmentProtocol treatmentProtocol = getTreatmentProtocol(treatmentObject.getString("Drug"),
                            treatmentObject.getString("Dose"),
                            treatmentObject.getString(responsekey), "");

                    if (treatmentProtocol != null) {
                        ts.addTreatmentProtocol(treatmentProtocol);
                    }
                }
                ts.setModelCreation(dto.getModelCreation());
                dto.getModelCreation().setTreatmentSummary(ts);
            }

            saveModelCreation(dto.getModelCreation());

        } catch (Exception e) { }

        return dto;
    }


    private JSONArray getTreament(JSONObject data, String ds) throws Exception {

        JSONArray treatments = new JSONArray();

        if (ds.equals(hci)){

            try {
                if (data.has("Treatments")) {
                    JSONObject treatmentObj = data.optJSONObject("Treatments");
                    //if the treatment attribute is not an object = it is an array
                    if (treatmentObj == null && data.optJSONArray("Treatments") != null) {
                        treatments = data.getJSONArray("Treatments");
                    }
                }
            }catch (Exception e){}
        }
        return treatments;
    }





    private JSONArray getSpecimens(JSONObject data,String ds) throws Exception {

        JSONArray specimens = new JSONArray();

        if (ds.equals(irccCrc)){
            specimens = data.getJSONArray("Specimens");
        }
        return specimens;
    }


    private String getFingerprinting(JSONObject data,String ds) throws Exception {
        String fingerprinting = Standardizer.NOT_SPECIFIED;

        if (ds.equals(irccCrc)){
            fingerprinting = data.getString("Fingerprinting");
        }
        return fingerprinting;
    }


    private String getSampleID(JSONObject data,String ds) throws Exception {
        String sampleID = Standardizer.NOT_SPECIFIED;

        if (ds.equals(hci)){
            sampleID = data.getString("Sample ID");
        }

        if (ds.equals(irccCrc) || ds.equals(mdAnderson)){
            sampleID = data.getString("Model ID");
        }

        return sampleID;
    }

    private String getDiagnosis(JSONObject data,String ds) throws Exception {
        String diagnosis = data.getString("Clinical Diagnosis");

        if (ds.equals(mdAnderson)){
            // mdAnderson preference is for histology
            String histology = data.getString("Histology");
            if (histology.trim().length() > 0) {
                if ("ACA".equals(histology)) {
                    diagnosis = "Adenocarcinoma";
                } else {
                    diagnosis = histology;
                }
            }
        }

        if (ds.equals(wustl)){
            // Preference is for Histology
            String histology = data.getString("Histology");
            if (histology.trim().length() > 0) {
                diagnosis = histology;
            }
        }

        return diagnosis;
    }

    private String getEthnicity(JSONObject data,String ds) throws Exception {
        String ethnicity = Standardizer.NOT_SPECIFIED;

        if (ds.equals(hci)) {
            ethnicity = data.getString("Ethnicity");
        }

        if (ds.equals(mdAnderson) || ds.equals(wustl)){
            ethnicity = Standardizer.getValue("Race",data);
            try {
                if (data.getString("Ethnicity").trim().length() > 0) {
                    ethnicity = data.getString("Ethnicity");
                }
            } catch (Exception e) {}
        }
        return ethnicity;
    }


    private String getClassification(JSONObject data,String ds) throws Exception {
        String classification = Standardizer.NOT_SPECIFIED;

        if (ds.equals(mdAnderson) || ds.equals(wustl)){
            classification = data.getString("Stage") + "/" + data.getString("Grades");
        }

        if (ds.equals(irccCrc) || ds.equals(wustl)){
            classification = data.getString("Stage");
        }

        return classification;
    }




    private String getImplantationType(JSONObject data,String ds) throws Exception {
        String implantationTypeStr = Standardizer.NOT_SPECIFIED;

        if (ds.equals(hci)){
            implantationTypeStr = Standardizer.getValue("Implantation Type", data);
        }

        if (ds.equals(mdAnderson) || ds.equals(wustl)){
            implantationTypeStr =  Standardizer.getValue("Tumor Prep",data);
        }

        return implantationTypeStr;
    }


    private String getEngraftmentSite(JSONObject data,String ds) throws Exception {
        String implantationSite = Standardizer.NOT_SPECIFIED;

        if (ds.equals(hci) || ds.equals(mdAnderson) || ds.equals(wustl)){
            implantationSite = Standardizer.getValue("Engraftment Site", data);
        }
        return implantationSite;
    }

    private String getMarkerPlatform(JSONObject data,String ds) throws Exception {
        String markerPlatform = Standardizer.NOT_SPECIFIED;
        if (ds.equals(mdAnderson) || ds.equals(wustl)){
            markerPlatform = data.getString("Marker Platform");
        }
        return markerPlatform;
    }


    private String getMarkerStr(JSONObject data,String ds) throws Exception {
        String markerStr = Standardizer.NOT_SPECIFIED;

        if (ds.equals(mdAnderson)){
            markerStr = data.getString("Markers");
        }

        return markerStr;
    }


    private String getQAPassage(JSONObject data,String ds) throws Exception {
        String passage = Standardizer.NOT_SPECIFIED;
        if (ds.equals(mdAnderson)){
            passage = data.getString("QA Passage").replaceAll("P", "");
        }
        return passage;
    }









    private QualityAssurance getQualityAssurance(JSONObject data,String ds)  throws Exception{

        QualityAssurance qa = new QualityAssurance();
        String qaType = Standardizer.NOT_SPECIFIED;

        if (ds.equals(hci)){

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



        if (ds.equals(mdAnderson) || ds.equals(wustl)) {

            try {
                qaType = data.getString("QA") + " on passage " + data.getString("QA Passage");
            } catch (Exception e) {
                // not all groups supplied QA
            }

            String qaPassage = data.has("QA Passage") ? data.getString("QA Passage") : null;
            qa = new QualityAssurance(qaType, Standardizer.NOT_SPECIFIED, qaPassage);
            saveQualityAssurance(qa);
        }


        if (ds.equals(irccCrc)) {

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


}
