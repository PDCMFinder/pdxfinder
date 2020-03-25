package org.pdxfinder.commandline;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.*;
import org.pdxfinder.BaseTest;
import org.pdxfinder.utils.CbpTransformer;
import org.pdxfinder.utils.ExportDataToTemplate;
import org.pdxfinder.utils.CbpTransformer.cbioType;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class FinderTransformerTest extends BaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File tempFile;
    private File rootDir;
    private File template;
    private File export;

    @Mock ExportDataToTemplate exportDataToTemplate;
    @Mock CbpTransformer cbpTransformer;

    @InjectMocks
    FinderTransformer finderTransformer;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.exportDataToTemplate).export(
                any(File.class),
                anyString()
        );
        rootDir = folder.newFolder();
        tempFile = new File(rootDir + "/tempFile");
        tempFile.createNewFile();
        template =  new File(rootDir + "/template");
        template.mkdir();
        export = new File(rootDir + "/export");
        export.mkdir();
    }
    @Test public void Given_NoPassDataDir_When_runIsCalled_returnDefault() throws IOException {
        finderTransformer.setDefaultDirectories(rootDir.getAbsolutePath());
        finderTransformer.run(
                null,
                null,
                null,
                null,
                "TEST",
                false,
                null);
        final ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
        final ArgumentCaptor<String> captorStr = ArgumentCaptor.forClass(String.class);
        verify(this.exportDataToTemplate).export(
                captor.capture(),
                captorStr.capture());
        Assert.assertEquals(rootDir.getAbsolutePath() ,captor.getValue().getAbsolutePath());
        Assert.assertEquals("TEST", captorStr.getValue());
    }

    @Test public void Given_dir_When_runIsCalled_Then_passGivenDir() throws IOException {
        finderTransformer.setDefaultDirectories(rootDir.toString());
        finderTransformer.run(
                tempFile,
                null,
                null,
                null,
                "TEST",
                false,
                null);
        final ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
        verify(this.exportDataToTemplate).export(captor.capture(), any());
        File actualFile = captor.getValue();
        Assert.assertEquals(tempFile,actualFile);
    }
    @Test public void Given_loadAll_CallExportAll() throws IOException {
        finderTransformer.setDefaultDirectories(rootDir.toString());
        finderTransformer.run(
                null,
                null,
                null,
                null,
                null,
                true,
                null);
        verify(exportDataToTemplate).exportAllGroups(any(File.class)) ;
    }

    @Test
    public void Given_cbioportalDataTypeMut_callCbioportalExportMUT() throws IOException {
        finderTransformer.setDefaultDirectories(rootDir.toString());
        finderTransformer.run(
                null,
                null,
                null,
                tempFile,
                null,
                false,
                "MUT");
        verify(cbpTransformer).exportCBP(
                any(File.class),
                any(File.class),
                eq(tempFile),
                eq(cbioType.MUT)
        );
    }
    @Test
    public void Given_cbioportalDataTypeGISTIC_callCbioportalExportGISTIC() throws IOException {
        finderTransformer.setDefaultDirectories(rootDir.toString());
        finderTransformer.run(
                null,
                null,
                null,
                tempFile,
                null,
                false,
                "GISTIC");
        verify(cbpTransformer).exportCBP(
                any(File.class),
                any(File.class),
                eq(tempFile),
                eq(cbioType.GISTIC)
        );
    }
    @Test
    public void Given_cbioportalTemplateAndExport_callCbioportalExportWithOverrides() throws IOException {
        File overrideTemplate = new File(rootDir + "/overrideTemplate");
        overrideTemplate.createNewFile();
        File overrideExport = new File(rootDir + "/overrideExport");
        overrideExport.createNewFile();

        finderTransformer.setDefaultDirectories(rootDir.toString());
        finderTransformer.run(
                null,
                overrideTemplate,
                overrideExport,
                tempFile,
                null,
                false,
                "GISTIC");
        verify(cbpTransformer).exportCBP(
                eq(overrideExport),
                eq(overrideTemplate),
                eq(tempFile),
                eq(cbioType.GISTIC)
        );


    }
}
