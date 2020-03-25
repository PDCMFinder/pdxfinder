package org.pdxfinder.utils;

import org.pdxfinder.dataexport.UniversalDataExporter;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.services.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CbpTransformer {

    @Autowired
    private UtilityService utilityService;
    private UniversalDataExporter universalDataExporter = new UniversalDataExporter();

    public enum cbioType {
        MUT,
        GISTIC
    };

    public void exportCBP(File exportDir,File templateDir, File pathToJson, cbioType dataType) throws IOException {

        if (doesFileNotExist(exportDir) || doesFileNotExist(templateDir) || doesFileNotExist(pathToJson)) {
            throw new IOException("A string argument passed to the exportCBP does not point to an existing file.");
        }
            Group jsonGroup = createGroupWithJsonsFilename(pathToJson.getAbsolutePath());

            List<Map<String, Object>> listMapTable = utilityService.serializeJSONToMaps(pathToJson.getAbsolutePath());
            CbpMapsToSheetsByDataType(listMapTable, dataType);

            universalDataExporter.setDs(jsonGroup);
            universalDataExporter.setTemplateDir(templateDir.getAbsolutePath());
            universalDataExporter.export(exportDir.getAbsolutePath());
    }

    private void CbpMapsToSheetsByDataType(List<Map<String, Object>> listMapTable, cbioType dataType){

        List<List<String>> sheet;
        if(dataType.equals(cbioType.MUT)){
            sheet = CbpMutJsonMapsToSheet(listMapTable);
            universalDataExporter.setMutationSheetDataExport(sheet);
        }
       else if(dataType.equals(cbioType.GISTIC)) {
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
    private boolean doesFileNotExist(File file){
        return file == null || !file.exists();
    }

}
