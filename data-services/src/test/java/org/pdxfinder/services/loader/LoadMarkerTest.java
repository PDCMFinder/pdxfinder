package org.pdxfinder.services.loader;

import static org.hamcrest.CoreMatchers.allOf;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.Marker;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadMarkers;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;


public class LoadMarkerTest extends BaseTest {

    @Mock
    private DataImportService dataImportService;

    @InjectMocks
    private LoadMarkers loadMarkers;

    private String hgncName = "TEST_MARKER_NAME";
    private String hgncSymbol = "TEST_MARKER_SYMBOL";

    private Marker marker = new Marker();

    @Before
    public void setup() {

        marker.setHgncSymbol(hgncSymbol);
        marker.setHgncName(hgncName);

        // given
        Marker expectedMarker = marker;
        doReturn(expectedMarker).when(dataImportService).saveMarker(expectedMarker);
    }


    @Test
    public void given_MarkerDataUrl_When_LoadGenesInvoked_Then_saveMarkers() {

        // given
        String markerDataUrl = DataUrl.HUGO_FILE_URL.get();

        // When
        loadMarkers.loadGenes(markerDataUrl);

        // Then
        verify(dataImportService, atLeast(100)).saveMarker(any(Marker.class));

    }

    @Test
    public void given_WrongUrl_When_LoadGenesInvokedWithMalformedURL_then_noMarkerIsSaved() {

        // given
        String malformedGeneURL = "genemaes.org";

        // When
        loadMarkers.loadGenes(malformedGeneURL);

        // Then
        verify(dataImportService, never()).saveMarker(any(Marker.class));

    }



}
