package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pdxfinder.dataloaders.updog.TSV;
import org.pdxfinder.graph.dao.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Objects.nonNull;

@Component
public class UniversalDataExporter {

    private static final Logger log = LoggerFactory.getLogger(UniversalDataExporter.class);

    private Group dataSource;
    private Path exportProviderDir;
    private XSSFWorkbook metadataTemplate;
    private XSSFWorkbook samplePlatformTemplate;
    private XSSFWorkbook mutationTemplate;
    private XSSFWorkbook cnaTemplate;
    private XSSFWorkbook cytoTemplate;
    private XSSFWorkbook exprTemplate;

    public void export(String exportDir, ExportSheets sheets) {
        dataSource = sheets.getGroup();
        metadataTemplate = sheets.getTemplate(TSV.templateNames.metadata_template.name());
        samplePlatformTemplate = sheets.getTemplate(TSV.templateNames.sampleplatform_template.name());
        mutationTemplate = sheets.getTemplate(TSV.templateNames.mutation_template.name());
        cnaTemplate = sheets.getTemplate(TSV.templateNames.cna_template.name());
        cytoTemplate = sheets.getTemplate(TSV.templateNames.cytogenetics_template.name());
        exprTemplate = sheets.getTemplate(TSV.templateNames.expression_template.name());

        try {
            createExportDir(exportDir);
            copyDataToXlsxTemplates(sheets);
            saveXlsxFiles(sheets);
            exportOmicData(sheets);
        } catch(IOException e) {
            log.error("IO error while exporting data for {} %n {}",dataSource.getAbbreviation(), e.toString());
        }
    }

    private void createExportDir(String exportDir) throws IOException {
        exportProviderDir = Paths.get(exportDir + "/" + dataSource.getAbbreviation());
        Files.createDirectories(exportProviderDir);
        log.info("Creating export folder at {}", exportProviderDir);

    }

    private void copyDataToXlsxTemplates(ExportSheets sheets) {
        updateXlsxSheetWithData(metadataTemplate.getSheetAt(1),
                sheets.get(TSV.metadataSheetNames.patient.name()), 6, 2);
        updateXlsxSheetWithData(metadataTemplate.getSheetAt(2),
                sheets.get(TSV.metadataSheetNames.sample.name()), 6, 2);
        updateXlsxSheetWithData(metadataTemplate.getSheetAt(3),
                sheets.get(TSV.metadataSheetNames.model.name()), 6, 2);
        updateXlsxSheetWithData(metadataTemplate.getSheetAt(4),
                sheets.get(TSV.metadataSheetNames.model_validation.name()), 6, 2);
        updateXlsxSheetWithData(metadataTemplate.getSheetAt(5),
                sheets.get(TSV.metadataSheetNames.sharing.name()), 6, 2);
        updateXlsxSheetWithData(metadataTemplate.getSheetAt(6),
                sheets.get(TSV.metadataSheetNames.loader.name()), 6, 2);
        updateXlsxSheetWithData(samplePlatformTemplate.getSheetAt(0),
                sheets.get(TSV.providerFileNames.sampleplatform.name()), 6, 1);
    }

    private void saveXlsxFiles(ExportSheets sheets) throws IOException {
        writXlsxFromWorkbook(metadataTemplate, exportProviderDir + "/metadata.xlsx");
        writXlsxFromWorkbook(samplePlatformTemplate, exportProviderDir + "/sampleplatform.xlsx");
    }

    private void writXlsxFromWorkbook(XSSFWorkbook dataWorkbook, String fileLocation) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(fileLocation);
        dataWorkbook.write(fileOut);
        fileOut.close();
    }

    public void updateXlsxSheetWithData(Sheet sheet, List<List<String>> data, int startRow, int startColumn) {
        if (nonNull(data)) {
            for (int i = 0; i < data.size(); i++) {
                int rowIndex = startRow + i - 1;
                sheet.createRow(rowIndex);
                for (int j = 0; j < data.get(i).size(); j++) {
                    int columnIndex = startColumn + j - 1;
                    sheet.getRow(rowIndex).createCell(columnIndex);
                    Cell cell;
                    try {
                        cell = sheet.getRow(rowIndex).getCell(columnIndex);
                        cell.setCellValue(data.get(i).get(j));
                    } catch (Exception e) {
                        log.error("Exception in {}  {}:{}", sheet.getSheetName(), rowIndex, columnIndex);
                    }
                }
            }
        }
    }

    private void exportOmicData(ExportSheets sheets) throws IOException {
        writeOmicFileFromWorkbook(mutationTemplate,
                sheets.get(TSV.providerFileNames.mut.name()), exportProviderDir + "/mut/", "_mut.tsv");
        writeOmicFileFromWorkbook(cnaTemplate,
                sheets.get(TSV.providerFileNames.cna.name()), exportProviderDir + "/cna/", "_cna.tsv");
        writeOmicFileFromWorkbook(cytoTemplate,
                sheets.get(TSV.providerFileNames.cytogenetics.name()), exportProviderDir + "/cyto/", "_cyto.tsv");
        writeOmicFileFromWorkbook(exprTemplate, sheets.get(TSV.providerFileNames.expression.name()), exportProviderDir + "/expr/", "_expr.tsv");
    }

    private void writeOmicFileFromWorkbook(XSSFWorkbook omicWorkbook,List<List<String>> exportSheet,String fileLocation, String suffix ) throws IOException {
        if (nonNull(exportSheet)) {
            if (!Paths.get(fileLocation).toFile().exists()) {
                Files.createDirectory(Paths.get(fileLocation));
            }
            String exportURI = fileLocation +  dataSource.getAbbreviation() + suffix;
            readSheetAndWriteOmicTsvFile(omicWorkbook.getSheetAt(0),exportSheet, exportURI);
        }
    }

    public void readSheetAndWriteOmicTsvFile(Sheet template, List<List<String>> exportSheet, String omicTsvDir) {
        try(FileWriter fileWriter = new FileWriter(omicTsvDir)) {
            if (exportSheet != null) {
                copyHeadersFromSheetToTsv(template,fileWriter);
                writeDataToTsv(template, exportSheet, fileWriter);
            }
        } catch(Exception e) {
            log.error("IO Error from reading omic TSV {}",e.toString());
        }
    }

    private void copyHeadersFromSheetToTsv(Sheet xlsxTemplate, FileWriter fileWriter) throws IOException {
        for (int j = 0; j < xlsxTemplate.getRow(0).getLastCellNum(); j++) {
            Cell cell;
            try {
                cell = xlsxTemplate.getRow(0).getCell(j);
                fileWriter.append(cell.toString());
                fileWriter.append("\t");
            } catch (Exception e) {
                log.error("Exception in loading export headers");
            }
        }
        fileWriter.append("\n");
    }

    private void writeDataToTsv(Sheet sheet, List<List<String>> data, FileWriter fileWriter) throws IOException {
        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < data.get(rowIndex).size(); columnIndex++) {
                try {
                    fileWriter.append(data.get(rowIndex).get(columnIndex));
                    fileWriter.append("\t");
                } catch (IOException e) {
                    log.error("Exception in {}  {}:{}", sheet.getSheetName(), rowIndex, columnIndex);
                }
            }
            fileWriter.append("\n");
        }
    }


}
