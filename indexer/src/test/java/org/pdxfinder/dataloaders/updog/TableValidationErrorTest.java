package org.pdxfinder.dataloaders.updog;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.isA;

public class TableValidationErrorTest {

    @Test public void create_givenInstantiation_returnsValidationErrorWithTableNameMember() {
        TableValidationError error = TableValidationError.create("table");
        assertEquals(
            error.getTable(),
            "table"
        );
    }

    @Test public void builderMethods_givenInstantiation_allReturnInstanceOfThisClass() {
        TableValidationError error = TableValidationError.create("table");
        assertThat(error, isA(TableValidationError.class));
        assertThat(error.setType("error type"), isA(TableValidationError.class));
        assertThat(error.setProvider("provider"), isA(TableValidationError.class));
        assertThat(error.setColumn("column name"), isA(TableValidationError.class));
    }

}