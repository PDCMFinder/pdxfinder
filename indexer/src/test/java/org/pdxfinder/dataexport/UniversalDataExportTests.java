package org.pdxfinder.dataexport;


import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.ModelCreation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class UniversalDataExportTests extends BaseTest {

    @Mock
    private UniversalDataWriterUtilities writerUtilities;

    @Mock
    private UniversalDataExtractionUtilities extractionUtilities;

    @InjectMocks
    private UniversalDataExporter universalDataExporter;

    private String templateDir;
    private Group group;
    private ExporterTemplates templates;
    private List<List<String>> testSheet;


    @Before
    public void init() throws IOException {
        TemporaryFolder templateRoot = new TemporaryFolder();
        templateRoot.create();
        templateDir = templateRoot.getRoot().getAbsolutePath();
        writeWorkbook(templateDir + "/" + TSV.templateNames.metadata_template.fileName, 7);
        writeWorkbook(templateDir + "/" + TSV.templateNames.sampleplatform_template.fileName, 2);
        writeWorkbook(templateDir + "/" + TSV.templateNames.mutation_template.fileName, 2);
        writeWorkbook(templateDir + "/" + TSV.templateNames.cna_template.fileName, 2);
        writeWorkbook(templateDir + "/" + TSV.templateNames.cytogenetics_template.fileName, 2);
        writeWorkbook(templateDir + "/" + TSV.templateNames.expression_template.fileName, 2);

        templateDir = templateRoot.getRoot().getAbsolutePath();
        templates = new ExporterTemplates(templateDir, false);
        group = new Group("test", "test", "test");

        testSheet = new ArrayList<>();
        ArrayList<String> testRow = new ArrayList<>();
        testSheet.add(testRow);
    }

    private void writeWorkbook(String directory, int sheetCount) throws IOException {
        OutputStream out = new FileOutputStream(directory);
        XSSFWorkbook workbook = new XSSFWorkbook();
        for (int i = 0; i < sheetCount; i++) {
            workbook.createSheet();
        }
        workbook.write(out);
    }

    @Test
    public void Given_dummyTemplatesAndNoData_ExportSamplePlatform_ExtractionUtilitesAreCalledWithoutWrite() throws IOException {
        universalDataExporter.exportSamplePlatform(templates, group, templateDir);

        verify(extractionUtilities, times(1)).extractSamplePlatform(group);
        verify(writerUtilities, times(1)).updateXlsxSheetWithData(any(Sheet.class),
                any(List.class), anyInt(), anyInt());

        verify(writerUtilities, never()).writXlsxFromWorkbook(any(XSSFWorkbook.class), anyString());
    }

    @Test
    public void Given_dummyTemplatesWithData_ExportSamplePlatform_ExtractionUtilitesAreCalledWithWrite() throws IOException {
        when(extractionUtilities.extractSamplePlatform(group)).thenReturn(testSheet);

        XSSFWorkbook samplePlatformTemplate = templates.getTemplate(TSV.templateNames.sampleplatform_template.name());
        universalDataExporter.exportSamplePlatform(templates, group, templateDir);

        verify(writerUtilities).updateXlsxSheetWithData(eq(samplePlatformTemplate.getSheetAt(0)),
                any(ArrayList.class), anyInt(), anyInt());
        verify(writerUtilities).writXlsxFromWorkbook(eq(samplePlatformTemplate), anyString());
    }

    @Test
    public void Given_dummyTemplatesAndNoData_ExportMetadata_ExtractionUtilitesAreCalledWithoutWrite() throws IOException {
        MetadataSheets  metadataSheets = new MetadataSheets(group);
        when(extractionUtilities.extractMetadata(group, metadataSheets, false)).thenReturn(metadataSheets);

        universalDataExporter.exportMetadata(metadataSheets, templates, group, false, templateDir);
        verify(writerUtilities, times(7)).updateXlsxSheetWithData(any(Sheet.class),
                any(List.class), anyInt(), anyInt());

        verify(writerUtilities).writXlsxFromWorkbook(any(XSSFWorkbook.class), anyString());
    }

    @Test
    public void Given_NoModels_When_callToExtractAndSaveOmicsByBatch_Then_doNotRun() throws IOException {
        String molecularType = "Mutation";
        Path testExportURI = Paths.get("/path/to/export");
        when(extractionUtilities.getAllModelsByGroupAndMoleculartype(group, molecularType))
                .thenReturn(new ArrayList<>());

        universalDataExporter.extractAndSaveOmicByBatch(molecularType,
                templates.getTemplate(TSV.templateNames.mutation_template.name()),testExportURI, group);

        verify(writerUtilities, never()).saveHeadersToTsv(any(Sheet.class),anyString());
        verify(extractionUtilities, never()).extractModelsOmicData(any(ModelCreation.class), anyString());
    }

    @Test
    public void Given_aSingleModel_When_callToExtractAndSaveOmicsByBatch_Then_runOnce() throws IOException {
        String molecularType = "Mutation";
        Path testExportURI = Paths.get("/path/to/export");
        ModelCreation testModel = new ModelCreation();
        when(extractionUtilities.getAllModelsByGroupAndMoleculartype(group,molecularType))
                .thenReturn(Collections.singletonList(testModel));

        universalDataExporter.extractAndSaveOmicByBatch(molecularType,
                templates.getTemplate(TSV.templateNames.mutation_template.name()),testExportURI, group);

        verify(writerUtilities).saveHeadersToTsv(any(Sheet.class),eq(testExportURI.toString()));
        verify(extractionUtilities).extractModelsOmicData(eq(testModel), eq(molecularType));
        verify(writerUtilities).appendDataToOmicTsvFile(anyList(), eq(testExportURI.toString()));
    }

    @Test
    public void Given_elevenModel_When_callToExtractAndSaveOmicsByBatch_Then_runOnceInBatchThenAgain() throws IOException {
        String molecularType = "Mutation";
        Path testExportURI = Paths.get("/path/to/export");
        ModelCreation[] testModelList = new ModelCreation[11];
        for(int i = 0; i < 11; i++){
            testModelList[i] = new ModelCreation();
        }

        when(extractionUtilities.getAllModelsByGroupAndMoleculartype(group,molecularType))
                .thenReturn(Arrays.asList(testModelList));

        universalDataExporter.extractAndSaveOmicByBatch(molecularType,
                templates.getTemplate(TSV.templateNames.mutation_template.name()),testExportURI, group);

        verify(writerUtilities).saveHeadersToTsv(any(Sheet.class),eq(testExportURI.toString()));
        verify(extractionUtilities, times(11)).extractModelsOmicData(any(ModelCreation.class), eq(molecularType));
        verify(writerUtilities, times(2)).appendDataToOmicTsvFile(anyList(), eq(testExportURI.toString()));
    }
}
