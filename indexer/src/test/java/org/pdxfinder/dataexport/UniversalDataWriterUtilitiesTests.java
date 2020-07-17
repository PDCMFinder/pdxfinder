package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.services.DataImportService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UniversalDataWriterUtilitiesTests extends BaseTest {

    @Mock
    private DataImportService dataImportService;

    @InjectMocks
    protected UniversalDataWriterUtilities universalDataWriterUtilities;


    @Test
    public void Given_SheetRowData_When_UpdateSheetIsCalled_Then_SheetIsUpdated(){
        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("Sheet1");

        List<String> rowData = new ArrayList<>(Arrays.asList("1","2","3","4"));
        List<List<String>> data = new ArrayList<>();
        data.add(rowData);

        universalDataWriterUtilities.updateXlsxSheetWithData(sheet1, data, 1, 1);
        Assert.assertEquals("2", sheet1.getRow(0).getCell(1).getStringCellValue());
    }

    @Test
    public void Given_nonExistingDirectoryStructureURI_CreateExportDirectiesIsCalled_createFullStructure() throws IOException {
        TemporaryFolder rootFolder = new TemporaryFolder();
        rootFolder.create();
        String rootFolderURI = rootFolder.getRoot().getAbsolutePath();
        String testExportURI = String.format("%s/%s/%s/data_mut.tsv", rootFolderURI, "testProvider", "mut");
        universalDataWriterUtilities.createExportDirectories(testExportURI);
        Assert.assertTrue(Paths.get(testExportURI).getParent().toFile().exists());
    }

    @Test
    public void Given_templatesAndexportLocation_WhenCopyHeaderIsCalled_WriteHeadersToTsvThenEOF() throws IOException {
        TemporaryFolder rootFolder = new TemporaryFolder();
        rootFolder.create();
        String tsvURI = String.format("%s/test_mut.tsv", rootFolder.getRoot().getAbsoluteFile());
        XSSFWorkbook templateWB = new XSSFWorkbook();
        templateWB.createSheet();
        Sheet sheet = templateWB.getSheetAt(0);
        sheet.createRow(0);
        Row row = sheet.getRow(0);
        for(int i=0; i < 23; i++) {
            row.createCell(i).setCellValue(i);
        }
        universalDataWriterUtilities.saveHeadersToTsv(sheet, tsvURI);
        File actualFile = Paths.get(tsvURI).toFile();
        Assert.assertTrue(actualFile.exists());
        BufferedReader reader = new BufferedReader(new FileReader(tsvURI));
        String[] delimitedHeaders = reader.readLine().split("\t");
        float index = 0;
        for(String header: delimitedHeaders){
            Assert.assertEquals(String.format("%.1f", index), header);
            index++;
        }
        Assert.assertEquals(reader.readLine(), null);
    }

    @Test
    public void Given_dataTableWithOneRow_WhenAppendedDataIsCalled_WriteToFirstLineThenToSecond() throws IOException{
        TemporaryFolder rootFolder = new TemporaryFolder();
        rootFolder.create();
        String tsvURI = String.format("%s/test_mut.tsv", rootFolder.getRoot().getAbsoluteFile());
        List<List<String>> testProviderData = new ArrayList<>();
        List<String> testRow = new ArrayList<>();
        for(int i = 0; i < 23; i++){
            testRow.add(String.valueOf(i));
        }
        testProviderData.add(testRow);
        universalDataWriterUtilities.appendDataToOmicTsvFile(testProviderData, tsvURI);
        universalDataWriterUtilities.appendDataToOmicTsvFile(testProviderData, tsvURI);
        BufferedReader reader = new BufferedReader(new FileReader(tsvURI));
        String[] actualDelimitedCellValues1 = reader.readLine().split("\t");
        String[] actualDelimitedCellValues2 = reader.readLine().split("\t");

        testForSequentialCellValues(actualDelimitedCellValues1);
        testForSequentialCellValues(actualDelimitedCellValues2);
        Assert.assertEquals(reader.readLine(), null);
    }

    private void testForSequentialCellValues(String[] delimitedCellValues){
        int index = 0;
        for(String cellData: delimitedCellValues){
            Assert.assertEquals(String.valueOf(index), cellData);
            index++;
        }
    }
}
