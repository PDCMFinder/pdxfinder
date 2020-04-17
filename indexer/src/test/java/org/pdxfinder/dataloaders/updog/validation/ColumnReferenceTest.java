package org.pdxfinder.dataloaders.updog.validation;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ColumnReferenceTest {

    @Test public void getters_givenInstantiation_returnsValues() {
        ColumnReference columnReference = new ColumnReference("table", "column");
        assertEquals(
            "table",
            columnReference.table()
        );
        assertEquals(
            "column",
            columnReference.column()
        );
    }

    @Test public void factoryMethod_givenCalled_createsNewInstance() {
        ColumnReference columnReference = ColumnReference.of("table", "column");
        assertEquals(
            "table",
            columnReference.table()
        );
        assertEquals(
            "column",
            columnReference.column()
        );


    }

    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        ColumnReference x = ColumnReference.of("x", "x");
        ColumnReference y = ColumnReference.of("x", "x");
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        ColumnReference x = ColumnReference.of("x", "x");
        ColumnReference y = ColumnReference.of("x", "x");
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        ColumnReference x = ColumnReference.of("x", "x");
        ColumnReference y = ColumnReference.of("x", "x");
        Map<ColumnReference, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}