package org.pdxfinder.commandline;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.BaseTest;
import org.pdxfinder.services.constants.DataProvider;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;


public class FinderCommandLineTest extends BaseTest {

    @Mock private FinderLoader finderLoader;
    @InjectMocks private FinderCommandLine.Load load;

    @Mock private FinderTransformer finderTransformer;
    @InjectMocks private FinderCommandLine.Transform transform;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.finderLoader).run(
                anyListOf(DataProvider.class),
                any(File.class),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()
        );
    }

    @Test public void givenLoadOnlyMinimal_LoaderIsCalled() {
        String[] args = {"--only=Test_Minimal", "--data-dir=path/"};
        int exitCode = new CommandLine(load).execute(args);
        assertEquals(0, exitCode);
        verify(this.finderLoader).run(
                anyList(),
                any(File.class),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()
        );
        verifyNoMoreInteractions(this.finderLoader);
    }

    @Test public void givenLoadAll_callsLoader() {
        String[] args = {"--group=All", "--data-dir=path/"};
        int exitCode = new CommandLine(load).execute(args);
        assertEquals(0, exitCode);
        verify(this.finderLoader).run(
                anyList(),
                any(File.class),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()
        );
        verifyNoMoreInteractions(this.finderLoader);
    }

    @Test public void givenTransform_When_cbioPortalIsCalled_Then_callsTransformer() throws IOException {
        String[] args = {"-c=MUT", "-f=/tmp"};
        int exitCode = new CommandLine(transform).execute(args);
        assertEquals(0, exitCode);
        verify(this.finderTransformer).run(
                any(),
                eq(null),
                eq(null),
                any(File.class),
                any(),
                any()
        );
        verifyNoMoreInteractions(this.finderTransformer);
    }

    @Test public void givenTransform_WhenTwoExclusiveArgumentsArepassed_Then_ReturnNonZeroExit() {
        String[] args = {"--data-dir=path/", "--export=test", "--all"};
        int exitCode = new CommandLine(transform).execute(args);
        assertNotEquals(0,exitCode);
    }





}