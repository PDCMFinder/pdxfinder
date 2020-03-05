package org.pdxfinder.graph.dao;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PatientTest {

    @Mock PatientSnapshot patientSnapshot;
    @Mock Group providerGroup;
    @InjectMocks Patient patient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test public void equals_givenIdenticalObjects_symmetricallyEqual() {
        Patient x = new Patient("patient", new Group());
        Patient y = new Patient("patient", new Group());
        assertTrue(x.equals(y) && y.equals(x));
    }

    @Test public void equals_givenIdenticalObjects_hashCodeIsEqual() {
        Patient x = new Patient("patient", new Group());
        Patient y = new Patient("patient", new Group());
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test public void hashCode_givenObjectPutInMap_identicalKeyRetrievesTheValue() {
        Patient x = new Patient("patient", new Group());
        Patient y = new Patient("patient", new Group());
        Map<Patient, String> map = new HashMap<>();
        map.put(x, "this");
        assertEquals("this", map.get(y));
    }

}