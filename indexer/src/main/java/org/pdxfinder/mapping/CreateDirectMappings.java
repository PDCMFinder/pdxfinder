package org.pdxfinder.mapping;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.services.mapping.MappingContainer;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.graph.queryresults.TreatmentMappingData;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/*
 * Created by csaba on 18/07/2019.
 */
@Component
public class CreateDirectMappings implements CommandLineRunner{

    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private MappingService mappingService;

    private Logger log = LoggerFactory.getLogger(CreateDirectMappings.class);


    private MappingContainer container;

    @Autowired
    public CreateDirectMappings(DataImportService dataImportService, MappingService mappingService) {
        this.dataImportService = dataImportService;
        this.mappingService = mappingService;
    }



    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("createDirectMappings", "Creating direct mappings");
        OptionSet options = parser.parse(args);

        if (options.has("createDirectMappings")) {

            createDirectMappings();
            printJson();
        }
    }




    private void createDirectMappings(){

        container = new MappingContainer();


        //get the unmapped treatments
        TreatmentMappingData result = dataImportService.getUnmappedPatientTreatments();
        int counter = 28;

        int treatmentSize = result.abbrAndTreatmentName.size();

        for(int i=0; i<treatmentSize; i++){

            System.out.println(result.abbrAndTreatmentName.get(i) );

            String[] treatmentDSCombo = result.abbrAndTreatmentName.get(i).split("___");
            String ds = treatmentDSCombo[0];
            String treatmentName = treatmentDSCombo[1];

            OntologyTerm ot = dataImportService.findOntologyTermByLabelAndType(treatmentName, "treatment");

            if(ot != null){

                MappingEntity mappingEntity = new MappingEntity();

                mappingEntity.setEntityId(new Long(counter));
                mappingEntity.setEntityType("TREATMENT");
                mappingEntity.setMappingLabels(mappingService.getTreatmentMappingLabels());

                Map<String, String> mappingValues = new HashMap<>();
                mappingValues.put("DataSource", ds);
                mappingValues.put("TreatmentName", treatmentName);
                mappingEntity.setMappingValues(mappingValues);

                mappingEntity.setMappedTermLabel(ot.getLabel());
                mappingEntity.setMappedTermUrl(ot.getUrl());

                mappingEntity.setMapType("direct");
                mappingEntity.setJustification("0");
                mappingEntity.setStatus("Created");

                mappingEntity.setSuggestedMappings(new ArrayList<>());
                mappingEntity.setMappingKey(mappingEntity.generateMappingKey());

                container.addEntity(mappingEntity);

                counter++;
                log.info("Added "+counter);
            }
        }
    }

    private void printJson(){



        System.out.println(container.getEntityList());

    }




}
