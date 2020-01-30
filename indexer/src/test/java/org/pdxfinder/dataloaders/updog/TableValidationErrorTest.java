package org.pdxfinder.dataloaders.updog;

import org.junit.Test;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.isA;

public class TableValidationErrorTest {

    @Test public void create_givenInstantiation_returnsValidationErrorWithTableNameMember() {
        String expected = "table";
        TableValidationError error = createBasicError();
        assertEquals(
            expected,
            error.getTable()
        );
    }

    @Test public void builderMethods_givenInstantiation_allReturnInstanceOfThisClass() {
        TableValidationError error = createBasicError();
        assertThat(error, isA(TableValidationError.class));
        assertThat(error.setProvider("provider"), isA(TableValidationError.class));
        assertThat(error.setDescription("Custom description of the error"), isA(TableValidationError.class));
    }

    @Test public void toString_givenInstantiation_returnsBasicErrorString() {
        String expected = "Error in [table]: Generic error: Custom description";
        TableValidationError error = createBasicError().setDescription("Custom description");
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenMissingColumnErrorWithValue_returnsAppropriateMessage() {
        String expected = "Error in [table]: Missing column: [required_col]";
        TableValidationError error = TableValidationError.missingColumn("table", "required_col");
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenMissingColumnErrorWithProvider_returnsAppropriateMessageWithProvider() {
        String expected = "Error in [table] for provider [Example provider]: Missing column: [required_col]";
        TableValidationError error = TableValidationError
            .missingColumn("table", "required_col")
            .setProvider("Example provider");
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenRequiredColumnHasMissingValue_returnsAppropriateMessage() {
        String expected = "Error in [table]: Missing value(s) in required column [required_col]:\n" +
            " required_col  |\n" +
            "----------------\n" +
            "               |";
        Table table = Table.create().addColumns(StringColumn.create("required_col", Arrays.asList("")));
        TableValidationError error = TableValidationError
            .missingRequiredValue(
                "table",
                "required_col",
                table.where(table.stringColumn("required_col").isEqualTo("")));
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenRequiredColumnHasMissingValueInRow1_returnsAppropriateMessage() {
        String expected = "Error in [table]: Missing value(s) in required column [required_col]:\n" +
            " required_col  |  optional_col  |\n" +
            "---------------------------------\n" +
            "               |       value 2  |";
        Table table = Table.create().addColumns(
            StringColumn.create("required_col", Arrays.asList("value 1", "")),
            StringColumn.create("optional_col", Arrays.asList("value 1", "value 2"))
        );
        TableValidationError error = TableValidationError
            .missingRequiredValue(
                "table",
                "required_col",
                table.rows(1)
            );
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenMissingRightColumn_returnsAppropriateMessage() {
        String expected = "Error in [bar.tsv]: Broken relation [((foo.tsv,foo_id),(bar.tsv,foo_id))]:\n" +
            " not_foo_id  |\n" +
            "--------------";
        Pair<Pair<String, String>, Pair<String, String>> relation =
            Pair.of(Pair.of("foo.tsv", "foo_id"), Pair.of("bar.tsv", "foo_id"));
        Table tableMissingColumn = Table.create().addColumns(StringColumn.create("not_foo_id"));
        TableValidationError error = TableValidationError
            .brokenRelation("bar.tsv", relation, tableMissingColumn);

        assertEquals(
            expected,
            error.toString()
        );
    }


    @Test public void toString_givenOrphanIdsInRightColumn_returnsAppropriateMessage() {
        String expected = "Error in [bar.tsv] for provider [PROVIDER-BC]: " +
            "Broken relation [((foo.tsv,foo_id),(bar.tsv,foo_id))]: " +
            "2 orphan row(s) found in [bar.tsv]:\n" +
            " foo_id  |\n" +
            "----------\n" +
            "      1  |\n" +
            "      1  |";

        Pair<Pair<String, String>, Pair<String, String>> relation =
            Pair.of(Pair.of("foo.tsv", "foo_id"), Pair.of("bar.tsv", "foo_id"));
        Table tableMissingValues = Table.create().addColumns(
            StringColumn.create("foo_id", Arrays.asList("1", "1")));
        TableValidationError error = TableValidationError
            .brokenRelation("bar.tsv", relation, tableMissingValues)
            .setDescription("2 orphan row(s) found in [bar.tsv]")
            .setProvider("PROVIDER-BC");

        assertEquals(
            expected,
            error.toString()
        );
    }



        private TableValidationError createBasicError() {
        return TableValidationError.generic("table");
    }

}