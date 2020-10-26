package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pdxfinder.BaseTest;
import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.Group;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class ExportTemplatesTest extends BaseTest {

    private String templateDir;
    private Group group;


    private void writeWorkbook(String directory, int sheetCount) throws IOException {
        OutputStream out = new FileOutputStream(directory);
        XSSFWorkbook workbook = new XSSFWorkbook();
        for (int i = 0; i < sheetCount; i++) {
            workbook.createSheet();
        }
        workbook.write(out);
    }

    @Test
    public void Give_dataIsHarmonized_When_exportTemplatesAreInitialized_Then_adjustedTemplatesIsMade() throws IOException {
        TemporaryFolder templateRoot = new TemporaryFolder();
        templateRoot.create();
        templateDir = templateRoot.getRoot().getAbsolutePath();

        OutputStream out = new FileOutputStream(templateDir + "/" + TSV.templateNames.metadata_template.fileName);
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(TSV.metadataSheetNames.sample.name());
        Row row = sheet.createRow(0);

        for (int i = 0; i < 24; i++){
            row.createCell(i).setCellValue(String.valueOf(i));
        }

        for (int i = 1; i < 7; i++) {
            workbook.createSheet();
        }
        workbook.write(out);
        writeWorkbook(templateDir + "/" + TSV.templateNames.sampleplatform_template.fileName, 2);
        writeWorkbook(templateDir + "/" + TSV.templateNames.mutation_template.fileName, 2);
        writeWorkbook(templateDir + "/" + TSV.templateNames.cna_template.fileName, 2);
        writeWorkbook(templateDir + "/" + TSV.templateNames.cytogenetics_template.fileName, 2);
        writeWorkbook(templateDir + "/" + TSV.templateNames.expression_template.fileName, 2);
        writeWorkbook(templateDir + "/" + TSV.templateNames.drugdosing_template.fileName, 2);
        writeWorkbook(templateDir + "/" + TSV.templateNames.patienttreatment_template.fileName, 2);
        group = new Group("test", "test", "test");

        ExporterTemplates templates = new ExporterTemplates(templateDir, true);
        XSSFWorkbook actualMetadataTemplate = templates.getTemplate(TSV.templateNames.metadata_template.name());
        Sheet actualSheet = actualMetadataTemplate.getSheet(TSV.metadataSheetNames.sample.name());
        Row actualRow = actualSheet.getRow(0);

        Assert.assertEquals(actualRow.getCell(8).getStringCellValue(), "PDX Finder Harmonized Diagnosis");
        for(int i = 10; i < 25; i++){
            Assert.assertEquals(String.valueOf(i-1), actualRow.getCell(i).getStringCellValue());
        }
    }



}
