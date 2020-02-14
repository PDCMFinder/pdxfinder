package org.pdxfinder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.dataloaders.LoadUniversal;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;
import org.pdxfinder.utils.DataProviders;
import picocli.CommandLine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


public class FinderCommandLineTest extends BaseTest {

    @Mock private LoadDiseaseOntology loadDiseaseOntology;
    @Mock private LoadMarkers loadMarkers;
    @Mock private LoadNCIT loadNCIT;
    @Mock private LoadNCITDrugs loadNCITDrugs;
    @Mock private DataImportService dataImportService;

    @Mock private LoadUniversal loadUniversal;
    @Mock private DataProviders dataProviders;
    @Mock private DataProviders.DataProvider dataProvider;
    @InjectMocks private FinderCommandLine finderCommandLineUnderTest;
    @InjectMocks private FinderCommandLine.Load load;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.loadDiseaseOntology).run();
        doNothing().when(this.loadMarkers).loadGenes(anyString());
        doNothing().when(this.loadNCIT).loadOntology(anyString());
        doNothing().when(this.loadNCITDrugs).loadRegimens();

        doNothing().when(this.loadUniversal).run();
        doNothing().when(this.dataProvider).load();

    }

    @Test public void load_givenLoadOnlyMinimal_callsLoader() {
        String[] args = {"--only=Test_Minimal", "--data-dir=path/", "--keep-db"};
        int exitCode = new CommandLine(load).execute(args);

        assertEquals(33, exitCode);
        verify(this.dataProvider).load();
        verifyNoMoreInteractions(this.dataProvider);
    }

    @Test public void load_givenMarkerCache_skipLoadingMarkers() {
        String[] args = {"--only=Test_Minimal", "--data-dir=/path/"};
        when(this.dataImportService.markerCacheIsEmpty()).thenReturn(false);
        int exitCode = new CommandLine(load).execute(args);

        assertEquals(33, exitCode);
        verify(this.loadMarkers, never()).loadGenes(anyString());
    }

    @Test public void load_givenNoMarkerCache_loadMarkers() {
        String[] args = {"--only=Test_Minimal", "--data-dir=/path/", "--keep-db"};
        when(this.dataImportService.markerCacheIsEmpty()).thenReturn(true);
        int exitCode = new CommandLine(load).execute(args);

        assertEquals(33, exitCode);
        verify(this.loadMarkers).loadGenes(anyString());
        verifyNoMoreInteractions(this.loadMarkers);
    }

    @Test public void load_givenCachesPresentButReloadAllCachesRequested_reloadAllCaches() {
        String[] args = {"--only=Test_Minimal", "--data-dir=/path/", "--keep-db", "--clear-cache"};
        when(this.dataImportService.markerCacheIsEmpty()).thenReturn(false);
        int exitCode = new CommandLine(load).execute(args);

        assertEquals(33, exitCode);
        verify(this.loadMarkers).loadGenes(anyString());
        verify(this.loadNCIT).loadOntology(anyString());
        verify(this.loadNCITDrugs).loadRegimens();
        verifyNoMoreInteractions(this.loadMarkers);
        verifyNoMoreInteractions(this.loadNCIT);
        verifyNoMoreInteractions(this.loadNCITDrugs);
    }

}