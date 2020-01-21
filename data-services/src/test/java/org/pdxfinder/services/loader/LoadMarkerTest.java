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

import static org.mockito.Mockito.*;


public class LoadMarkerTest extends BaseTest {

    @Mock
    private DataImportService dataImportService;

    @InjectMocks
    private LoadMarkers loadMarkers;

    private String hgncName = "TEST_MARKER_NAME";
    private String hgncSymbol = "TEST_MARKER_SYMBOL";

    private String malformedGeneURL = "genemaes.org";

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
    public void given_DataImportServiceMocked_When_LoadGenesInvoked_Then_saveMarkers() {

        // When
        loadMarkers.loadGenes(DataUrl.HUGO_FILE_URL.get());

        // Then
        verify(dataImportService, atLeast(100)).saveMarker(any(Marker.class));

    }


    @Test
    public void given_When_LoadGenesInvokedWithMalformedURL_then_noMarkerIsSaved() {

        // When
        loadMarkers.loadGenes(malformedGeneURL);

        // Then
        verify(dataImportService, never()).saveMarker(any(Marker.class));

    }



















    /*


       // when(this.dataImportService.saveMarker(marker)).thenReturn(marker);


    @Test
    public void when_ReloadCacheCommand_then_loadMarkers() throws Exception {

        loadMarkers.run("-reloadCache");

    }


    @Test
    public void loadMarkerTest() {

        when(this.dataImportService.saveMarker(marker)).thenReturn(marker);

        //Marker marker = dataImportService.saveMarker(this.marker);

        loadMarkers.loadMarkers();

     //   assertEquals(marker, this.marker);
    }


    @Test
    public void markerSaveTest() {

        when(markerRepository.save(marker)).thenReturn(marker);

        Marker marker = dataImportService.saveMarker(this.marker);

       // assertEquals(marker, this.marker);
    }







        @Mock
    private Reader reader;

    @Mock
    private Writer writer;

    @Test(expected = IllegalArgumentException.class)
    public void testNoArgs() throws Exception {
        app.run();
    }
     */
}
