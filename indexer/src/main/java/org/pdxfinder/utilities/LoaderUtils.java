/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pdxfinder.utilities;

import org.apache.commons.cli.Option;
import org.pdxfinder.dao.*;
import org.pdxfinder.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * The hope was to put a lot of reused repository actions into one place ie find
 * or create a node or create a node with that requires a number of 'child'
 * nodes that are terms
 *
 * @author sbn
 */
@Component
public class LoaderUtils {

    public static Option loadAll = new Option("LoadAll", false, "Load all PDX Finder data");
    
    private TumorTypeRepository tumorTypeRepository;
    private HostStrainRepository hostStrainRepository;
    private EngraftmentTypeRepository engraftmentTypeRepository;
    private EngraftmentSiteRepository engraftmentSiteRepository;
    private ExternalDataSourceRepository externalDataSourceRepository;
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
    private ExternalUrlRepository externalUrlRepository;

    private final static Logger log = LoggerFactory.getLogger(LoaderUtils.class);

    public LoaderUtils(TumorTypeRepository tumorTypeRepository,
                       HostStrainRepository hostStrainRepository,
                       EngraftmentTypeRepository engraftmentTypeRepository,
                       EngraftmentSiteRepository engraftmentSiteRepository,
                       ExternalDataSourceRepository externalDataSourceRepository,
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
                       ExternalUrlRepository externalUrlRepository) {

        Assert.notNull(tumorTypeRepository, "tumorTypeRepository cannot be null");
        Assert.notNull(hostStrainRepository, "hostStrainRepository cannot be null");
        Assert.notNull(engraftmentTypeRepository, "implantationTypeRepository cannot be null");
        Assert.notNull(engraftmentSiteRepository, "implantationSiteRepository cannot be null");
        Assert.notNull(externalDataSourceRepository, "externalDataSourceRepository cannot be null");
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
        this.externalDataSourceRepository = externalDataSourceRepository;
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
        this.externalUrlRepository = externalUrlRepository;

    }

    public ExternalDataSource getExternalDataSource(String abbr, String name, String description, String contact, String url) {
        ExternalDataSource eDS = externalDataSourceRepository.findByAbbreviation(abbr);
        if (eDS == null) {
            log.info("External data source '{}' not found. Creating", abbr);
            eDS = new ExternalDataSource(
                    name,
                    abbr,
                    description,
                    contact,
                    Date.from(Instant.now()),
                    url);
            externalDataSourceRepository.save(eDS);
        }

        return eDS;

    }


    public ExternalUrl getExternalUrl(ExternalUrl.Type type, String url) {
        ExternalUrl externalUrl = externalUrlRepository.findByType(type);
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
        modelCreationRepository.save(modelCreation);
        return modelCreation;
    }

    public Collection<ModelCreation> getAllModelsPlatforms(){

        return modelCreationRepository.getAllModelsPlatforms();
    }

    public int countMarkerAssociationBySourcePdxId(String modelId, String platformName){

        return modelCreationRepository.countMarkerAssociationBySourcePdxId(modelId,platformName);
    }

    public Collection<ModelCreation> getModelsWithPatientData(){

        return modelCreationRepository.getModelsWithPatientData();
    }

    public Collection<ModelCreation> getAllModels(){

        return this.modelCreationRepository.getAllModels();
    }

    public ModelCreation findModelByIdAndDataSource(String modelId, String dataSource){

        return modelCreationRepository.findBySourcePdxIdAndDataSource(modelId, dataSource);
    }

    public void saveModelCreation(ModelCreation modelCreation){
        this.modelCreationRepository.save(modelCreation);
    }

    public ModelCreation getModelByMolChar(MolecularCharacterization mc){

        return modelCreationRepository.findByMolChar(mc);
    }

    public PatientSnapshot getPatientSnapshot(String externalId, String sex, String race, String ethnicity, String age, ExternalDataSource externalDataSource) {

        Patient patient = patientRepository.findByExternalId(externalId);
        PatientSnapshot patientSnapshot;

        if (patient == null) {
            log.info("Patient '{}' not found. Creating", externalId);

            patient = this.getPatient(externalId, sex, race, ethnicity, externalDataSource);

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
            if (ps.getAge().equals(age)) {
                patientSnapshot = ps;
                break loop;
            }
        }
        if (patientSnapshot == null) {
            log.info("PatientSnapshot for patient '{}' at age '{}' not found. Creating", patient.getExternalId(), age);
            patientSnapshot = new PatientSnapshot(patient, age);
            patientSnapshotRepository.save(patientSnapshot);
        }

        return patientSnapshot;
    }

    public PatientSnapshot getPatientSnapshot(String patientId, String age, String dataSource){

        PatientSnapshot ps = patientSnapshotRepository.findByPatientIdAndDataSourceAndAge(patientId, dataSource, age);

        return ps;

    }

    public Patient getPatient(String externalId, String sex, String race, String ethnicity, ExternalDataSource externalDataSource) {

        Patient patient = patientRepository.findByExternalId(externalId);

        if (patient == null) {
            log.info("Patient '{}' not found. Creating", externalId);

            patient = new Patient(externalId, sex, race, ethnicity, externalDataSource);

            patientRepository.save(patient);
        }

        return patient;
    }

