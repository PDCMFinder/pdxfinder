package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.junit.Test;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import static org.junit.Assert.*;

public class EmptyValueErrorTest {

    private EmptyValueErrorCreator emptyValueErrorCreator = new EmptyValueErrorCreator();
    private String PROVIDER = "provider";

    @Test public void verboseMessage_givenMissingValue_returnsAppropriateError() {
        String expected = "Error in [table] for provider [provider]: Missing value(s) in required column [column]:\n" +
            "  invalid  \n" +
            " column1  |\n" +
            "-----------\n" +
            "          |";
        ColumnReference columnReference = ColumnReference.of("table", "column");
        Table invalidRows = Table.create("invalid", StringColumn.create("column1", ""));
        EmptyValueError error = emptyValueErrorCreator.create(columnReference, invalidRows, PROVIDER);

        assertEquals(
            expected,
            error.verboseMessage()
        );
    }

    @Test public void message_givenMissingValue_returnsAppropriateError() {
        String expected = "Error in [table] for provider [provider]: Missing value(s) in required column [column]";
        ColumnReference columnReference = ColumnReference.of("table", "column");
        Table invalidRows = Table.create("invalid", StringColumn.create("column1", ""));

        EmptyValueError error = emptyValueErrorCreator.create(columnReference, invalidRows, PROVIDER);

        assertEquals(
            expected,
            error.message()
        );
    }

}