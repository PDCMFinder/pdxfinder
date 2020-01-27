package org.pdxfinder.dataloaders.updog;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.*;

public class DomainObjectCreator {

    private Map<String, Table> pdxDataTables;
    //nodeType=>ID=>NodeObject
    private Map<String, Map<String, Object>> domainObjects;

    private DataImportService dataImportService;
    private static final Logger log = LoggerFactory.getLogger(DomainObjectCreator.class);

    private static final String PATIENT_KEY = "patient";
    private static final String PROVIDER_KEY = "provider_group";
    private static final String MODEL_KEY = "model";
    private static final String TUMOR_TYPE_KEY = "tumor_type";
    private static final String TISSUE_KEY = "tissue";
    private static final String HOST_STRAIN_KEY = "hoststrain";
    private static final String ENGRAFTMENT_SITE_KEY = "engraftment_site";
    private static final String ENGRAFTMENT_TYPE_KEY = "engraftment_type";
    private static final String ENGRAFTMENT_MATERIAL_KEY = "engraftment_material";
    private static final String PLATFORM_KEY = "platform";

    private static final String NOT_SPECIFIED = "Not Specified";


    public DomainObjectCreator(
        DataImportService dataImportService,
        Map<String, Table> pdxDataTables) {
        this.dataImportService = dataImportService;
        this.pdxDataTables = pdxDataTables;
        domainObjects = new HashMap<>();
    }

    public void loadDomainObjects(){
        //: Do not change the order of these unless you want to risk 1. the universe to collapse OR 2. missing nodes in the db
        createProvider();
        createPatientData();
        createModelData();
        createSampleData();
        createSharingData();

        createSamplePlatformData();

        //createMolecularData();

        persistNodes();
    }

    void createProvider(){

        Table finderRelatedTable = pdxDataTables.get("metadata-loader.tsv");
        Row row = finderRelatedTable.row(0);

        String providerName = row.getString(TSV.Metadata.name.name());
        String abbrev = row.getString(TSV.Metadata.abbreviation.name());
        String internalUrl = row.getString(TSV.Metadata.internal_url.name());

        Group providerGroup = dataImportService.getProviderGroup(
            providerName, abbrev, "", "", "", internalUrl);

        addDomainObject(PROVIDER_KEY, null, providerGroup);
    }

    void createPatientData() {

        Table patientTable = pdxDataTables.get("metadata-patient.tsv");
        for (Row row : patientTable) {
            try {
                Patient patient = dataImportService.createPatient(
                    row.getText(TSV.Metadata.patient_id.name()),
                    (Group) getDomainObject(TSV.Metadata.provider_group.name(), null),
                    row.getText(TSV.Metadata.sex.name()),
                    "",
                    row.getText(TSV.Metadata.ethnicity.name()));

                patient.setCancerRelevantHistory(row.getText(TSV.Metadata.history.name()));
                patient.setFirstDiagnosis(row.getText(TSV.Metadata.initial_diagnosis.name()));
                patient.setAgeAtFirstDiagnosis(row.getText(TSV.Metadata.age_at_initial_diagnosis.name()));

                addDomainObject(
                    PATIENT_KEY,
                    row.getText(TSV.Metadata.patient_id.name()),
                    dataImportService.savePatient(patient));
            }
            catch(Exception e) {
                log.error(
                    "Error loading patient {} at row {}",
                    row.getText(TSV.Metadata.patient_id.name()),
                    row.getRowNumber());
            }
        }
    }

