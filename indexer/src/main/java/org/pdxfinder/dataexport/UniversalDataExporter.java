package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.ModelCreation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class UniversalDataExporter {

    private static final Logger log = LoggerFactory.getLogger(UniversalDataExporter.class);
    private UniversalDataWriterUtilities writerUtilities;
    private UniversalDataExtractionUtilities extractionUtilities;

    @Autowired
    void UniversalDataExporter(UniversalDataWriterUtilities writerUtilities, UniversalDataExtractionUtilities extractionUtilities){
        this.writerUtilities = writerUtilities;
        this.extractionUtilities = extractionUtilities;
    }

    private XSSFWorkbook metadataTemplate;
    private Path exportProviderDir;

    public void exportAllFromGroup(String exportDir, Group dataSource, boolean isHarmonized, String templateDir) throws IOException {
        this.exportProviderDir = Paths.get(exportDir + "/" + dataSource.getAbbreviation());
        ExporterTemplates templates = new ExporterTemplates(templateDir, isHarmonized);
        MetadataSheets providerSheets = new MetadataSheets(dataSource, isHarmonized);

        try {
            writerUtilities.createExportDirectories(exportProviderDir.toString());
            exportMetadata(providerSheets,templates, dataSource);
            exportSamplePlatform(templates, dataSource);
            exportAllOmicSheets(templates, dataSource);

        } catch(IOException e) {
            log.error("IO error while exporting data for {} %n {}",dataSource.getAbbreviation(), e.toString());
        }
    }

    public void exportSamplePlatform(ExporterTemplates templates,Group group) throws IOException {
        List<List<String>> samplePlatform = extractionUtilities.extractSamplePlatform(group);
        XSSFWorkbook samplePlatformTemplate = templates.getTemplate(TSV.templateNames.sampleplatform_template.name());
        saveSamplePlatformToXlsx(samplePlatformTemplate, samplePlatform);
    }

    public void exportMetadata(MetadataSheets providerSheets, ExporterTemplates templates, Group dataSource) throws IOException {
        extractionUtilities.extractMetadata(dataSource, providerSheets);
        saveMetadataToXlsx(providerSheets, templates);
    }

    private void saveMetadataToXlsx(MetadataSheets providerData, ExporterTemplates templates) throws IOException {
        metadataTemplate = templates.getTemplate(TSV.templateNames.metadata_template.name());
        int sheetNumber = 0;
        for(TSV.metadataSheetNames sheetName: TSV.metadataSheetNames.values()){
            writerUtilities.updateXlsxSheetWithData(metadataTemplate.getSheetAt(sheetNumber),
                    providerData.get(sheetName.name()),  6, 2);
            sheetNumber++;
        }
        if(allMetadataSheetsHaveData(providerData)) {
            writerUtilities.writXlsxFromWorkbook(metadataTemplate, exportProviderDir + "/metadata.xlsx");
        }
    }

    private void saveSamplePlatformToXlsx(XSSFWorkbook samplePlatformTemplate, List<List<String>> samplePlatform) throws IOException {
        writerUtilities.updateXlsxSheetWithData(samplePlatformTemplate.getSheetAt(0),
                samplePlatform, 6, 1);
        if(!samplePlatform.isEmpty()){
            writerUtilities.writXlsxFromWorkbook(samplePlatformTemplate, exportProviderDir + "/sampleplatform.xlsx");
        }
    }

    private void exportAllOmicSheets(ExporterTemplates templates, Group dataSource) throws IOException {
        XSSFWorkbook mutationTemplate = templates.getTemplate(TSV.templateNames.mutation_template.name());
        XSSFWorkbook cnaTemplate = templates.getTemplate(TSV.templateNames.cna_template.name());
        XSSFWorkbook cytoTemplate = templates.getTemplate(TSV.templateNames.cytogenetics_template.name());
        XSSFWorkbook exprTemplate = templates.getTemplate(TSV.templateNames.expression_template.name());
        String mutExportURI = exportProviderDir +  dataSource.getAbbreviation() + TSV.molecular_characterisation_type.mut.name();
        String cnaExportURI = exportProviderDir +  dataSource.getAbbreviation() + TSV.molecular_characterisation_type.cna.name();
        String expressionExportURI = exportProviderDir +  dataSource.getAbbreviation() + TSV.molecular_characterisation_type.expression.name();
        String cytogeneticsExportURI = exportProviderDir +  dataSource.getAbbreviation() + TSV.molecular_characterisation_type.cyto.name();
        extractAndSaveOmicByBatch(TSV.molecular_characterisation_type.mut.mcType ,mutationTemplate, mutExportURI,dataSource);
        extractAndSaveOmicByBatch(TSV.molecular_characterisation_type.cna.mcType ,cnaTemplate, cnaExportURI, dataSource);
        extractAndSaveOmicByBatch(TSV.molecular_characterisation_type.cyto.mcType ,cytoTemplate, expressionExportURI, dataSource);
        extractAndSaveOmicByBatch(TSV.molecular_characterisation_type.expression.mcType ,exprTemplate, cytogeneticsExportURI,dataSource);

    }

    public void extractAndSaveOmicByBatch(String molecularType, XSSFWorkbook template, String exportURI, Group dataSource) throws IOException {
        Sheet templateSheet = template.getSheetAt(0);
        List<ModelCreation> models = extractionUtilities.getAllModelsByGroupAndMoleculartype(dataSource, molecularType);
        if(models.size() > 0) {
            writerUtilities.createExportDirectories(exportURI);
            writerUtilities.saveHeadersToTsv(templateSheet, exportURI);
            List<List<String>> modelsOmicData = new ArrayList<>();

            int counter = 0;
            for (ModelCreation model : models) {
                modelsOmicData.addAll(extractionUtilities.extractModelsOmicData(model, molecularType));
                if (counter % 10 == 0) {
                    writerUtilities.appendDataToOmicTsvFile(modelsOmicData, exportURI);
                    modelsOmicData.clear();
                }
                counter++;
            }
            if (!modelsOmicData.isEmpty()) {
                writerUtilities.appendDataToOmicTsvFile(modelsOmicData, exportURI);
                modelsOmicData.clear();
            }
        }
    }

    private boolean allMetadataSheetsHaveData(MetadataSheets providerData){
        for(TSV.metadataSheetNames sheetName: TSV.metadataSheetNames.values()){
            if (providerData.get(sheetName.name()) != null && !providerData.get(sheetName.name()).isEmpty() ) {
                return false;
            }
        }
        return true;
    }

}
