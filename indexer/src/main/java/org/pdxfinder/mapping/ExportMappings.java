package org.pdxfinder.mapping;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.services.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Created by csaba on 24/04/2019.
 */
@Component
public class ExportMappings implements CommandLineRunner {

    @Autowired
    MappingService mappingService;
    Logger logger = LoggerFactory.getLogger(ExportMappings.class);

    @Value("${data-dir}")
    private String finderRootDir;

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("exportEuroPdxMappings", "Exporting mapping rules to a file");
        OptionSet options = parser.parse(args);

        if (options.has("exportEuroPdxMappings")) {
            exportEuroPdxMappings();
        }
    }

    private void exportEuroPdxMappings(){

        String diagnosisFileName = String.format("%s/mappings_out/diagnosis_mappings.json", finderRootDir);
        String treatmentFileName = String.format("%s/mappings_out/treatment_mappings.json", finderRootDir);




        List<String> dataSourcesToExport = new ArrayList<>(Arrays.asList(
            "Curie-BC",
            "Curie-LC",
            "Curie-OC",
            "IRCC-CRC",
            "IRCC-GC",
            "TRACE",
            "UOC-BC",
            "UOM-BC",
            "VHIO-BC",
            "VHIO-CRC"
        ));
        logger.info("Exporting diagnosis mappings to :"+diagnosisFileName);
        mappingService.saveMappingsToFile(
            diagnosisFileName,
            mappingService.getMappingsByDSAndType(dataSourcesToExport, "diagnosis").getEntityList()
        );
        logger.info("Exporting treatment mappings to "+treatmentFileName);
        mappingService.saveMappingsToFile(
            treatmentFileName,
            mappingService.getMappingsByDSAndType(dataSourcesToExport, "treatment").getEntityList()
        );

    }

}