    void createSampleData(){

        Table sampleTable = pdxDataTables.get("metadata-sample.tsv");
        for (Row row : sampleTable) {
            String patientId = row.getString(TSV.Metadata.patient_id.name());
            String modelId = row.getString(TSV.Metadata.model_id.name());
            String dateOfCollection = row.getString(TSV.Metadata.collection_date.name());
            String ageAtCollection = row.getString(TSV.Metadata.age_in_years_at_collection.name());
            String collectionEvent = row.getString(TSV.Metadata.collection_event.name());
            String elapsedTime = row.getString(TSV.Metadata.months_since_collection_1.name());
            String primarySiteName = row.getString(TSV.Metadata.primary_site.name());
            String virologyStatus = row.getString(TSV.Metadata.virology_status.name());
            String treatmentNaive = row.getString(TSV.Metadata.treatment_naive_at_collection.name());

            Patient patient = (Patient) getDomainObject(PATIENT_KEY, patientId);
            if (patient == null) throw new NullPointerException();

            PatientSnapshot patientSnapshot = patient.getSnapShotByCollection(
                ageAtCollection,
                dateOfCollection,
                collectionEvent,
                elapsedTime);

            if (patientSnapshot == null) {
                patientSnapshot = new PatientSnapshot(
                    patient,
                    ageAtCollection,
                    dateOfCollection,
                    collectionEvent,
                    elapsedTime);
                patientSnapshot.setVirologyStatus(virologyStatus);
                patientSnapshot.setTreatmentNaive(treatmentNaive);
                patient.addSnapshot(patientSnapshot);
            }

            Sample sample = createPatientSample(row);
            patientSnapshot.addSample(sample);

            ModelCreation modelCreation = (ModelCreation) getDomainObject(MODEL_KEY, modelId);
            if(modelCreation == null) throw new NullPointerException();

            modelCreation.setSample(sample);
            modelCreation.addRelatedSample(sample);

        }
    }

    void createModelData(){

        Table modelTable = pdxDataTables.get("metadata-model.tsv");
        Group providerGroup = (Group) domainObjects.get(PROVIDER_KEY).get(null);
        for (Row row : modelTable) {
            String modelId = row.getString(TSV.Metadata.model_id.name());
            String hostStrainNomenclature = row.getString(TSV.Metadata.host_strain_full.name());
            String passageNum = row.getString(TSV.Metadata.passage_number.name());

            ModelCreation modelCreation = new ModelCreation();
            modelCreation.setSourcePdxId(modelId);
            modelCreation.setDataSource(providerGroup.getAbbreviation());
            addDomainObject(MODEL_KEY,modelId, modelCreation);

            Specimen specimen = modelCreation.getSpecimenByPassageAndHostStrain(passageNum, hostStrainNomenclature);
            if (specimen == null) {
                specimen = createSpecimen(row, row.getRowNumber());
                modelCreation.addSpecimen(specimen);
                modelCreation.addRelatedSample(specimen.getSample());
            }
        }
        createModelValidationData();
    }

    void createModelValidationData() {

        Table modelValidationTable = pdxDataTables.get("metadata-model_validation.tsv");
        for (Row row : modelValidationTable) {
            String modelId = row.getString(TSV.Metadata.model_id.name());
            String validationTechnique = row.getString(TSV.Metadata.validation_technique.name());
            String description = row.getString(TSV.Metadata.description.name());
            String passagesTested = row.getString(TSV.Metadata.passages_tested.name());
            String hostStrainFull = row.getString(TSV.Metadata.validation_host_strain_full.name());

            ModelCreation modelCreation = (ModelCreation) getDomainObject(MODEL_KEY, modelId);
            if(modelCreation == null) throw new NullPointerException();

            QualityAssurance qa = new QualityAssurance();
            qa.setTechnology(validationTechnique);
            qa.setDescription(description);
            qa.setPassages(passagesTested);
            qa.setValidationHostStrain(hostStrainFull);
            modelCreation.addQualityAssurance(qa);
        }
    }

    void createSharingData(){

        Table sharingTable = pdxDataTables.get("metadata-sharing.tsv");

        Group providerGroup = (Group) domainObjects.get(PROVIDER_KEY).get(null);
        if(providerGroup == null) throw new NullPointerException();

        for (Row row : sharingTable) {
            String modelId = row.getString(TSV.Metadata.model_id.name());
            String providerType = row.getString(TSV.Metadata.provider_type.name());
            String accessibility = row.getString(TSV.Metadata.accessibility.name());
            String europdxAccessModality = row.getString(TSV.Metadata.europdx_access_modality.name());
            String email = row.getString(TSV.Metadata.email.name());
            String formUrl = row.getString(TSV.Metadata.form_url.name());
            String databaseUrl = row.getString(TSV.Metadata.database_url.name());
            String project = row.getString(TSV.Metadata.project.name());

            ModelCreation modelCreation = (ModelCreation) getDomainObject(MODEL_KEY, modelId);
            if(modelCreation == null) throw new NullPointerException();

            List<ExternalUrl> externalUrls = getExternalUrls(email, formUrl, databaseUrl);
            modelCreation.setExternalUrls(externalUrls);

            Optional.ofNullable(project).ifPresent(
                s -> {
                    Group projectGroup = dataImportService.getProjectGroup(s);
                    modelCreation.addGroup(projectGroup);
                });

            if (eitherIsPresent(accessibility, europdxAccessModality)) {
                Group access = dataImportService.getAccessibilityGroup(accessibility, europdxAccessModality);
                modelCreation.addGroup(access);
            }

            providerGroup.setProviderType(providerType);
            providerGroup.setContact(email);
        }
    }

