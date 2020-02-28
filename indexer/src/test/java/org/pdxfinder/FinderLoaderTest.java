package org.pdxfinder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.dataloaders.LoadAdditionalDatasets;
import org.pdxfinder.mapping.LinkTreatmentsToNCITTerms;
import org.pdxfinder.postload.CreateDataProjections;
import org.pdxfinder.postload.SendNotifications;
import org.pdxfinder.postload.SetDataVisibility;
import org.pdxfinder.postload.ValidateDB;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;
import org.pdxfinder.utils.DataProviders;

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
    @Mock private DataProviders.DataProvider dataProvider;

    @Mock private LoadAdditionalDatasets loadAdditionalDatasets;
    @Mock private LinkTreatmentsToNCITTerms linkTreatmentsToNCITTerms;
    @Mock private CreateDataProjections createDataProjections;
    @Mock private SetDataVisibility setDataVisibility;
    @Mock private ValidateDB validateDB;
    @Mock private SendNotifications sendNotifications;
    @Mock private ValidateGeneSymbols validateGeneSymbols;

    @InjectMocks private FinderLoader finderLoader;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.loadDiseaseOntology).run();
        doNothing().when(this.loadMarkers).loadGenes(anyString());
        doNothing().when(this.loadNCIT).loadOntology(anyString());
        doNothing().when(this.loadNCITDrugs).loadRegimens();
        doNothing().when(this.dataProvider).load();
        doNothing().when(this.loadAdditionalDatasets).run();
        doNothing().when(this.linkTreatmentsToNCITTerms).run();
        doNothing().when(this.createDataProjections).run();
        doNothing().when(this.setDataVisibility).run();
        doNothing().when(this.validateDB).run();
        doNothing().when(this.sendNotifications).run();
        doNothing().when(this.validateGeneSymbols).run();
    }



    @Test public void run_givenSingleProvider_callsRelevantLoader() {
        finderLoader.run(Collections.singletonList(dataProvider), false, true, false);
        verify(this.dataProvider).load();
        verifyNoMoreInteractions(this.dataProvider);
    }

    @Test public void run_givenTwoProviders_callsRelevantLoaderForEach() {
        finderLoader.run(Arrays.asList(dataProvider, dataProvider), false, true, false);
        verify(this.dataProvider, times(2)).load();
        verifyNoMoreInteractions(this.dataProvider);
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