package org.pdxfinder.dataexport;

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
            XSSFWorkbook metadataTemplate = getTemplate(TSV.templateNames.metadata_template.name());
            XSSFSheet sampleSheet = metadataTemplate.getSheet(TSV.metadataSheetNames.sample.name());
        }
    }

    private XSSFWorkbook getWorkbookFromFS(String templatePath) throws IOException {
        XSSFWorkbook workbook;
        File file = new File(templatePath);
        if (!file.exists()) throw new IOException(String.format("Template %s was not found", templatePath));
        FileInputStream fileInputStream = new FileInputStream(file);
        workbook = new XSSFWorkbook(fileInputStream);
        return workbook;
    }

    private void loadTemplates() throws IOException {
        metadataTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.metadata_template.fileName);
        samplePlatformTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.sampleplatform_template.fileName);
        mutationTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.mutation_template.fileName);
        cnaTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.cna_template.fileName);
        cytoTemplate = getWorkbookFromFS(templateDir + "/" + TSV.templateNames.cytogenetics_template.fileName);
        exprTemplate = getWorkbookFromFS(templateDir + "/" + TSV.templateNames.expression_template.fileName);
    }

    private void addAllSheetsToMap() {
        templatesMap.put(TSV.templateNames.metadata_template.name(),metadataTemplate);
        templatesMap.put(TSV.templateNames.sampleplatform_template.name(),samplePlatformTemplate);
        templatesMap.put(TSV.templateNames.mutation_template.name() ,mutationTemplate);
        templatesMap.put(TSV.templateNames.cna_template.name() ,cnaTemplate);
        templatesMap.put(TSV.templateNames.cytogenetics_template.name() ,cytoTemplate);
        templatesMap.put(TSV.templateNames.expression_template.name() ,exprTemplate);
    }

    public XSSFWorkbook getTemplate(String template){
        return templatesMap.get(template);
    }

    public boolean isHarmonized(){ return this.isHarmonized; }
}
