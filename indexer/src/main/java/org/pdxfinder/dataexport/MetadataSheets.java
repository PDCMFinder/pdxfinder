package org.pdxfinder.dataexport;

import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.Group;

import java.util.*;

public class MetadataSheets {

    private List<List<String>> checklistDataExport;
    private List<List<String>> patientSheetDataExport;
    private List<List<String>> patientSampleSheetDataExport;
    private List<List<String>> pdxModelSheetDataExport;
    private List<List<String>> pdxModelValidationSheetDataExport;
    private List<List<String>> sharingAndContactSheetDataExport;
    private List<List<String>> loaderRelatedDataSheetDataExport;


    private Map<String, List<List<String>>> exportSheetsMap;

    private List<List<String>> blankSheet;
    private List<String> blankList;
    private Group group;
    private boolean isHarmonized;

    public MetadataSheets(Group group, boolean isHarmonized) {
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
    }

    private void createExportSheetMap() {
        exportSheetsMap.put(TSV.metadataSheetNames.checklist.name(), checklistDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.patient.name(),patientSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.sample.name(),patientSampleSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.model.name(),pdxModelSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.model_validation.name(),pdxModelValidationSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.sharing.name(),sharingAndContactSheetDataExport);
        exportSheetsMap.put(TSV.metadataSheetNames.loader.name(),loaderRelatedDataSheetDataExport);
    }

    private void initBlankSheet() {
        blankSheet = new ArrayList<>();
        blankList = new ArrayList<>();
        blankSheet.add(blankList);
    }
}
