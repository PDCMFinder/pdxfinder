package org.pdxfinder.graph.dao;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class EngraftmentSiteTest {

    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        EngraftmentSite x = new EngraftmentSite("name");
        EngraftmentSite y = new EngraftmentSite("name");
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        EngraftmentSite x = new EngraftmentSite("name");
        EngraftmentSite y = new EngraftmentSite("name");
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        EngraftmentSite x = new EngraftmentSite("name");
        EngraftmentSite y = new EngraftmentSite("name");
        Map<EngraftmentSite, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}