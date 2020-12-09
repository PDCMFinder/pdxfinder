package org.pdxfinder.dataloaders.updog.tablevalidation;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TableSetSpecificationTest {

    private final Set<String> REQUIRED_TABLES = new HashSet<>(
        Arrays.asList("file_1.tsv", "file_2.tsv"));

    private final ColumnReference REQUIRED_COLUMN_1 = ColumnReference.of("table.tsv", "column_1");
    private final ColumnReference REQUIRED_COLUMN_2 = ColumnReference.of("table.tsv", "column_2");

    @Test public void builderMethods_givenInstantiation_allReturnInstanceOfThisClass() {
        TableSetSpecification tableSetSpecification = TableSetSpecification.create();
        assertThat(tableSetSpecification, isA(TableSetSpecification.class));
    }

    @Test public void builderMethods_givenRequiredFileSetList_setsRequiredFiles() {
        TableSetSpecification tableSetSpecification = TableSetSpecification.create();
        assertEquals(
            REQUIRED_TABLES,
            tableSetSpecification
                .addRequiredTables(REQUIRED_TABLES)
                .getRequiredTables()
        );
    }

    @Test public void addRequiredColumns_givenTableColumnTwice_storesOnceInField() {
        TableSetSpecification expected = TableSetSpecification.create()
            .addRequiredColumns(REQUIRED_COLUMN_1);
        assertEquals(
            expected,
            TableSetSpecification.create()
                .addRequiredColumns(REQUIRED_COLUMN_1)
                .addRequiredColumns(REQUIRED_COLUMN_1)
        );
    }

    @Test public void addRequiredColumns_givenTwoTableColumnsSeparately_equalsGivenAsSet() {
        TableSetSpecification expected = TableSetSpecification.create()
            .addRequiredColumns(REQUIRED_COLUMN_1)
            .addRequiredColumns(REQUIRED_COLUMN_2);
        Set<ColumnReference> setOfRequiredColumns = new HashSet<>(
            Arrays.asList(REQUIRED_COLUMN_1, REQUIRED_COLUMN_2));

        assertEquals(
            expected,
            TableSetSpecification.create()
                .addRequiredColumns(setOfRequiredColumns)
        );
    }

    @Test public void toString_givenRequiredFiles_returnsAppropriateMessage() {
        TableSetSpecification tableSetSpecification = TableSetSpecification.create()
            .addRequiredTables(new HashSet<>(Arrays.asList("file1.tsv")));

        assertEquals(
            "[file1.tsv]",
            tableSetSpecification.getRequiredTables().toString()
        );
    }

    @Test public void merge_givenOneSpecification_returnsEqualSpecification() {
        Set<String> requiredTables = new HashSet<>(Collections.singletonList("table1.tsv"));
        TableSetSpecification expected =  TableSetSpecification.create().setProvider("provider")
                .addRequiredTables(requiredTables);
        assertEquals(
                expected,
                TableSetSpecification.merge(expected)
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
                TableSetSpecification.merge(specfication1, specfication2)
        );
    }

    @Test public void merge_givenTwoIdenticalSpecification_returnsEqualSpecification() {
        Set<String> requiredTables = new HashSet<>(Collections.singletonList("table1.tsv"));
        TableSetSpecification expected =  TableSetSpecification.create().setProvider("provider")
                .addRequiredTables(requiredTables);
        assertEquals(
                expected,
                TableSetSpecification.merge(expected, expected)
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
                .addRelations(Relation.betweenTableKeys(columnReference1, columnReference2));
        TableSetSpecification specification1 =  TableSetSpecification.create().setProvider("provider")
                .addRequiredTables(requiredTables1)
                .addRequiredColumns(columnReference1)
                .addNonEmptyColumns(columnReference1)
                .addUniqueColumns(columnReference1)
                .addRelations(Relation.betweenTableKeys(columnReference1, columnReference2));
        TableSetSpecification specfication2 =  TableSetSpecification.create().setProvider("provider")
                .addRequiredTables(requiredTables2)
                .addRequiredColumns(columnReference2)
                .addNonEmptyColumns(columnReference2)
                .addUniqueColumns(columnReference2)
                .addRelations(Relation.betweenTableKeys(columnReference1, columnReference2));
        assertEquals(
                expected,
                TableSetSpecification.merge(specification1, specfication2)
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
                TableSetSpecification.merge(expected, expected)
        );
    }

}