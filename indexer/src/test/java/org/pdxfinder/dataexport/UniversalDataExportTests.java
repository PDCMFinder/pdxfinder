package org.pdxfinder.dataexport;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.Group;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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


    @Before
    public void init() throws IOException {
        TemporaryFolder templateRoot = new TemporaryFolder();
        templateRoot.create();
        templateDir = templateRoot.getRoot().getAbsolutePath();
        writeWorkbook(templateDir + "/" + TSV.templateNames.metadata_template.fileName);
        writeWorkbook(templateDir + "/" + TSV.templateNames.sampleplatform_template.fileName);
        writeWorkbook(templateDir + "/" + TSV.templateNames.mutation_template.fileName);
        writeWorkbook(templateDir + "/" + TSV.templateNames.cna_template.fileName);
        writeWorkbook(templateDir + "/" + TSV.templateNames.cytogenetics_template.fileName);
        writeWorkbook(templateDir + "/" + TSV.templateNames.expression_template.fileName);

        templateDir = templateRoot.getRoot().getAbsolutePath();
        group = new Group("test", "test", "test");
    }

    private void writeWorkbook(String directory) throws IOException {
        OutputStream out = new FileOutputStream(directory);
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.write(out);
    }


    @Test
    public void Given_dummyTemplatesAndNoData_ExportAllFromGroupIsCalled_ThenAppropriateExtractionUtilitesAreCalled() throws IOException {
        universalDataExporter.exportAllFromGroup(templateDir, group, false, templateDir);
        verify(extractionUtilities.extractMetadata(any(MetadataSheets.class)), calls(1));
        verify(extractionUtilities.extractSamplePlatform(group), calls(1));
    }

}
