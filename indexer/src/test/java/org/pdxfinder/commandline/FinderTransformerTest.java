package org.pdxfinder.commandline;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.*;
import org.pdxfinder.BaseTest;
import org.pdxfinder.utils.CbpTransformer;
import org.pdxfinder.utils.CbpTransformer.cbioType;
import org.springframework.boot.test.mock.mockito.MockBean;

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

    @MockBean
    CbpTransformer cbpTransformer;

    @InjectMocks
    FinderTransformer finderTransformer;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.cbpTransformer).exportCBP(
                any(File.class),
                any(File.class),
                any(File.class),
                any(cbioType.class)
        );
        rootDir = folder.newFolder();
        tempFile = new File(rootDir + "/tempFile");
        tempFile.createNewFile();
        template =  new File(rootDir + "/template");
        template.mkdir();
        export = new File(rootDir + "/export");
        export.mkdir();
    }
    @Test public void Given_NoDataisPassed_When_runIsCalled_returnDefaultValues() throws IOException {
        finderTransformer.setDefaultDirectories(rootDir.getAbsolutePath());
        finderTransformer.run(
                null,
                null,
                null,
                tempFile,
                cbioType.MUT,
                null);
        final ArgumentCaptor<File> actualExportDir = ArgumentCaptor.forClass(File.class);
        final ArgumentCaptor<File> actualTemplateDir = ArgumentCaptor.forClass(File.class);
        final ArgumentCaptor<cbioType> captorBiotype = ArgumentCaptor.forClass(cbioType.class);
        verify(this.cbpTransformer).exportCBP(
                actualExportDir.capture(),
                actualTemplateDir.capture(),
                eq(tempFile),
                captorBiotype.capture());
        Assert.assertEquals(actualExportDir.getValue(), new File(rootDir.getAbsoluteFile() + "/export"));
        Assert.assertEquals(actualTemplateDir.getValue(), new File(rootDir.getAbsoluteFile() + "/template"));
        Assert.assertEquals(cbioType.MUT, captorBiotype.getValue());
    }

    @Test
    public void Given_cbioportalDataTypeGISTIC_callCbioportalExportGISTIC() throws IOException {
        finderTransformer.setDefaultDirectories(rootDir.toString());
        finderTransformer.run(
                null,
                null,
                null,
                tempFile,
                cbioType.GISTIC,
                null);
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
                cbioType.GISTIC,
                null);
        verify(cbpTransformer).exportCBP(
                eq(overrideExport),
                eq(overrideTemplate),
                eq(tempFile),
                eq(cbioType.GISTIC)
        );


    }
}
