package org.pdxfinder.graph.dao;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class EngraftmentTypeTest {

    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        EngraftmentType x = new EngraftmentType("name");
        EngraftmentType y = new EngraftmentType("name");
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        EngraftmentType x = new EngraftmentType("name");
        EngraftmentType y = new EngraftmentType("name");
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        EngraftmentType x = new EngraftmentType("name");
        EngraftmentType y = new EngraftmentType("name");
        Map<EngraftmentType, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}