    void createSamplePlatformData(){

        Table samplePlatformTable = pdxDataTables.get("sampleplatform-data.tsv");


        for(Row row : samplePlatformTable){


            String sampleOrigin = row.getString(TSV.SamplePlatform.sample_origin.name());
            String platformName = row.getString(TSV.SamplePlatform.platform.name());
            String molCharType = row.getString(TSV.SamplePlatform.molecular_characterisation_type.name());

            Sample sample = null;

            if(sampleOrigin.equals("patient")){

                sample = getPatientSample(row);
            }
            else if(sampleOrigin.equals("xenograft")){

                sample = getOrCreateSpecimen(row).getSample();
            }

            if(sample == null) throw new NullPointerException();


            getOrCreateMolecularCharacterization(sample, platformName, molCharType);

        }

    }

    void createMolecularData(){

        Table mutationTable = pdxDataTables.get("mutation.tsv");
        String molCharType = "mutation";
        for(Row row:mutationTable){

            String sampleOrigin = row.getString(TSV.Mutation.sample_origin.name());
            String platformName = row.getString(TSV.Mutation.platform.name());

            Sample sample = null;

            if(sampleOrigin.equals("patient")){

                sample = getPatientSample(row);
            }
            else if(sampleOrigin.equals("xenograft")){

                sample = getOrCreateSpecimen(row).getSample();
            }

            if(sample == null) throw new NullPointerException();


            MolecularCharacterization molecularCharacterization = getOrCreateMolecularCharacterization(sample, platformName, molCharType);

            addMutationData(molecularCharacterization, row);

        }


    }



    private Sample getPatientSample(Row row){

        String modelId = row.getString(TSV.Mutation.model_id.name());
        ModelCreation modelCreation = (ModelCreation) getDomainObject(MODEL_KEY, modelId);
        if(modelCreation == null) throw new NullPointerException();

        return modelCreation.getSample();
    }

    private Specimen getOrCreateSpecimen(Row row){

        String modelId = row.getString(TSV.Mutation.model_id.name());
        String hostStrainSymbol = row.getString(TSV.Mutation.host_strain_nomenclature.name());
        String passage = row.getString(TSV.Mutation.passage.name());
        String sampleId = row.getString(TSV.Mutation.sample_id.name());

        ModelCreation modelCreation = (ModelCreation) getDomainObject(MODEL_KEY, modelId);
        if(modelCreation == null) throw new NullPointerException();

        Specimen specimen = modelCreation.getSpecimenByPassageAndHostStrain(passage, hostStrainSymbol);

        if(specimen == null){
            specimen = new Specimen();
            specimen.setPassage(passage);

            HostStrain hostStrain = getOrCreateHostStrain(NOT_SPECIFIED, hostStrainSymbol, row.getRowNumber());
            specimen.setHostStrain(hostStrain);

            Sample sample = new Sample();
            sample.setSourceSampleId(sampleId);
            specimen.setSample(sample);
            modelCreation.addRelatedSample(sample);
        }

        return specimen;
    }

    private MolecularCharacterization getOrCreateMolecularCharacterization(Sample sample, String platformName, String molCharType){

        MolecularCharacterization molecularCharacterization = sample.getMolecularCharacterization(molCharType, platformName);

        if(molecularCharacterization == null){

            molecularCharacterization = new MolecularCharacterization();
            molecularCharacterization.setType(molCharType);
            molecularCharacterization.setPlatform(getOrCreatePlatform(platformName, molCharType));
            sample.addMolecularCharacterization(molecularCharacterization);

        }

        return molecularCharacterization;
    }


