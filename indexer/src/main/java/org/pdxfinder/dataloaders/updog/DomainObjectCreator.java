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
        Group providerGroup = dataImportService.getProviderGroup(row.getString("name"), row.getString("abbreviation"), "", "", "", row.getString("internal_url"));

        addToDomainObjects("provider_group", null, providerGroup);
    }


    private void loadPatientData() {

        Table patientTable = pdxDataTables.get("metadata-patient.tsv");
        int rowCount = patientTable.rowCount();

        //start this from 1, row 0 is the header
        for (int i = 1; i < rowCount; i++) {

            if (i < 4) continue;

            Row row = patientTable.row(i);

            Patient patient = dataImportService.createPatient(row.getText("patient_id"), (Group) getExistingDomainObject("provider_group", null), row.getText("sex"), "", row.getText("ethnicity"));
            patient.setCancerRelevantHistory(row.getText("history"));
            patient.setFirstDiagnosis(row.getText("initial_diagnosis"));
            patient.setAgeAtFirstDiagnosis(row.getText("age_at_initial_diagnosis"));

            addToDomainObjects("patient", row.getText("patient_id"), dataImportService.savePatient(patient));

        }

    }

    private void loadSampleData(){


        Table sampleTable = pdxDataTables.get("metadata-sample.tsv");
        int rowCount = sampleTable.rowCount();

        //start this from 1, row 0 is the header
        for(int i = 1; i < rowCount; i++){

            if(i < 4) continue;

            Row row = sampleTable.row(i);

            String patientId = row.getString("patient_id");
            String sampleId = row.getString("sample_id");
            String modelId = row.getString("model_id");
            String dateOfCollection = row.getString("collection_date");
            String ageAtCollection = row.getString("age_in_years_at_collection");
            String collectionEvent = row.getString("collection_event");
            String elapsedTime = row.getString("months_since_collection_1");
            String diagnosis = row.getString("diagnosis");


            String tumorTypeName = row.getString("tumour_type");
            String primarySiteName = row.getString("primary_site");
            String collectionSiteName = row.getString("collection_site");

            String stage = row.getString("stage");
            String stagingSystem = row.getString("staging_system");
            String grade = row.getString("grade");
            String gradingSystem = row.getString("grading_system");
            String virologyStatus = row.getString("virology_status");
            String sharable = row.getString("sharable");
            String treatmentNaive = row.getString("treatment_naive_at_collection");
            String treated = row.getString("treated");
            String priorTreatment = row.getString("prior_treatment");


            Patient patient = (Patient) getExistingDomainObject("patient", patientId);

            PatientSnapshot patientSnapshot = patient.getSnapShotByCollection(ageAtCollection, dateOfCollection, collectionEvent, elapsedTime);

            if(patientSnapshot == null){

                patientSnapshot = new PatientSnapshot(patient, ageAtCollection, dateOfCollection, collectionEvent, elapsedTime);
                patientSnapshot.setVirologyStatus(virologyStatus);
                patientSnapshot.setTreatmentNaive(treatmentNaive);
                patient.addSnapshot(patientSnapshot);
            }

            Tissue primarySite = (Tissue) getExistingDomainObject("tissue", primarySiteName);

            if(primarySite == null){

                primarySite = dataImportService.getTissue(primarySiteName);
                addToDomainObjects("tissue", primarySiteName, primarySite);
            }

            Tissue collectionSite = (Tissue) getExistingDomainObject("tissue", collectionSiteName);

            if(collectionSite == null){

                collectionSite = dataImportService.getTissue(collectionSiteName);
                addToDomainObjects("tissue", collectionSiteName, collectionSite);
            }

            TumorType tumorType = (TumorType) getExistingDomainObject("tumor_type", tumorTypeName);

            if(tumorType == null){

                tumorType = dataImportService.getTumorType(tumorTypeName);
                addToDomainObjects("tumor_type", tumorTypeName, tumorType);

            }


            Sample sample = new Sample();
            sample.setSourceSampleId(sampleId);
            sample.setDiagnosis(diagnosis);
            sample.setStage(stage);
            sample.setStageClassification(stagingSystem);
            sample.setGrade(grade);
            sample.setGradeClassification(gradingSystem);

            patientSnapshot.addSample(sample);

            ModelCreation modelCreation = (ModelCreation) getExistingDomainObject("model", modelId);

            modelCreation.setSample(sample);
            modelCreation.addRelatedSample(sample);

        }


    }


    private void loadModelData(){


    }


    private Object getExistingDomainObject(String key1,  String key2){

        return domainObjects.get(key1).get(key2);
    }

}