    public Patient getPatientBySampleId(String sampleId){

        return patientRepository.findBySampleId(sampleId);
    }

    public PatientSnapshot getPatientSnapshotByModelId(String modelId){

        return patientSnapshotRepository.findByModelId(modelId);
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

    public Sample getSampleByDataSourceAndSourceSampleId(String dataSource, String sampleId){

        return sampleRepository.findBySourceSampleIdAndDataSource(sampleId, dataSource);
    }

    public Collection<Sample> getSamplesWithoutOntologyMapping(){

        return sampleRepository.findSamplesWithoutOntologyMapping();
    }

    public Sample getMouseSample(ModelCreation model, String specimenId, String dataSource, String passage, String sampleId){

        Specimen specimen = this.getSpecimen(model, specimenId, dataSource, passage);
        Sample sample = null;

        if(specimen.getSample() == null){
            sample = new Sample();
            sample.setSourceSampleId(sampleId);
            sampleRepository.save(sample);
        }
        else{

            sample = specimen.getSample();
        }

        return sample;
    }

    public Sample getHumanSample(String sampleId, String dataSource){


        return sampleRepository.findHumanSampleBySampleIdAndDataSource(sampleId, dataSource);
    }

    public int getHumanSamplesNumber(){

        return sampleRepository.findHumanSamplesNumber();
    }

    public Collection<Sample> getHumanSamplesFromTo(int from, int to){

        return sampleRepository.findHumanSamplesFromTo(from, to);
    }

    public Sample getSampleBySourcePdxId(String pdxId){
        return sampleRepository.findBySourcePdxId(pdxId);
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
            log.info("Implantation Site '{}' not found. Creating.", iType);
            type = new EngraftmentType(iType);
            engraftmentTypeRepository.save(type);
        }

        return type;
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

    public HostStrain getHostStrain(String name, String symbol, String url, String description) {

        HostStrain hostStrain = hostStrainRepository.findBySymbol(symbol);

        if (hostStrain == null) {
            log.info("Background Strain '{}' not found. Creating", name);
            hostStrain = new HostStrain(name, symbol, description, url);
            hostStrainRepository.save(hostStrain);
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
            log.info("Marker '{}' not found. Creating", name);
            marker = new Marker(symbol, name);
            marker = markerRepository.save(marker);
        }
        return marker;
    }
    
    public Marker getMarkerByEnsemblId(String id){
        Marker marker = markerRepository.findByEnsemblId(id);
        
        if (marker == null) {
            log.info("Marker '{}' not found. Creating ensemble", id);
            marker = new Marker();
            marker.setEnsemblId(id);
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


    public Set<MarkerAssociation> getMarkerAssocsByMolChar(MolecularCharacterization mc){

        return markerAssociationRepository.findByMolChar(mc);
    }

    // wow this is misleading it doesn't do anyting!
    public void deleteAllByEDSName(String edsName) throws Exception {
        throw new Exception("Nothing deleted. Method not implemented!");
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

    public Collection<MolecularCharacterization> getMolCharsByType(String type){

        return molecularCharacterizationRepository.getAllByType(type);
    }

    
    public Specimen getSpecimen(String id){
        Specimen specimen = specimenRepository.findByExternalId(id);
        if(specimen == null){
            specimen = new Specimen();
            specimen.setExternalId(id);
        }
             
        return specimen;
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

    public OntologyTerm getOntologyTermByLabel(String label){

        OntologyTerm ot = ontologyTermRepository.findByLabel(label);
        return ot;
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

    public int getDirectMappingNumber(String label) {


        Set<OntologyTerm> otset = ontologyTermRepository.getDistinctSubTreeNodes(label);
        int mapNum = 0;
        for (OntologyTerm ot : otset) {
            mapNum += ot.getDirectMappedSamplesNumber();
        }
        return mapNum;
    }

    public int getOntologyTermNumber(){

        return ontologyTermRepository.getOntologyTermNumber();
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

    public Platform getPlatform(String name, ExternalDataSource eds) {
        Platform p = platformRepository.findByNameAndDataSource(name, eds.getName());
        if (p == null) {
            p = new Platform();
            p.setName(name);
            p.setExternalDataSource(eds);
      //      platformRepository.save(p);
        }

        return p;
    }

    public Platform getPlatform(String name, ExternalDataSource eds, String platformUrl) {
        Platform p = platformRepository.findByNameAndDataSourceAndUrl(name, eds.getName(), platformUrl);
        if (p == null) {
            p = new Platform();
            p.setName(name);
            p.setExternalDataSource(eds);
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
        if (p.getExternalDataSource() == null) {
            System.out.println("P.EDS is null");
        }
        if (m == null) {
            System.out.println("Marker is null");
        }
        PlatformAssociation pa = platformAssociationRepository.findByPlatformAndMarker(p.getName(), p.getExternalDataSource().getName(), m.getSymbol());
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

    public DataProjection getDataProjectionByLabel(String label){

        return dataProjectionRepository.findByLabel(label);
    }

    public boolean isTreatmentSummaryAvailable(String dataSource, String modelId){

        TreatmentSummary ts = treatmentSummaryRepository.findByDataSourceAndModelId(dataSource, modelId);

        if(ts != null && ts.getTreatmentProtocols() != null){
            return true;
        }
        return false;
    }

}