    private Platform getOrCreatePlatform(String platformName, String molCharType){

        Group providerGroup = (Group) getDomainObject(PROVIDER_KEY, null);
        String platformId = molCharType+platformName;
        Platform platform = (Platform) getDomainObject(PLATFORM_KEY, platformId);

        if(platform == null){

            platform = new Platform();
            platform.setGroup(providerGroup);
            platform.setName(platformName);

            addDomainObject(PLATFORM_KEY, platformId, platform);
        }

        return platform;
    }



    private void addMutationData(MolecularCharacterization molecularCharacterization, Row row){

        MarkerAssociation markerAssociation = molecularCharacterization.getMarkerAssociations().get(0);



    }

    private boolean eitherIsPresent(String string, String anotherString) {
        return (
            Optional.ofNullable(string).isPresent() ||
            Optional.ofNullable(anotherString).isPresent()
        );
    }

    private List<ExternalUrl> getExternalUrls(String email, String formUrl, String databaseUrl) {
        List<ExternalUrl> externalUrls = new ArrayList<>();
        Optional.ofNullable(email).ifPresent(
            s -> externalUrls.add(
                dataImportService.getExternalUrl(ExternalUrl.Type.CONTACT, s)));
        Optional.ofNullable(formUrl).ifPresent(
            s -> externalUrls.add(
                dataImportService.getExternalUrl(ExternalUrl.Type.CONTACT, s)));
        Optional.ofNullable(databaseUrl).ifPresent(
            s -> externalUrls.add(
                dataImportService.getExternalUrl(ExternalUrl.Type.SOURCE, s)));
        return externalUrls;
    }

    private Specimen createSpecimen(Row row, int rowNumber){

        String hostStrainName = row.getString(TSV.Metadata.host_strain.name());
        String hostStrainNomenclature = row.getString(TSV.Metadata.host_strain_full.name());
        String engraftmentSiteName = row.getString(TSV.Metadata.engraftment_site.name());
        String engraftmentTypeName = row.getString(TSV.Metadata.engraftment_type.name());
        String sampleType = row.getString(TSV.Metadata.sample_type.name());
        String passageNum = row.getString(TSV.Metadata.passage_number.name());

        HostStrain hostStrain = getOrCreateHostStrain(hostStrainName, hostStrainNomenclature, rowNumber);
        EngraftmentSite engraftmentSite = getOrCreateEngraftment(engraftmentSiteName);
        EngraftmentType engraftmentType = getOrCreateEngraftmentType(engraftmentTypeName);
        EngraftmentMaterial engraftmentMaterial = getOrCreateEngraftmentMaterial(sampleType);

        Sample xenoSample = new Sample();
        Specimen specimen = new Specimen();
        specimen.setPassage(passageNum);
        specimen.setHostStrain(hostStrain);
        specimen.setEngraftmentMaterial(engraftmentMaterial);
        specimen.setEngraftmentSite(engraftmentSite);
        specimen.setEngraftmentType(engraftmentType);
        specimen.setSample(xenoSample);

        return specimen;
    }

    private EngraftmentMaterial getOrCreateEngraftmentMaterial(String sampleType) {
        EngraftmentMaterial engraftmentMaterial = (EngraftmentMaterial) getDomainObject(ENGRAFTMENT_MATERIAL_KEY, sampleType);
        if(engraftmentMaterial == null){
            engraftmentMaterial = dataImportService.getEngraftmentMaterial(sampleType);
            addDomainObject(ENGRAFTMENT_MATERIAL_KEY, sampleType, engraftmentMaterial);
        }
        return engraftmentMaterial;
    }

    private EngraftmentType getOrCreateEngraftmentType(String engraftmentTypeName) {
        EngraftmentType engraftmentType = (EngraftmentType) getDomainObject(ENGRAFTMENT_TYPE_KEY, engraftmentTypeName);
        if(engraftmentType == null){
            engraftmentType = dataImportService.getImplantationType(engraftmentTypeName);
            addDomainObject(ENGRAFTMENT_TYPE_KEY, engraftmentTypeName, engraftmentType);
        }
        return engraftmentType;
    }

