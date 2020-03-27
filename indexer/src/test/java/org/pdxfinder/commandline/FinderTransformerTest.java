package org.pdxfinder.commandline;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.pdxfinder.BaseTest;
import org.pdxfinder.utils.ExportDataToTemplate;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class FinderTransformerTest extends BaseTest {

    @Mock
    ExportDataToTemplate exportDataToTemplate;
    @Spy
    @InjectMocks
    FinderTransformer finderTransformer;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.exportDataToTemplate).export(
                any(File.class),
                anyString()
        );
    }
    @Test public void Given_NoPassDataDir_When_runIsCalled_returnDefault() throws IOException {
        finderTransformer.setDefaultDirectory("/tmp");
        finderTransformer.run(new File("DOES_NOT_EXIST"), "TEST", false);
        final ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
        verify(this.exportDataToTemplate).export(captor.capture(), any());
        System.out.printf(captor.getValue().getName());
        Assert.assertEquals("tmp",captor.getValue().getName());
    }

    @Test public void Given_dir_When_runIsCalled_Then_passGivenDir() throws IOException {
        File expectedFile = new File("/tmp");
        File actualFile;
        finderTransformer.run(expectedFile, "TEST", false);
        final ArgumentCaptor<File> captor = ArgumentCaptor.forClass(File.class);
        verify(this.exportDataToTemplate).export(captor.capture(), any());
        actualFile = captor.getValue();
        Assert.assertEquals(expectedFile,actualFile);
    }
    @Test public void Given_loadAll_CallExportAll() throws IOException {
        finderTransformer.run(new File("/tmp"), "TEST", true);
        verify(exportDataToTemplate).exportAllGroups(any(File.class)) ;
    }
    @Test(expected=IOException.class)
    public void Given_NonExistentDefaultAndPassedRootDir_WhenRunIsCalled_returnIOexception() throws IOException {
        finderTransformer.run(new File("DOES-NOT-EXIST"), "TEST", true);
    }
}
