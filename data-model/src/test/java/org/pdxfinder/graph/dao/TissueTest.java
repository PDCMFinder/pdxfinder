package org.pdxfinder.graph.dao;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TissueTest {

    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        Tissue x = new Tissue("name");
        Tissue y = new Tissue("name");
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        Tissue x = new Tissue("name");
        Tissue y = new Tissue("name");
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        Tissue x = new Tissue("name");
        Tissue y = new Tissue("name");
        Map<Tissue, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}