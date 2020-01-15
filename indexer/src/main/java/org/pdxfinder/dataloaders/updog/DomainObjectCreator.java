package org.pdxfinder.dataloaders.updog;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.*;

public class DomainObjectCreator {

    private Map<String, Table> pdxDataTables;
    //nodeType=>ID=>NodeObject
    private Map<String, Map<String, Object>> domainObjects;
    private UtilityService utilityService;
    private DataImportService dataImportService;


    private String patientKey = "patient";
    private String providerKey = "provider_group";
    private String modelKey = "model";
    private String tumorTypeKey = "tumor_type";
    private String tissueKey = "tissue";

    public DomainObjectCreator(DataImportService dataImportService, UtilityService utilityService,
                               Map<String, Table> pdxDataTables) {
        this.dataImportService = dataImportService;
        this.utilityService = utilityService;
        this.pdxDataTables = pdxDataTables;
        domainObjects = new HashMap<>();
    }


    public void loadDomainObjects(){


        //: Do not change the order of these unless you want to risk 1. the universe to collapse OR 2. missing nodes in the db
        loadProvider();
        loadPatientData();
        loadModelData();
        loadSampleData();

    }


    private void addToDomainObjects(String key1, String key2, Object object){

        if(domainObjects.containsKey(key1)){

            domainObjects.get(key1).put(key2, object);
        }
        else{

            Map map = new HashMap();
            map.put(key2,object);
            domainObjects.put(key1, map);
        }
    }

    private void loadProvider(){

        Table finderRelatedTable = pdxDataTables.get("metadata-loader.tsv");
        Row row = finderRelatedTable.row(5);
        Group providerGroup = dataImportService.getProviderGroup(row.getString(TSV.name.name()),
                row.getString(TSV.abbreviation.name()), "", "", "",
                row.getString(TSV.internal_url.name()));

        addToDomainObjects(providerKey, null, providerGroup);
    }


    private void loadPatientData() {

        Table patientTable = pdxDataTables.get("metadata-patient.tsv");
        int rowCount = patientTable.rowCount();

        //start this from 1, row 0 is the header
        for (int i = 1; i < rowCount; i++) {

            if (i < 4) continue;

            Row row = patientTable.row(i);

            Patient patient = dataImportService.createPatient(row.getText(TSV.patient_id.name()),
                    (Group) getExistingDomainObject(TSV.provider_group.name(), null), row.getText(TSV.sex.name()),
                    "", row.getText(TSV.ethnicity.name()));
            patient.setCancerRelevantHistory(row.getText(TSV.history.name()));
            patient.setFirstDiagnosis(row.getText(TSV.initial_diagnosis.name()));
            patient.setAgeAtFirstDiagnosis(row.getText(TSV.age_at_initial_diagnosis.name()));

            addToDomainObjects(patientKey, row.getText(TSV.patient_id.name()), dataImportService.savePatient(patient));

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
            String sampleId = row.getString(TSV.sample_id.name());
            String modelId = row.getString(TSV.model_id.name());
            String dateOfCollection = row.getString(TSV.collection_date.name());
            String ageAtCollection = row.getString(TSV.age_in_years_at_collection.name());
            String collectionEvent = row.getString(TSV.collection_event.name());
            String elapsedTime = row.getString(TSV.months_since_collection_1.name());
            String diagnosis = row.getString(TSV.diagnosis.name());


            String tumorTypeName = row.getString(TSV.tumour_type.name());
            String primarySiteName = row.getString(TSV.primary_site.name());
            String collectionSiteName = row.getString(TSV.collection_site.name());

            String stage = row.getString(TSV.stage.name());
            String stagingSystem = row.getString(TSV.staging_system.name());
            String grade = row.getString(TSV.grade.name());
            String gradingSystem = row.getString(TSV.grading_system.name());
            String virologyStatus = row.getString(TSV.virology_status.name());
            String sharable = row.getString(TSV.sharable.name());
            String treatmentNaive = row.getString(TSV.treatment_naive_at_collection.name());
            String treated = row.getString(TSV.treated.name());
            String priorTreatment = row.getString(TSV.prior_treatment.name());


            Patient patient = (Patient) getExistingDomainObject(patientKey, patientId);

            PatientSnapshot patientSnapshot = patient.getSnapShotByCollection(ageAtCollection, dateOfCollection, collectionEvent, elapsedTime);

            if(patientSnapshot == null){

                patientSnapshot = new PatientSnapshot(patient, ageAtCollection, dateOfCollection, collectionEvent, elapsedTime);
                patientSnapshot.setVirologyStatus(virologyStatus);
                patientSnapshot.setTreatmentNaive(treatmentNaive);
                patient.addSnapshot(patientSnapshot);
            }

            Tissue primarySite = (Tissue) getExistingDomainObject(tissueKey, primarySiteName);

            if(primarySite == null){

                primarySite = dataImportService.getTissue(primarySiteName);
                addToDomainObjects(tissueKey, primarySiteName, primarySite);
            }

            Tissue collectionSite = (Tissue) getExistingDomainObject(tissueKey, collectionSiteName);

            if(collectionSite == null){

                collectionSite = dataImportService.getTissue(collectionSiteName);
                addToDomainObjects(tissueKey, collectionSiteName, collectionSite);
            }

            TumorType tumorType = (TumorType) getExistingDomainObject(tumorTypeKey, tumorTypeName);

            if(tumorType == null){

                tumorType = dataImportService.getTumorType(tumorTypeName);
                addToDomainObjects(tumorTypeKey, tumorTypeName, tumorType);

            }


            Sample sample = new Sample();
            sample.setSourceSampleId(sampleId);
            sample.setDiagnosis(diagnosis);
            sample.setStage(stage);
            sample.setStageClassification(stagingSystem);
            sample.setGrade(grade);
            sample.setGradeClassification(gradingSystem);

            patientSnapshot.addSample(sample);

            ModelCreation modelCreation = (ModelCreation) getExistingDomainObject(modelKey, modelId);

            modelCreation.setSample(sample);
            modelCreation.addRelatedSample(sample);

        }


    }


    private void loadModelData(){

        Table modelTable = pdxDataTables.get("metadata-model.tsv");
        int rowCount = modelTable.rowCount();

        //start this from 1, row 0 is the header
        for (int i = 1; i < rowCount; i++) {

            if (i < 4) continue;

            Row row = modelTable.row(i);

            String modelId = row.getString(TSV.model_id.name());
            String hostStrainName = row.getString(TSV.host_strain.name());
            String hostStrainNomenclature = row.getString(TSV.host_strain_full.name());
            String engraftmentSiteName = row.getString(TSV.engraftment_site.name());
            String engraftmentTypeName = row.getString(TSV.engraftment_type.name());
            String sampleType = row.getString(TSV.sample_type.name());
            String sampleState = row.getString(TSV.sample_state.name());
            String passageNum = row.getString(TSV.passage_number.name());
            String publications = row.getString(TSV.publications.name());

        }


    }


    private Object getExistingDomainObject(String key1,  String key2){

        return domainObjects.get(key1).get(key2);
    }

}
