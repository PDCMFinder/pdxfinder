package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pdxfinder.TSV;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExporterTemplates {

    private XSSFWorkbook metadataTemplate;
    private XSSFWorkbook samplePlatformTemplate;
    private XSSFWorkbook mutationTemplate;
    private XSSFWorkbook cnaTemplate;
    private XSSFWorkbook cytoTemplate;
    private XSSFWorkbook exprTemplate;
    private XSSFWorkbook drugTemplate;
    private XSSFWorkbook patientTreatmentTemplate;

    private Map<String, XSSFWorkbook> templatesMap;
    private String templateDir;
    private boolean isHarmonized;


    public ExporterTemplates(String templateDir, boolean isharmonized) throws IOException {
        this.templateDir = templateDir;
        this.templatesMap = new HashMap<>();
        this.isHarmonized = isharmonized;

        loadTemplates();
        adjustMetadataTemplateIfHarmonized();
        addAllSheetsToMap();
    }

    private void adjustMetadataTemplateIfHarmonized() {
        if(isHarmonized){
            XSSFSheet sampleSheet = metadataTemplate.getSheet(TSV.metadataSheetNames.sample.name());
            for (Row row : sampleSheet) {
                row.createCell(row.getLastCellNum(), CellType.STRING);
                String nextCellValue = row.getCell(8).getStringCellValue();
                String harmonizedRowMessage = "PDX Finder Harmonized Diagnosis";
                row.getCell(8).setCellValue(harmonizedRowMessage);
                for (int i = 9; i < (row.getLastCellNum()); i++) {
                    String previousCellValue = nextCellValue;
                    nextCellValue = row.getCell(i).getStringCellValue();
                    row.getCell(i).setCellValue(previousCellValue);
                }
            }
        }
    }

    private XSSFWorkbook getWorkbookFromFS(String templatePath) throws IOException {
        XSSFWorkbook workbook;
        File file = new File(templatePath);
        if (!file.exists()) throw new IOException(String.format("Template %s was not found", templatePath));
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            workbook = new XSSFWorkbook(fileInputStream);
        } catch (Exception e) {
            throw new IOException(e);
        }
        return workbook;
    }

    private void loadTemplates() throws IOException {
        metadataTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.metadata_template.fileName);
        samplePlatformTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.sampleplatform_template.fileName);
        mutationTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.mutation_template.fileName);
        cnaTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.cna_template.fileName);
        cytoTemplate = getWorkbookFromFS(templateDir + "/" + TSV.templateNames.cytogenetics_template.fileName);
        exprTemplate = getWorkbookFromFS(templateDir + "/" + TSV.templateNames.expression_template.fileName);
        drugTemplate = getWorkbookFromFS(templateDir + "/" + TSV.templateNames.drugdosing_template.fileName);
        patientTreatmentTemplate = getWorkbookFromFS(templateDir + "/" + TSV.templateNames.patienttreatment_template.fileName);
    }

    private void addAllSheetsToMap() {
        templatesMap.put(TSV.templateNames.metadata_template.name(),metadataTemplate);
        templatesMap.put(TSV.templateNames.sampleplatform_template.name(),samplePlatformTemplate);
        templatesMap.put(TSV.templateNames.mutation_template.name() ,mutationTemplate);
        templatesMap.put(TSV.templateNames.cna_template.name() ,cnaTemplate);
        templatesMap.put(TSV.templateNames.cytogenetics_template.name() ,cytoTemplate);
        templatesMap.put(TSV.templateNames.expression_template.name() ,exprTemplate);
        templatesMap.put(TSV.templateNames.drugdosing_template.name(), drugTemplate);
        templatesMap.put(TSV.templateNames.patienttreatment_template.name(), patientTreatmentTemplate);
    }

    public XSSFWorkbook getTemplate(String template){
        return templatesMap.get(template);
    }

    public boolean isHarmonized(){ return this.isHarmonized; }
}
