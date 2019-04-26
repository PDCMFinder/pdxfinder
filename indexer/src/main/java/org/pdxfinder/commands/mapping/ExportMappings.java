package org.pdxfinder.commands.mapping;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.services.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sun.tools.jar.CommandLine;

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



    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("exportMappings", "Exporting mapping rules to a file");
        OptionSet options = parser.parse(args);

        if (options.has("exportMappings")) {

            exportMappings();

        }
    }




    private void exportMappings(){


        String fileName = "/Users/csaba/Downloads/saved_mappings.json";

        List<String> dataSourcesToExport = new ArrayList<>(Arrays.asList("TRACE", "IRCC-CRC"));

        mappingService.saveMappingsToFile(fileName, mappingService.getDiagnosisMappingsByDS(dataSourcesToExport).getMappings().values());



    }

}