    private EngraftmentSite getOrCreateEngraftment(String engraftmentSiteName) {
        EngraftmentSite engraftmentSite = (EngraftmentSite) getDomainObject(ENGRAFTMENT_SITE_KEY, engraftmentSiteName);
        if (engraftmentSite == null) {
            engraftmentSite = dataImportService.getImplantationSite(engraftmentSiteName);
            addDomainObject(ENGRAFTMENT_SITE_KEY, engraftmentSiteName, engraftmentSite);
        }
        return engraftmentSite;
    }

    private HostStrain getOrCreateHostStrain(String hostStrainName, String hostStrainNomenclature, int rowNumber) {
        HostStrain hostStrain = (HostStrain) getDomainObject(HOST_STRAIN_KEY, hostStrainNomenclature);
        if (hostStrain == null) {
            try {
                hostStrain = dataImportService.getHostStrain(hostStrainName, hostStrainNomenclature, "", "");
                addDomainObject(HOST_STRAIN_KEY, hostStrainNomenclature, hostStrain);
            }
            catch(Exception e){
                log.error("Host strain symbol is empty in row {}", rowNumber);
            }
        }
        return hostStrain;
    }

    private Sample createPatientSample(Row row){

        String diagnosis = row.getString(TSV.Metadata.diagnosis.name());
        String sampleId = row.getString(TSV.Metadata.sample_id.name());
        String tumorTypeName = row.getString(TSV.Metadata.tumour_type.name());
        String primarySiteName = row.getString(TSV.Metadata.primary_site.name());
        String collectionSiteName = row.getString(TSV.Metadata.collection_site.name());
        String stage = row.getString(TSV.Metadata.stage.name());
        String stagingSystem = row.getString(TSV.Metadata.staging_system.name());
        String grade = row.getString(TSV.Metadata.grade.name());
        String gradingSystem = row.getString(TSV.Metadata.grading_system.name());

        Tissue primarySite = getOrCreateTissue(primarySiteName);
        Tissue collectionSite = getOrCreateTissue(collectionSiteName);
        TumorType tumorType = getOrCreateTumorType(tumorTypeName);

        Sample sample = new Sample();
        sample.setType(tumorType);
        sample.setSampleSite(collectionSite);
        sample.setOriginTissue(primarySite);

        sample.setSourceSampleId(sampleId);
        sample.setDiagnosis(diagnosis);
        sample.setStage(stage);
        sample.setStageClassification(stagingSystem);
        sample.setGrade(grade);
        sample.setGradeClassification(gradingSystem);

        return sample;
    }

    private TumorType getOrCreateTumorType(String tumorTypeName) {
        TumorType tumorType = (TumorType) getDomainObject(TUMOR_TYPE_KEY, tumorTypeName);
        if (tumorType == null) {
            tumorType = dataImportService.getTumorType(tumorTypeName);
            addDomainObject(TUMOR_TYPE_KEY, tumorTypeName, tumorType);
        }
        return tumorType;
    }

    private Tissue getOrCreateTissue(String siteName) {
        Tissue primarySite = (Tissue) getDomainObject(TISSUE_KEY, siteName);
        if (primarySite == null) {
            primarySite = dataImportService.getTissue(siteName);
            addDomainObject(TISSUE_KEY, siteName, primarySite);
        }
        return primarySite;
    }

    private void persistNodes(){

        Map<String, Object> patients = domainObjects.get(PATIENT_KEY);
        for (Object patient : patients.values()) {
            dataImportService.savePatient((Patient) patient);
        }

        Map<String, Object> models = domainObjects.get(MODEL_KEY);
        for (Object model : models.values()) {
            dataImportService.saveModelCreation((ModelCreation) model);
        }
    }

    public void addDomainObject(String key1, String key2, Object object){

        if (domainObjects.containsKey(key1)) {
            domainObjects.get(key1).put(key2, object);
        }
        else {
            Map map = new HashMap();
            map.put(key2,object);
            domainObjects.put(key1, map);
        }
    }

    public Object getDomainObject(String key1, String key2){

        if (containsBothKeys(key1, key2)) {
            return domainObjects.get(key1).get(key2);
        }
        return null;
    }

    private boolean containsBothKeys(String key1, String key2) {
        return domainObjects.containsKey(key1) && domainObjects.get(key1).containsKey(key2);
    }

}
