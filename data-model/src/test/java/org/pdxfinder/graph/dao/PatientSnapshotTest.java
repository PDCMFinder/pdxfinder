package org.pdxfinder.graph.dao;


import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;


public class PatientSnapshotTest {

    @Test
    public void given_PatientSnapshot_when_PaediatricAge_then_AgeBinCorrect(){
        Patient patient = new Patient("test1", new Group());

        PatientSnapshot patientSnapshot1 = new PatientSnapshot(patient, "1", new HashSet<>(), new TreatmentSummary());
        PatientSnapshot patientSnapshot2 = new PatientSnapshot(patient, "12mo", new HashSet<>());
        patientSnapshot2.addSample(new Sample("sample123"));
        PatientSnapshot patientSnapshot3 = new PatientSnapshot(patient, "26mo", "", "", "");
        PatientSnapshot patientSnapshot4 = new PatientSnapshot(patient, "5", "", "", "");
        PatientSnapshot patientSnapshot5 = new PatientSnapshot(patient, "15", "", "", "");
        PatientSnapshot patientSnapshot6 = new PatientSnapshot(patient, "25", "", "", "");
        PatientSnapshot patientSnapshot7 = new PatientSnapshot(patient, "35", "", "", "");
        PatientSnapshot patientSnapshot8 = new PatientSnapshot(patient, "45", "", "", "");
        PatientSnapshot patientSnapshot9 = new PatientSnapshot(patient, "55", "", "", "");
        PatientSnapshot patientSnapshot10 = new PatientSnapshot(patient, "65", "", "", "");
        PatientSnapshot patientSnapshot11 = new PatientSnapshot(patient, "75", "", "", "");
        PatientSnapshot patientSnapshot12 = new PatientSnapshot(patient, "85", "", "", "");
        PatientSnapshot patientSnapshot13 = new PatientSnapshot(patient, "95", "", "", "");


        Assert.assertEquals(patientSnapshot1.getAgeBin(), "0-23 months");
        Assert.assertEquals(patientSnapshot2.getAgeBin(), "0-23 months");
        Assert.assertEquals(patientSnapshot3.getAgeBin(), "2-9");
        Assert.assertEquals(patientSnapshot4.getAgeBin(), "2-9");
        Assert.assertEquals(patientSnapshot5.getAgeBin(), "10-19");
        Assert.assertEquals(patientSnapshot6.getAgeBin(), "20-29");
        Assert.assertEquals(patientSnapshot7.getAgeBin(), "30-39");
        Assert.assertEquals(patientSnapshot8.getAgeBin(), "40-49");
        Assert.assertEquals(patientSnapshot9.getAgeBin(), "50-59");
        Assert.assertEquals(patientSnapshot10.getAgeBin(), "60-69");
        Assert.assertEquals(patientSnapshot11.getAgeBin(), "70-79");
        Assert.assertEquals(patientSnapshot12.getAgeBin(), "80-89");
        Assert.assertEquals(patientSnapshot13.getAgeBin(), "90+");
        Assert.assertEquals(false, patientSnapshot2.equals(patientSnapshot3));
    }

}
