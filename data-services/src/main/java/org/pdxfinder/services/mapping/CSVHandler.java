package org.pdxfinder.services.mapping;

import org.apache.commons.lang3.StringUtils;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.services.MappingService;
import org.pdxfinder.services.UtilityService;
import org.springframework.stereotype.Service;

import java.util.*;

/*
 * Created by abayomi on 22/08/2019.
 */
@Service
public class CSVHandler {

    private UtilityService utilityService;
    private MappingService mappingService;


    public CSVHandler(UtilityService utilityService, MappingService mappingService) {
        this.utilityService = utilityService;
        this.mappingService = mappingService;
    }


    /**
     * Validates CSV column headers and contents, confirm each row data based on entityId and mappingKey
     * Capture failed rows and return report accordingly then Halt the process if failure
     *
     * @param csvData {List<Map>} holding serialized csv contents
     * @return List of errors if any
     */
    public List validateUploadedCSV(List<Map<String, String>> csvData) {

        List<String> report = new ArrayList<>();

        /*
         * Validation 1: Ensure csv data is not an empty, if empty return error report
         */
        if (csvData.size() < 1 ) {

            report.add("Empty File Data");
            return report;
        }


        /*
         * Validation 2: Validate the CSV header for the correct entity type
         * Get Column head using one of the row data
         */
        Map<String, String> oneData = csvData.get(0);
        Integer firstDataId = Integer.parseInt(oneData.get(CSV.entityId.get()));


        List<String> incomingCSVHead = new ArrayList<>();

        oneData.forEach((key, value)-> incomingCSVHead.add(key) );

        // Get Expected CSV header for this entity type
        MappingEntity disEntity = mappingService.getMappingEntityById(firstDataId);
        List<String> expectedCSVHead = getMappingEntityCSVHead(disEntity);


        // If discrepancy occurs, add expected csv head into report
        if (!expectedCSVHead.equals(incomingCSVHead)) {

            report.add(expectedCSVHead.toString());
            return report;
        }



        /*
         *  Iterate all rows to Validate each data based on existing entityId and mappingKey
         *  Capture failed rows and return report accordingly
         */
        String entityType = disEntity.getEntityType();

        for (Map<String, String> eachData : csvData) {


            String decision = eachData.get(CSV.decision.get()).toLowerCase();
            String validTerm = eachData.get(CSV.validTerm.get());
            String dataId = eachData.get(CSV.entityId.get());

            // Validation 3: Confirm that decision column is filled
            boolean decisionFound =
                    Arrays.asList(CSV.yes.get(), CSV.no.get())
                            .stream()
                            .anyMatch(s -> decision.contains(s));
            if (!decisionFound) {

                report.add("Decision is empty on row with "+ CSV.entityId.get()+" "+dataId);
            }


            // Validation 4: Ensure that when decision column is NO, a new term is supplied in the valid term column
            if ((decision.equalsIgnoreCase(CSV.no.get()) && StringUtils.isEmpty(validTerm))) {

                report.add("Please supply "+CSV.validTerm.get()+" on row with "+ CSV.entityId.get()+" "+dataId);
            }


            // Validation 5: Validate uploaded mapped term url


            // Validation 6: Ensure that the data in each row originated from pdxfinder
            Long entityId = Long.parseLong(dataId);

            String mappingKey = getMappingKeyFromCSVData(entityType, eachData);

            Optional<MappingEntity> me = mappingService.getByMappingKeyAndEntityId(mappingKey, entityId);

            if (!me.isPresent()) {

                report.add("Row with "+ CSV.entityId.get()+" "+dataId +" is not valid");
            }

        }

        return report;
    }



    public List<List<String>> prepareMappingEntityForCSV(List<MappingEntity> mappingEntities){

        List<List<String>> mappingDataCSV = new ArrayList<>();

        mappingEntities.forEach(mappingEntity -> {

            List<String> csvData = new ArrayList<>();

            csvData.add(mappingEntity.getEntityId().toString());

            for (Map.Entry<String, String> entry : mappingEntity.getMappingValues().entrySet() ) {
                csvData.add(
                        entry.getKey().equals("DataSource") ? entry.getValue().toUpperCase() : entry.getValue()
                );
            }

            csvData.add(mappingEntity.getMappedTermLabel());
            csvData.add(mappingEntity.getMappedTermUrl());
            csvData.add(mappingEntity.getMapType());
            csvData.add(mappingEntity.getJustification());
            csvData.add(" ");
            csvData.add(" ");

            mappingDataCSV.add(csvData);
        });

        return mappingDataCSV;
    }




    public List<String> getMappingEntityCSVHead(MappingEntity mappingEntity) {

        List<String> csvHead = new ArrayList<>();

        csvHead.add(CSV.entityId.get());

        for (Map.Entry<String, String> entry : mappingEntity.getMappingValues().entrySet()) {
            csvHead.add(utilityService.camelCaseToSentence(entry.getKey()));
        }

        csvHead.addAll(Arrays.asList(
                CSV.mappedTerm.get(),
                CSV.mappedTermUrl.get(),
                CSV.mapType.get(),
                CSV.justification.get(),
                CSV.decision.get(),
                CSV.validTerm.get())
        );

        return csvHead;
    }



    public String getMappingKeyFromCSVData(String entityType, Map<String, String> data){

        String mappingKey = "";

        if (entityType.equals(MappingEntityType.diagnosis.get())) {

            mappingKey = String.join("__",
                                     entityType,
                                     data.get(CSV.dataSource.get()),
                                     data.get(CSV.sampleDiagnosis.get()),
                                     data.get(CSV.originTissue.get()),
                                     data.get(CSV.tumorType.get())

            ).toLowerCase().replaceAll("[^a-zA-Z0-9 _-]", "");

        }
        else if (entityType.equals(MappingEntityType.treatment.get())) {

            mappingKey = String.join("__",
                                     entityType,
                                     data.get(CSV.dataSource.get()),
                                     data.get(CSV.treatmentName.get())

            ).toLowerCase().replaceAll("[^a-zA-Z0-9 _-]", "");

        }


        return mappingKey;
    }
}
