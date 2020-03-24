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

import java.io.File;
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
    private static  enum omicType {
        MUT,
        GISTIC
    };

    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    @Override
    public void run(String... args) throws IOException {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("TRANSFORM_CBP", "Ingest CBP omic JSONs and converts to PdxFinder templates");
        parser.accepts("MUT", "Pares MUT data");
        parser.accepts("CNA", "parses CNA data");
        OptionSet options = parser.parse(args);

        if (options.has(TRANSFORM_CBP) && args.length > 1) {
            log.info("Exporting data from {}", args[2]);
            String templateDir = finderRootDir + "/template";
            String exportDir = finderRootDir + "/export";
            if (options.has(omicType.MUT.name())) {
                exportCBP(exportDir, templateDir, args[2], omicType.MUT.name());
            }
            else if (options.has(omicType.GISTIC.name())){
                exportCBP(exportDir, templateDir, args[2], omicType.GISTIC.name());
            }
        }
    }

    public void exportCBP(String exportDir,String templateDir, String pathToJson, String dataType) throws IOException {

        if (!doesFileExist(exportDir) || !doesFileExist(templateDir) || !doesFileExist(pathToJson)) {
            throw new IOException("A string argument passed to the exportCBP does not point to an existing file.");
        }

            Group jsonGroup = createGroupWithJsonsFilename(pathToJson);

            List<Map<String, Object>> listMapTable = utilityService.serializeJSONToMaps(pathToJson);
            CbpMapsToSheetsByDataType(listMapTable, dataType);

            universalDataExporter.setDs(jsonGroup);
            universalDataExporter.setTemplateDir(templateDir);
            universalDataExporter.export(exportDir);
    }


    private void CbpMapsToSheetsByDataType(List<Map<String, Object>> listMapTable, String dataType){

        List<List<String>> sheet;

        if(dataType.equals(omicType.MUT.name())){
            sheet = CbpMutJsonMapsToSheet(listMapTable);
            universalDataExporter.setMutationSheetDataExport(sheet);
        }
       else if(dataType.equals(omicType.GISTIC.name())) {
            sheet = CbpGisticsonMapsToSheet(listMapTable);
            universalDataExporter.setCnaSheetDataExport(sheet);
        }
    }

    private List<List<String>> CbpMutJsonMapsToSheet(List<Map<String, Object>> jsonMap){

        List<List<String>> sheet = new ArrayList<>();

        jsonMap.forEach(f -> {

            List<String> row = new LinkedList<>();

            row.add(f.get("patientId").toString());
            row.add(f.get("sampleId").toString());
            row.add("Not Specified");
            row.add("Not Specified");
            row.add("Not Specified");
            addBlanksToList(row,10);
            row.add(f.get("chr").toString());
            row.add(f.get("startPosition").toString());
            row.add(f.get("referenceAllele").toString());
            row.add(f.get("variantAllele").toString());
            addBlanksToList(row,6);
            row.add(f.get("ncbiBuild").toString());
            row.add("");

            sheet.add(row);
        });


        return sheet;
    }


    private List<List<String>> CbpGisticsonMapsToSheet(List<Map<String,Object>> jsonMap){

        List<List<String>> sheet = new ArrayList<>();

        jsonMap.forEach(f -> {

            List<String> row = new LinkedList<>();

            row.add(f.get("patientId").toString());
            row.add(f.get("sampleId").toString());
            row.add("Not Specified");
            row.add("Not Specified");
            row.add("Not Specified");
            addBlanksToList(row,6);
            row.add(f.get("entrezGeneId").toString());
            addBlanksToList(row, 3);
            row.add(f.get("alteration").toString());

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
    private boolean doesFileExist(String fileURI){
        return new File(fileURI).exists();
    }

}
