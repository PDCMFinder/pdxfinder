package org.pdxfinder.graph.dao;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SampleTest {

    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        Sample x = new Sample("id");
        Sample y = new Sample("id");
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        Sample x = new Sample("id");
        Sample y = new Sample("id");
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        Sample x = new Sample("id");
        Sample y = new Sample("id");
        Map<Sample, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}