package org.pdxfinder.dataloaders.updog.tablevalidation;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class  RelationTest {

    private Relation createRelation() {
        return (Relation.betweenTableKeys(
                ColumnReference.of("table1", "join_column"),
                ColumnReference.of("table2", "join_column")));
    }

    @Test public void getters_givenInstantiation_returnsValues() {
        Relation relation = createRelation();
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

    @Test(expected = IllegalArgumentException.class)
    public void between_givenSelfRelationCreated_throwException() {
        Relation.betweenTableKeys(
            ColumnReference.of("x", "x"),
            ColumnReference.of("x", "x"));
    }

    @Test public void getOtherColumn_givenColumn_returnsOtherColumn() {
        Relation relation = createRelation();
        assertEquals(
            relation.leftColumnReference(),
            relation.getOtherColumn(relation.rightColumnReference())
        );
        assertEquals(
            relation.rightColumnReference(),
            relation.getOtherColumn(relation.leftColumnReference())
        );
    }

    @Test public void getOtherColumn_givenIncorrectColumn_returnsError() {
        Relation relation = createRelation();
        ColumnReference incorrectReference =  ColumnReference.of("incorrectTable", "incorrectColumn");
        assertEquals(
            "table linked to incorrectTable not found",
            relation.getOtherColumn(incorrectReference).table()
        );
        assertEquals(
            "column linked to incorrectColumn not found",
            relation.getOtherColumn(incorrectReference).column()
        );
    }

    @Test public void toString_returnsExpectedFormat() {
        Relation relation =  Relation.betweenTableKeys(
            ColumnReference.of("foo.tsv", "foo_id"),
            ColumnReference.of("bar.tsv", "bar_id")
        );
        assertEquals(
            "(foo.tsv) foo_id -> bar_id (bar.tsv)",
            relation.toString()
        );
    }


    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        Relation x = Relation.betweenTableKeys(
            ColumnReference.of("x.tsv", "x"),
            ColumnReference.of("y.tsv", "x"));
        Relation y = Relation.betweenTableKeys(
            ColumnReference.of("x.tsv", "x"),
            ColumnReference.of("y.tsv", "x"));
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        Relation x = Relation.betweenTableKeys(
            ColumnReference.of("x.tsv", "x"),
            ColumnReference.of("y.tsv", "x"));
        Relation y = Relation.betweenTableKeys(
            ColumnReference.of("x.tsv", "x"),
            ColumnReference.of("y.tsv", "x"));
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void equals_givenSameObject_returnsTrue() {
        Relation x = Relation.betweenTableKeys(
            ColumnReference.of("x.tsv", "x"),
            ColumnReference.of("y.tsv", "x"));
        assertEquals(x, x);
    }

    @Test public void equals_givenNonIdenticalObjects_returnsFalse() {
        Relation x = Relation.betweenTableKeys(
            ColumnReference.of("x.tsv", "x"),
            ColumnReference.of("y.tsv", "x"));
        Relation y = Relation.betweenTableKeys(
            ColumnReference.of("a.tsv", "a"),
            ColumnReference.of("b.tsv", "b"));
        assertNotEquals(x, y);
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        Relation x = Relation.betweenTableKeys(
            ColumnReference.of("x.tsv", "x"),
            ColumnReference.of("y.tsv", "x"));
        Relation y = Relation.betweenTableKeys(
            ColumnReference.of("x.tsv", "x"),
            ColumnReference.of("y.tsv", "x"));
        Map<Relation, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}