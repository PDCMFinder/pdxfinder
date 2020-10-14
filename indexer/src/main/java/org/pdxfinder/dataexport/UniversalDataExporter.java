package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.ModelCreation;
import org.pdxfinder.services.DataImportService;
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
    private UniversalDataWriterServices writerUtilities;
    private UniversalDataExtractionServices extractionServices;
    private DataImportService dataImportService;

    @Autowired
    UniversalDataExporter(UniversalDataWriterServices writerUtilities, UniversalDataExtractionServices extractionUtilities, DataImportService dataImportService){
        this.writerUtilities = writerUtilities;
        this.extractionServices = extractionUtilities;
        this.dataImportService = dataImportService;
    }

    public void exportAllFromGroup(String exportDir, Group dataSource, boolean isHarmonized, String templateDir) throws IOException {
        String exportProviderDir = Paths.get(String.format("%s/%s/",exportDir ,dataSource.getAbbreviation())).toString();
        ExporterTemplates templates = new ExporterTemplates(templateDir, isHarmonized);
        MetadataSheets providerSheets = new MetadataSheets();
        writerUtilities.createExportDirectories(exportProviderDir);

        try {
            exportMetadata(providerSheets,templates, dataSource, isHarmonized, exportProviderDir);
            exportSamplePlatform(templates, dataSource, exportProviderDir);
            exportAllOmicSheets(templates, dataSource, exportProviderDir);

        } catch(IOException e) {
            log.error("IO error while exporting data for {} \n {}",dataSource.getAbbreviation(), e.toString());
        }
    }

    public void exportSamplePlatform(ExporterTemplates templates, Group group, String exportProviderDir) throws IOException {
        List<List<String>> samplePlatform = extractionServices.extractSamplePlatform(group);
        XSSFWorkbook samplePlatformTemplate = templates.getTemplate(TSV.templateNames.sampleplatform_template.name());
        String samplePlatformURI = String.format("%s/%s_sampleplatform.xlsx", exportProviderDir, group.getAbbreviation());
        saveSamplePlatformToXlsx(samplePlatformTemplate, samplePlatform, samplePlatformURI);
    }

    public void exportMetadata(MetadataSheets providerSheets, ExporterTemplates templates, Group dataSource, boolean isHarmonized, String exportProviderDir) throws IOException {
        extractionServices.extractMetadata(dataSource, providerSheets, isHarmonized);
        saveMetadataToXlsx(providerSheets, templates, exportProviderDir, dataSource);
    }

    private void saveMetadataToXlsx(MetadataSheets providerData, ExporterTemplates templates, String exportProviderDir, Group dataSource) throws IOException {
        XSSFWorkbook metadataTemplate = templates.getTemplate(TSV.templateNames.metadata_template.name());
        int sheetNumber = 0;
        for(TSV.metadataSheetNames sheetName: TSV.metadataSheetNames.values()){
            writerUtilities.updateXlsxSheetWithData(metadataTemplate.getSheetAt(sheetNumber),
                    providerData.get(sheetName.name()),  6, 2);
            sheetNumber++;
        }
        if(allMetadataSheetsHaveData(providerData)) {
            String metadataFileURI = String.format("%s/%s_metadata.xlsx", exportProviderDir, dataSource.getAbbreviation());
            writerUtilities.writXlsxFromWorkbook(metadataTemplate, metadataFileURI);
        } else { log.error("Empty or Null metadata sheet. Skipping export of Metadata."); }
    }

    private void saveSamplePlatformToXlsx(XSSFWorkbook samplePlatformTemplate, List<List<String>> samplePlatform, String samplePlatformURI) throws IOException {
        writerUtilities.updateXlsxSheetWithData(samplePlatformTemplate.getSheetAt(0),
                samplePlatform, 6, 1);
        if(!samplePlatform.isEmpty()){
            writerUtilities.writXlsxFromWorkbook(samplePlatformTemplate, samplePlatformURI);
        }
    }

    private void exportAllOmicSheets(ExporterTemplates templates, Group dataSource, String exportProviderDir) throws IOException {
        XSSFWorkbook mutationTemplate = templates.getTemplate(TSV.templateNames.mutation_template.name());
        XSSFWorkbook cnaTemplate = templates.getTemplate(TSV.templateNames.cna_template.name());
        XSSFWorkbook cytoTemplate = templates.getTemplate(TSV.templateNames.cytogenetics_template.name());
        XSSFWorkbook exprTemplate = templates.getTemplate(TSV.templateNames.expression_template.name());
        XSSFWorkbook dosingTemplate = templates.getTemplate(TSV.templateNames.drugdosing_template.name());
        XSSFWorkbook treatmentTemplate = templates.getTemplate(TSV.templateNames.patienttreatment_template.name());
        String mut = TSV.molecular_characterisation_type.mut.name();
        String cna = TSV.molecular_characterisation_type.cna.name();
        String expression = TSV.molecular_characterisation_type.expression.name();
        String cyto =  TSV.molecular_characterisation_type.cyto.name();
        String dosing = "drug";
        String treatment = "patientTreatment";
        Path mutExportURI = Paths.get(String.format("%s/%s/%s_%s.tsv",exportProviderDir, mut, dataSource.getAbbreviation(), mut));
        Path cnaExportURI = Paths.get(String.format( "%s/%s/%s_%s.tsv",exportProviderDir, cna, dataSource.getAbbreviation(), cna));
        Path expressionExportURI = Paths.get(String.format("%s/%s/%s_%s.tsv", exportProviderDir,  expression, dataSource.getAbbreviation() ,expression));
        Path cytoExportURI = Paths.get(String.format("%s/%s/%s_%s.tsv",exportProviderDir,cyto, dataSource.getAbbreviation() , cyto));
        Path dosingExportURI = Paths.get(String.format("%s/%s/%s_%s.tsv", exportProviderDir, dosing, dataSource.getAbbreviation(), dosing));
        Path treatmentExportURI = Paths.get(String.format("%s/%s/%s_%s.tsv", exportProviderDir, treatment, dataSource.getAbbreviation(), treatment));
        extractAndSaveOmicByBatch(TSV.molecular_characterisation_type.mut.mcType ,mutationTemplate, mutExportURI,dataSource);
        extractAndSaveOmicByBatch(TSV.molecular_characterisation_type.cna.mcType ,cnaTemplate, cnaExportURI, dataSource);
        extractAndSaveOmicByBatch(TSV.molecular_characterisation_type.cyto.mcType ,cytoTemplate, cytoExportURI, dataSource);
        extractAndSaveOmicByBatch(TSV.molecular_characterisation_type.expression.mcType ,exprTemplate, expressionExportURI,dataSource);
        extractAndSaveOmicByBatch(dosing, dosingTemplate, dosingExportURI, dataSource);
        extractAndSaveOmicByBatch(treatment, treatmentTemplate, treatmentExportURI, dataSource);
    }

    public void extractAndSaveOmicByBatch(String molecularType, XSSFWorkbook template, Path exportURI, Group dataSource) throws IOException {
        String parentDirectory = exportURI.getParent().toString();
        String fileURI = exportURI.toString();
        Sheet templateSheet = template.getSheetAt(0);
        List<ModelCreation> models;
        models = getModelsByMolecularTypeAndDataSource(molecularType, dataSource);
        if(!models.isEmpty()) {
            writerUtilities.createExportDirectories(parentDirectory);
            writerUtilities.saveHeadersToTsv(templateSheet, exportURI.toString());
            List<List<String>> modelsOmicData = new ArrayList<>();
            int counter = 0;
            for (ModelCreation model : models) {
                modelsOmicData.addAll(extractionServices.extractModelsOmicData(model, molecularType));
                if (counter % 10 == 0) {
                    writerUtilities.appendDataToOmicTsvFile(modelsOmicData, fileURI);
                    modelsOmicData.clear();
                }
                counter++;
            }
            if (!modelsOmicData.isEmpty()) {
                writerUtilities.appendDataToOmicTsvFile(modelsOmicData, fileURI);
                modelsOmicData.clear();
            }
        }
    }

    List<ModelCreation> getModelsByMolecularTypeAndDataSource(String molecularType, Group dataSource) {
        List<ModelCreation> models;
        if (molecularType.equals("drug")) {
            models = dataImportService.findModelsWithTreatmentSummaryByDS(dataSource.getAbbreviation());
        } else if(molecularType.equals("patientTreatment")) {
            models = dataImportService.findModelFromPatienSnapshotWithTreatmentSummaryByDS(dataSource.getAbbreviation());
        }
        else {
            models = dataImportService.findModelsWithMolecularDataByDSAndMolcharType(dataSource.getAbbreviation(), molecularType);
        }
        return models;
    }


    private boolean allMetadataSheetsHaveData(MetadataSheets providerData){
        for(TSV.metadataSheetNames sheetName: TSV.metadataSheetNames.values()){
            if (providerData.get(sheetName.name()) == null || providerData.get(sheetName.name()).isEmpty() ) {
                log.error("Metadata sheet {} was not found. Skipping metadata export", sheetName.name());
                return false;
            }
        }
        return true;
    }

}
