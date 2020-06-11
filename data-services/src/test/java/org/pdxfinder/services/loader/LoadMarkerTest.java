package org.pdxfinder.services.loader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.Marker;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadMarkers;

import java.io.IOException;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

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
    public void given_MarkerDataUrl_When_LoadGenesInvoked_Then_saveMarkers() throws Exception {

        // given
        String markerDataUrl = DataUrl.HUGO_FILE_URL.get();

        // When
        loadMarkers.loadGenes(markerDataUrl);

        // Then
        verify(dataImportService).saveAllMarkers(anyListOf(Marker.class));

    }

}
