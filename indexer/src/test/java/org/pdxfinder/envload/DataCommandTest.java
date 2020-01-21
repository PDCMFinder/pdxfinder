package org.pdxfinder.envload;

import static org.hamcrest.CoreMatchers.allOf;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;;
import org.pdxfinder.BaseTest;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.constants.Option;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;

import static org.mockito.Mockito.*;


public class DataCommandTest extends BaseTest {

    @Mock
    private LoadMarkers loadMarkers;
    @Mock
    private LoadNCIT loadNCIT;
    @Mock
    private LoadNCITDrugs loadNCITDrugs;

    @Mock
    private DataImportService dataImportService;

    @InjectMocks
    private DataCommand dataCommand;

    private int minNumberOfInvocations;


    @Before
    public void setup() {

        minNumberOfInvocations = 1;

        doNothing().when(this.loadMarkers)
                .loadGenes(DataUrl.HUGO_FILE_URL.get());

        doNothing().when(this.loadNCIT)
                .loadOntology(any(String.class));

        doNothing().when(this.loadNCITDrugs)
                .loadRegimens();
    }

    @Test
    public void givenReloadCacheCommand_WhenRunInvoked_then_LoadCache() throws Exception {

        // given
        String command = String.format("-%s", Option.reloadCache);

        // When
        this.dataCommand.run(command);

        // Then:
        verify(this.loadMarkers, times(this.minNumberOfInvocations))
                .loadGenes(any(String.class));

        verify(this.loadNCIT, times(this.minNumberOfInvocations))
                .loadOntology(any(String.class));

        verify(this.loadNCITDrugs, times(this.minNumberOfInvocations))
                .loadRegimens();
    }


    @Test
    public void givenLoadAllCommand_WhenCacheExists_then_skipLoadCache() throws Exception {

        // given
        String command = String.format("-%s", Option.loadALL);
        int cachedMarkers = 1;

        when(this.dataImportService.countAllMarkers())
                .thenReturn(cachedMarkers);

        // When
        this.dataCommand.run(command);

        // Then
        verify(this.loadMarkers, never()).loadGenes(any(String.class));

        verify(this.loadNCIT, never()).loadOntology(any(String.class));

        verify(this.loadNCITDrugs, never()).loadRegimens();
    }


    @Test
    public void givenLoadAllCommand_WhenNoCacheExists_then_LoadCache() throws Exception {

        // given
        String command = String.format("-%s", Option.loadALL);
        int cachedMarkers = 0;

        when(this.dataImportService.countAllMarkers())
                .thenReturn(cachedMarkers);

        // When
        this.dataCommand.run(command);

        // Then
        verify(this.loadMarkers, atLeast(this.minNumberOfInvocations)).loadGenes(any(String.class));
    }



    @Test
    public void givenLoadMarkersCommand_WhenRunInvoked_then_LoadCache() throws Exception {

        // given
        String command = String.format("-%s", Option.loadMarkers);

        // When
        this.dataCommand.run(command);

        // Then: there must be at least one invocation on the saveMarker method
        verify(this.loadMarkers, times(this.minNumberOfInvocations)).loadGenes(any(String.class));
    }





}
