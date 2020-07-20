package org.pdxfinder.utils;


import org.apache.poi.ss.usermodel.Sheet;
import org.pdxfinder.TSV;
import org.pdxfinder.dataexport.ExporterTemplates;
import org.pdxfinder.dataexport.UniversalDataWriterUtilities;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.services.OmicTransformationService;
import org.pdxfinder.services.UtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CbpTransformer {

    private static final Logger log = LoggerFactory.getLogger(CbpTransformer.class);

    private UtilityService utilityService;
    private OmicTransformationService omicTransformationService;
    private UniversalDataWriterUtilities universalDataWriterUtilities;

    CbpTransformer(UtilityService utilityService, OmicTransformationService omicTransformationService,
                   UniversalDataWriterUtilities universalDataWriterUtilities){
        this.utilityService = utilityService;
        this.omicTransformationService = omicTransformationService;
        this.universalDataWriterUtilities = universalDataWriterUtilities;
    }
    private static String notSpecified = "Not Specified";
    private static String patientId = "patientId";
    private static String sampleId = "sampleId";
    private static String entrezGeneId = "EntrezGeneId";
    private static String mutFileId = TSV.molecular_characterisation_type.mut.name();
    private static String cnaFileId = TSV.molecular_characterisation_type.cna.name();
    public enum cbioType {
        MUT,
        GISTIC
    }

    public void exportCBP(File exportDir,File templateDir, File pathToJson, cbioType dataType) throws IOException {
        if (doesFileNotExist(exportDir) || doesFileNotExist(templateDir) || doesFileNotExist(pathToJson)) {
            throw new IOException(String.format("A string argument passed to the exportCBP does not point to an existing file." +
                    "%s %n %s %n %s %n", exportDir, templateDir, pathToJson));
        }
            ExporterTemplates templates = new ExporterTemplates(templateDir.toString(), false);
            Group jsonGroup = createGroupWithJsonsFilename(pathToJson.getAbsolutePath());
            List<Map<String, Object>> listMapTable = utilityService.serializeJSONToMaps(pathToJson.getAbsolutePath());
            List<List<String>> cbioParsedData = cbpMapsToSheetsByDataType(listMapTable, dataType);

            Path providerDir = Paths.get(exportDir + "/" + jsonGroup.getAbbreviation());
            String exportUri = "";
            if(dataType.equals(cbioType.MUT)) {
                exportUri = String.format("%s/%s/%s_%s", providerDir,mutFileId,jsonGroup.getAbbreviation(),mutFileId);
                Sheet mutationTemplate = templates.getTemplate(TSV.templateNames.mutation_template.name()).getSheetAt(0);
                universalDataWriterUtilities.writeSingleOmicFileToTsv(exportUri,mutationTemplate, cbioParsedData);
            } else if(dataType.equals(cbioType.GISTIC)){
                exportUri = String.format("%s/%s/%s_%s", providerDir,cnaFileId,jsonGroup.getAbbreviation(),cnaFileId);
                Sheet cnaTemplate = templates.getTemplate(TSV.templateNames.cna_template.name()).getSheetAt(0);
                universalDataWriterUtilities.writeSingleOmicFileToTsv(exportUri,cnaTemplate, cbioParsedData);
            }
    }

    private List<List<String>> cbpMapsToSheetsByDataType(List<Map<String, Object>> listMapTable, cbioType dataType){
        List<List<String>> parsedCbioData = new ArrayList<>();
        if(dataType.equals(cbioType.MUT)){
            parsedCbioData = cbpMutJsonMapsToSheet(listMapTable);
        }
        else if(dataType.equals(cbioType.GISTIC)) {
            parsedCbioData = cbpGisticsonMapsToSheet(listMapTable);
        }
        return parsedCbioData;
    }

    private List<List<String>> cbpMutJsonMapsToSheet(List<Map<String, Object>> jsonMap){
        AtomicInteger rowCount = new AtomicInteger();
        List<List<String>> cbioData = new ArrayList<>();
        jsonMap.forEach(f -> {
            try {
                rowCount.incrementAndGet();
                List<String> row = new LinkedList<>();
                row.add(f.get(patientId).toString());
                row.add(f.get(sampleId).toString());
                row.add(notSpecified);
                row.add(notSpecified);
                row.add(notSpecified);
                row.add(omicTransformationService.ncbiGeneIdToHgncSymbol(String.valueOf(f.get(entrezGeneId))));
                addBlanksToList(row, 9);
                row.add(f.get("chr").toString());
                row.add(f.get("startPosition").toString());
                row.add(f.get("referenceAllele").toString());
                row.add(f.get("variantAllele").toString());
                addBlanksToList(row, 6);
                row.add(f.get("ncbiBuild").toString());
                row.add("");
                cbioData.add(row);
            }catch(NullPointerException e){
                log.error(String.format("Missing value in Json Mut map. Skipping Json Map %d", rowCount.get()));
            }

        });
        return cbioData;
    }

    private List<List<String>> cbpGisticsonMapsToSheet(List<Map<String,Object>> jsonMap){
        AtomicInteger rowCount = new AtomicInteger();
        List<List<String>> cbioData = new ArrayList<>();
        try {
        jsonMap.forEach(f -> {
            List<String> row = new LinkedList<>();
            row.add(f.get(patientId).toString());
            row.add(f.get(sampleId).toString());
            row.add(notSpecified);
            row.add(notSpecified);
            row.add(notSpecified);
            addBlanksToList(row,3);
            row.add(omicTransformationService.ncbiGeneIdToHgncSymbol(String.valueOf(f.get(entrezGeneId))));
            row.add(f.get(entrezGeneId).toString());
            addBlanksToList(row, 6);
            row.add(f.get("alteration").toString());
            addBlanksToList(row, 3);
            cbioData.add(row);   });
        }catch(NullPointerException e){
                log.error(String.format("Missing value in Json gistic map. Skipping Json Map %d", rowCount.get()));
            }
        return cbioData;
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
