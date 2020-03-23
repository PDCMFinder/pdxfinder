package org.pdxfinder.dataloaders.updog;

import org.junit.Test;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.pdxfinder.dataloaders.updog.TableSetUtilities.*;

public class TableSetUtilitiesTest {

    @Test
    public void cleanPdxTableSet() {
    }

    @Test
    public void removeHeaderRows_givenHeader_headerRemoved() {
        Map<String, Table> tableSet = new HashMap<>();
        Arrays.asList("table_1.tsv", "table_2.tsv").forEach(
            s -> tableSet.put(s, Table.create().addColumns(StringColumn.create(
                "column_name",
                Arrays.asList("Header2", "Header3", "Header4", "Header5", "1")))));
        Map<String, Table> expected = new HashMap<>();
        Arrays.asList("table_1.tsv", "table_2.tsv").forEach(
            s -> expected.put(s, Table.create().addColumns(StringColumn.create(
                "column_name",
                Collections.singletonList("1")))));
        assertEquals(
            expected.toString(),
            removeHeaderRows(tableSet).toString()
        );
    }

    @Test public void removeHeaderRowsIfPresent_givenTableSet_runsOnEachTable() {
        Map<String, Table> tableSet = new HashMap<>();
        Arrays.asList("table_1.tsv", "table_2.tsv").forEach(
            s -> tableSet.put(s, Table.create(s, Collections.emptyList())));
        Map<String, Table> expected = tableSet;
        assertEquals(
            expected,
            removeHeaderRowsIfPresent(tableSet)
        );
    }

    @Test public void removeHeaderRowsIfPresent_givenHeadersPresent_removeHeaders() {
        Table table = Table.create().addColumns(
            StringColumn.create("Field", Arrays.asList("Description", "Example", "Format Requirements", "Essential?")),
            StringColumn.create("required_column", Arrays.asList(
                "required col description",
                "example value",
                "alphanumeric",
                "essential")));
        Table expected = Table.create().addColumns(
            StringColumn.create("Field", Collections.emptyList()),
            StringColumn.create("required_column", Collections.emptyList()));
        assertEquals(
            expected.toString(),
            removeHeaderRowsIfPresent(table).toString()
        );
    }

    @Test public void removeHeaderRowsIfPresent_givenNoHeaders_returnTable() {
        Table table = Table.create().addColumns(
            StringColumn.create("required_column", Arrays.asList("1", "2", "3", "4", "5")));
        Table expected = table;
        assertEquals(
            expected,
            removeHeaderRowsIfPresent(table)
        );
    }


    @Test public void removeDescriptionColumn_givenDescriptionColumn_removeDescriptionColumn() {
        Map<String, Table> tableSet = new HashMap<>();
        Arrays.asList("table_1.tsv").forEach(
            s -> tableSet.put(s, Table.create().addColumns(
                StringColumn.create("Field", Collections.emptyList()))));
        Map<String, Table> expected = new HashMap<>();
        Arrays.asList("table_1.tsv").forEach(
            s -> expected.put(s, Table.create()));
        removeDescriptionColumn(tableSet);
        assertEquals(
            expected.toString(),
            tableSet.toString()
        );
    }

    @Test public void removeProviderNameFromFilename_givenProviderInFilename_stripProvider() {
        final String TABLE_NAME = "PROVIDER-BC_table_1.tsv";
        final String NEW_TABLE_NAME = "table_1.tsv";
        Map<String, Table> tableSet = new HashMap<>();
        Arrays.asList(TABLE_NAME).forEach(
            s -> tableSet.put(s, Table.create(TABLE_NAME)));
        Map<String, Table> expected = new HashMap<>();
        Arrays.asList(NEW_TABLE_NAME).forEach(
            s -> expected.put(s, Table.create(NEW_TABLE_NAME)));
        assertEquals(
            expected.toString(),
            removeProviderNameFromFilename(tableSet).toString()
        );
    }

    @Test public void removeProviderNameFromFilename_givenMultipleSep_stripProvider() {
        final String TABLE_NAME = "PROVIDER_123_table_1.tsv";
        final String NEW_TABLE_NAME = "123_table_1.tsv";
        Map<String, Table> tableSet = new HashMap<>();
        Arrays.asList(TABLE_NAME).forEach(
            s -> tableSet.put(s, Table.create(TABLE_NAME)));
        Map<String, Table> expected = new HashMap<>();
        Arrays.asList(NEW_TABLE_NAME).forEach(
            s -> expected.put(s, Table.create(NEW_TABLE_NAME)));
        assertEquals(
            expected.toString(),
            removeProviderNameFromFilename(tableSet).toString()
        );
    }

    @Test public void removeProviderNameFromFilename_givenNoSeparators_doNotStrip() {
        Map<String, Table> expected = new HashMap<>();
        Arrays.asList("table.tsv").forEach(
            s -> expected.put(s, Table.create("table.tsv")));
        assertEquals(
            expected.toString(),
            removeProviderNameFromFilename(expected).toString()
        );
    }

}