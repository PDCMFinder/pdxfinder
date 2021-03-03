package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.junit.Test;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class IllegalValueErrorTest {
    private IllegalValueErrorCreator illegalValueErrorCreator = new IllegalValueErrorCreator();

    @Test
    public void columnMissing_givenIllegalValueError_returnsAppropriateMessage() {
        String expected =
                "Error in [bar.tsv] for provider [TEST]: because [bar.tsv] is missing column [foo_id]:\n" +
                        " not_foo_id  |\n" +
                        "--------------";

        Table tableMissingColumn = Table.create().addColumns(StringColumn.create("not_foo_id"));
        IllegalValueError error = illegalValueErrorCreator.create(
                "bar.tsv",
                "because [bar.tsv] is missing column [foo_id]",
                tableMissingColumn,
                "TEST"
        );
        assertEquals(
                expected,
                error.verboseMessage()
        );
    }

    @Test public void verboseMessage_givenInvalidCharactersInColumn_returnsAppropriateMessage() {
        String expected =
                "Error in [bar.tsv] for provider [PROVIDER-BC]: " +
                        "in column [foo_id] found 1 values " +
                        "has characters not contained in US ASCII Alphabet and ._~- : TE#/ST:\n" +
                        " foo_id  |\n" +
                        "----------\n" +
                        " TE#/ST  |";

        Table tableInvalidColumns = Table.create().addColumns(StringColumn.create("foo_id", Collections.singleton("TE#/ST") ));
        IllegalValueError error = illegalValueErrorCreator.create(
                "bar.tsv",
                "in column [foo_id] found 1 values has characters not contained in US ASCII Alphabet and ._~- : TE#/ST",
                tableInvalidColumns,
                "PROVIDER-BC"
        );

        System.out.println(error.verboseMessage());

        assertEquals(
                expected,
                error.verboseMessage()
        );
    }

    @Test public void errorMessage_givenInvalidCharactersInColumn_returnsAppropriateMessage() {
        String expected =
                "Error in [bar.tsv] for provider [PROVIDER-BC]: " +
                        "in column [foo_id] found 1 values has characters not contained in US ASCII Alphabet and ._~- : TE#/ST";

        Table tableInvalidColumns = Table.create().addColumns(StringColumn.create("foo_id", Collections.singleton("TE#/ST") ));
        IllegalValueError error = illegalValueErrorCreator.create(
                "bar.tsv",
                "in column [foo_id] found 1 values has characters not contained in US ASCII Alphabet and ._~- : TE#/ST",
                tableInvalidColumns,
                "PROVIDER-BC"
        );

        System.out.println(error.verboseMessage());

        assertEquals(
                expected,
                error.message()
        );
    }
}
