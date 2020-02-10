package org.pdxfinder.utils;

import com.fasterxml.jackson.databind.JsonNode;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dataexport.UniversalDataExporter;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class CbpTransformer implements CommandLineRunner {

    @Autowired
    private UtilityService utilityService;

    private UniversalDataExporter universalDataExporter = new UniversalDataExporter();

    private static final Logger log = LoggerFactory.getLogger(CbpTransformer.class);
    private static final String TRANSFORM_CBP = "transform_CBP";

    @Override
    public void run(String... args) throws IOException {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("TRANSFORM_CBP", "Ingest mut JSON and converts to templates");


        OptionSet options = parser.parse(args);
        long startTime = System.currentTimeMillis();

        if (options.has(TRANSFORM_CBP) && args.length > 1) {

            log.info("Exporting data from {}", args[1]);
            exportCBP(args[1], args[2]);

        }
    }

    public void exportCBP(String templateDir, String pathToJson) throws IOException {

       //   Map<String,Map<String,Object>> = utilityService.serializeJSONToMaps(templateDir);
      //    Map<String,String> CbpOmicVariables = parseCBPmut(jsonNode);
       // List<List<String>> Omicsheet = createOmicSheet(CbpOmicVariables);
   //     universalDataExporter.setMutationSheetDataExport(Omicsheet);
     //   universalDataExporter.export(templateDir);

    }



}
