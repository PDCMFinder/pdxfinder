package org.pdxfinder.dataloaders.updog;

import org.junit.Test;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.isA;

public class ValidationErrorTest {

    @Test public void create_givenInstantiation_returnsValidationErrorWithTableNameMember() {
        String expected = "table";
        ValidationError error = ValidationError.generic("table");
        assertEquals(
            expected,
            error.getTable()
        );
    }

    @Test public void factoryMethods_givenInstantiation_returnInstanceOfThisClass() {
        ValidationError error = ValidationError.generic("table");
        assertThat(error, isA(ValidationError.class));
        assertThat(error.setProvider("provider"), isA(ValidationError.class));
        assertThat(error.setDescription("Custom description of the error"), isA(ValidationError.class));
    }

    @Test public void toString_givenInstantiation_returnsBasicErrorString() {
        String expected = "Error in [table]: Generic error: Custom description";
        ValidationError error = ValidationError.generic("table").setDescription("Custom description");
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenMissingColumnErrorWithValue_returnsAppropriateMessage() {
        String expected = "Error in [table]: Missing column: [required_col]";
        ValidationError error = ValidationError.missingColumn("table", "required_col");
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenMissingColumnErrorWithProvider_returnsAppropriateMessageWithProvider() {
        String expected = "Error in [table] for provider [Example provider]: Missing column: [required_col]";
        ValidationError error = ValidationError.missingColumn("table", "required_col").setProvider("Example provider");
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenDDuplicateValue_returnsAppropriateMessage() {
        String expected = "Error in [table]: Duplicate value(s) in required column [required_col]: [bar, foo]";
        Set<String> duplicates = new HashSet<>(Arrays.asList("foo", "bar"));
        ValidationError error = ValidationError.duplicateValue("table", "required_col", duplicates);
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
        ValidationError error = ValidationError
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
        ValidationError error = ValidationError.missingRequiredValue("table", "required_col", table.rows(1));
        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void toString_givenBrokenRelationMissingRightColumn_returnsAppropriateMessage() {
        String expected =
            "Error in [bar.tsv]: Broken relation [(foo.tsv) foo_id -> foo_id (bar.tsv)]:\n" +
            " not_foo_id  |\n" +
            "--------------";
        Pair<Pair<String, String>, Pair<String, String>> relation =
            Pair.of(Pair.of("foo.tsv", "foo_id"), Pair.of("bar.tsv", "foo_id"));
        Table tableMissingColumn = Table.create().addColumns(StringColumn.create("not_foo_id"));
        ValidationError error = ValidationError.brokenRelation("bar.tsv", relation, tableMissingColumn);

        assertEquals(
            expected,
            error.toString()
        );
    }


    @Test public void toString_givenBrokenRelationOrphanIdsInRightColumn_returnsAppropriateMessage() {
        String expected =
            "Error in [bar.tsv] for provider [PROVIDER-BC]: " +
                "Broken relation [(foo.tsv) foo_id -> foo_id (bar.tsv)]: " +
                "2 orphan row(s) found in [bar.tsv]:\n" +
                " foo_id  |\n" +
                "----------\n" +
                "      1  |\n" +
                "      1  |";
        Pair<Pair<String, String>, Pair<String, String>> relation =
            Pair.of(Pair.of("foo.tsv", "foo_id"), Pair.of("bar.tsv", "foo_id"));
        Table tableMissingValues = Table.create().addColumns(
            StringColumn.create("foo_id", Arrays.asList("1", "1")));
        ValidationError error = ValidationError
            .brokenRelation("bar.tsv", relation, tableMissingValues)
            .setDescription("2 orphan row(s) found in [bar.tsv]")
            .setProvider("PROVIDER-BC");

        assertEquals(
            expected,
            error.toString()
        );
    }

    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        ValidationError x = ValidationError.generic("table");
        ValidationError y = ValidationError.generic("table");
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        ValidationError x = ValidationError.generic("table");
        ValidationError y = ValidationError.generic("table");
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        ValidationError x = ValidationError.generic("table");
        ValidationError y = ValidationError.generic("table");
        Map<ValidationError, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}