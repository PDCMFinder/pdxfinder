package org.pdxfinder.graph.dao;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TumorTypeTest {

    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        TumorType x = new TumorType("name");
        TumorType y = new TumorType("name");
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        TumorType x = new TumorType("name");
        TumorType y = new TumorType("name");
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        TumorType x = new TumorType("name");
        TumorType y = new TumorType("name");
        Map<TumorType, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}