package org.pdxfinder.dataloaders.updog.tablevalidation;

import org.junit.Test;
import org.pdxfinder.dataloaders.updog.tablevalidation.error.ValidationErrorImpl;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.isA;

public class ValidationErrorImplTest {

    @Test public void create_givenInstantiation_returnsValidationErrorWithTableNameMember() {
        String expected = "table";
        ValidationErrorImpl error = ValidationErrorImpl.generic("table");
        assertEquals(
            expected,
            error.getTable()
        );
    }

    @Test public void factoryMethods_givenInstantiation_returnInstanceOfThisClass() {
        ValidationErrorImpl error = ValidationErrorImpl.generic("table");
        assertThat(error, isA(ValidationErrorImpl.class));
        assertThat(error.setProvider("provider"), isA(ValidationErrorImpl.class));
        assertThat(error.setDescription("Custom description of the error"), isA(ValidationErrorImpl.class));
    }

    @Test public void toString_givenInstantiation_returnsBasicErrorString() {
        String expected = "Error in [table]: Generic error: Custom description";
        ValidationErrorImpl error = ValidationErrorImpl.generic("table").setDescription("Custom description");
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenMissingColumnErrorWithValue_returnsAppropriateMessage() {
        String expected = "Error in [table]: Missing column: [required_col]";
        ValidationErrorImpl error = ValidationErrorImpl.missingColumn("table", "required_col");
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenMissingColumnErrorWithProvider_returnsAppropriateMessageWithProvider() {
        String expected = "Error in [table] for provider [Example provider]: Missing column: [required_col]";
        ValidationErrorImpl error = ValidationErrorImpl.missingColumn("table", "required_col").setProvider("Example provider");
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenDDuplicateValue_returnsAppropriateMessage() {
        String expected = "Error in [table]: Duplicates found in column [required_col]: [bar, foo]";
        Set<String> duplicates = new HashSet<>(Arrays.asList("foo", "bar"));
        ValidationErrorImpl error = ValidationErrorImpl.duplicateValue("table", "required_col", duplicates);
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenRequiredColumnHasMissingValue_returnsAppropriateMessage() {
        String expected =
            "Error in [table]: Missing value(s) in required column [required_col]:\n" +
            " required_col  |\n" +
            "----------------\n" +
            "               |";
        Table table = Table.create().addColumns(StringColumn.create("required_col", Arrays.asList("")));
        ValidationErrorImpl error = ValidationErrorImpl
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
        String expected =
            "Error in [table]: Missing value(s) in required column [required_col]:\n" +
            " required_col  |  optional_col  |\n" +
            "---------------------------------\n" +
            "               |       value 2  |";
        Table table = Table.create().addColumns(
            StringColumn.create("required_col", Arrays.asList("value 1", "")),
            StringColumn.create("optional_col", Arrays.asList("value 1", "value 2"))
        );
        ValidationErrorImpl error = ValidationErrorImpl.missingRequiredValue("table", "required_col", table.rows(1));
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        ValidationErrorImpl x = ValidationErrorImpl.generic("table");
        ValidationErrorImpl y = ValidationErrorImpl.generic("table");
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        ValidationErrorImpl x = ValidationErrorImpl.generic("table");
        ValidationErrorImpl y = ValidationErrorImpl.generic("table");
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        ValidationErrorImpl x = ValidationErrorImpl.generic("table");
        ValidationErrorImpl y = ValidationErrorImpl.generic("table");
        Map<ValidationErrorImpl, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}