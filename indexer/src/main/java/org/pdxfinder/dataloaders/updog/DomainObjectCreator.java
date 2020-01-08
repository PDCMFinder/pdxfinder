package org.pdxfinder.dataloaders.updog;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.Patient;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ds.Standardizer;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.*;

public class DomainObjectCreator {

    private Map<String, Table> pdxDataTables;
    private Map<String, List<Object>> domainObjects;
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

        loadProvider();
        loadPatientData();


    }


    private void addToDomainObjects(String key, Object object){

        if(domainObjects.containsKey(key)){

            domainObjects.get(key).add(object);
        }
        else{

            List l = new ArrayList();
            l.add(object);
            domainObjects.put(key, l);
        }
    }

    private void loadProvider(){

        Table finderRelatedTable = pdxDataTables.get("metadata-loader.tsv");
        Row row = finderRelatedTable.row(5);

        Group providerGroup = dataImportService.getProviderGroup(row.getString(1), row.getString(2), "", "", "", row.getString(3));
        List<Object> groupList = new ArrayList<>();
        groupList.add(providerGroup);

        domainObjects.put("groups",groupList);

    }


    private void loadPatientData(){
        System.out.println(pdxDataTables.keySet().toString());
        Table patientTable = pdxDataTables.get("metadata-patient.tsv");
        int rowCount = patientTable.rowCount();

        //start this from 1, row 0 is the header
        for(int i = 1; i < rowCount; i++){

            if(i < 4) continue;

            Row row = patientTable.row(i);
            System.out.println(row.getText(1));

            Patient patient = dataImportService.createPatient(row.getText(1), (Group) domainObjects.get("groups").get(0), row.getText(2), "", row.getText(4));
            patient.setCancerRelevantHistory(row.getText(3));
            patient.setFirstDiagnosis(row.getText(6));
            patient.setAgeAtFirstDiagnosis(row.getText(7));

            addToDomainObjects("patient", dataImportService.savePatient(patient));

        }

    }




}
