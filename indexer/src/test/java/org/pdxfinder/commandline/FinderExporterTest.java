package org.pdxfinder.commandline;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pdxfinder.BaseTest;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class FinderExporterTest extends BaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File tempFile;

    @MockBean
    private UtilityService utilityService;
    @MockBean
    private DataImportService dataImportService;

    @SpyBean
    FinderExporter finderExporter;

    @Before public void init() throws IOException {
        tempFile = folder.newFile();
    }

    @Test
    public void Given_loadAll_CallExportAll() throws IOException {
        finderExporter.setDefaultDirectory(tempFile.getAbsolutePath());
        finderExporter.run(null, null, true);
        verify(finderExporter).exportAllGroups(any(File.class)) ;
    }

    @Test
    public void Given_provider_CallExport() throws IOException {
        finderExporter.setDefaultDirectory(tempFile.getAbsolutePath());
        finderExporter.run(null, "test", false);
        verify(finderExporter).export(
                eq(tempFile.getAbsoluteFile()),
                eq("test")
        );
    }

}
