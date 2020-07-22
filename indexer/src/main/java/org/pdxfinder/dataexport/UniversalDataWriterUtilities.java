package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Objects.nonNull;

@Component
public class UniversalDataWriterUtilities {

    private static final Logger log = LoggerFactory.getLogger(UniversalDataWriterUtilities.class);

    public void writXlsxFromWorkbook(XSSFWorkbook dataWorkbook, String fileLocation) throws IOException {
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

    public void writeSingleOmicFileToTsv(String exportLocation, Sheet template,
                                        List<List<String>> exportSheet) throws IOException {
        createExportDirectories(exportLocation);
        saveHeadersToTsv(template, exportLocation);
        appendDataToOmicTsvFile(exportSheet, exportLocation);
    }

    public void createExportDirectories(String exportFileLocation) throws IOException {
        Path directory = Paths.get(exportFileLocation);
        Files.createDirectories(directory);
        if(!directory.toFile().exists()) {
            throw new IOException(String.format("Failed to create file directory at %s",
                    directory.toAbsolutePath().toString()));
        }
        }

    public void saveHeadersToTsv(Sheet template, String exportFileLocation) {
        try (FileWriter fileWriter = new FileWriter(exportFileLocation)) {
            saveHeadersToTsv(template, fileWriter);
        } catch (IOException e) {
            log.error("IO Error writiner headers from {} %n {}", template.getSheetName(), e.toString());
        }
    }

    public void appendDataToOmicTsvFile(List<List<String>> exportSheet, String exportFileLocation) {
        try(FileWriter fileWriter = new FileWriter(exportFileLocation, true)) {
            if (exportSheet != null) {
                writeDataToTsv(exportSheet, fileWriter);
            }
        } catch(Exception e) {
            log.error("IO Error from reading omic TSV {}",e.toString());
        }
    }

    private void saveHeadersToTsv(Sheet xlsxTemplate, FileWriter fileWriter) throws IOException {
        for (int j = 0; j < xlsxTemplate.getRow(0).getLastCellNum(); j++) {
            Cell cell;
            try {
                cell = xlsxTemplate.getRow(0).getCell(j);
                fileWriter.append(cell.toString());
                fileWriter.append("\t");
            } catch (IOException e) {
                log.error("IOException in loading export headers");
            }
        }
        fileWriter.append("\n");
    }

    private void writeDataToTsv(List<List<String>> data, FileWriter fileWriter) throws IOException {
        for (int rowIndex = 0; rowIndex < data.size(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < data.get(rowIndex).size(); columnIndex++) {
                try {
                    fileWriter.append(data.get(rowIndex).get(columnIndex));
                    fileWriter.append("\t");
                } catch (IOException e) {
                    log.error("IOException writing data to TSV on {}:{}", rowIndex, columnIndex);
                }
            }
            fileWriter.append("\n");
        }
    }
}
