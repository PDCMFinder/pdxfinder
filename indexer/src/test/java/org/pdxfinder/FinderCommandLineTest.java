package org.pdxfinder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.utils.DataProviders;
import picocli.CommandLine;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class FinderCommandLineTest extends BaseTest {

    @Mock private LoaderNew loaderNew;
    @InjectMocks private FinderCommandLine.Load load;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.loaderNew).run(
            anyListOf(DataProviders.DataProvider.class),
            any(String.class),
            anyBoolean(),
            anyBoolean()
        );

    }

    @Test public void load_givenLoadOnlyMinimal_callsLoader() {
        String[] args = {"--only=Test_Minimal", "--data-dir=path/", "--keep-db"};
        int exitCode = new CommandLine(load).execute(args);
        assertEquals(0, exitCode);
        verify(this.loaderNew).run(
            anyListOf(DataProviders.DataProvider.class),
            any(String.class),
            anyBoolean(),
            anyBoolean()
        );
        verifyNoMoreInteractions(this.loaderNew);
    }

}