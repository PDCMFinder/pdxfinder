package org.pdxfinder.dataloaders.updog.validation;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RelationTest {

    @Test public void getters_givenInstantiation_returnsValues() {
        Relation relation = Relation.between(
            ColumnReference.of("table1", "join_column"),
            ColumnReference.of("table2", "join_column")
        );
        assertEquals(
            "table1",
            relation.leftTable()
        );
        assertEquals(
            "table2",
            relation.rightTable()
        );
        assertEquals(
            relation.leftColumn(),
            relation.rightColumn()
        );
    }

    @Test public void toString1() {
        Relation relation =  Relation.between(
            ColumnReference.of("foo.tsv", "foo_id"),
            ColumnReference.of("bar.tsv", "bar_id")
        );
        assertEquals(
            "(foo.tsv) foo_id -> bar_id (bar.tsv)",
            relation.toString()
        );
    }


    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        Relation x = Relation.between(
            ColumnReference.of("x", "x"),
            ColumnReference.of("x", "x"));
        Relation y = Relation.between(
            ColumnReference.of("x", "x"),
            ColumnReference.of("x", "x"));
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        Relation x = Relation.between(
            ColumnReference.of("x", "x"),
            ColumnReference.of("x", "x"));
        Relation y = Relation.between(
            ColumnReference.of("x", "x"),
            ColumnReference.of("x", "x"));
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        Relation x = Relation.between(
            ColumnReference.of("x", "x"),
            ColumnReference.of("x", "x"));
        Relation y = Relation.between(
            ColumnReference.of("x", "x"),
            ColumnReference.of("x", "x"));
        Map<Relation, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}