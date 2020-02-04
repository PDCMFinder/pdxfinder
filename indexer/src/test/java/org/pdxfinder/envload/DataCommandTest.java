package org.pdxfinder.envload;

import static org.hamcrest.CoreMatchers.allOf;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadNCIT;
import org.pdxfinder.services.loader.envload.LoadMarkers;
import org.pdxfinder.constants.Option;
import org.pdxfinder.services.loader.envload.LoadNCITDrugs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class DataCommandTest extends BaseTest {

    private Logger log = LoggerFactory.getLogger(DataCommandTest.class);

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
    public void given_ReloadCacheCommand_WhenRunInvoked_then_LoadCache() throws Exception {

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
    public void given_LoadAllCommand_WhenCacheExists_then_skipLoadCache() throws Exception {

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
    public void given_LoadAllCommand_WhenNoCacheExists_then_LoadCache() throws Exception {

        // given
        String command = String.format("-%s", Option.loadALL);
        int cachedMarkers = 0;

        when(this.dataImportService.countAllMarkers())
                .thenReturn(cachedMarkers);

        // When
        this.dataCommand.run(command);

        // Then
        verify(this.loadMarkers, atLeast(this.minNumberOfInvocations)).loadGenes(any(String.class));

        verify(this.loadNCIT, atLeast(this.minNumberOfInvocations)).loadOntology(any(String.class));

        verify(this.loadNCITDrugs, atLeast(this.minNumberOfInvocations)).loadRegimens();
    }


    @Test
    public void given_LoadMarkersCommand_WhenRunInvoked_then_LoadMarkersOnly() throws Exception {

        // given
        String command = String.format("-%s", Option.loadMarkers);

        // When
        this.dataCommand.run(command);

        // Then: there must be at least one invocation on the saveMarker method
        verify(this.loadMarkers, times(this.minNumberOfInvocations)).loadGenes(any(String.class));

        verify(this.loadNCIT, never()).loadOntology(any(String.class));

        verify(this.loadNCITDrugs, never()).loadRegimens();
    }


    @Test
    public void given_LoadNCITCommand_WhenRunInvoked_then_LoadNCITOntologyOnly() throws Exception {

        // given
        String command = String.format("-%s", Option.loadNCIT);

        // When
        this.dataCommand.run(command);

        // Then: there must be at least one invocation on the saveMarker method
        verify(this.loadNCIT, times(this.minNumberOfInvocations)).loadOntology(any(String.class));

        verify(this.loadMarkers, never()).loadGenes(any(String.class));

        verify(this.loadNCITDrugs, never()).loadRegimens();
    }


    @Test
    public void givenLoadNCITDrugsCommand_WhenRunInvoked_then_LoadNCITDrugsOnly() throws Exception {

        // given
        String command = String.format("-%s", Option.loadNCITDrugs);

        // When
        this.dataCommand.run(command);

        // Then: there must be at least one invocation on the saveMarker method
        verify(this.loadNCITDrugs, times(this.minNumberOfInvocations)).loadRegimens();

        verify(this.loadMarkers, never()).loadGenes(any(String.class));

        verify(this.loadNCIT, never()).loadOntology(any(String.class));
    }


    @Test
    public void givenLoadNCITPreDefCommand_WhenRunInvoked_then_LoadPredefinedFileOnly() throws Exception {

        // given
        String command = String.format("-%s", Option.loadNCITPreDef);

        // When
        this.dataCommand.run(command);

        // Then: there must be at least one invocation on the saveMarker method
        verify(this.loadNCIT, times(this.minNumberOfInvocations)).loadNCITPreDef(any(String.class));

        verify(this.loadMarkers, never()).loadGenes(any(String.class));

        verify(this.loadNCIT, never()).loadOntology(any(String.class));

        verify(this.loadNCITDrugs, never()).loadRegimens();
    }




    @Test
    public void given_MultipleCommands_WhenRunInvoked_then_LoadRelevantData() throws Exception {

        // given
        String command1 = String.format("-%s", Option.loadMarkers);
        String command2 = String.format("-%s", Option.loadSlim);
        String command3 = String.format("-%s", Option.loadEssentials);
        String command4 = String.format("-%s", Option.loadALL);

        int cachedMarkers = 1;

        when(this.dataImportService.countAllMarkers())
                .thenReturn(cachedMarkers);

        // When
        dataCommand.run( command1, "arg1", command2, "arg2", command3, "arg3", command4, "arg4");

        // Then
        verify(this.loadMarkers, atLeast(this.minNumberOfInvocations)).loadGenes(any(String.class));

        verify(this.loadNCIT, atLeast(this.minNumberOfInvocations)).loadOntology(any(String.class));

        verify(this.loadNCITDrugs, atLeast(this.minNumberOfInvocations)).loadRegimens();
    }


}
