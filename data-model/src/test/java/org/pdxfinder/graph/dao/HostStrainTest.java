package org.pdxfinder.graph.dao;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class HostStrainTest {

    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        HostStrain x = new HostStrain("symbol", "host_strain_nomenclature");
        HostStrain y = new HostStrain("symbol", "host_strain_nomenclature");
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        HostStrain x = new HostStrain("symbol", "host_strain_nomenclature");
        HostStrain y = new HostStrain("symbol", "host_strain_nomenclature");
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        HostStrain x = new HostStrain("symbol", "host_strain_nomenclature");
        HostStrain y = new HostStrain("symbol", "host_strain_nomenclature");
        Map<HostStrain, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}
