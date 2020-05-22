package org.pdxfinder.dataexport;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pdxfinder.graph.dao.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import org.pdxfinder.dataloaders.updog.TSV;

public class ExportSheets {

    private List<List<String>> patientSheetDataExport;
    private List<List<String>> patientSampleSheetDataExport;;
    private List<List<String>> pdxModelSheetDataExport;
    private List<List<String>> pdxModelValidationSheetDataExport;
    private List<List<String>> sharingAndContactSheetDataExport;
    private List<List<String>> loaderRelatedDataSheetDataExport;

    private List<List<String>> samplePlatformSheetDataExport;
    private List<List<String>> drugDosingSheetDataExport;

    private List<List<String>> mutationSheetDataExport;
    private List<List<String>> cnaSheetDataExport;
    private List<List<String>> cytogeneticsSheetDataExport;
    private List<List<String>> expressionSheetDataExport;

    private XSSFWorkbook metadataTemplate;
    private XSSFWorkbook samplePlatformTemplate;
    private XSSFWorkbook mutationTemplate;
    private XSSFWorkbook cnaTemplate;
    private XSSFWorkbook cytoTemplate;
    private XSSFWorkbook exprTemplate;

    private List<List<String>> nullSheet;
    private List<String> nullList;

    private Map<String, List<List<String>>> exportSheetsMap;
    private Map<String, XSSFWorkbook> templatesMap;
    private String templateDir;
    private Group group;

    private static final Logger log = LoggerFactory.getLogger(ExportSheets.class);

    public void init(String templateDir, Group group) throws IOException {
        this.templateDir = templateDir;
        this.exportSheetsMap = new HashMap<>();
        this.templatesMap = new HashMap<>();
        this.group = group;
        Objects.requireNonNull(this.group, "ExportSheets must have a group provider");

        loadTemplates();
        validateMetadataSheetCount();
        initNullSheet();
        initDataExportSheets();
        addAllSheetsToMap();
    }

    public List<List<String>> get(String exportSheets){
        return exportSheetsMap.getOrDefault(exportSheets, nullSheet);
    }

    public XSSFWorkbook getTemplate(String template){
        return templatesMap.get(template);
    }

    public Group getGroup(){
        return group;
    }

    private XSSFWorkbook getWorkbook(String templatePath) throws IOException {
        XSSFWorkbook workbook;
        File file = new File(templatePath);
        log.debug("Loading template {}", templatePath);
        if (!file.exists()) throw new IOException(String.format("Template %s was not found", templatePath));
        FileInputStream fileInputStream = new FileInputStream(file);
        workbook = new XSSFWorkbook(fileInputStream);
        return workbook;
    }

    private void addAllSheetsToMap() {
        exportSheetsMap.put(TSV.metadataSheetNames.patient.name(), patientSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.sample.name() ,patientSampleSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.model.name(),pdxModelSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.model_validation.name() ,pdxModelValidationSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.sharing.name() ,sharingAndContactSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.loader.name() ,loaderRelatedDataSheetDataExport);
        exportSheetsMap.put(TSV.providerFileNames.sampleplatform.name(),samplePlatformSheetDataExport);
        exportSheetsMap.put(TSV.providerFileNames.drugdosing.name() ,drugDosingSheetDataExport);
        exportSheetsMap.put(TSV.providerFileNames.mut.name() ,mutationSheetDataExport);
        exportSheetsMap.put(TSV.providerFileNames.cna.name() ,cnaSheetDataExport);
        exportSheetsMap.put(TSV.providerFileNames.cytogenetics.name() ,cytogeneticsSheetDataExport);

        templatesMap.put(TSV.templateNames.metadata_template.name(),metadataTemplate);
        templatesMap.put(TSV.templateNames.sampleplatform_template.name(),samplePlatformTemplate);
        templatesMap.put(TSV.templateNames.mutation_template.name() ,mutationTemplate);
        templatesMap.put(TSV.templateNames.cna_template.name() ,cnaTemplate);
        templatesMap.put(TSV.templateNames.cytogenetics_template.name() ,cytoTemplate);
        templatesMap.put(TSV.templateNames.expression_template.name() ,exprTemplate);
    }

    private void loadTemplates() throws IOException {
        metadataTemplate = getWorkbook(templateDir+"/" + TSV.templateNames.metadata_template);
        samplePlatformTemplate = getWorkbook(templateDir+"/" + TSV.templateNames.sampleplatform_template);
        mutationTemplate = getWorkbook(templateDir+"/" + TSV.templateNames.mutation_template);
        cnaTemplate = getWorkbook(templateDir+"/" + TSV.templateNames.cna_template);
        cytoTemplate = getWorkbook(templateDir + "/" + TSV.templateNames.cytogenetics_template);
        exprTemplate = getWorkbook(templateDir + "/" + TSV.templateNames.expression_template);
    }

    private void validateMetadataSheetCount() throws IOException{
      if (metadataTemplate.getNumberOfSheets() != TSV.numberOfMetadataSheets.numberSheets.count){
          throw new IOException("metadata template has the incorrect number of sheets");
        }
    }

    private void initDataExportSheets(){
        patientSheetDataExport = new ArrayList<>();
        patientSampleSheetDataExport = new ArrayList<>();
        pdxModelSheetDataExport = new ArrayList<>();
        pdxModelValidationSheetDataExport = new ArrayList<>();
        sharingAndContactSheetDataExport = new ArrayList<>();
        loaderRelatedDataSheetDataExport = new ArrayList<>();
        samplePlatformSheetDataExport = new ArrayList<>();
        drugDosingSheetDataExport = new ArrayList<>();
        mutationSheetDataExport = new ArrayList<>();
        cnaSheetDataExport = new ArrayList<>();
        cytogeneticsSheetDataExport = new ArrayList<>();
        expressionSheetDataExport = new ArrayList<>();
    }

    private void initNullSheet() {
        nullSheet = new ArrayList<>();
        nullList = new ArrayList<>();
        nullSheet.add(nullList);
    }
}
