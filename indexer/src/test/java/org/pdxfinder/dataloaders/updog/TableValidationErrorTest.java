package org.pdxfinder.dataloaders.updog;

import org.junit.Test;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

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

    private TableValidationError createBasicError() {
        return TableValidationError.generic("table");
    }

}