package org.pdxfinder.commandline;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.pdxfinder.BaseTest;
import org.pdxfinder.LoadDiseaseOntology;
import org.pdxfinder.dataloaders.LoadJAXData;
import org.pdxfinder.dataloaders.updog.Updog;
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

import java.nio.file.Path;
import java.io.File;
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
    @Mock private LoadJAXData loadJAXData;
    @Mock private Updog updog;

    private DataProvider dataProvider;
    private DataProvider updogDataProvider;
    private static boolean NO_VALIDATION_ONLY = false;

    @Mock private LoadAdditionalDatasets loadAdditionalDatasets;
    @Mock private LinkSamplesToNCITTerms linkSamplesToNCITTerms;
    @Mock private LinkTreatmentsToNCITTerms linkTreatmentsToNCITTerms;
    @Mock private CreateDataProjections createDataProjections;
    @Mock private SetDataVisibility setDataVisibility;
    @Mock private ValidateDB validateDB;
    @Mock private File dataDirectory;

    @Spy
    @InjectMocks
    private FinderLoader finderLoader;
    private boolean isFalse = false;
    private boolean isTrue = true;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.loadJAXData).run();
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
        doNothing().when(this.updog).run(any(Path.class), anyString(), anyBoolean());

        this.dataProvider = DataProvider.JAX;
        this.updogDataProvider = DataProvider.UOC_BC;
    }



    @Test public void run_givenSingleProvider_callsRelevantCustomLoader() throws Exception {
        finderLoader.run(
            Collections.singletonList(dataProvider),
            dataDirectory,
            NO_VALIDATION_ONLY,
            isFalse, isFalse, isFalse);
        verify(this.loadJAXData).run();
        verifyNoMoreInteractions(this.loadJAXData);
    }

    @Test public void run_givenSingleProvider_callsRelevantUpdogLoader() throws Exception {
        finderLoader.run(
            Collections.singletonList(updogDataProvider),
            dataDirectory,
            NO_VALIDATION_ONLY,
            isFalse, isFalse, isFalse);
        verify(this.updog).run(any(Path.class), anyString(), anyBoolean());
        verifyNoMoreInteractions(this.updog);
    }

    @Test public void run_givenTwoProviders_callsRelevantLoaderForEach() throws Exception {
        finderLoader.run(
            Arrays.asList(dataProvider, updogDataProvider),
            dataDirectory,
            NO_VALIDATION_ONLY,
            isFalse, isFalse, isFalse);
        verify(this.loadJAXData).run();
        verify(this.updog).run(any(Path.class), anyString(), anyBoolean());
        verifyNoMoreInteractions(this.loadJAXData);
        verifyNoMoreInteractions(this.updog);
    }

    @Test public void run_givenZeroProviders_callNoLoaders() throws Exception {
        finderLoader.run(Arrays.asList(),
            dataDirectory,
            NO_VALIDATION_ONLY,
            isFalse, isFalse, isFalse);
        verify(this.loadJAXData, never()).run();
        verify(this.updog, never()).run(any(Path.class), anyString(), anyBoolean());
    }

    @Test public void load_givenMarkerCache_skipLoadingMarkers() {
        givenEmptyMarkerCache(isFalse);
        finderLoader.run(Collections.singletonList(dataProvider), dataDirectory,
                         NO_VALIDATION_ONLY, isFalse, isFalse, isFalse);
        verify(this.loadMarkers, never()).loadGenes(anyString());
    }

    @Test public void load_givenNoMarkerCache_loadMarkers() {
        givenEmptyMarkerCache(isTrue);
        finderLoader.run(Collections.singletonList(dataProvider), dataDirectory, NO_VALIDATION_ONLY, isFalse, isFalse, isFalse);
        verify(this.loadMarkers).loadGenes(anyString());
        verifyNoMoreInteractions(this.loadMarkers);
    }

    @Test public void load_givenMarkerCacheButReloadRequested_reloadMarkers() {
        givenEmptyMarkerCache(isFalse);
        finderLoader.run(Collections.singletonList(dataProvider), dataDirectory, NO_VALIDATION_ONLY, isTrue, isFalse, isFalse);
        verify(this.loadMarkers).loadGenes(anyString());
        verifyNoMoreInteractions(this.loadMarkers);
    }

    @Test public void load_givenOntologyCache_skipLoadingOntologyTerms() {
        givenEmptyOntologyCache(isFalse);
        finderLoader.run(Collections.singletonList(dataProvider), dataDirectory, NO_VALIDATION_ONLY, isFalse, isFalse, isFalse);
        verify(this.loadMarkers, never()).loadGenes(anyString());
    }

    @Test public void load_givenNoOntologyCache_loadOntologyTerms() {
        givenEmptyOntologyCache(isTrue);
        finderLoader.run(Collections.singletonList(dataProvider), dataDirectory, NO_VALIDATION_ONLY, isFalse, isFalse, isFalse);
        verify(this.loadNCIT).loadOntology(DataUrl.DISEASES_BRANCH_URL.get());
        verifyNoMoreInteractions(this.loadNCIT);
    }

    @Test public void load_givenOntologyCacheButReloadRequested_reloadOntologyTerms() {
        givenEmptyOntologyCache(isFalse);
        finderLoader.run(Collections.singletonList(dataProvider), dataDirectory, NO_VALIDATION_ONLY, isTrue, isFalse, isFalse);
        verify(this.loadNCIT).loadOntology(anyString());
        verifyNoMoreInteractions(this.loadNCIT);
    }

    @Test public void load_givenOntologyCache_skipLoadingRegimens() {
        givenEmptyOntologyCache(isFalse, "treatment");
        finderLoader.run(Collections.singletonList(dataProvider), dataDirectory, NO_VALIDATION_ONLY, isFalse, isFalse, isFalse);
        verify(this.loadNCITDrugs, never()).loadRegimens();
    }

    @Test public void load_givenNoOntologyCache_loadRegimens() {
        givenEmptyOntologyCache(isTrue, "treatment");
        finderLoader.run(Collections.singletonList(dataProvider), dataDirectory, NO_VALIDATION_ONLY, isFalse, isFalse, isFalse);
        verify(this.loadNCITDrugs).loadRegimens();
        verifyNoMoreInteractions(this.loadNCITDrugs);
    }

    @Test public void load_givenOntologyCacheButReloadRequested_reloadRegimens() {
        givenEmptyOntologyCache(isFalse, "treatment");
        finderLoader.run(Collections.singletonList(dataProvider), dataDirectory, NO_VALIDATION_ONLY, isTrue, isFalse, isFalse);
        verify(this.loadNCITDrugs).loadRegimens();
        verifyNoMoreInteractions(this.loadNCITDrugs);
    }

    private void givenEmptyOntologyCache(boolean b) {
        when(this.dataImportService.ontologyCacheIsEmpty()).thenReturn(b);
    }

    private void givenEmptyOntologyCache(boolean b, String type) {
        when(this.dataImportService.ontologyCacheIsEmptyByType(type)).thenReturn(b);
    }

    private void givenEmptyMarkerCache(boolean b) {
        when(this.dataImportService.markerCacheIsEmpty()).thenReturn(b);
    }

}