package org.pdxfinder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;
import org.pdxfinder.utils.DataProviders;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class LoaderNewTest extends BaseTest {

    @Mock private LoadDiseaseOntology loadDiseaseOntology;
    @Mock private LoadMarkers loadMarkers;
    @Mock private LoadNCIT loadNCIT;
    @Mock private LoadNCITDrugs loadNCITDrugs;
    @Mock private DataImportService dataImportService;
    @Mock private DataProviders.DataProvider dataProvider;
    @Mock private File dataDirectory;
    @InjectMocks private LoaderNew loaderNew;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.loadDiseaseOntology).run();
        doNothing().when(this.loadMarkers).loadGenes(anyString());
        doNothing().when(this.loadNCIT).loadOntology(anyString());
        doNothing().when(this.loadNCITDrugs).loadRegimens();
        doNothing().when(this.dataProvider).load();
    }

    @Test public void run_givenSingleProvider_callsRelevantLoader() {
        loaderNew.run(Collections.singletonList(dataProvider), dataDirectory, false, true);
        verify(this.dataProvider).load();
        verifyNoMoreInteractions(this.dataProvider);
    }

    @Test public void run_givenTwoProviders_callsRelevantLoaderForEach() {
        loaderNew.run(Arrays.asList(dataProvider, dataProvider), dataDirectory, false, true);
        verify(this.dataProvider, times(2)).load();
        verifyNoMoreInteractions(this.dataProvider);
    }

    @Test public void load_givenMarkerCache_skipLoadingMarkers() {
        givenEmptyMarkerCache(false);
        loaderNew.run(Collections.singletonList(dataProvider), dataDirectory, false, true);
        verify(this.loadMarkers, never()).loadGenes(anyString());
    }

    @Test public void load_givenNoMarkerCache_loadMarkers() {
        givenEmptyMarkerCache(true);
        loaderNew.run(Collections.singletonList(dataProvider), dataDirectory, false, true);
        verify(this.loadMarkers).loadGenes(anyString());
        verifyNoMoreInteractions(this.loadMarkers);
    }

    @Test public void load_givenMarkerCacheButReloadRequested_reloadMarkers() {
        givenEmptyMarkerCache(false);
        loaderNew.run(Collections.singletonList(dataProvider), dataDirectory, true, true);
        verify(this.loadMarkers).loadGenes(anyString());
        verifyNoMoreInteractions(this.loadMarkers);
    }

    @Test public void load_givenOntologyCache_skipLoadingOntologyTerms() {
        givenEmptyOntologyCache(false);
        loaderNew.run(Collections.singletonList(dataProvider), dataDirectory, false, true);
        verify(this.loadMarkers, never()).loadGenes(anyString());
    }

    @Test public void load_givenNoOntologyCache_loadOntologyTerms() {
        givenEmptyOntologyCache(true);
        loaderNew.run(Collections.singletonList(dataProvider), dataDirectory,false, true);
        verify(this.loadNCIT).loadOntology(anyString());
        verifyNoMoreInteractions(this.loadNCIT);
    }

    @Test public void load_givenOntologyCacheButReloadRequested_reloadOntologyTerms() {
        givenEmptyOntologyCache(false);
        loaderNew.run(Collections.singletonList(dataProvider), dataDirectory, true, true);
        verify(this.loadNCIT).loadOntology(anyString());
        verifyNoMoreInteractions(this.loadNCIT);
    }

    @Test public void load_givenOntologyCache_skipLoadingRegimens() {
        givenEmptyOntologyCache(false);
        loaderNew.run(Collections.singletonList(dataProvider), dataDirectory, false, true);
        verify(this.loadNCITDrugs, never()).loadRegimens();
    }

    @Test public void load_givenNoOntologyCache_loadRegimens() {
        givenEmptyOntologyCache(true);
        loaderNew.run(Collections.singletonList(dataProvider), dataDirectory, false, true);
        verify(this.loadNCITDrugs).loadRegimens();
        verifyNoMoreInteractions(this.loadNCITDrugs);
    }

    @Test public void load_givenOntologyCacheButReloadRequested_reloadRegimens() {
        givenEmptyOntologyCache(false);
        loaderNew.run(Collections.singletonList(dataProvider), dataDirectory, true, true);
        verify(this.loadNCITDrugs).loadRegimens();
        verifyNoMoreInteractions(this.loadNCITDrugs);
    }

    private void givenEmptyOntologyCache(boolean b) {
        when(this.dataImportService.ontologyCacheIsEmpty()).thenReturn(b);
    }

    private void givenEmptyMarkerCache(boolean b) {
        when(this.dataImportService.markerCacheIsEmpty()).thenReturn(b);
    }

}