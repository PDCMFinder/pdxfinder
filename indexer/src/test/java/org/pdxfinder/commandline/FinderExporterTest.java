package org.pdxfinder.commandline;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.pdxfinder.BaseTest;
import org.pdxfinder.dataexport.UniversalDataExtractionUtilities;
import org.pdxfinder.services.DataImportService;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class FinderExporterTest extends BaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File tempFile;

    @Mock
    private DataImportService dataImportService;
    @Mock
    private UniversalDataExtractionUtilities universalDataExtractionUtilities;

    @Spy
    @InjectMocks
    private FinderExporter finderExporter;

    @Before public void init() throws IOException {
        tempFile = folder.newFile();
    }

    @Test
    public void given_loadAllisTrue_When_runIsCalled_Then_CallExportAll() throws IOException {
        finderExporter.setDefaultDirectory(tempFile.getAbsolutePath());
        finderExporter.run(null, null, true,false );
        verify(finderExporter).exportAllGroups(any(File.class), false) ;
    }

    @Test
    public void given_provider_when_runIsCalled_Then_CallExport() throws IOException {
        finderExporter.setDefaultDirectory(tempFile.getAbsolutePath());
        finderExporter.run(null, "test", false,false);
        verify(finderExporter).export(
                eq(tempFile.getAbsoluteFile()),
                eq("test"),
                eq(false)
        );
    }

    @Test
    public void given_WithSingleProviderAndharmonizedIsTrue_when_runisCalled_Then_CallExport() throws IOException {
        finderExporter.setDefaultDirectory(tempFile.getAbsolutePath());
        finderExporter.run(null, "test", false, true);
        verify(finderExporter).export(
                eq(tempFile.getAbsoluteFile()),
                eq("test"),
                eq(true)
        );

    }

}
