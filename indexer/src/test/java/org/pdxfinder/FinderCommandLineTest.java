package org.pdxfinder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.dataloaders.LoadUniversal;
import org.pdxfinder.dataloaders.UniversalLoader;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;
import org.pdxfinder.utils.DataProviders;
import picocli.CommandLine;

import static org.mockito.Mockito.*;
import static org.mockito.Matchers.any;

public class FinderCommandLineTest {

    @Mock private LoadDiseaseOntology loadDiseaseOntology;
    @Mock private LoadMarkers loadMarkers;
    @Mock private LoadNCIT loadNCIT;
    @Mock private LoadNCITDrugs loadNCITDrugs;
    @Mock private DataImportService dataImportService;
    @Mock private LoadUniversal loadUniversal;
    @Mock private DataProviders.DataProvider dataProvider;
    @InjectMocks private FinderCommandLine finderCommandLineUnderTest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.loadDiseaseOntology).run();
        doNothing().when(this.loadMarkers).loadGenes(DataUrl.HUGO_FILE_URL.get());
        doNothing().when(this.loadNCIT).loadOntology(any(String.class));
        doNothing().when(this.loadNCITDrugs).loadRegimens();
        // TODO stub method, change when new Updog added.
        doNothing().when(this.loadUniversal).run();
//        doNothing().when(this.dataProvider).run();
    }

    @Test
    public void execute_givenLoadMinimal_callsUpdog() throws Exception {
        String[] args = {"load", "--only=Test_Minimal", "--data-dir=path/"};
        new CommandLine(finderCommandLineUnderTest).execute(args);
        verify(this.loadUniversal, times(1)).run();
    }

    @Test public void run_givenCacheExists_skipReloadCache() {
        String[] args = {"load", "-o Test_Minimal", "-d path/"};
        new CommandLine(finderCommandLineUnderTest).execute();
        verify(this.loadMarkers, never()).loadGenes(any(String.class));
        verify(this.loadNCIT, never()).loadOntology(any(String.class));
        verify(this.loadNCITDrugs, never()).loadRegimens();
    }

    @Test public void run_givenCacheDoesNotExist_loadCache() {
        String[] args = {"load", "-o Test_Minimal", "-d path/"};
        new CommandLine(finderCommandLineUnderTest).execute();
        verify(this.loadMarkers, times(1)).loadGenes(any(String.class));
        verify(this.loadNCIT, times(1)).loadOntology(any(String.class));
        verify(this.loadNCITDrugs, times(1)).loadRegimens();
    }

    @Test public void run_givenReloadCacheCommand_loadCache() throws Exception {
        String[] args = {"load", "-o Test_Minimal", "-d path/",  "-c"};
        new CommandLine(finderCommandLineUnderTest).execute();
        verify(this.loadMarkers, times(1)).loadGenes(any(String.class));
        verify(this.loadNCIT, times(1)).loadOntology(any(String.class));
        verify(this.loadNCITDrugs, times(1)).loadRegimens();

    }
}