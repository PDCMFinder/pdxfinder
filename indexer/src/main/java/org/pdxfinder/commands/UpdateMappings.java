package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLine;
import org.pdxfinder.admin.pojos.MappingContainer;
import org.pdxfinder.admin.pojos.MappingEntity;
import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.repositories.SampleRepository;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * Created by csaba on 16/11/2018.
 */
@Component
@Order(value = 0)
public class UpdateMappings implements CommandLineRunner {


    private final static Logger log = LoggerFactory.getLogger(UpdateMappings.class);

    private SampleRepository sampleRepository;
    private DataImportService dataImportService;


    @Value("${mappings.diagnosis.file}")
    private String savedDiagnosisMappingsFile;

    @Value("${mappings.diagnosis.file2}")
    private String savedDiagnosisMappingsFile2;

    @Autowired
    public UpdateMappings(SampleRepository sampleRepository, DataImportService dataImportService) {
        this.sampleRepository = sampleRepository;
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {


        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("updateMappings", "Updating mapping labels");

        OptionSet options = parser.parse(args);


        if (options.has("updateMappings")) {

            log.info("Updating mappings");

            updateMappings();
        }
    }





    private void updateMappings(){

        MappingService mappingService = new MappingService(sampleRepository);

        mappingService.setSavedDiagnosisMappingsFile(savedDiagnosisMappingsFile);
        MappingContainer mcont = mappingService.getSavedDiagnosisMappings(null);

        TreeMap<Long, MappingEntity> mappings = mcont.getMappings();

        for(Map.Entry<Long,MappingEntity> entry: mappings.entrySet()){

            MappingEntity me = entry.getValue();
            OntologyTerm ot = dataImportService.findOntologyTermByLabel(me.getMappedTermLabel());
            if(ot != null){
                me.setMappedTermUrl(ot.getUrl());
                //log.info("Updating "+me.getMappedTermLabel());
            }
            else{
                log.error("Term not found for: "+me.getMappedTermLabel());
            }
        }

        String stop = null;

        log.info("Saving file");
        //save mappings to file
        //temporary solution so we don't overwrite existing file
        mappingService.saveMappingsToFile(savedDiagnosisMappingsFile2, (Collection<MappingEntity>) mappings);

    }
}
