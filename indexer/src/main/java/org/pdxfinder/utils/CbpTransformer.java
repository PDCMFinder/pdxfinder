package org.pdxfinder.utils;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.pdxfinder.dataexport.UniversalDataExporter;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class CbpTransformer implements CommandLineRunner {

    private UtilityService utilityService = new UtilityService();

    private UniversalDataExporter universalDataExporter = new UniversalDataExporter();

    private static final Logger log = LoggerFactory.getLogger(CbpTransformer.class);
    private static final String TRANSFORM_CBP = "TRANSFORM_CBP";

    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    @Override
    public void run(String... args) throws IOException {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("TRANSFORM_CBP", "Ingest mut JSON and converts to templates");

        String exportDir = finderRootDir + "/export/";

        OptionSet options = parser.parse(args);
        long startTime = System.currentTimeMillis();

        if (options.has(TRANSFORM_CBP) && args.length > 1) {

            log.info("Exporting data from {}", args[1]);
            exportCBP(exportDir,finderRootDir, args[1]);

        }
    }

    public void exportCBP(String exportDir,String templateDir, String pathToJson) throws IOException {

         Group jsonGroup = createGroupWithJsonsFilename(pathToJson);

         List<Map<String,Object>> listMapTable = utilityService.serializeJSONToMaps(pathToJson);
         List<List<String>> CbpMutLists = CbpMutJsonMapsToSheet(listMapTable);

         universalDataExporter.setDs(jsonGroup);
         universalDataExporter.setMutationSheetDataExport(CbpMutLists);
         universalDataExporter.setTemplateDir(templateDir);
         universalDataExporter.export(exportDir);

    }

    private List<List<String>> CbpMutJsonMapsToSheet(List<Map<String,Object>> jsonMap){

        List<List<String>> sheet = new ArrayList<>();
        List<String> row = new LinkedList<>();

        jsonMap.forEach(f -> {

            row.add(f.get("patientId").toString());
            row.add(f.get("sampleId").toString());
            row.add("Not Specified");
            row.add("Not Specified");
            row.add("Not Specified");
            addBlanksToList(row,13);
            row.add("chr");
            row.add("startPosition");
            row.add("referenceAllele");
            row.add("variantAllele");
            addBlanksToList(row,6);
            row.add("ncbiBuild");
            row.add("");

            sheet.add(row);
        });


        return sheet;
    }

    private Group createGroupWithJsonsFilename(String pathToJson) {

        Path json = Paths.get(pathToJson);
        int nameIndex = json.getNameCount() - 1;
        String jsonAbbreviation = json.getName(nameIndex).toString();

        Group jsonDs = new Group();
        jsonDs.setAbbreviation(jsonAbbreviation);

        return jsonDs;
    }

    private void addBlanksToList(List<String> row, int blanks){

        for(int i = 0; i < blanks; i++){
            row.add("");
        }
    }



}
