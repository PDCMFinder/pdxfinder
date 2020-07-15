package org.pdxfinder.dataexport;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pdxfinder.TSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(ExporterTemplates.class);

    public ExporterTemplates(String templateDir) throws IOException {
        this(templateDir, false);
    }

    public ExporterTemplates(String templateDir, boolean isharmonized) throws IOException {
        this.templateDir = templateDir;
        this.templatesMap = new HashMap<>();
        this.isHarmonized = isharmonized;

        loadTemplates();
        validateMetadataSheetCount();
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
        log.debug("Loading template {}", templatePath);
        if (!file.exists()) throw new IOException(String.format("Template %s was not found", templatePath));
        FileInputStream fileInputStream = new FileInputStream(file);
        workbook = new XSSFWorkbook(fileInputStream);
        return workbook;
    }

    private void loadTemplates() throws IOException {
        metadataTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.metadata_template);
        samplePlatformTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.sampleplatform_template);
        mutationTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.mutation_template);
        cnaTemplate = getWorkbookFromFS(templateDir+"/" + TSV.templateNames.cna_template);
        cytoTemplate = getWorkbookFromFS(templateDir + "/" + TSV.templateNames.cytogenetics_template);
        exprTemplate = getWorkbookFromFS(templateDir + "/" + TSV.templateNames.expression_template);
    }

    private void addAllSheetsToMap() {
        templatesMap.put(TSV.templateNames.metadata_template.name(),metadataTemplate);
        templatesMap.put(TSV.templateNames.sampleplatform_template.name(),samplePlatformTemplate);
        templatesMap.put(TSV.templateNames.mutation_template.name() ,mutationTemplate);
        templatesMap.put(TSV.templateNames.cna_template.name() ,cnaTemplate);
        templatesMap.put(TSV.templateNames.cytogenetics_template.name() ,cytoTemplate);
        templatesMap.put(TSV.templateNames.expression_template.name() ,exprTemplate);
    }

    private void validateMetadataSheetCount() throws IOException{
      if (metadataTemplate.getNumberOfSheets() != TSV.numberOfMetadataSheets.numberSheets.count){
          throw new IOException("metadata template has the incorrect number of sheets");
        }
    }

    public XSSFWorkbook getTemplate(String template){
        return templatesMap.get(template);
    }

    public boolean isHarmonized(){ return this.isHarmonized; }
}
