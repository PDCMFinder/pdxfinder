package org.pdxfinder.graph.dao;


import org.junit.Assert;
import org.junit.Test;


public class PatientSnapshotTest {

    @Test
    public void given_PatientSnapshot_when_PaediatricAge_then_AgeBinCorrect(){
        Patient patient = new Patient("test1", new Group());
        PatientSnapshot patientSnapshot1 = new PatientSnapshot(patient, "1", "", "", "");
        PatientSnapshot patientSnapshot2 = new PatientSnapshot(patient, "17mo", "", "", "");
        PatientSnapshot patientSnapshot3 = new PatientSnapshot(patient, "25", "", "", "");
        Assert.assertEquals(patientSnapshot1.getAgeBin(), "0-23 months");
        Assert.assertEquals(patientSnapshot2.getAgeBin(), "0-23 months");
        Assert.assertEquals(patientSnapshot3.getAgeBin(), "20-29");
    }

}
