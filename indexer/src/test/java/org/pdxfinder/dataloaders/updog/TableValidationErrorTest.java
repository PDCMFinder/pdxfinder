package org.pdxfinder.dataloaders.updog;

import org.junit.Test;

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
        assertThat(error.setType(TableValidationError.Type.MISSING_COL), isA(TableValidationError.class));
        assertThat(error.setProvider("provider"), isA(TableValidationError.class));
        assertThat(error.setColumn("column name"), isA(TableValidationError.class));
    }

    @Test public void toString_givenInstantiation_returnsBasicErrorString() {
        String expected = "Error in table: ";
        TableValidationError error = createBasicError();
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenErrorType_returnsAppropriateMessage() {
        String expected = "Error in table: Missing column: (not specified)";
        TableValidationError error = createBasicError().setType(TableValidationError.Type.MISSING_COL);
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenMissingColumnErrorWithValue_returnsAppropriateMessage() {
        String expected = "Error in table: Missing column: required_col";
        TableValidationError error = TableValidationError.missingColumn("table", "required_col");
        assertEquals(
            expected,
            error.toString()
        );
    }

    private TableValidationError createBasicError() {
        return TableValidationError.create("table");
    }

}