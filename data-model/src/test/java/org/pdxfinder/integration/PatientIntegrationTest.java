package org.pdxfinder.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.Patient;
import org.pdxfinder.graph.dao.PatientSnapshot;
import org.pdxfinder.graph.repositories.GroupRepository;
import org.pdxfinder.graph.repositories.PatientRepository;
import org.pdxfinder.graph.repositories.PatientSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientIntegrationTest extends BaseTest {

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    PatientSnapshotRepository patientSnapshotRepository;

    @Autowired
    GroupRepository groupRepository;


    Patient patient;
    Group providerGroup;

    @Before
    public void setupDB(){

        patientRepository.deleteAll();
        patientSnapshotRepository.deleteAll();
        groupRepository.deleteAll();

        providerGroup = new Group("testgroup", "tg", "groupdescription", "academia", "", "");

        patient = new Patient("p1", "male", "-", "-", providerGroup);

        PatientSnapshot ps1 = new PatientSnapshot();
        ps1.setAgeAtCollection("40");

        PatientSnapshot ps2 = new PatientSnapshot();
        ps2.setAgeAtCollection("45");

        patient.addSnapshot(ps1);
        patient.addSnapshot(ps2);


    }

    @Test
    public void PatientTest(){

        Assert.assertEquals(patient.getExternalId(), "p1");
        Assert.assertEquals(patient.getProviderGroup().getAbbreviation(), providerGroup.getAbbreviation());
        Assert.assertEquals(patient.getLastSnapshot().getAgeAtCollection(), "45");

    }



}
