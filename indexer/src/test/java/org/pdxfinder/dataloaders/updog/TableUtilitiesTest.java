package org.pdxfinder.dataloaders.updog;

import org.junit.Test;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.pdxfinder.dataloaders.updog.TableUtilities.*;

public class TableUtilitiesTest {

    @Test public void removeHeaderRows_givenEmptyTable_returnsEmptyTable() {
        Table table = Table.create();
        assertEquals(
            table.toString(),
            removeHeaderRows(table, 4).toString());
    }

    @Test public void removeHeaderRows_givenSmallerThenExpectedHeader_returnsEmptyTable() {
        Table table = Table.create().addColumns(
            StringColumn.create("column_1", Arrays.asList("header_1", "header_2")));
        assertEquals(
            table.emptyCopy().toString(),
            removeHeaderRows(table, 3).toString()
        );
    }

    @Test public void removeHeaderRows_givenOnlyHeader_returnsEmptyTable() {
        Table table = Table.create().addColumns(
            StringColumn.create("column_1", Arrays.asList("header_1", "header_2")));
        assertEquals(
            table.emptyCopy().toString(),
            removeHeaderRows(table, 2).toString()
        );
    }

    @Test public void removeHeaderRows_givenSmallestTable_removesOneHeaderRow() {
        Table table = Table.create().addColumns(
            StringColumn.create("column_1", Arrays.asList("header_1", "value_1")));
        Table expected = Table.create().addColumns(
            StringColumn.create("column_1", Arrays.asList("value_1")));
        assertEquals(
            expected.toString(),
            removeHeaderRows(table, 1).toString()
        );
    }

    @Test public void removeSpacesAndLowerCase_GivenAColumnWithPaddingSpacesAndUppercase_returnsCleanedStrings(){
        Table table = Table.create().addColumns(
                StringColumn.create("column_1", Arrays.asList(" PADDEDSTRING ", " PADDEDSTRING_1 ")));
        Table expected = Table.create().addColumns(
                StringColumn.create("column_1", Arrays.asList("paddedstring", "paddedstring_1")));

        assertEquals(
               expected.toString(),
               cleanTableValues(table, table.name(), Collections.singletonList("")).toString()
        );
    }

    @Test public void doNotCleanExceptionColumns_GivenATableWithTwoColumns_returnsUncleanedExceptions(){
        String exceptionColumn = "exception_column";
        Table table = Table.create().addColumns(
                StringColumn.create("column_1", Arrays.asList(" PADDEDSTRING ", " PADDEDSTRING_1 ")),
                StringColumn.create(exceptionColumn, Arrays.asList(" PADDEDSTRING ", " PADDEDSTRING_1 ")
                ));
        Table expected = Table.create().addColumns(
                StringColumn.create("column_1", Arrays.asList("paddedstring", "paddedstring_1")),
                StringColumn.create(exceptionColumn, Arrays.asList("PADDEDSTRING", "PADDEDSTRING_1")
                ));

        assertEquals(
                expected.toString(),
                cleanTableValues(table, table.name(), Collections.singletonList(exceptionColumn)).toString()
        );
    }




    @Test public void removeHeaderRows_givenTableWithTypicalHeader_removesHeaderRows() {
        Table expected = Table.create().addColumns(
            StringColumn.create("Field", Arrays.asList("")),
            StringColumn.create("patient_id", Arrays.asList("A0088")));
        Table table = Table.create().addColumns(
            StringColumn.create("Field",
                Arrays.asList("Description", "Example", "Format Requirements", "Essential?", "")),
            StringColumn.create("patient_id",
                Arrays.asList("Must be unique", "Example_ID", "free alphanumerical", "essential", "A0088")));
        assertEquals(
            expected.toString(),
            removeHeaderRows(table, 4).toString()
        );
    }

    @Test public void removeRowsMissingRequiredColumnValue_givenTableWithOnlyOneEmptyRow_returnEmptyTable() {
        Table table = Table.create().addColumns(
            StringColumn.create("required_column_1", Arrays.asList("")));
        Table expected = table.emptyCopy();
        assertEquals(
            expected.toString(),
            removeRowsMissingRequiredColumnValue(table, "required_column_1").toString()
        );
    }

    @Test public void removeRowsMissingRequiredColumnValue_givenTableWithOneEmptyRow_removeEmptyRow() {
        Table table = Table.create().addColumns(
            StringColumn.create("required_column_1", Arrays.asList("value_1", "")));
        Table expected = Table.create().addColumns(
            StringColumn.create("required_column_1", Arrays.asList("value_1")));
        assertEquals(
            expected.toString(),
            removeRowsMissingRequiredColumnValue(table, "required_column_1").toString()
        );
    }

    @Test public void removeRowsMissingRequiredColumnValue_givenTableWithOneMissingValue_doesNotRemoveRow() {
        Table table = Table.create().addColumns(
            StringColumn.create("required_column_1", Arrays.asList("value_1", "value_2")),
            StringColumn.create("column_2", Arrays.asList("value_3", "")));
        assertEquals(
            table.toString(),
            removeRowsMissingRequiredColumnValue(table, "required_column_1").toString()
        );
    }

    @Test public void removeRowsMissingRequiredColumnValue_givenTableWithOneMissingValueInRequired_removesInvalidRow() {
        Table table = Table.create().addColumns(
            StringColumn.create("required_column_1", Arrays.asList("value_1", "")),
            StringColumn.create("column_2", Arrays.asList("value2", "value_3")));
        Table expected = Table.create().addColumns(
            StringColumn.create("required_column_1", Arrays.asList("value_1")),
            StringColumn.create("column_2", Arrays.asList("value2")));
        assertEquals(
            expected.toString(),
            removeRowsMissingRequiredColumnValue(table, "required_column_1").toString()
        );
    }
    @Test public void removeRowsMissingRequiredColumnValue_givenColumnObject_removesInvalidRow() {
        Table table = Table.create().addColumns(
            StringColumn.create("required_column_1", Arrays.asList("value_1", "")),
            StringColumn.create("column_2", Arrays.asList("value2", "value_3")));
        Table expected = Table.create().addColumns(
            StringColumn.create("required_column_1", Arrays.asList("value_1")),
            StringColumn.create("column_2", Arrays.asList("value2")));
        assertEquals(
            expected.toString(),
            removeRowsMissingRequiredColumnValue(
                table,
                table.column("required_column_1").asStringColumn()
            ).toString()
        );
    }

    @Test public void fromString_createTableWithOneColumn_matchesTableSawConstruction() {
        Table table = TableUtilities.fromString("table_name", "column_1", "value_1");
        Table table2 = Table.create("table_name", StringColumn.create("column_1", "value_1"));
        assertEquals(
            table.toString(),
            table2.toString()
        );
    }

    @Test public void  fromString_createTableWithTwoColumns_matchesTableSawConstruction() {
        Table table = TableUtilities.fromString("table_name",
            "column_1, column_2",
            "value_1, value_2");
        Table table2 = Table.create("table_name",
           StringColumn.create("column_1", "value_1"),
            StringColumn.create("column_2", "value_2"));
        assertEquals(
            table.toString(),
            table2.toString()
        );
    }
}