package org.pdxfinder.dataloaders.updog;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.*;

public class TableSetSpecificationTest {

    private final Set<String> REQUIRED_TABLES = Stream
        .of("file_1.tsv", "file_2.tsv")
        .collect(Collectors.toSet());

    private final Pair<String, String> REQUIRED_COLUMN_1 = Pair.of("table.tsv", "column_1");
    private final Pair<String, String> REQUIRED_COLUMN_2 = Pair.of("table.tsv", "column_2");

    @Test
    public void builderMethods_givenInstantiation_allReturnInstanceOfThisClass() {
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
        assertEquals(
            expected,
            TableSetSpecification.create()
                .addRequiredColumns(new HashSet<>(Arrays.asList(REQUIRED_COLUMN_1, REQUIRED_COLUMN_2)))
        );
    }

}