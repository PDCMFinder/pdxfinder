package org.pdxfinder.commandline;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pdxfinder.BaseTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

public class FinderExporterTest extends BaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File tempFile;

    @SpyBean
    FinderExporter finderExporter;

    @Before public void init() throws IOException {
        tempFile = folder.newFile();
    }

    @Test
    public void Given_loadAll_CallExportAll() throws IOException {
        finderExporter.setDefaultDirectory(tempFile.getAbsolutePath());
        finderExporter.run(
             null,
                null,
                true
                );
        verify(finderExporter).exportAllGroups(any(File.class)) ;
    }
}
