package org.pdxfinder.dataexport;

import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.Group;

import java.util.*;

public class ExportProviderSheets {

    private List<List<String>> patientSheetDataExport;
    private List<List<String>> patientSampleSheetDataExport;
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

    private Map<String, List<List<String>>> exportSheetsMap;

    private List<List<String>> blankSheet;
    private List<String> blankList;
    private Group group;
    private boolean isHarmonized;

    public ExportProviderSheets(Group group){
        this(group, false);
    }

    public ExportProviderSheets(Group group, boolean isHarmonized) {
        Objects.requireNonNull(group, "ExportSheets must have a group provider");
        this.group = group;
        this.isHarmonized = isHarmonized;

        initDataExportSheets();
        createExportSheetMap();
        initBlankSheet();
    }

    public List<List<String>> get(String exportSheets) {
        return exportSheetsMap.getOrDefault(exportSheets, blankSheet);
    }

    public void set(String enumName, List<List<String>> sheetData){
        exportSheetsMap.put(enumName, sheetData);
    }

    public Group getGroup(){
        return group;
    }

    public boolean isHarmonized(){ return this.isHarmonized; }

    private void initDataExportSheets() {
        exportSheetsMap = new HashMap<>();

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

    private void createExportSheetMap() {
        exportSheetsMap.put(TSV.metadataSheetNames.patient.name(),patientSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.sample.name(),patientSampleSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.model.name(),pdxModelSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.model_validation.name(),pdxModelValidationSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.sharing.name(),sharingAndContactSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.loader.name(),loaderRelatedDataSheetDataExport);
        exportSheetsMap.put(TSV.providerFileNames.sampleplatform.name(),samplePlatformSheetDataExport);
        exportSheetsMap.put(TSV.providerFileNames.drugdosing.name(),drugDosingSheetDataExport);
        exportSheetsMap.put(TSV.providerFileNames.mut.name(),mutationSheetDataExport);
        exportSheetsMap.put(TSV.providerFileNames.cna.name(),cnaSheetDataExport);
        exportSheetsMap.put(TSV.providerFileNames.cytogenetics.name(),cytogeneticsSheetDataExport);
    }

    private void initBlankSheet() {
        blankSheet = new ArrayList<>();
        blankList = new ArrayList<>();
        blankSheet.add(blankList);
    }
}
