package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.services.DataImportService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataExportorTests extends BaseTest {

    @Mock
    private DataImportService dataImportService;

    @InjectMocks
    protected UniversalDataWriterUtilities exporter;


    @Test
    public void Given_SheetRowData_When_UpdateSheetIsCalled_Then_SheetIsUpdated(){
        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("Sheet1");

        List<String> rowData = new ArrayList<>(Arrays.asList("1","2","3","4"));
        List<List<String>> data = new ArrayList<>();
        data.add(rowData);

        exporter.updateXlsxSheetWithData(sheet1, data, 1, 1);
        Assert.assertEquals("2", sheet1.getRow(0).getCell(1).getStringCellValue());
    }

    @Test
    public void Given_dataIsReadyToExport_When_exportIsCalled_Then_createCorrectFileStructure() throws IOException {

        Path metaData = Paths.get( "/tmp/metadata_template.xlsx");
        Path samplePlatform = Paths.get("/tmp/sampleplatform_template.xlsx" );
        Path mutation = Paths.get("/tmp/mutation_template.xlsx");
        Path cnaTemplate = Paths.get("/tmp/cna_template.xlsx");

        List<List<String>> genericSheet = new ArrayList<>();
        List<String> genericColumn = new ArrayList<>();
        genericSheet.add(genericColumn);

        /**
        exporter.setPatientSheetDataExport(genericSheet);
        exporter.setSampleSheetDataExport(genericSheet);
        exporter.setPatientTreatmentSheetDataExport(genericSheet);
        exporter.setModelSheetDataExport(genericSheet);
        exporter.setPdxModelValidationSheetDataExport(genericSheet);
        exporter.setSamplePlatformDescriptionSheetDataExport(genericSheet);
        exporter.setSharingAndContactSheetDataExport(genericSheet);
        exporter.setCytogeneticsSheetDataExport(genericSheet);
        exporter.setLoaderRelatedDataSheetDataExport(genericSheet);
        exporter.setDrugDosingSheetDataExport(genericSheet);
        exporter.setCnaSheetDataExport(genericSheet);
        exporter.setMutationSheetDataExport(genericSheet);
**/

        Workbook metaDataXlsx = new XSSFWorkbook();
        Workbook samplePlatformXlsx = new XSSFWorkbook();
        Workbook mutationXlsx = new XSSFWorkbook();
        Workbook cnaTemplateXlsx = new XSSFWorkbook();

        for(int i = 0; i < 7; i++) {
            metaDataXlsx.createSheet(String.format("%s", i));
        }

        samplePlatformXlsx.createSheet("0");
        mutationXlsx.createSheet("0");
        cnaTemplateXlsx.createSheet("0");

        metaDataXlsx.write(new FileOutputStream(metaData.toFile()));
        samplePlatformXlsx.write(new FileOutputStream(samplePlatform.toFile()));
        mutationXlsx.write(new FileOutputStream(mutation.toFile()));
        cnaTemplateXlsx.write(new FileOutputStream(cnaTemplate.toFile()));

        /**
        try {
            exporter.setTemplateDir("/tmp");
            exporter.setDs(providerGroup);
            exporter.extractPatientSheet();
            //universalDataExtractor.export("/tmp");
        } finally {

            Files.delete(metaData);
            Files.delete(samplePlatform);
            Files.delete(mutation);
            Files.delete(cnaTemplate);
        }
         **/

        Path expectedProviderDir = Paths.get("/tmp/TG");
        Path expectedCnaDir = Paths.get("/tmp/TG/cna");
        Path expectedMutDir = Paths.get("/tmp/TG/mut");
        Path expectedMetaData = Paths.get("/tmp/TG/metadata.xlsx");

        Assert.assertTrue(expectedProviderDir.toFile().exists());
        Assert.assertTrue(expectedCnaDir.toFile().exists());
        Assert.assertTrue(expectedMutDir.toFile().exists());
        Assert.assertTrue(expectedMetaData.toFile().exists());

    }


}
