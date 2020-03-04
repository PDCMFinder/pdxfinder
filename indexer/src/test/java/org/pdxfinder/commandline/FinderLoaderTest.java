package org.pdxfinder.commandline;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.pdxfinder.BaseTest;
import org.pdxfinder.LoadDiseaseOntology;
import org.pdxfinder.services.constants.DataProvider;
import org.pdxfinder.dataloaders.LoadAdditionalDatasets;
import org.pdxfinder.mapping.LinkSamplesToNCITTerms;
import org.pdxfinder.mapping.LinkTreatmentsToNCITTerms;
import org.pdxfinder.postload.CreateDataProjections;
import org.pdxfinder.postload.SetDataVisibility;
import org.pdxfinder.postload.ValidateDB;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class FinderLoaderTest extends BaseTest {

    @Mock private LoadDiseaseOntology loadDiseaseOntology;
    @Mock private LoadMarkers loadMarkers;
    @Mock private LoadNCIT loadNCIT;
    @Mock private LoadNCITDrugs loadNCITDrugs;
    @Mock private DataImportService dataImportService;

    private DataProvider dataProvider;

    @Mock private LoadAdditionalDatasets loadAdditionalDatasets;
    @Mock private LinkSamplesToNCITTerms linkSamplesToNCITTerms;
    @Mock private LinkTreatmentsToNCITTerms linkTreatmentsToNCITTerms;
    @Mock private CreateDataProjections createDataProjections;
    @Mock private SetDataVisibility setDataVisibility;
    @Mock private ValidateDB validateDB;

    @Spy
    @InjectMocks
    private FinderLoader finderLoader;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.loadDiseaseOntology).run();
        doNothing().when(this.loadMarkers).loadGenes(anyString());
        doNothing().when(this.loadNCIT).loadOntology(anyString());
        doNothing().when(this.loadNCITDrugs).loadRegimens();
        doNothing().when(this.loadAdditionalDatasets).run();
        doNothing().when(this.linkSamplesToNCITTerms).run();
        doNothing().when(this.linkTreatmentsToNCITTerms).run();
        doNothing().when(this.createDataProjections).run();
        doNothing().when(this.setDataVisibility).run();
        doNothing().when(this.validateDB).run();

        this.dataProvider = DataProvider.JAX;
    }



    @Test public void run_givenSingleProvider_callsRelevantLoader() {
        doNothing().when(this.finderLoader).callRelevantLoader(dataProvider);
        finderLoader.run(Collections.singletonList(dataProvider), false, true, false);
        verify(this.finderLoader).callRelevantLoader(any(DataProvider.class));
        //verifyNoMoreInteractions(this.finderLoader.callRelevantLoader(dataProvider));
    }

    @Test public void run_givenTwoProviders_callsRelevantLoaderForEach() {
        doNothing().when(this.finderLoader).callRelevantLoader(dataProvider);
        finderLoader.run(Arrays.asList(dataProvider, dataProvider), false, true, false);
        verify(this.finderLoader, times(2)).callRelevantLoader(any(DataProvider.class));
//        verifyNoMoreInteractions(this.finderLoader);
    }

    @Test public void load_givenMarkerCache_skipLoadingMarkers() {
        givenEmptyMarkerCache(false);
        finderLoader.run(Collections.singletonList(dataProvider), false, true, false);
        verify(this.loadMarkers, never()).loadGenes(anyString());
    }

    @Test public void load_givenNoMarkerCache_loadMarkers() {
        givenEmptyMarkerCache(true);
        finderLoader.run(Collections.singletonList(dataProvider), false, true, false);
        verify(this.loadMarkers).loadGenes(anyString());
        verifyNoMoreInteractions(this.loadMarkers);
    }

    @Test public void load_givenMarkerCacheButReloadRequested_reloadMarkers() {
        givenEmptyMarkerCache(false);
        finderLoader.run(Collections.singletonList(dataProvider), true, true, false);
        verify(this.loadMarkers).loadGenes(anyString());
        verifyNoMoreInteractions(this.loadMarkers);
    }

    @Test public void load_givenOntologyCache_skipLoadingOntologyTerms() {
        givenEmptyOntologyCache(false);
        finderLoader.run(Collections.singletonList(dataProvider), false, true, false);
        verify(this.loadMarkers, never()).loadGenes(anyString());
    }

    @Test public void load_givenNoOntologyCache_loadOntologyTerms() {
        givenEmptyOntologyCache(true);
        finderLoader.run(Collections.singletonList(dataProvider),false, true, false);
        verify(this.loadNCIT).loadOntology(DataUrl.DISEASES_BRANCH_URL.get());
        verifyNoMoreInteractions(this.loadNCIT);
    }

    @Test public void load_givenOntologyCacheButReloadRequested_reloadOntologyTerms() {
        givenEmptyOntologyCache(false);
        finderLoader.run(Collections.singletonList(dataProvider), true, true, false);
        verify(this.loadNCIT).loadOntology(anyString());
        verifyNoMoreInteractions(this.loadNCIT);
    }

    @Test public void load_givenOntologyCache_skipLoadingRegimens() {
        givenEmptyOntologyCache(false);
        finderLoader.run(Collections.singletonList(dataProvider), false, true, false);
        verify(this.loadNCITDrugs, never()).loadRegimens();
    }

    @Test public void load_givenNoOntologyCache_loadRegimens() {
        givenEmptyOntologyCache(true);
        finderLoader.run(Collections.singletonList(dataProvider), false, true, false);
        verify(this.loadNCITDrugs).loadRegimens();
        verifyNoMoreInteractions(this.loadNCITDrugs);
    }

    @Test public void load_givenOntologyCacheButReloadRequested_reloadRegimens() {
        givenEmptyOntologyCache(false);
        finderLoader.run(Collections.singletonList(dataProvider), true, true, false);
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