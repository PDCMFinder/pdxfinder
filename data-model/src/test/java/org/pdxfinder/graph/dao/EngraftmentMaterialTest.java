package org.pdxfinder.graph.dao;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class EngraftmentMaterialTest {

    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        EngraftmentMaterial x = new EngraftmentMaterial("type", "state");
        EngraftmentMaterial y = new EngraftmentMaterial("type", "state");
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        EngraftmentMaterial x = new EngraftmentMaterial("type", "state");
        EngraftmentMaterial y = new EngraftmentMaterial("type", "state");
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        EngraftmentMaterial x = new EngraftmentMaterial("type", "state");
        EngraftmentMaterial y = new EngraftmentMaterial("type", "state");
        Map<EngraftmentMaterial, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}