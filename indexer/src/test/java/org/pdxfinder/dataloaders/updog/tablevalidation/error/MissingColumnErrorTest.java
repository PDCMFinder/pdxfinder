package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.junit.Test;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;

import static org.junit.Assert.*;

public class MissingColumnErrorTest {

    private MissingColumnErrorCreator missingColumnErrorCreator = new MissingColumnErrorCreator();
    private String PROVIDER = "provider";

    @Test public void message_givenMissingValue_returnsAppropriateError() {
        String expected = "Error in [table] for provider [provider]: Missing column: [column]";

        ColumnReference columnReference = ColumnReference.of("table", "column");
        MissingColumnError error = missingColumnErrorCreator.create(columnReference, PROVIDER);

        assertEquals(
            expected,
            error.message()
        );
    }

    @Test public void verboseMessage_sameAsMessage() {
        ColumnReference columnReference = ColumnReference.of("table", "column");
        MissingColumnError error = missingColumnErrorCreator.create(columnReference, PROVIDER);

        assertEquals(
            error.message(),
            error.verboseMessage()
        );
    }

}