package org.pdxfinder.dataloaders.updog;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.dataloaders.updog.domainobjectcreation.DomainObjectCreator;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import org.pdxfinder.dataloaders.updog.tablevalidation.TableSetSpecification;
import org.pdxfinder.dataloaders.updog.tablevalidation.error.ValidationError;
import org.pdxfinder.dataloaders.updog.tablevalidation.Validator;
import tech.tablesaw.api.Table;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
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

    @Test public void run_whenCalled_readInAllOmicsFiles() {
        updog.run(Paths.get("provider/dir"), "Provider", false);
        verify(this.reader).readAllOmicsFilesIn(any(), any());
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

    @Test public void merge_givenOneSpecification_returnsEqualSpecification() {
        Set<String> requiredTables = new HashSet<>(Collections.singletonList("table1.tsv"));
        TableSetSpecification expected =  TableSetSpecification.create().setProvider("provider")
            .addRequiredTables(requiredTables);
        assertEquals(
            expected,
            Updog.merge(expected)
        );
    }

    @Test public void merge_givenTwoDifferentSpecifications_returnsCombinedSpecification() {
        Set<String> requiredTables = new HashSet<>(Collections.singletonList("table1.tsv"));
        Set<String> requiredTables2 = new HashSet<>(Collections.singletonList("table2.tsv"));
        TableSetSpecification expected =  TableSetSpecification.create().setProvider("provider")
            .addRequiredTables(requiredTables)
            .addRequiredTables(requiredTables2);
        TableSetSpecification specfication1 =  TableSetSpecification.create().setProvider("provider")
            .addRequiredTables(requiredTables);
        TableSetSpecification specfication2 =  TableSetSpecification.create().setProvider("provider")
            .addRequiredTables(requiredTables2);
        assertEquals(
            expected,
            Updog.merge(specfication1, specfication2)
        );
    }

    @Test public void merge_givenTwoIdenticalSpecification_returnsEqualSpecification() {
        Set<String> requiredTables = new HashSet<>(Collections.singletonList("table1.tsv"));
        TableSetSpecification expected =  TableSetSpecification.create().setProvider("provider")
            .addRequiredTables(requiredTables);
        assertEquals(
            expected,
            Updog.merge(expected, expected)
        );
    }

    @Test public void merge_givenTwoDifferentSpecificationsWithAllValidations_returnsCombinedSpecification() {
        Set<String> requiredTables1 = new HashSet<>(Collections.singletonList("table1.tsv"));
        Set<String> requiredTables2 = new HashSet<>(Collections.singletonList("table2.tsv"));
        ColumnReference columnReference1 = ColumnReference.of("table1", "column1");
        ColumnReference columnReference2 = ColumnReference.of("table1", "column2");

        TableSetSpecification expected =  TableSetSpecification.create().setProvider("provider")
            .addRequiredTables(requiredTables1)
            .addRequiredTables(requiredTables2)
            .addRequiredColumns(columnReference1)
            .addRequiredColumns(columnReference2)
            .addNonEmptyColumns(columnReference1)
            .addNonEmptyColumns(columnReference2)
            .addUniqueColumns(columnReference1)
            .addUniqueColumns(columnReference2)
            .addRelations(Relation.between(columnReference1, columnReference2));
        TableSetSpecification specification1 =  TableSetSpecification.create().setProvider("provider")
            .addRequiredTables(requiredTables1)
            .addRequiredColumns(columnReference1)
            .addNonEmptyColumns(columnReference1)
            .addUniqueColumns(columnReference1)
            .addRelations(Relation.between(columnReference1, columnReference2));
        TableSetSpecification specfication2 =  TableSetSpecification.create().setProvider("provider")
            .addRequiredTables(requiredTables2)
            .addRequiredColumns(columnReference2)
            .addNonEmptyColumns(columnReference2)
            .addUniqueColumns(columnReference2)
            .addRelations(Relation.between(columnReference1, columnReference2));
        assertEquals(
            expected,
            Updog.merge(specification1, specfication2)
        );
    }

    @Test public void merge_givenTwoIdenticalSpecificationWithAllValidations_returnsEqualSpecification() {
        Set<String> requiredTables = new HashSet<>(Collections.singletonList("table1.tsv"));
        Set<ColumnReference> requiredColumns = new HashSet<>(
            Collections.singletonList(ColumnReference.of("table1", "column1")));
        TableSetSpecification expected =  TableSetSpecification.create().setProvider("provider")
            .addRequiredTables(requiredTables)
            .addRequiredColumns(requiredColumns)
            .addNonEmptyColumns(requiredColumns)
            .addUniqueColumns(requiredColumns);
        assertEquals(
            expected,
            Updog.merge(expected, expected)
        );
    }

}