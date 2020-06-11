package org.pdxfinder.dataloaders.updog.tablevalidation.error;

import org.junit.Test;
import org.pdxfinder.dataloaders.updog.tablevalidation.ColumnReference;
import org.pdxfinder.dataloaders.updog.tablevalidation.Relation;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Arrays;

import static org.junit.Assert.*;

public class BrokenRelationErrorTest {
    private BrokenRelationErrorCreator brokenRelationErrorCreator = new BrokenRelationErrorCreator();

    @Test public void toString_givenBrokenRelationMissingRightColumn_returnsAppropriateMessage() {
        String expected =
            "Error in [bar.tsv] for provider [TEST]: Broken relation [(foo.tsv) foo_id -> foo_id (bar.tsv)]" +
                ": because [bar.tsv] is missing column [foo_id]:\n" +
                " not_foo_id  |\n" +
                "--------------";
        Relation relation = Relation.between(
            ColumnReference.of("foo.tsv", "foo_id"),
            ColumnReference.of("bar.tsv", "foo_id"));
        Table tableMissingColumn = Table.create().addColumns(StringColumn.create("not_foo_id"));
        BrokenRelationError error = brokenRelationErrorCreator.create(
            "bar.tsv",
            relation,
            tableMissingColumn,
            "because [bar.tsv] is missing column [foo_id]",
            "TEST"
        );

        assertEquals(
            expected,
            error.verboseMessage()
        );
    }


    @Test public void verboseMessage_givenBrokenRelationOrphanIdsInRightColumn_returnsAppropriateMessage() {
        String expected =
            "Error in [bar.tsv] for provider [PROVIDER-BC]: " +
                "Broken relation [(foo.tsv) foo_id -> foo_id (bar.tsv)]: " +
                "2 orphan row(s) found in [bar.tsv]:\n" +
                " foo_id  |\n" +
                "----------\n" +
                "      1  |\n" +
                "      1  |";
        Relation relation = Relation.between(
            ColumnReference.of("foo.tsv", "foo_id"),
            ColumnReference.of("bar.tsv", "foo_id"));
        Table tableMissingValues = Table.create().addColumns(
            StringColumn.create("foo_id", Arrays.asList("1", "1")));
        BrokenRelationError error = brokenRelationErrorCreator.create(
            "bar.tsv",
            relation,
            tableMissingValues,
            "2 orphan row(s) found in [bar.tsv]",
            "PROVIDER-BC"
        );

        assertEquals(
            expected,
            error.verboseMessage()
        );
    }

    @Test public void message_givenError_returnsAppropriateMessage() {
        String expected =
            "Error in [bar.tsv] for provider [PROVIDER-BC]: " +
                "Broken relation [(foo.tsv) foo_id -> foo_id (bar.tsv)]: " +
                "2 orphan row(s) found in [bar.tsv]";
        Relation relation = Relation.between(
            ColumnReference.of("foo.tsv", "foo_id"),
            ColumnReference.of("bar.tsv", "foo_id"));
        Table tableMissingValues = Table.create().addColumns(
            StringColumn.create("foo_id", Arrays.asList("1", "1")));
        BrokenRelationError error = brokenRelationErrorCreator.create(
            "bar.tsv",
            relation,
            tableMissingValues,
            "2 orphan row(s) found in [bar.tsv]",
            "PROVIDER-BC"
        );
        assertEquals(
            expected,
            error.message()
        );
    }

}