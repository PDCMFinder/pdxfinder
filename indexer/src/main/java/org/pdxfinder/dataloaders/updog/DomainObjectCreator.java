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

    private String patientKey = "patient";
    private String providerKey = "provider_group";
    private String modelKey = "model";
    private String tumorTypeKey = "tumor_type";
    private String tissueKey = "tissue";
    private String hostStrainKey = "hoststrain";
    private String engraftmentSiteKey = "engraftment_site";
    private String engraftmentTypeKey = "engraftment_type";
    private String engraftmentMaterialKey = "engraftment_material";


    public DomainObjectCreator(DataImportService dataImportService,
                               Map<String, Table> pdxDataTables) {
        this.dataImportService = dataImportService;

        this.pdxDataTables = pdxDataTables;
        domainObjects = new HashMap<>();
    }


    public void loadDomainObjects(){


        //: Do not change the order of these unless you want to risk 1. the universe to collapse OR 2. missing nodes in the db
        loadProvider();
        loadPatientData();
        loadModelData();
        loadSampleData();

        persistNodes();

    }




    private void loadProvider(){

        Table finderRelatedTable = pdxDataTables.get("metadata-loader.tsv");
        Row row = finderRelatedTable.row(4);
        Group providerGroup = dataImportService.getProviderGroup(row.getString(TSV.name.name()),
                row.getString(TSV.abbreviation.name()), "", "", "",
                row.getString(TSV.internal_url.name()));

        addDomainObject(providerKey, null, providerGroup);
    }


    private void loadPatientData() {

        Table patientTable = pdxDataTables.get("metadata-patient.tsv");
        int rowCount = patientTable.rowCount();

        //start this from 1, row 0 is the header
        for (int i = 1; i < rowCount; i++) {

            if (i < 4) continue;

            Row row = patientTable.row(i);

            String patientId = row.getText(TSV.patient_id.name());

            //temporary check to escape empty rows
            //TODO:remove this when validation filters out empty rows
            if(patientId == null || patientId.isEmpty()){
                continue;
            }

            try {
                Patient patient = dataImportService.createPatient(row.getText(TSV.patient_id.name()),
                        (Group) getDomainObject(TSV.provider_group.name(), null), row.getText(TSV.sex.name()),
                        "", row.getText(TSV.ethnicity.name()));
                patient.setCancerRelevantHistory(row.getText(TSV.history.name()));
                patient.setFirstDiagnosis(row.getText(TSV.initial_diagnosis.name()));
                patient.setAgeAtFirstDiagnosis(row.getText(TSV.age_at_initial_diagnosis.name()));

                addDomainObject(patientKey, row.getText(TSV.patient_id.name()), dataImportService.savePatient(patient));
            }
            catch(Exception e){

                log.error("Error loading patient {} at row {}", row.getText(TSV.patient_id.name()), i);
            }

        }

    }

    private void loadSampleData(){


        Table sampleTable = pdxDataTables.get("metadata-sample.tsv");
        int rowCount = sampleTable.rowCount();

        //start this from 1, row 0 is the header
        for(int i = 1; i < rowCount; i++){

            if(i < 4) continue;

            Row row = sampleTable.row(i);

            String patientId = row.getString(TSV.patient_id.name());

            String modelId = row.getString(TSV.model_id.name());
            String dateOfCollection = row.getString(TSV.collection_date.name());
            String ageAtCollection = row.getString(TSV.age_in_years_at_collection.name());
            String collectionEvent = row.getString(TSV.collection_event.name());
            String elapsedTime = row.getString(TSV.months_since_collection_1.name());
            String primarySiteName = row.getString(TSV.primary_site.name());
            String virologyStatus = row.getString(TSV.virology_status.name());
            String treatmentNaive = row.getString(TSV.treatment_naive_at_collection.name());

            //temporary check to escape empty rows
            //TODO:remove this when validation filters out empty rows
            if(primarySiteName == null || primarySiteName.isEmpty()) {

                continue;
            }

            Patient patient = (Patient) getDomainObject(patientKey, patientId);

            if(patient == null) throw new NullPointerException();

            PatientSnapshot patientSnapshot = patient.getSnapShotByCollection(ageAtCollection, dateOfCollection, collectionEvent, elapsedTime);

            if(patientSnapshot == null){

                patientSnapshot = new PatientSnapshot(patient, ageAtCollection, dateOfCollection, collectionEvent, elapsedTime);
                patientSnapshot.setVirologyStatus(virologyStatus);
                patientSnapshot.setTreatmentNaive(treatmentNaive);
                patient.addSnapshot(patientSnapshot);
            }


            Sample sample = createPatientSample(row);

            patientSnapshot.addSample(sample);

            ModelCreation modelCreation = (ModelCreation) getDomainObject(modelKey, modelId);

            if(modelCreation == null) throw new NullPointerException();

            modelCreation.setSample(sample);
            modelCreation.addRelatedSample(sample);

        }


    }


    private void loadModelData(){

        Table modelTable = pdxDataTables.get("metadata-model.tsv");
        int rowCount = modelTable.rowCount();

        Group providerGroup = (Group) domainObjects.get(providerKey).get(null);

        //start this from 1, row 0 is the header
        for (int i = 1; i < rowCount; i++) {

            if (i < 4) continue;

            Row row = modelTable.row(i);

            String modelId = row.getString(TSV.model_id.name());
            String hostStrainNomenclature = row.getString(TSV.host_strain_full.name());
            String passageNum = row.getString(TSV.passage_number.name());
            String publications = row.getString(TSV.publications.name());

            //temporary check to escape empty rows
            //TODO:remove this when validation filters out empty rows
            if(modelId == null || modelId.isEmpty()) continue;

            ModelCreation modelCreation = new ModelCreation();
            modelCreation.setSourcePdxId(modelId);
            modelCreation.setDataSource(providerGroup.getAbbreviation());

            addDomainObject(modelKey,modelId, modelCreation);

            Specimen specimen = modelCreation.getSpecimenByPassageAndHostStrain(passageNum, hostStrainNomenclature);

            if(specimen == null){

                specimen = createSpecimen(row, i);
                modelCreation.addSpecimen(specimen);
                modelCreation.addRelatedSample(specimen.getSample());
            }
        }

        Table modelValidationTable = pdxDataTables.get("metadata-model_validation.tsv");
        rowCount = modelValidationTable.rowCount();

        for (int i = 1; i < rowCount; i++) {

            if (i < 4) continue;

            Row row = modelValidationTable.row(i);

            String modelId = row.getString(TSV.model_id.name());
            String validationTechnique = row.getString(TSV.validation_technique.name());
            String description = row.getString(TSV.description.name());
            String passagesTested = row.getString(TSV.passages_tested.name());
            String hostStrainFull = row.getString(TSV.validation_host_strain_full.name());

            //temporary check to escape empty rows
            //TODO:remove this when validation filters out empty rows
            if(modelId == null || modelId.isEmpty()) continue;

            ModelCreation modelCreation = (ModelCreation) getDomainObject(modelKey, modelId);

            if(modelCreation == null) throw new NullPointerException();

            QualityAssurance qa = new QualityAssurance();
            qa.setTechnology(validationTechnique);
            qa.setDescription(description);
            qa.setPassages(passagesTested);
            qa.setValidationHostStrain(hostStrainFull);

            modelCreation.addQualityAssurance(qa);
        }
    }



    private Specimen createSpecimen(Row row, int rowCounter){

        String hostStrainName = row.getString(TSV.host_strain.name());
        String hostStrainNomenclature = row.getString(TSV.host_strain_full.name());
        String engraftmentSiteName = row.getString(TSV.engraftment_site.name());
        String engraftmentTypeName = row.getString(TSV.engraftment_type.name());
        String sampleType = row.getString(TSV.sample_type.name());
        String passageNum = row.getString(TSV.passage_number.name());

        HostStrain hostStrain = (HostStrain) getDomainObject(hostStrainKey, hostStrainNomenclature);

        if(hostStrain == null){
            try {
                hostStrain = dataImportService.getHostStrain(hostStrainName, hostStrainNomenclature, "", "");
                addDomainObject(hostStrainKey, hostStrainNomenclature, hostStrain);
            }
            catch(Exception e){
                log.error("Host strain symbol is empty in row {}", rowCounter);
            }
        }

        EngraftmentSite engraftmentSite = (EngraftmentSite) getDomainObject(engraftmentSiteKey, engraftmentSiteName);

        if(engraftmentSite == null){

            engraftmentSite = dataImportService.getImplantationSite(engraftmentSiteName);
            addDomainObject(engraftmentSiteKey, engraftmentSiteName, engraftmentSite);
        }

        EngraftmentType engraftmentType = (EngraftmentType) getDomainObject(engraftmentTypeKey, engraftmentTypeName);

        if(engraftmentType == null){

            engraftmentType = dataImportService.getImplantationType(engraftmentTypeName);
            addDomainObject(engraftmentTypeKey, engraftmentTypeName, engraftmentType);
        }

        EngraftmentMaterial engraftmentMaterial = (EngraftmentMaterial) getDomainObject(engraftmentMaterialKey, sampleType);

        if(engraftmentMaterial == null){

            engraftmentMaterial = dataImportService.getEngraftmentMaterial(sampleType);
            addDomainObject(engraftmentMaterialKey, sampleType, engraftmentMaterial);
        }

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

    private Sample createPatientSample(Row row){

        String diagnosis = row.getString(TSV.diagnosis.name());
        String sampleId = row.getString(TSV.sample_id.name());
        String tumorTypeName = row.getString(TSV.tumour_type.name());
        String primarySiteName = row.getString(TSV.primary_site.name());
        String collectionSiteName = row.getString(TSV.collection_site.name());
        String stage = row.getString(TSV.stage.name());
        String stagingSystem = row.getString(TSV.staging_system.name());
        String grade = row.getString(TSV.grade.name());
        String gradingSystem = row.getString(TSV.grading_system.name());

        Tissue primarySite = (Tissue) getDomainObject(tissueKey, primarySiteName);

        if(primarySite == null){

            primarySite = dataImportService.getTissue(primarySiteName);
            addDomainObject(tissueKey, primarySiteName, primarySite);
        }

        Tissue collectionSite = (Tissue) getDomainObject(tissueKey, collectionSiteName);

        if(collectionSite == null){

            collectionSite = dataImportService.getTissue(collectionSiteName);
            addDomainObject(tissueKey, collectionSiteName, collectionSite);
        }

        TumorType tumorType = (TumorType) getDomainObject(tumorTypeKey, tumorTypeName);

        if(tumorType == null){

            tumorType = dataImportService.getTumorType(tumorTypeName);
            addDomainObject(tumorTypeKey, tumorTypeName, tumorType);

        }

        Sample sample = new Sample();
        sample.setSourceSampleId(sampleId);
        sample.setDiagnosis(diagnosis);
        sample.setStage(stage);
        sample.setStageClassification(stagingSystem);
        sample.setGrade(grade);
        sample.setGradeClassification(gradingSystem);

        return sample;
    }



    private void persistNodes(){


        Map<String, Object> patients = domainObjects.get(patientKey);

        for(Object value : patients.values()){

            dataImportService.savePatient((Patient) value);

        }

        Map<String, Object> models = domainObjects.get(modelKey);

        for(Object value : models.values()){

            dataImportService.saveModelCreation((ModelCreation) value);

        }

    }


    private void addDomainObject(String key1, String key2, Object object){

        if(domainObjects.containsKey(key1)){

            domainObjects.get(key1).put(key2, object);
        }
        else{

            Map map = new HashMap();
            map.put(key2,object);
            domainObjects.put(key1, map);
        }
    }

    private Object getDomainObject(String key1, String key2){

        if(domainObjects.containsKey(key1) && domainObjects.get(key1).containsKey(key2)){

            return domainObjects.get(key1).get(key2);
        }

        return null;
    }

}
