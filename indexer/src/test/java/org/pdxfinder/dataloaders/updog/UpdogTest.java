package org.pdxfinder.dataloaders.updog;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.dataloaders.updog.domainobjectcreation.DomainObjectCreator;
import org.pdxfinder.dataloaders.updog.tablevalidation.Validator;
import org.pdxfinder.dataloaders.updog.tablevalidation.error.ValidationError;
import tech.tablesaw.api.Table;

import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.pdxfinder.dataloaders.updog.TableSetUtilities.concatenate;

public class UpdogTest {

    @Mock private Reader reader;
    @Mock private TableSetCleaner tableSetCleaner;
    @Mock private Validator validator;
    @Mock private DomainObjectCreator domainObjectCreator;
    @InjectMocks private Updog updog;
    private Map<String, Table> EMPTY_TABLESET = new HashMap<>();
    private List<ValidationError> EMPTY_ERROR_LIST = new ArrayList<>();


    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(this.reader.readAllTsvFilesIn(any(), any())).thenReturn(EMPTY_TABLESET);
        when(this.reader.readAllOmicsFilesIn(any(), any())).thenReturn(EMPTY_TABLESET);
        when(this.reader.readAllTreatmentFilesIn(any(), any())).thenReturn(EMPTY_TABLESET);
        when(this.tableSetCleaner.cleanPdxTables(any())).thenReturn(EMPTY_TABLESET);
        when(this.tableSetCleaner.cleanOmicsTables(any())).thenReturn(EMPTY_TABLESET);
        when(this.validator.validate(any(), any())).thenReturn(EMPTY_ERROR_LIST);
        doNothing().when(this.domainObjectCreator).loadDomainObjects(any(), any());
    }

    @Test public void run_whenCalled_readInAllPdxFiles() {
        updog.run(Paths.get("provider/dir"), "Provider", false);
        verify(this.reader).readAllTsvFilesIn(any(), any());
    }

    @Test public void run_whenCalled_readInAllTreatmentFiles() {
        updog.run(Paths.get("provider/dir"), "Provider", false);
        verify(this.reader).readAllTreatmentFilesIn(any(), any());
    }

    @Test public void run_whenCalled_validationIsRunAtLeastOnce() {
        updog.run(Paths.get("provider/dir"), "Provider", false);
        verify(this.validator, atLeastOnce()).validate(any(), any());
    }

    @Test public void run_whenCalled_objectsCreated() {
        updog.run(Paths.get("provider/dir"), "Provider", false);
        verify(this.domainObjectCreator).loadDomainObjects(any(), any());
        verifyNoMoreInteractions(this.domainObjectCreator);
    }

    @Test public void run_whenValidationOnlyRequested_doNotRunObjectCreation() {
        updog.run(Paths.get("provider/dir"), "Provider", true);
        verifyZeroInteractions(this.domainObjectCreator);
    }

    @Test public void concatenate_whenGivenTwoEmptyLists_returnsEmptyList() {
        List<String> emptyList = Collections.emptyList();
        List<String> expected = emptyList;
        assertEquals(
            expected,
            concatenate(emptyList, emptyList)
        );
    }

    @Test public void concatenate_whenGivenThreeLists_returnsConcatenatedList() {
        List<String> listOne = Arrays.asList("item 1");
        List<String> listTwo = Arrays.asList("item 2");
        List<String> listThree = Arrays.asList("item 3");
        List<String> expected = Arrays.asList("item 1", "item 2", "item 3");
        assertEquals(
            expected,
            concatenate(listOne, listTwo, listThree)
        );
    }
}
