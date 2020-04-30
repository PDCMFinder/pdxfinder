package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.junit.Test;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class DuplicateValueErrorTest {
    private DuplicateValueErrorCreator duplicateValueErrorCreator = new DuplicateValueErrorCreator();
    private String PROVIDER = "provider";

    @Test public void message() {
        String expected = "Error in [table] for provider [provider]: Duplicates found in column [column]: [a]";
        ColumnReference uniqueColumn = ColumnReference.of("table", "column");
        Set<String> duplicateValues = new HashSet<>(Arrays.asList("a"));

        DuplicateValueError error = duplicateValueErrorCreator.create(
            uniqueColumn,
            duplicateValues,
            PROVIDER);

        assertEquals(
            expected,
            error.message()
        );
    }
}